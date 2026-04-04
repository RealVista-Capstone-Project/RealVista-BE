package com.sep.realvista.domain.aichat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single AI chat conversation per user.
 * Standalone entity — does not extend BaseEntity (no soft-delete).
 */
@Entity
@Table(name = "ai_conversations")
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_conversation_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "thread_id", nullable = false, unique = true)
    private UUID threadId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AiConversation() {
        // JPA
    }

    private AiConversation(UUID userId, UUID threadId) {
        this.userId = userId;
        this.threadId = threadId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Factory method to create a new AI conversation for a user.
     */
    public static AiConversation create(UUID userId) {
        return new AiConversation(userId, UUID.randomUUID());
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }
}
