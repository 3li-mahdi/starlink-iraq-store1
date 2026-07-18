package com.starlinkiraq.store.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * يحدّ من عدد الطلبات المسموح بها لكل عنوان IP على endpoints الحساسة (تسجيل الدخول، التسجيل، الشراء)
 * لمنع هجمات brute-force والبوتات الآلية.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Map<String, Bandwidth> LIMITED_PATHS = Map.of(
            "/api/auth/login", Bandwidth.classic(10, io.github.bucket4j.Refill.intervally(10, Duration.ofMinutes(1))),
            "/api/auth/register", Bandwidth.classic(10, io.github.bucket4j.Refill.intervally(10, Duration.ofMinutes(1))),
            "/api/checkout", Bandwidth.classic(20, io.github.bucket4j.Refill.intervally(20, Duration.ofMinutes(1)))
    );

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        Bandwidth limit = LIMITED_PATHS.get(path);

        if (limit != null) {
            String clientIp = IpAddressUtil.resolveClientIp(request);
            String bucketKey = path + "|" + clientIp;
            Bucket bucket = buckets.computeIfAbsent(bucketKey, key -> Bucket.builder().addLimit(limit).build());

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(
                        "{\"error\":\"عدد الطلبات كبير جداً، الرجاء المحاولة لاحقاً\",\"status\":429}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
