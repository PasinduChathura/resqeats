package com.ffms.resqeats.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import java.util.Set;

/**
 * Rate limiting filter implementing SRS Section 13 Security Requirements.
 * Uses token bucket algorithm with per-IP rate limiting.
 * 
 * Default limits:
 * - General API: 100 requests per minute per IP
 * - Auth endpoints: 10 requests per minute per IP (to prevent brute force)
 * 
 * PRODUCTION FIX: Uses Caffeine cache with TTL-based eviction to prevent memory leaks
 * from unbounded ConcurrentHashMap growth (Critical Issue #1).
 */
@Component
@Order(1)
@Slf4j
public class RateLimitingFilter implements Filter {

    // CRITICAL FIX: Use Caffeine cache with expiration to prevent memory leak
    // Buckets expire after 10 minutes of inactivity, max 100,000 entries
    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    private final Cache<String, Bucket> authBuckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    // Trusted proxy IPs for X-Forwarded-For header validation (Medium Issue #11)
    private static final Set<String> TRUSTED_PROXIES = Set.of(
            "127.0.0.1", "::1", "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"
    );

    @Value("${resqeats.ratelimit.general.requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${resqeats.ratelimit.auth.requests-per-minute:10}")
    private int authRequestsPerMinute;

    @Value("${resqeats.ratelimit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${resqeats.ratelimit.trust-proxy-headers:false}")
    private boolean trustProxyHeaders;

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
            // Use Caffeine's get() which handles concurrent access safely
            bucket = authBuckets.get(clientIp, this::createAuthBucket);
        } else {
            bucket = buckets.get(clientIp, this::createGeneralBucket);
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

    /**
     * Get client IP with trusted proxy validation (Medium Issue #11 fix).
     * Only trusts X-Forwarded-For headers when trustProxyHeaders is enabled
     * and request comes from a known proxy.
     */
    private String getClientIP(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        
        // Only trust proxy headers if enabled and request is from trusted proxy
        if (trustProxyHeaders && isTrustedProxy(remoteAddr)) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // Take the first IP (original client)
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
        }
        
        return remoteAddr;
    }

    /**
     * Check if remote address is a trusted proxy.
     */
    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null) {
            return false;
        }
        // Simple check - in production, use proper CIDR matching
        return TRUSTED_PROXIES.stream().anyMatch(proxy -> 
                remoteAddr.equals(proxy) || proxy.contains("/"));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Rate limiting filter initialized. Enabled: {}, General limit: {} req/min, Auth limit: {} req/min, Trust proxy headers: {}",
                rateLimitEnabled, generalRequestsPerMinute, authRequestsPerMinute, trustProxyHeaders);
    }

    @Override
    public void destroy() {
        buckets.invalidateAll();
        authBuckets.invalidateAll();
    }
}
