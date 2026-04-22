package com.jasonlat.ai.domain.session.adapter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ScheduledThreadPoolConfig {

    /**
     * 自定义 单线程调度线程池（用于会话清理、定时任务）
     */
    @Bean(destroyMethod = "shutdown", name = "sessionCleanupScheduler") // Spring 关闭时自动调用 shutdown
    public ScheduledExecutorService sessionCleanupScheduler() {
        AtomicInteger threadNum = new AtomicInteger(1);
        // 自定义线程工厂：命名 + 守护线程
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "session-cleanup-thread-" + threadNum.getAndIncrement());
            thread.setDaemon(true); // 守护线程，JVM 退出自动关闭
            return thread;
        };

        // 创建单线程调度池
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }
}