package com.sep.realvista.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String FIREBASE_JSON = "firebase-service-account.json";
    private static final String FIREBASE_FILE_PATH = "/app/" + FIREBASE_JSON;

    @PostConstruct
    public void initialize() {
        try {
            InputStream serviceAccount = resolveServiceAccount();
            if (serviceAccount == null) {
                log.warn("Firebase service account not found — push notifications will be disabled. "
                        + "Place {} on the classpath or mount it at {}", FIREBASE_JSON, FIREBASE_FILE_PATH);
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            }
        } catch (IOException e) {
            log.error("Firebase initialization failed: {}", e.getMessage());
        }
    }

    /**
     * Resolve the Firebase service account JSON.
     * Priority: filesystem path (Docker volume mount) > classpath (bundled in JAR).
     */
    private InputStream resolveServiceAccount() {
        // 1. Try filesystem path (for Docker deployments with volume mount)
        Path filePath = Path.of(FIREBASE_FILE_PATH);
        if (Files.exists(filePath)) {
            try {
                log.info("Loading Firebase config from filesystem: {}", FIREBASE_FILE_PATH);
                return new FileInputStream(filePath.toFile());
            } catch (IOException e) {
                log.warn("Failed to read Firebase config from {}: {}", FIREBASE_FILE_PATH, e.getMessage());
            }
        }

        // 2. Try classpath (for local development with file in src/main/resources/)
        try {
            ClassPathResource resource = new ClassPathResource(FIREBASE_JSON);
            if (resource.exists()) {
                log.info("Loading Firebase config from classpath");
                return resource.getInputStream();
            }
        } catch (IOException e) {
            log.warn("Failed to read Firebase config from classpath: {}", e.getMessage());
        }

        return null;
    }
}