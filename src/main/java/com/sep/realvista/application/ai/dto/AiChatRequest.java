package com.sep.realvista.application.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for AI chat conversations.
 * The backend manages threadId internally — callers only send a message.
 * Fields use camelCase; Jackson global SNAKE_CASE handles JSON conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI chat request payload")
public class AiChatRequest {

    @Schema(
            description = "The user's chat message",
            example = "Is this property worth buying?",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    @Schema(
            description = "Optional listing UUID when chatting from a listing detail page",
            example = "550e8400-e29b-41d4-a716-446655440000",
            nullable = true
    )
    private UUID listingId;
}
