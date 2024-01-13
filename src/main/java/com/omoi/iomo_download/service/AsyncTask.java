package com.omoi.iomo_download.service;

import com.omoi.iomo_download.entity.dto.DownloadInfo;
import com.omoi.iomo_download.entity.dto.OsuCookie;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

/**
 * @author omoi
 * @date 2024/1/10
 */
public interface AsyncTask {
    Future<DownloadInfo> downloadOsz(String setId, OsuCookie cookie, CountDownLatch latch);

    Future<String> uploadFileToKook(String oszPath, String targetFileName);
}
