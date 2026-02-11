package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.PropertyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyCategoryRepository extends JpaRepository<PropertyCategory, UUID> {
    Optional<PropertyCategory> findByCode(String code);
}
