package com.sep.realvista.infrastructure.persistence.agent;

import com.sep.realvista.domain.agent.AgentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for AgentProfile entity.
 */
public interface AgentProfileJpaRepository extends JpaRepository<AgentProfile, UUID> {

    /**
     * Finds an agent profile by user ID with user data eagerly fetched.
     *
     * @param userId the user ID
     * @return optional agent profile if found
     */
    @Query("SELECT ap FROM AgentProfile ap "
           + "LEFT JOIN FETCH ap.user "
           + "WHERE ap.userId = :userId AND ap.deleted = false")
    Optional<AgentProfile> findByUserId(@Param("userId") UUID userId);

    /**
     * Finds agent profiles by a list of user IDs with user data eagerly fetched.
     *
     * @param userIds the list of user IDs
     * @return list of agent profiles
     */
    @Query("SELECT ap FROM AgentProfile ap "
           + "LEFT JOIN FETCH ap.user "
           + "WHERE ap.userId IN :userIds AND ap.deleted = false")
    List<AgentProfile> findByUserIdIn(@Param("userIds") List<UUID> userIds);
}
