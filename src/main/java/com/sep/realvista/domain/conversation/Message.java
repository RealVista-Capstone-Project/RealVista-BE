package com.sep.realvista.domain.conversation;

import com.sep.realvista.domain.common.entity.BaseEntity;
import com.sep.realvista.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_message_sender", columnList = "sender_id"),
        @Index(name = "idx_message_type", columnList = "message_type"),
        @Index(name = "idx_message_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id")
    private UUID messageId;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

    @Column(name = "reply_to_message_id")
    private UUID replyToMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id", insertable = false, updatable = false)
    private Message replyToMessage;

    @Column(name = "sender_id")
    private UUID senderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "json")
    private String metadata;

    public boolean isText() {
        return messageType == MessageType.TEXT;
    }

    public boolean isListingCard() {
        return messageType == MessageType.LISTING_CARD;
    }

    public boolean isContractCard() {
        return messageType == MessageType.CONTRACT_CARD;
    }

    public boolean isSystemMessage() {
        return messageType == MessageType.SYSTEM;
    }

    public boolean isReply() {
        return replyToMessageId != null;
    }

    public static Message createTextMessage(UUID conversationId, UUID senderId, String content) {
        return Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .messageType(MessageType.TEXT)
                .content(content)
                .build();
    }

    public static Message createSystemMessage(UUID conversationId, String content) {
        return Message.builder()
                .conversationId(conversationId)
                .senderId(null)
                .messageType(MessageType.SYSTEM)
                .content(content)
                .build();
    }

    public static Message createListingCardMessage(UUID conversationId, UUID senderId, String metadata) {
        return Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .messageType(MessageType.LISTING_CARD)
                .metadata(metadata)
                .build();
    }
}
