package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public void validateUniqueEmail(String email) {
        if (userRepository.findByEmailValue(email).isPresent()) {
            throw new BusinessConflictException("Email already exists: " + email, "EMAIL_ALREADY_EXISTS");
        }
    }

    public User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmailValue(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}

