package com.sep.realvista.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.sep.realvista.infrastructure.security.websocket.WebSocketAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

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
 * <p>STOMP relay: RabbitMQ on rabbitmq.stomp.host:rabbitmq.stomp.port (default localhost:61613)
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthenticationInterceptor authenticationInterceptor;

    @Value("${rabbitmq.stomp.host:localhost}")
    private String stompHost;

    @Value("${rabbitmq.stomp.port:61613}")
    private int stompPort;

    @Value("${rabbitmq.stomp.login:guest}")
    private String stompLogin;

    @Value("${rabbitmq.stomp.passcode:guest}")
    private String stompPasscode;

    /**
     * Configure STOMP broker relay using RabbitMQ.
     * Requires a running RabbitMQ with STOMP plugin enabled on stompPort (default 61613).
     * Set rabbitmq.stomp.* in application.yml (or environment variables) to override defaults.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(stompHost)
                .setRelayPort(stompPort)
                .setClientLogin(stompLogin)
                .setClientPasscode(stompPasscode)
                .setSystemLogin(stompLogin)
                .setSystemPasscode(stompPasscode);

        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");

        log.info("STOMP broker relay configured - host: {}:{}, destinations: /topic, /queue",
                stompHost, stompPort);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:3000",
                        "http://localhost:19006",
                        "http://192.168.*.*:*",
                        "http://10.0.*.*:*",
                        "https://*.vercel.app",
                        "https://*.netlify.app",
                        "*"
                );

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

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor);
        log.info("WebSocket authentication interceptor registered");
    }

    /**
     * Register a MappingJackson2MessageConverter with snake_case naming strategy
     * so all STOMP message payloads are serialised consistently.
     * Returning false prevents Spring from adding its default converters on top.
     */
    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        ObjectMapper snakeCaseMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(snakeCaseMapper);
        messageConverters.add(converter);
        return false;
    }
}
