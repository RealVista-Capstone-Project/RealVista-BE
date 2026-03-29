package com.sep.realvista.application.engagement.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for cancelling an engagement.
 *
 * Reason is validated at the domain level based on engagement status:
 * - ACCEPTED engagements require a reason
 * - SUBMITTED engagements allow optional reason
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelEngagementRequest {

    @Size(max = 1000, message = "Cancellation reason must not exceed 1000 characters")
    private String reason;
}
