package com.sep.realvista.application.listing.contract.mapper;

import com.sep.realvista.application.listing.contract.dto.LeaseResponse;
import com.sep.realvista.domain.common.value.Email;
import com.sep.realvista.domain.listing.contract.LeaseAgreement;
import com.sep.realvista.domain.property.PropertyType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for {@link LeaseAgreement} entity to/from DTOs.
 */
@Mapper(componentModel = "spring")
public interface LeaseAgreementMapper {

    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "renterFullName", 
             expression = "java(leaseAgreement.getRenter() != null " 
                 + "? leaseAgreement.getRenter().getFullName() : null)")
    @Mapping(target = "renterEmail", source = "renter.email")
    @Mapping(target = "renterPhone", source = "renter.phone")
    @Mapping(target = "renterAvatarUrl", source = "renter.avatarUrl")
    @Mapping(target = "landlordFullName", 
             expression = "java(leaseAgreement.getLandlord() != null " 
                 + "? leaseAgreement.getLandlord().getFullName() : null)")
    @Mapping(target = "landlordEmail", source = "landlord.email")
    @Mapping(target = "landlordPhone", source = "landlord.phone")
    @Mapping(target = "landlordAvatarUrl", source = "landlord.avatarUrl")
    @Mapping(target = "propertyTitle", source = "property.streetAddress")
    @Mapping(target = "propertyAddress", source = "property.streetAddress")
    @Mapping(target = "propertyType", source = "property.propertyType")
    @Mapping(target = "propertyThumbnailUrl", ignore = true)
    @Mapping(target = "propertyImageUrl", ignore = true)
    LeaseResponse toResponse(LeaseAgreement leaseAgreement);

    default String mapEmail(Email email) {
        return email != null ? email.getValue() : null;
    }

    default String mapPropertyType(PropertyType type) {
        return type != null ? type.getName() : null;
    }
}
