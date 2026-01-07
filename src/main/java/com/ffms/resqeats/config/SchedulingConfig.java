package com.ffms.resqeats.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling configuration with ShedLock for distributed task locking.
 * 
 * MEDIUM FIX (Issue #10): Added ShedLock to prevent duplicate task execution
 * in multi-instance deployments.
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M") // Lock for max 5 minutes
public class SchedulingConfig {
    
    /**
     * Redis-based lock provider for distributed scheduling.
     * Ensures only one instance runs a scheduled task at a time.
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "resqeats", "shedlock:");
    }
}
