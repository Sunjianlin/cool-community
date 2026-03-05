package com.cool.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {
    @Bean("seckillAsyncExecutor")
    public ThreadPoolExecutor seckillAsyncExecutor() {
        return new ThreadPoolExecutor(
                10, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10000),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "seckill-async-" + count++);
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时降级到主线程，避免丢任务
        );
    }
}

