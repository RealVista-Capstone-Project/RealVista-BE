package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.value.Email;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void deleteById(Long id);

    Optional<User> findByEmailValue(String email);
}

