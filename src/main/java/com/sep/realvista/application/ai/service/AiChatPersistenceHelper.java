package com.sep.realvista.application.ai.service;

import com.sep.realvista.domain.aichat.AiMessage;
import com.sep.realvista.domain.aichat.AiMessageRepository;
import com.sep.realvista.domain.aichat.AiMessageRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Helper that persists the assistant response in its own
 * transaction so it can be called from a Reactor callback
 * (outside the original request transaction).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatPersistenceHelper {

    private final AiMessageRepository messageRepository;

    /**
     * Save the assistant's full response as an {@link AiMessage}.
     * Runs in a new transaction ({@code REQUIRES_NEW}) so it is
     * independent of the caller's transaction context.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAssistantMessage(UUID conversationId,
                                     String content,
                                     int sequence) {
        if (content == null || content.isBlank()) {
            log.warn("Empty AI response, conversation={}",
                    conversationId);
            return;
        }

        try {
            AiMessage msg = AiMessage.create(
                    conversationId, AiMessageRole.ASSISTANT,
                    content, sequence);
            messageRepository.save(msg);
            log.debug("Saved assistant message seq={} for conv={}",
                    sequence, conversationId);
        } catch (Exception ex) {
            log.error("Failed to save assistant message: {}",
                    ex.getMessage(), ex);
        }
    }
}
