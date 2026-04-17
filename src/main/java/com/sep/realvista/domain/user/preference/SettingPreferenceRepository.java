package com.sep.realvista.domain.user.preference;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettingPreferenceRepository {

    SettingPreference save(SettingPreference settingPreference);

    Optional<SettingPreference> findByUserId(UUID userId);

    List<SettingPreference> findByUserIdIn(Collection<UUID> userIds);

    boolean existsByUserId(UUID userId);
}
