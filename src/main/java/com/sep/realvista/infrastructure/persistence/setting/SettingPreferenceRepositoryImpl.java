package com.sep.realvista.infrastructure.persistence.setting;

import com.sep.realvista.domain.user.preference.SettingPreference;
import com.sep.realvista.domain.user.preference.SettingPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SettingPreferenceRepositoryImpl implements SettingPreferenceRepository {

    private final SettingPreferenceJpaRepository jpaRepository;

    @Override
    public SettingPreference save(SettingPreference settingPreference) {
        return jpaRepository.save(settingPreference);
    }

    @Override
    public Optional<SettingPreference> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public java.util.List<SettingPreference> findByUserIdIn(java.util.Collection<java.util.UUID> userIds) {
        return jpaRepository.findByUserIdIn(userIds);
    }
}
