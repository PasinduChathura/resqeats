package com.ffms.resqeats.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter implementing SRS Section 13 Security Requirements.
 * Uses token bucket algorithm with per-IP rate limiting.
 * 
 * Default limits:
 * - General API: 100 requests per minute per IP
 * - Auth endpoints: 10 requests per minute per IP (to prevent brute force)
 */
@Component
@Order(1)
@Slf4j
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();

    @Value("${resqeats.ratelimit.general.requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${resqeats.ratelimit.auth.requests-per-minute:10}")
    private int authRequestsPerMinute;

    @Value("${resqeats.ratelimit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (!rateLimitEnabled) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIP(httpRequest);
        String path = httpRequest.getRequestURI();

        Bucket bucket;
        if (isAuthEndpoint(path)) {
            bucket = authBuckets.computeIfAbsent(clientIp, this::createAuthBucket);
        } else {
            bucket = buckets.computeIfAbsent(clientIp, this::createGeneralBucket);
        }

        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            httpResponse.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
        }
    }

    private Bucket createGeneralBucket(String key) {
        // Using the newer builder API
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(generalRequestsPerMinute)
                        .refillIntervally(generalRequestsPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private Bucket createAuthBucket(String key) {
        // Stricter limit for auth endpoints to prevent brute force attacks
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(authRequestsPerMinute)
                        .refillIntervally(authRequestsPerMinute, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private boolean isAuthEndpoint(String path) {
        return path.contains("/auth/signin") || 
               path.contains("/auth/signup") ||
               path.contains("/auth/resetPassword");
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP if multiple are present
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Rate limiting filter initialized. Enabled: {}, General limit: {} req/min, Auth limit: {} req/min",
                rateLimitEnabled, generalRequestsPerMinute, authRequestsPerMinute);
    }

    @Override
    public void destroy() {
        buckets.clear();
        authBuckets.clear();
    }
}
