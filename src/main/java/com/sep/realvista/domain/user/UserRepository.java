package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.value.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void deleteById(UUID id);

    Optional<User> findByEmailValue(String email);
}

