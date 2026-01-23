package com.sep.realvista.domain.common.value;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Email implements Serializable {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String value;

    public static Email of(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        // Normalize: trim and convert to lowercase first
        String normalized = email.trim().toLowerCase();
        
        // Then validate the normalized email
        if (!normalized.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + normalized);
        }
        
        return new Email(normalized);
    }

    @Override
    public String toString() {
        return value;
    }
}

