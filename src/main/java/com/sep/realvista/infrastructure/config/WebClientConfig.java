package com.sep.realvista.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configures a WebClient bean for reactive/streaming HTTP calls
 * to the AI microservice.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aiWebClient(
            @Value("${realvista.ai.service-url:http://localhost:3001}")
            String aiServiceUrl
    ) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(120));

        return WebClient.builder()
                .baseUrl(aiServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(cfg -> cfg.defaultCodecs()
                        .maxInMemorySize(512 * 1024))
                .build();
    }
}
