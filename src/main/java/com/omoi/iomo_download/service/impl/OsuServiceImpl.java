package com.omoi.iomo_download.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omoi.iomo_download.entity.dto.*;
import com.omoi.iomo_download.enums.MinioTypesEnum;
import com.omoi.iomo_download.exception.ServiceException;
import com.omoi.iomo_download.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author omoi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OsuServiceImpl implements OsuService {
    private static final String OSU_INDEX = "https://osu.ppy.sh/home";
    private static final String OSU_LOGIN = "https://osu.ppy.sh/session";

    private static final String MINIO_UPLOAD_FAILED = "minio上传失败";

    private final UserCookieService cookieService;
    private final AsyncTask task;
    private final MinioService minioService;
    private final MinioResourceService resourceService;
    private final OszFileService oszService;
    private final OsuAudioService audioService;

    @Value("${osu.username}")
    private String username;

    @Value("${osu.password}")
    private String password;

    @Value("${download.path}")
    private String downloadPath;

    /**
     * osu官网模拟登录
     *
     * @return 登录后的cookie
     */
    private OsuCookie login() {
        HttpResponse index = null;
        HttpResponse login = null;
        try {
            // 模拟浏览器登录osu.ppy.sh 获取cookie
            index = HttpRequest.get(OSU_INDEX).execute();
            log.info("login: index code: {}", index.getStatus());
            HttpCookie loginSession = index.getCookie("osu_session");
            HttpCookie csrfToken = index.getCookie("XSRF-TOKEN");

            // 模拟登录
            Map<String, String> loginMap = Map.of("token", csrfToken.getValue(), "username", username, "password", password);
            String json = new ObjectMapper().writeValueAsString(loginMap);

            login = HttpRequest.post(OSU_LOGIN)
                    .header(Header.COOKIE, CharSequenceUtil.format("XSRF-TOKEN={}; osu_session={}", csrfToken.getValue(), loginSession.getValue()))
                    .header(Header.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8")
                    .header(Header.ORIGIN, "https://osu.ppy.sh")
                    .header(Header.REFERER, OSU_INDEX)
                    .header("X-Csrf-Token", csrfToken.getValue())
                    .body(json)
                    .execute();
            log.info("login: login code: {}", login.getStatus());

            // 登录后的cookie
            HttpCookie userSession = login.getCookie("osu_session");
            HttpCookie userCsrfToken = login.getCookie("XSRF-TOKEN");

            // 保存cookie
            OsuCookie cookie = OsuCookie.builder()
                    .session(userSession.getValue())
                    .token(userCsrfToken.getValue())
                    .expireAt(System.currentTimeMillis() + userSession.getMaxAge() * 1000)
                    .build();

            cookieService.save(cookie);

            return cookie;
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getLocalizedMessage());
        } finally {
            if (login != null) login.close();
            if (index != null) index.close();
        }
    }

    /**
     * 多set id批量下载
     *
     * @param setIdList set id list
     * @return 下载信息列表
     */
    private List<DownloadInfo> downloadOszList(List<String> setIdList) {
        // 查询是否有可用cookie
        OsuCookie cookie = cookieService.lambdaQuery()
                .gt(OsuCookie::getExpireAt, System.currentTimeMillis())
                .orderByDesc(OsuCookie::getCreateTime)
                .last("limit 1")
                .oneOpt()
                .orElseGet(login());

        int size = setIdList.size();
        CountDownLatch latch = new CountDownLatch(size);

        // 异步下载谱面
        List<Future<DownloadInfo>> futureList = new ArrayList<>(size);
        for (String setId : setIdList) {
            futureList.add(task.downloadOsz(setId, cookie, latch));
            // 随机休眠 防止429
            int sleep = RandomUtil.randomInt(250, 500);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            // 等待异步任务完成
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(e.getLocalizedMessage());
        }

        // osz的本地路径
        List<DownloadInfo> infoList = new ArrayList<>();
        for (Future<DownloadInfo> future : futureList) {
            try {
                DownloadInfo downloadInfo = future.get();
                if (ObjectUtil.isNotNull(downloadInfo)) {
                    infoList.add(downloadInfo);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                // ignore
            }
        }

        // 下载路径为空时下载失败
        if (CollUtil.isEmpty(infoList)) {
            throw new ServiceException("下载失败");
        }

        // 批量保存
        List<OszFile> oszList = infoList.stream()
                .map(info -> OszFile.builder().path(info.getFilePath().toString()).setId(info.getSetId()).build())
                .toList();
        oszService.saveBatch(oszList);

        return infoList;
    }

    /**
     * 单set下载
     *
     * @param setId set id
     * @return osz文件路径
     */
    private Path downloadOsz(String setId) {
        List<DownloadInfo> infoList = downloadOszList(List.of(setId));
        if (CollUtil.isEmpty(infoList)) {
            throw new ServiceException("下载失败");
        }
        return infoList.get(0).getFilePath();
    }

    /**
     * 下载谱面并上传minio
     *
     * @param setId 谱面集id
     * @return minio下载地址
     */
    @Override
    public String downloadMap(String setId) {
        // 查询minio是否已经上传过
        MinioResource resource = resourceService.lambdaQuery()
                .eq(MinioResource::getKeyword, setId)
                .eq(MinioResource::getType, MinioTypesEnum.MAP.getType())
                .one();

        if (ObjectUtil.isNotNull(resource)) {
            return resource.getUrl();
        }

        // 查询本地是否已经有下载过的文件
        OszFile oszFile = oszService.lambdaQuery()
                .eq(OszFile::getSetId, setId)
                .last("limit 1")
                .one();


        Path filePath;
        if (ObjectUtil.isNotNull(oszFile)) {
            File osz = new File(oszFile.getPath());
            if (osz.isFile() && osz.exists()) {
                filePath = osz.toPath();
            } else {
                filePath = downloadOsz(setId);
            }
        } else {
            filePath = downloadOsz(setId);
        }

        // 上传至minio
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return minioService.uploadFile(setId + ".osz", inputStream, Files.size(filePath), MinioTypesEnum.MAP.getType(), setId);
        } catch (Exception e) {
            throw new ServiceException(MINIO_UPLOAD_FAILED);
        }
    }

    /**
     * 上传音乐文件到kook
     *
     * @param setId     谱面集id
     * @param musicFile 音乐文件名
     * @return kook下载地址
     */
    @Override
    public String uploadMusic(String setId, String musicFile) {
        // 查询是否已经上传过
        OsuAudio audioAsset = audioService.lambdaQuery()
                .eq(OsuAudio::getSetId, setId)
                .one();

        if (ObjectUtil.isNotNull(audioAsset)) {
            return audioAsset.getKookUrl();
        }

        // 查询谱面是否已经下载过
        OszFile oszFile = oszService.lambdaQuery()
                .eq(OszFile::getSetId, setId)
                .one();

        String oszFilePath;

        if (ObjectUtil.isNotNull(oszFile)) {
            File file = new File(oszFile.getPath());
            if (file.isFile() && file.exists()) {
                oszFilePath = file.getPath();
            } else {
                oszFilePath = downloadOsz(setId).toString();
            }
        } else {
            oszFilePath = downloadOsz(setId).toString();
        }

        // 上传至kook
        Future<String> future = task.uploadFileToKook(oszFilePath, musicFile);

        String kookAssetUrl;

        try {
            kookAssetUrl = future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("上传失败");
        } catch (ExecutionException e) {
            throw new ServiceException("上传失败");
        }

        if (CharSequenceUtil.isEmpty(kookAssetUrl)) {
            throw new ServiceException("上传失败");
        }

        // 保存到数据库
        OsuAudio audio = new OsuAudio();
        audio.setSetId(setId);
        audio.setKookUrl(kookAssetUrl);
        audioService.save(audio);

        return kookAssetUrl;
    }

    /**
     * 批量下载bp并上传至minio
     *
     * @param osuUserName osu用户名
     * @param setIdList   谱面集id列表
     * @return minio下载地址
     */
    @Override
    public String createBpPack(String osuUserName, List<String> setIdList) {
        // setIdList去重
        setIdList = setIdList.stream().distinct().toList();

        // 查询已经下载的谱面
        Map<String, String> existOsz = oszService.lambdaQuery()
                .in(OszFile::getSetId, setIdList)
                .list()
                .stream()
                .collect(Collectors.toMap(OszFile::getSetId, OszFile::getPath, (k1, k2) -> k1));

        // 未下载谱面
        List<String> notExistSetIds = setIdList.stream()
                .filter(setId -> !existOsz.containsKey(setId))
                .toList();

        // 下载osz
        if (CollUtil.isNotEmpty(notExistSetIds)) {
            List<DownloadInfo> infoList = downloadOszList(notExistSetIds);
            for (DownloadInfo info : infoList) {
                existOsz.put(info.getSetId(), info.getFilePath().toString());
            }
        }

        File[] files = existOsz.values().stream()
                .map(File::new)
                .toArray(File[]::new);

        // 打包成zip
        String fileName = osuUserName + System.currentTimeMillis() + ".zip";
        File zipFile = FileUtil.file(downloadPath + File.separator + "bp" + File.separator + fileName);
        ZipUtil.zip(zipFile, false, files);

        // 上传至minio
        try (InputStream inputStream = FileUtil.getInputStream(zipFile)) {
            return minioService.uploadFile(fileName, inputStream, FileUtil.size(zipFile), MinioTypesEnum.BP.getType(), osuUserName);
        } catch (Exception e) {
            throw new ServiceException(MINIO_UPLOAD_FAILED);
        } finally {
            FileUtil.del(zipFile);
        }
    }
}
