package com.sep.realvista.domain.property.fee;

import java.util.List;
import java.util.Optional;
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

    /**
     * Find a fee by its ID.
     */
    Optional<PropertyFeeService> findById(UUID feeId);

    /**
     * Persist a new or updated fee.
     */
    PropertyFeeService save(PropertyFeeService fee);

    /**
     * Soft-delete a single fee.
     */
    void softDelete(UUID feeId);

    /**
     * Soft-delete all fees belonging to a property.
     */
    void softDeleteAllByPropertyId(UUID propertyId);
}
