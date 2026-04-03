package com.sep.realvista.domain.aichat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single message within an AI chat conversation.
 * Standalone entity — does not extend BaseEntity (no soft-delete).
 *
 * <p>The FK column is owned by the raw {@code conversationId} UUID field
 * so that messages can be persisted without a managed
 * {@link AiConversation} reference (important for async saves).</p>
 */
@Entity
@Table(name = "ai_messages")
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_message_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ai_conversation_id", nullable = false)
    private UUID conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_conversation_id",
            insertable = false, updatable = false)
    private AiConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private AiMessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AiMessage() {
        // JPA
    }

    private AiMessage(UUID conversationId,
                      AiMessageRole role,
                      String content,
                      int sequence) {
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.sequence = sequence;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory: create a message using the conversation UUID.
     */
    public static AiMessage create(UUID conversationId,
                                   AiMessageRole role,
                                   String content,
                                   int sequence) {
        return new AiMessage(conversationId, role, content, sequence);
    }

    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public AiConversation getConversation() {
        return conversation;
    }

    public AiMessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public int getSequence() {
        return sequence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
