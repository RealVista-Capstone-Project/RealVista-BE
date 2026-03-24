package com.sep.realvista.infrastructure.persistence.property.amenity;

import com.sep.realvista.domain.property.amenity.Amenity;
import com.sep.realvista.domain.property.amenity.repository.AmenityRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AmenityJpaRepository extends JpaRepository<Amenity, UUID>, AmenityRepository {
}
