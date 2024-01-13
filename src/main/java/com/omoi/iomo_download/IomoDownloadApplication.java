package com.omoi.iomo_download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class IomoDownloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(IomoDownloadApplication.class, args);
    }

}
