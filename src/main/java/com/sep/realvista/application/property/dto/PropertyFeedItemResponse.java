package com.sep.realvista.application.property.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sep.realvista.application.listing.dto.AmenityDTO;
import com.sep.realvista.application.listing.dto.LocationInfoDTO;
import com.sep.realvista.application.listing.dto.MediaDTO;
import com.sep.realvista.application.listing.dto.PropertyAttributeDTO;
import com.sep.realvista.application.listing.dto.PropertyTypeInfoDTO;
import com.sep.realvista.domain.property.PropertyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for a property in the agent feed.
 *
 * <p>Represents a property that an agent can view and potentially submit a proposal for.
 * Includes a {@code has_active_proposal} flag indicating whether the authenticated agent
 * has already submitted a proposal for this property.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyFeedItemResponse {

    @JsonProperty("property_id")
    private UUID propertyId;

    @JsonProperty("owner_id")
    private UUID ownerId;

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("street_address")
    private String streetAddress;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @JsonProperty("land_size_m2")
    private BigDecimal landSizeM2;

    @JsonProperty("usable_size_m2")
    private BigDecimal usableSizeM2;

    @JsonProperty("width_m")
    private BigDecimal widthM;

    @JsonProperty("length_m")
    private BigDecimal lengthM;

    private PropertyStatus status;

    private String descriptions;

    @JsonProperty("property_type_info")
    private PropertyTypeInfoDTO propertyTypeInfo;

    @JsonProperty("location_info")
    private LocationInfoDTO locationInfo;

    private List<MediaDTO> media;

    private List<PropertyAttributeDTO> attributes;

    private List<AmenityDTO> amenities;
    
    @JsonProperty("price_range")
    private java.util.Map<String, Object> priceRange;

    /**
     * Whether the authenticated agent has already submitted a proposal for this property.
     * Agents should not submit duplicate proposals.
     */
    @JsonProperty("has_active_proposal")
    private boolean hasActiveProposal;
}
