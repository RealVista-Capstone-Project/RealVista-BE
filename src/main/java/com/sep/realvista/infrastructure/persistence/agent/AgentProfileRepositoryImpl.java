package com.sep.realvista.infrastructure.persistence.agent;

import com.sep.realvista.domain.agent.AgentProfile;
import com.sep.realvista.domain.agent.AgentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of AgentProfileRepository interface.
 *
 * Bridges the domain repository contract with Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class AgentProfileRepositoryImpl implements AgentProfileRepository {

    private final AgentProfileJpaRepository jpaRepository;

    @Override
    public Optional<AgentProfile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId);
    }

    @Override
    public List<AgentProfile> findByUserIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findByUserIdIn(userIds);
    }
}
