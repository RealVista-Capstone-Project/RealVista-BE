package com.sep.realvista.domain.user;

import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.user.role.RoleCode;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAllByIdIn(Collection<UUID> ids);

    Optional<User> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void deleteById(UUID id);

    Optional<User> findByEmailValue(String email);

    boolean hasRole(UUID userId, RoleCode roleCode);

    Optional<User> findByPhone(String phone);
}

