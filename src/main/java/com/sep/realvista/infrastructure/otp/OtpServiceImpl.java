package com.sep.realvista.infrastructure.otp;

import com.sep.realvista.application.service.OtpService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OTP store backed by a {@link ConcurrentHashMap}.
 * Suitable for single-instance deployments; swap with Redis for multi-instance.
 */
@Service
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private record OtpEntry(String otp, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    @Override
    public String generateAndStore(String key, int expiryMinutes) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(expiryMinutes * 60L);
        store.put(key, new OtpEntry(otp, expiresAt));
        return otp;
    }

    @Override
    public void store(String key, String value, int expiryMinutes) {
        Instant expiresAt = Instant.now().plusSeconds(expiryMinutes * 60L);
        store.put(key, new OtpEntry(value, expiresAt));
    }

    @Override
    public String get(String key) {
        OtpEntry entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return entry.otp();
    }

    @Override
    public void remove(String key) {
        store.remove(key);
    }

    @Override
    public boolean verify(String key, String otp) {
        OtpEntry entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            store.remove(key);
            return false;
        }
        if (entry.otp().equals(otp)) {
            store.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public long remainingSeconds(String key) {
        OtpEntry entry = store.get(key);
        if (entry == null || entry.isExpired()) {
            return -1;
        }
        return Instant.now().until(entry.expiresAt(), java.time.temporal.ChronoUnit.SECONDS);
    }
}
