package com.sep.realvista.infrastructure.config;

import com.sep.realvista.infrastructure.security.websocket.WebSocketAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time bidirectional communication.
 * Supports both web (Next.js) and mobile (React Native) clients.
 *
 * <p>Connection endpoint: /ws
 * <p>Message broker destinations:
 * <ul>
 *   <li>/topic - for broadcasting to multiple subscribers</li>
 *   <li>/queue - for point-to-point messaging</li>
 * </ul>
 *
 * <p>Application destination prefix: /app
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthenticationInterceptor authenticationInterceptor;

    /**
     * Configure message broker options.
     * - Simple broker for /topic and /queue destinations
     * - Application destination prefix /app for @MessageMapping
     *
     * @param config the message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for pub/sub
        config.enableSimpleBroker("/topic", "/queue");

        // Set prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");

        // Optional: Set prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");

        log.info("Message broker configured - broker destinations: /topic, /queue | app prefix: /app");
    }

    /**
     * Register STOMP endpoints for WebSocket connections.
     * Supports SockJS fallback for clients that don't support WebSocket.
     *
     * @param registry the STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Native WebSocket endpoint (for Postman, mobile apps, etc.)
        // TODO: AllowedOriginPatterns should not be hardcoded
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",           // Next.js development
                        "http://localhost:19006",          // React Native Expo
                        "http://192.168.*.*:*",            // Local network for mobile testing
                        "http://10.0.*.*:*",               // Local network for mobile testing
                        "https://*.vercel.app",            // Vercel deployment
                        "https://*.netlify.app",           // Netlify deployment
                        "*"                                 // Allow all for development
                );

        // SockJS fallback endpoint (for browsers that don't support WebSocket)
        // TODO: AllowedOriginPatterns should not be hardcoded
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://localhost:19006",
                        "http://192.168.*.*:*",
                        "http://10.0.*.*:*",
                        "https://*.vercel.app",
                        "https://*.netlify.app",
                        "*"
                )
                .withSockJS();

        log.info("STOMP endpoints registered at /ws with native WebSocket and SockJS fallback support");
    }

    /**
     * Configure client inbound channel with authentication interceptor.
     * Validates JWT tokens for WebSocket connections.
     *
     * @param registration the channel registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor);
        log.info("WebSocket authentication interceptor registered");
    }
}
