package com.sep.realvista.application.property;

import com.sep.realvista.application.property.dto.PropertyAttributeDTO;
import com.sep.realvista.domain.property.attribute.PropertyTypeAttribute;
import com.sep.realvista.domain.property.repository.PropertyTypeAttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyAttributeService {

    private final PropertyTypeAttributeRepository propertyTypeAttributeRepository;

    @Transactional(readOnly = true)
    public List<PropertyAttributeDTO> getSearchableAttributesByPropertyType(String typeCode) {
        log.debug("Fetching searchable attributes for property type: {}", typeCode);
        
        List<PropertyTypeAttribute> attributes = propertyTypeAttributeRepository
                .findSearchableAttributesByPropertyTypeCode(typeCode);
        
        return attributes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PropertyAttributeDTO> getSearchableAttributesByPropertyCategory(String categoryCode) {
        log.debug("Fetching searchable attributes for property category: {}", categoryCode);
        
        List<PropertyTypeAttribute> attributes = propertyTypeAttributeRepository
                .findSearchableAttributesByPropertyCategoryCode(categoryCode);
        
        return attributes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private PropertyAttributeDTO toDTO(PropertyTypeAttribute pta) {
        return PropertyAttributeDTO.builder()
                .propertyAttributeId(pta.getPropertyAttribute().getPropertyAttributeId())
                .name(pta.getPropertyAttribute().getName())
                .code(pta.getPropertyAttribute().getCode())
                .dataType(pta.getPropertyAttribute().getDataType().name())
                .isSearchable(pta.getPropertyAttribute().getIsSearchable())
                .icon(pta.getPropertyAttribute().getIcon())
                .unit(pta.getPropertyAttribute().getUnit())
                .isRequired(pta.getIsRequired())
                .build();
    }
}
