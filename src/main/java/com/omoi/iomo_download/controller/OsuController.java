package com.omoi.iomo_download.controller;

import com.omoi.iomo_download.entity.vo.request.BpRequest;
import com.omoi.iomo_download.entity.vo.request.DownloadMapRequest;
import com.omoi.iomo_download.entity.vo.request.UploadMusicRequest;
import com.omoi.iomo_download.entity.vo.response.ResponseEntity;
import com.omoi.iomo_download.service.OsuService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author omoi
 * @date 2024/1/11
 */
@RestController
@RequiredArgsConstructor
public class OsuController {
    private final OsuService osuService;

    /**
     * 下载谱面并上传至本地minio
     */
    @PostMapping("/map")
    public ResponseEntity downloadMap(@RequestBody @Validated DownloadMapRequest request) {
        String downloadUrl = osuService.downloadMap(request.getSetId());
        return ResponseEntity.success(downloadUrl);
    }

    /**
     * 下载谱面并上传音乐到kook
     */
    @PostMapping("/music")
    public ResponseEntity uploadMusicToKook(@RequestBody @Validated UploadMusicRequest request) {
        String downloadUrl = osuService.uploadMusic(request.getSetId(), request.getMusicFile());
        return ResponseEntity.success(downloadUrl);
    }

    /**
     * 下载某人bp并上传至本地minio
     */
    @PostMapping("/bp")
    public ResponseEntity downloadBps(@RequestBody @Validated BpRequest request) {
        String downloadUrl = osuService.createBpPack(request.getOsuName(), request.getSetIdList());
        return ResponseEntity.success(downloadUrl);
    }
}