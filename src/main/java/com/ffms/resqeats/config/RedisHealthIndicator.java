package com.ffms.resqeats.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom Redis health indicator.
 * 
 * LOW FIX (Issue #19): Provides detailed Redis health status for monitoring.
 */
@Component("redis")
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        try {
            RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
            if (factory == null) {
                return Health.down().withDetail("error", "No Redis connection factory").build();
            }

            // Ping Redis
            String result = factory.getConnection().ping();
            
            if ("PONG".equals(result)) {
                return Health.up()
                        .withDetail("status", "Redis is responding")
                        .build();
            } else {
                return Health.down()
                        .withDetail("error", "Unexpected ping response: " + result)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
