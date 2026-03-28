-- ============================================================
-- AI CHAT CONVERSATION TABLES
-- Separate from the existing human-to-human conversations.
-- One AI conversation per user (UNIQUE on user_id).
-- Hard-delete: no 'deleted' column; CASCADE removes messages.
-- ============================================================

CREATE TABLE ai_conversations
(
    ai_conversation_id UUID      DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id            UUID      NOT NULL UNIQUE,
    thread_id          UUID      NOT NULL UNIQUE,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_conversations_user ON ai_conversations (user_id);

CREATE TABLE ai_messages
(
    ai_message_id      UUID         DEFAULT gen_random_uuid() PRIMARY KEY,
    ai_conversation_id UUID         NOT NULL,
    role               VARCHAR(10)  NOT NULL,
    content            TEXT         NOT NULL,
    sequence           INTEGER      NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),

    FOREIGN KEY (ai_conversation_id)
        REFERENCES ai_conversations (ai_conversation_id) ON DELETE CASCADE,
    CONSTRAINT chk_ai_message_role CHECK (role IN ('USER', 'ASSISTANT'))
);

CREATE INDEX idx_ai_messages_conversation ON ai_messages (ai_conversation_id);
CREATE INDEX idx_ai_messages_sequence ON ai_messages (ai_conversation_id, sequence);
