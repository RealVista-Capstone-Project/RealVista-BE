package com.sep.realvista.presentation.common.util;

import com.sep.realvista.domain.user.User;
import com.sep.realvista.domain.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Utility class for common controller operations.
 * Provides helper methods for trace ID initialization and user authentication.
 */
@Component
@RequiredArgsConstructor
public class ControllerUtils {

    private final UserDomainService userDomainService;

    /**
     * Initialize trace ID and set it in MDC for request tracking.
     *
     * @return generated trace ID
     */
    public String initializeTraceId() {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        return traceId;
    }

    /**
     * Get authenticated user from authentication object.
     *
     * @param authentication Spring Security authentication object
     * @return authenticated User entity
     */
    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userDomainService.getUserByEmailOrThrow(email);
    }
}
