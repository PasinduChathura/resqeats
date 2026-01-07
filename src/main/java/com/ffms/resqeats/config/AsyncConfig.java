package com.ffms.resqeats.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async configuration for non-blocking operations.
 * 
 * MEDIUM FIX (Issue #13): Enables @Async for WebSocket broadcasts
 * to prevent blocking transactional methods.
 * 
 * MEDIUM-001 FIX: Increased thread pool sizes for production workloads
 * and added proper rejection handling.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // MEDIUM-001 FIX: Increased pool sizes for production workloads
        // CorePoolSize: Number of threads to keep in pool
        executor.setCorePoolSize(8);
        // MaxPoolSize: Maximum number of threads to allow
        executor.setMaxPoolSize(20);
        // QueueCapacity: Size of the queue for pending tasks
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // MEDIUM-001 FIX: Add rejection handler to log when pool is saturated
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                log.warn("Task rejected from async executor. Pool size: {}, Active: {}, Queue: {}/{}",
                        e.getPoolSize(), e.getActiveCount(), 
                        e.getQueue().size(), executor.getQueueCapacity());
                // Run in caller's thread as fallback (CallerRunsPolicy behavior)
                if (!e.isShutdown()) {
                    r.run();
                }
            }
        });
        
        executor.initialize();
        return executor;
    }
}
