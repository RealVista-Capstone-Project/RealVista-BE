package com.sep.realvista.domain.property.amenity.repository;

import com.sep.realvista.domain.property.amenity.Amenity;
import java.util.List;

public interface AmenityRepository {
    List<Amenity> findAll();
}
