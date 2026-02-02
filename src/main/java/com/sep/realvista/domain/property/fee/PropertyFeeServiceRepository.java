package com.sep.realvista.domain.property.fee;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for PropertyFeeService entity.
 */
public interface PropertyFeeServiceRepository {

    /**
     * Find all property fees by property ID.
     *
     * @param propertyId the property ID
     * @return list of property fees
     */
    List<PropertyFeeService> findByPropertyId(UUID propertyId);
}
