package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.exception.BusinessConflictException;
import com.sep.realvista.domain.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public void validateUniqueEmail(String email) {
        if (userRepository.findByEmailValue(email).isPresent()) {
            throw new BusinessConflictException("Email already exists: " + email, "EMAIL_ALREADY_EXISTS");
        }
    }

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmailValue(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email));
    }
}

