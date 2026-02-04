package com.sep.realvista.application.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Sender information DTO for message responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderInfo {

    private UUID userId;
    private String name;
    private String avatarUrl;
}
