package com.sep.realvista.infrastructure.persistence.conversation;

import com.sep.realvista.domain.conversation.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ConversationJpaRepository extends JpaRepository<Conversation, UUID> {
}
