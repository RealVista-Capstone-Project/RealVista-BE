package com.sep.realvista.application.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for paginated messages.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePaginationResponse {

    private List<MessageResponse> messages;
    private PaginationMetadata pagination;
}
