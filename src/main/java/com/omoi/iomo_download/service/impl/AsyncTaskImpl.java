package com.omoi.iomo_download.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ZipUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omoi.iomo_download.callback.SaveOszCallback;
import com.omoi.iomo_download.entity.dto.DownloadInfo;
import com.omoi.iomo_download.entity.dto.OsuCookie;
import com.omoi.iomo_download.entity.vo.KookResponse;
import com.omoi.iomo_download.exception.ServiceException;
import com.omoi.iomo_download.service.AsyncTask;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;
import java.util.concurrent.*;

/**
 * @author omoi
 * @date 2024/1/10
 */
@Component
@Slf4j
public class AsyncTaskImpl implements AsyncTask {
    private static final String OSU_SET = "https://osu.ppy.sh/beatmapsets/";

    @Value("${download.path}")
    private String downloadPath;

    @Value("${kook.token}")
    private String kookAuthorization;

    @Value("${kook.assetUrl}")
    private String kookAssetUrl;

    @Value("${dev.proxy.port}")
    private Integer proxyPort;

    /**
     * 从ppy.sh下载osz文件
     *
     * @param setId  谱面集id
     * @param cookie 用户cookie
     * @return 下载文件路径
     */
    @Async("netThreadPool")
    @Override
    public Future<DownloadInfo> downloadOsz(String setId, OsuCookie cookie, CountDownLatch latch) {
        // 如果设置了代理，使用代理
        OkHttpClient client;
        if (proxyPort != null) {
            client = new OkHttpClient.Builder()
                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyPort)))
                    .build();
        } else {
            client = new OkHttpClient();
        }

        // 构建请求
        Request request = new Request.Builder()
                .url(OSU_SET + setId + "/download")
                .addHeader("Cookie", CharSequenceUtil.format("XSRF-TOKEN={}; osu_session={}", cookie.getToken(), cookie.getSession()))
                .addHeader("Referer", OSU_SET + setId)
                .build();

        // 构建回调
        CyclicBarrier barrier = new CyclicBarrier(2);
        Path savePath = Path.of(downloadPath, setId + ".osz");
        SaveOszCallback callback = SaveOszCallback.builder()
                .savePath(savePath)
                .barrier(barrier)
                .setId(setId)
                .client(client)
                .build();

        // 发送请求
        client.newCall(request).enqueue(callback);

        try {
            // 等待回调完成
            barrier.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("download osz failed: {}", e.getLocalizedMessage());
            return CompletableFuture.completedFuture(null);
        } catch (BrokenBarrierException e) {
            log.error("download osz failed: {}", e.getLocalizedMessage());
            return CompletableFuture.completedFuture(null);
        }

        try {
            if (callback.isSuccess()) {
                return CompletableFuture.completedFuture(new DownloadInfo(setId, savePath));
            }

            log.error("download osz failed");
            return CompletableFuture.completedFuture(null);
        } finally {
            // 待执行线程数减一
            latch.countDown();
        }
    }

    /**
     * 上传音乐
     *
     * @param oszPath        osz文件路径
     * @param targetFileName 音乐文件名
     * @return kook服务器地址
     */
    @Async("netThreadPool")
    @Override
    public Future<String> uploadFileToKook(String oszPath, String targetFileName) {
        // 解压osz
        String unzipPath = IdUtil.simpleUUID();
        File unzipFolder = ZipUtil.unzip(oszPath, downloadPath + File.separator + "unzip" + File.separator + unzipPath);
        File targetFile = new File(unzipFolder, targetFileName);
        if (!targetFile.exists()) {
            return CompletableFuture.completedFuture(null);
        }

        // 上传到kook
        OkHttpClient client = new OkHttpClient();

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", targetFileName, RequestBody.Companion.create(targetFile, MediaType.parse("multipart/form-data")))
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", kookAuthorization)
                .url(kookAssetUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                ObjectMapper mapper = new ObjectMapper();
                KookResponse kookResponse = mapper.readValue(json, KookResponse.class);
                return CompletableFuture.completedFuture(kookResponse.getData().getUrl());
            }
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            throw new ServiceException(e.getLocalizedMessage());
        } finally {
            FileUtil.del(unzipFolder);
        }
    }
}
