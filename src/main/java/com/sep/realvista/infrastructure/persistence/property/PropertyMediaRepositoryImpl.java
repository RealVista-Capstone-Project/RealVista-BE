package com.sep.realvista.infrastructure.persistence.property;

import com.sep.realvista.domain.property.PropertyMedia;
import com.sep.realvista.domain.property.repository.PropertyMediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PropertyMediaRepositoryImpl implements PropertyMediaRepository {

    private final PropertyMediaJpaRepository jpaRepository;

    @Override
    public PropertyMedia save(PropertyMedia media) {
        return jpaRepository.save(media);
    }

    @Override
    public Optional<PropertyMedia> findById(UUID id) {
        return jpaRepository.findById(id).filter(m -> Boolean.FALSE.equals(m.getDeleted()));
    }

    @Override
    public List<PropertyMedia> findByPropertyId(UUID propertyId) {
        return jpaRepository.findByPropertyIdAndDeletedIsFalse(propertyId);
    }

    @Override
    public void saveAll(List<PropertyMedia> medias) {
        jpaRepository.saveAll(medias);
    }
}
