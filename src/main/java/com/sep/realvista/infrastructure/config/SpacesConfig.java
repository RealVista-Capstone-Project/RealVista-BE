package com.sep.realvista.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "digitalocean.spaces")
@Getter
@Setter
public class SpacesConfig {

    private String accessKey;
    private String secretKey;
    private String region;
    private String endpoint;
    private String bucketName;
    private String cdnEndpoint;

    @Bean
    public S3Client s3Client() {
        String safeAccessKey = (accessKey == null || accessKey.trim().isEmpty()) ? "dummy-access-key" : accessKey;
        String safeSecretKey = (secretKey == null || secretKey.trim().isEmpty()) ? "dummy-secret-key" : secretKey;
        String safeRegion = (region == null || region.trim().isEmpty()) ? "sgp1" : region;
        String safeEndpoint = (endpoint == null || endpoint.trim().isEmpty()) ? "https://sgp1.digitaloceanspaces.com" : endpoint;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(safeAccessKey, safeSecretKey);

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(safeRegion))
                .endpointOverride(URI.create(safeEndpoint))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(false)
                        .build())
                .build();
    }
}
