package com.sep.realvista.infrastructure.persistence.property.fee;

import com.sep.realvista.domain.property.fee.PropertyFeeService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for PropertyFeeService entity.
 */
public interface PropertyFeeServiceJpaRepository extends JpaRepository<PropertyFeeService, UUID> {

    /**
     * Find all property fees by property ID, excluding deleted ones.
     * Results are ordered by optional status (required first) and fee type.
     *
     * @param propertyId the property ID
     * @return list of property fees
     */
    @Query("SELECT pfs FROM PropertyFeeService pfs "
            + "WHERE pfs.propertyId = :propertyId "
            + "AND pfs.deleted = false "
            + "ORDER BY pfs.isOptional ASC, pfs.feeType ASC")
    List<PropertyFeeService> findByPropertyId(@Param("propertyId") UUID propertyId);

}
