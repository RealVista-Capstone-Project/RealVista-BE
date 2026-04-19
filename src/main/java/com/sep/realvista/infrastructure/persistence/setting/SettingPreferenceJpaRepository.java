package com.sep.realvista.infrastructure.persistence.setting;

import com.sep.realvista.domain.user.preference.SettingPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SettingPreferenceJpaRepository extends JpaRepository<SettingPreference, UUID> {

    Optional<SettingPreference> findByUserId(UUID userId);

    java.util.List<SettingPreference> findByUserIdIn(java.util.Collection<UUID> userIds);

    boolean existsByUserId(UUID userId);
}
