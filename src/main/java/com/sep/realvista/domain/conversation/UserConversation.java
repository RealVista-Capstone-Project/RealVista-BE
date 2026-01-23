package com.sep.realvista.domain.conversation;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_conversations", 
        indexes = {
                @Index(name = "idx_user_conversation_conv", columnList = "conversation_id"),
                @Index(name = "idx_user_conversation_user", columnList = "user_id"),
                @Index(name = "idx_user_conversation_archived", columnList = "is_archived"),
                @Index(name = "idx_user_conversation_deleted", columnList = "deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"conversation_id", "user_id"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserConversation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_conversation_id")
    private UUID userConversationId;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_read_message_id", insertable = false, updatable = false)
    private Message lastReadMessage;

    @Column(name = "unread_count")
    @Builder.Default
    private Integer unreadCount = 0;

    @Column(name = "is_archived")
    @Builder.Default
    private Boolean isArchived = false;

    @Column(name = "is_muted")
    @Builder.Default
    private Boolean isMuted = false;

    public void markAsRead(UUID messageId) {
        this.lastReadMessageId = messageId;
        this.unreadCount = 0;
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void archive() {
        this.isArchived = true;
    }

    public void unarchive() {
        this.isArchived = false;
    }

    public void mute() {
        this.isMuted = true;
    }

    public void unmute() {
        this.isMuted = false;
    }

    public boolean isMuted() {
        return Boolean.TRUE.equals(isMuted);
    }

    public boolean isArchived() {
        return Boolean.TRUE.equals(isArchived);
    }

    public static UserConversation create(UUID conversationId, UUID userId) {
        return UserConversation.builder()
                .conversationId(conversationId)
                .userId(userId)
                .build();
    }
}
