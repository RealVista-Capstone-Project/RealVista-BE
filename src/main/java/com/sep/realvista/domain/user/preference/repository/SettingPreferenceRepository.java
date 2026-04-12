package com.sep.realvista.domain.user.preference.repository;

import com.sep.realvista.domain.user.preference.SettingPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettingPreferenceRepository extends JpaRepository<SettingPreference, UUID> {
    Optional<SettingPreference> findByUserId(UUID userId);

    List<SettingPreference> findByUserIdIn(Collection<UUID> userIds);
}
