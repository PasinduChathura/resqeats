package com.ffms.resqeats.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global request logging filter.
 * - Adds/propagates X-Correlation-Id into MDC
 * - Logs incoming request method, URI, remote IP
 * - Logs completed status and duration
 * - Optionally logs non-sensitive headers at DEBUG level
 */
@Component
@Slf4j
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Value("${app.logging.request.enabled:true}")
    private boolean enabled;

    @Value("${app.logging.request.log-headers:false}")
    private boolean logHeaders;

    @Value("${app.logging.request.exclude-patterns:}")
    private String excludePatterns;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (!enabled) return true;
        if (excludePatterns == null || excludePatterns.isBlank()) return false;
        List<String> patterns = Arrays.stream(excludePatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        String path = request.getRequestURI();
        for (String p : patterns) {
            if (PATH_MATCHER.match(p, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);

        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String remote = request.getRemoteAddr();

        log.info("Incoming request: {} {}{} from {}", method, uri, (query != null ? "?" + query : ""), remote);

        if (logHeaders && log.isDebugEnabled()) {
            log.debug("Request headers:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if ("authorization".equalsIgnoreCase(name) || "cookie".equalsIgnoreCase(name)) continue; // avoid sensitive
                Enumeration<String> values = request.getHeaders(name);
                String joined = Collections.list(values).stream().collect(Collectors.joining(","));
                log.debug("  {}: {}", name, joined);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            long duration = System.currentTimeMillis() - start;
            log.info("Completed request: {} {}{} status={} timeMs={}", method, uri, (query != null ? "?" + query : ""), status, duration);
            MDC.remove("correlationId");
        }
    }
}