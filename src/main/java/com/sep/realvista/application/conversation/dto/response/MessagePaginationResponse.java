package com.sep.realvista.application.conversation.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.sep.realvista.application.conversation.dto.PaginationMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated messages.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePaginationResponse {

    private List<MessageResponse> messages;
    private PaginationMetadata pagination;
}
