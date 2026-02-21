package com.sep.realvista.application.listing.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.sep.realvista.domain.engagement.rental.TenantApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TenantApplicationDto {
    private UUID tenantApplicationId;
    private UUID userId;
    private UUID rentalProfileId;
    private UUID listingId;
    private String title; // Listing Name
    private String propertyAddress;
    private String propertyImageUrl;
    private BigDecimal monthlyIncome;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate moveInDate;
    private Integer leaseTermMonths;
    private TenantApplicationStatus status;
    private String note;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
