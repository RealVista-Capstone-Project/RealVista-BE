package com.sep.realvista.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for sensitive auth endpoints.
 * Limits POST /api/v1/auth/register, /login, /forgot-password, and /reset-password
 * to 5 requests per minute per client IP address using Bucket4j.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int RATE_LIMIT_CAPACITY = 5;
    private static final Duration RATE_LIMIT_PERIOD = Duration.ofMinutes(1);
    private static final String RATE_LIMITED_RESPONSE =
            "{\"error\": \"TOO_MANY_REQUESTS\","
            + " \"message\": \"Quá nhiều yêu cầu. Vui lòng thử lại sau 1 phút.\"}";

    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String FORGOT_PASSWORD_PATH = "/api/v1/auth/forgot-password";
    private static final String RESET_PASSWORD_PATH = "/api/v1/auth/reset-password";
    private static final String POST_METHOD = "POST";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String JSON_CONTENT_TYPE = "application/json";

    /** One token bucket per unique client IP address. */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isRateLimitedPath = REGISTER_PATH.equals(path)
                || LOGIN_PATH.equals(path)
                || FORGOT_PASSWORD_PATH.equals(path)
                || RESET_PASSWORD_PATH.equals(path);

        if (!isRateLimitedPath || !POST_METHOD.equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(RATE_LIMITED_RESPONSE);
        }
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RATE_LIMIT_CAPACITY)
                .refillGreedy(RATE_LIMIT_CAPACITY, RATE_LIMIT_PERIOD)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resolves the actual client IP, handling reverse-proxy X-Forwarded-For header.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
