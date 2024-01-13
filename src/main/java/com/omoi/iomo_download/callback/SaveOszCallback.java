package com.omoi.iomo_download.callback;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author omoi
 * @date 2024/1/12
 */
@Data
@Builder
@Slf4j
public class SaveOszCallback implements Callback {
    private CyclicBarrier barrier;
    private Path savePath;
    private boolean success;

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        this.success = false;
        log.error("download osz error: {}", e.getMessage());
        try {
            this.barrier.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException ex) {
            // ignore
        }
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        log.info("download osz url: {}", response.request().url());
        log.info("download osz code: {}", response.code());
        log.info("save path: {}", this.savePath);
        if (response.code() == 200) {
            ResponseBody body = response.body();
            if (body == null) {
                this.success = false;
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (BrokenBarrierException e) {
                    // ignore
                }
                return;
            }

            Files.write(this.savePath, body.bytes(), StandardOpenOption.CREATE);
            this.success = true;
            try {
                barrier.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                // ignore
            }
        } else {
            this.success = false;
            try {
                barrier.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException e) {
                // ignore
            }
        }
    }
}
