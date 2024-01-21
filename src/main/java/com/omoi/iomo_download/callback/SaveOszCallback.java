package com.omoi.iomo_download.callback;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * 下载谱面回调
 *
 * @author omoi
 * @date 2024/1/12
 */
@Data
@Builder
@Slf4j
public class SaveOszCallback implements Callback {
    private CyclicBarrier barrier;
    private String setId;
    private OkHttpClient client;
    private Path savePath;
    private boolean success;

    /**
     * 下载失败回调
     */
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        log.error("download osz error: {}", e.getMessage());
        this.awaitBarrier(false);
    }

    /**
     * 下载成功回调
     */
    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        HttpUrl url = response.request().url();
        log.info("download osz code: {}, url: {}", response.code(), url);
        log.info("save path: {}", this.savePath);
        if (response.code() == 200) {
            ResponseBody body = response.body();
            if (body == null) {
                this.awaitBarrier(false);
                return;
            }

            Files.write(this.savePath, body.bytes(), StandardOpenOption.CREATE);
            this.awaitBarrier(true);
        } else if (response.code() == 404) {
            // ppy.sh禁止下载谱面，使用备用镜像
            String mirrorUrl = "https://beatconnect.io/b/" + this.setId;
            log.info("mirror url: {}", mirrorUrl);
            Request request = new Request.Builder()
                    .url(mirrorUrl)
                    .build();
            try (Response newResponse = this.client.newCall(request).execute()) {
                log.info("mirror download osz code: {}", newResponse.code());
                if (newResponse.code() == 200) {
                    ResponseBody body = newResponse.body();
                    if (body == null) {
                        this.awaitBarrier(false);
                        return;
                    }

                    Files.write(this.savePath, body.bytes(), StandardOpenOption.CREATE);
                    this.awaitBarrier(true);
                } else {
                    this.awaitBarrier(false);
                }
            }
        } else {
            this.awaitBarrier(false);
        }
    }

    private void awaitBarrier(boolean success) {
        this.success = success;
        try {
            barrier.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            // ignore
        }
    }
}
