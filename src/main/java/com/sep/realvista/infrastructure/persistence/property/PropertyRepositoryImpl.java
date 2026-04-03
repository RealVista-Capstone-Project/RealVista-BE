package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.Property;
import com.sep.realvista.domain.property.PropertyStatus;
import com.sep.realvista.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class PropertyRepositoryImpl implements PropertyRepository {

    private final PropertyJpaRepository jpaRepository;

    @Override
    public Property save(Property property) {
        return jpaRepository.save(property);
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
            Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        return jpaRepository.findByOwnerIdAndKeyword(ownerId, keywordPattern, status, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<Property> findByAgentIdAndCriteria(
            UUID agentId,
            String keyword,
            PropertyStatus status,
            Pageable pageable) {
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? null
                : "%" + keyword.trim().toLowerCase() + "%";
        return jpaRepository.findByAgentIdAndKeyword(agentId, keywordPattern, status, pageable);
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
}
