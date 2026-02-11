package com.sep.realvista.domain.property.repository;

import com.sep.realvista.domain.property.attribute.PropertyTypeAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyTypeAttributeRepository extends JpaRepository<PropertyTypeAttribute, UUID> {

    @Query("SELECT pta FROM PropertyTypeAttribute pta " +
           "JOIN FETCH pta.propertyAttribute pa " +
           "WHERE pta.propertyType.code = :typeCode " +
           "AND pta.deleted = false " +
           "AND pa.deleted = false " +
           "AND pa.isSearchable = true " +
           "ORDER BY pa.name")
    List<PropertyTypeAttribute> findSearchableAttributesByPropertyTypeCode(@Param("typeCode") String typeCode);
    
    @Query("SELECT pta FROM PropertyTypeAttribute pta " +
           "JOIN FETCH pta.propertyAttribute pa " +
           "WHERE pta.propertyType.propertyCategory.code = :categoryCode " +
           "AND pta.deleted = false " +
           "AND pa.deleted = false " +
           "AND pa.isSearchable = true " +
           "ORDER BY pa.name")
    List<PropertyTypeAttribute> findSearchableAttributesByPropertyCategoryCode(@Param("categoryCode") String categoryCode);
}
