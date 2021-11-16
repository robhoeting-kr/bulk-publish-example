package com.kroger.desp;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfig {
    @Value("${threadpool.executor.corePoolSize}")
    private int corePoolSize;

    @Value("${threadpool.executor.maxPoolSize}")
    private int maxPoolSize;

    @Value("${threadpool.executor.queueCapacity}")
    private int queueCapacity;

    @Bean("threadPoolTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("Review Thread processing-");
        executor.setRejectedExecutionHandler(new BlockCallerExecutionPolicy());
        executor.initialize();
        return executor;
    }
}
