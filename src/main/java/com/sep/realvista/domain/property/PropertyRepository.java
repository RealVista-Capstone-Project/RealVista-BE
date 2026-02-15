package com.sep.realvista.domain.property;

import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {

    Property save(Property property);

    Optional<Property> findById(UUID id);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
