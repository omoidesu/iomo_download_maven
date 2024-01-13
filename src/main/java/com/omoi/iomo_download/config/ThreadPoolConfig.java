package com.omoi.iomo_download.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author omoi
 * @date 2024/1/11
 */
@Configuration
public class ThreadPoolConfig {
    @Bean("netThreadPool")
    public Executor downloadThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        return executor;
    }
}