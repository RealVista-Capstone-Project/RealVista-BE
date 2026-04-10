package com.sep.realvista.infrastructure.security.apikey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sep.realvista.application.common.dto.ApiResponse;
import com.sep.realvista.infrastructure.constants.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authentication filter for internal service-to-service API calls.
 * Validates the {@code x-service-api-key} header against the configured API key.
 * Only applies to paths matching {@link SecurityConstants.InternalEndpoints#INTERNAL_PATHS}.
 */
@Slf4j
public class InternalApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "x-service-api-key";

    private final String expectedApiKey;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public InternalApiKeyAuthenticationFilter(
            @Value("${realvista.ai.api-key:}") String expectedApiKey,
            ObjectMapper objectMapper
    ) {
        this.expectedApiKey = expectedApiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String pattern : SecurityConstants.InternalEndpoints.INTERNAL_PATHS) {
            if (pathMatcher.match(pattern, path)) {
                return false; // DO filter this request
            }
        }
        return true; // skip filter for non-internal paths
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String providedKey = request.getHeader(API_KEY_HEADER);

        if (expectedApiKey.isBlank()) {
            log.error("Internal API key is not configured. Rejecting request to {}", request.getServletPath());
            writeUnauthorizedResponse(response, "Internal API key is not configured");
            return;
        }

        if (providedKey == null || providedKey.isBlank()) {
            log.warn("Missing {} header for internal endpoint: {}", API_KEY_HEADER, request.getServletPath());
            writeUnauthorizedResponse(response, "Missing API key");
            return;
        }

        if (!expectedApiKey.equals(providedKey)) {
            log.warn("Invalid API key provided for internal endpoint: {}", request.getServletPath());
            writeUnauthorizedResponse(response, "Invalid API key");
            return;
        }

        log.debug("Internal API key validated for: {}", request.getServletPath());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "internal-service",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiResponse = ApiResponse.error(message);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
