package com.sep.realvista.application.media.dto;

import com.sep.realvista.domain.property.Property3DGenerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property3DGenerationDto {
    private UUID id;
    private UUID propertyId;
    private String operationId;
    private Property3DGenerationStatus status;
    private String errorMessage;
    private String createdAt;
}
