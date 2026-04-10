package com.sep.realvista.domain.user.preference;

import java.util.Optional;
import java.util.UUID;

public interface SettingPreferenceRepository {

    SettingPreference save(SettingPreference settingPreference);

    Optional<SettingPreference> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
