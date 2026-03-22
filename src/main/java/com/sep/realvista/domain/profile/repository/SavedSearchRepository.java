package com.sep.realvista.domain.profile.repository;

import com.sep.realvista.domain.profile.SavedSearch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {
    List<SavedSearch> findByProfileIdAndDeletedFalse(UUID profileId);
    Optional<SavedSearch> findBySavedSearchIdAndProfileIdAndDeletedFalse(UUID savedSearchId, UUID profileId);

    @Query(value = "SELECT * FROM saved_searches WHERE profile_id = :profileId "
                   + "AND search_type = :#{#searchType?.name()} "
                   + "AND criteria::jsonb = CAST(:criteria AS jsonb) "
                   + "AND deleted = false LIMIT 1", nativeQuery = true)
    Optional<SavedSearch> findByProfileIdAndSearchTypeAndCriteriaAndDeletedFalse(
            @Param("profileId") UUID profileId,
            @Param("searchType") com.sep.realvista.domain.profile.SearchType searchType,
            @Param("criteria") String criteria);
}
