package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PropertyRepositoryImpl implements PropertyRepository {

    private final PropertyJpaRepository jpaRepository;

    @Override
    public Property save(Property property) {
        return jpaRepository.save(property);
    }

    @Override
    public List<Property> saveAll(List<Property> properties) {
        return jpaRepository.saveAll(properties);
    }

    @Override
    public Optional<Property> findById(UUID id) {
        return jpaRepository.findActiveById(id);
    }

    @Override
    public org.springframework.data.domain.Page<Property> findByOwnerIdAndCriteria(
            UUID ownerId,
            String keyword,
            PropertyStatus status,
            List<PropertyStatus> statuses,
            Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        List<PropertyStatus> statusFilter = statuses == null || statuses.isEmpty() ? null : statuses;
        PropertyStatus singleStatus = statusFilter == null ? status : null;
        return jpaRepository.findByOwnerIdAndKeyword(ownerId, keywordPattern, singleStatus, statusFilter, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Property> findByAgentIdAndCriteria(
            UUID agentId,
            String keyword,
            PropertyStatus status,
            List<PropertyStatus> statuses,
            Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        List<PropertyStatus> statusFilter = statuses == null || statuses.isEmpty() ? null : statuses;
        PropertyStatus singleStatus = statusFilter == null ? status : null;
        return jpaRepository.findByAgentIdAndKeyword(agentId, keywordPattern, singleStatus, statusFilter, pageable);
    }

    @Override
    public List<Property> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Property> findByOwnerIdOrAgentId(UUID userId) {
        return jpaRepository.findByOwnerIdOrAgentId(userId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public List<Property> findInLocationRange(BigDecimal northLat, BigDecimal southLat,
                                                BigDecimal eastLng, BigDecimal westLng) {
        return jpaRepository.findByLocationRange(northLat, southLat, eastLng, westLng);
    }

    @Override
    public List<Property> searchByAddress(String address) {
        return jpaRepository.searchByAddress(address);
    }

    @Override
    public long countByLocationIds(List<UUID> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            return 0;
        }
        return jpaRepository.countByLocationIds(locationIds);
    }

    @Override
    public org.springframework.data.domain.Page<Property> findPropertyFeed(
            UUID agentId,
            String keyword,
            UUID propertyTypeId,
            UUID locationId,
            org.springframework.data.domain.Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase(java.util.Locale.ROOT) + "%";
        return jpaRepository.findPropertyFeed(agentId, keywordPattern, propertyTypeId, locationId, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Property> findByAdminCriteria(
            String keyword,
            com.sep.realvista.domain.property.PropertyStatus status,
            UUID userId,
            UUID propertyTypeId,
            UUID locationId,
            org.springframework.data.domain.Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase(java.util.Locale.ROOT) + "%";
        return jpaRepository.findByAdminCriteria(
                keywordPattern, status, userId, propertyTypeId, locationId, pageable);
    }
}
