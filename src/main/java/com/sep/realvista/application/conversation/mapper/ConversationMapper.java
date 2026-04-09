package com.sep.realvista.application.conversation.mapper;

import com.sep.realvista.application.conversation.dto.response.ConversationResponse;
import com.sep.realvista.domain.conversation.Conversation;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Conversation entity.
 */
@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(source = "conversation.conversationId", target = "conversationId")
    @Mapping(source = "conversation.createdAt", target = "createdAt")
    @Mapping(source = "otherUser.userId", target = "otherUserId")
    @Mapping(source = "otherUser.fullName", target = "otherUserName")
    @Mapping(source = "otherUser.avatarUrl", target = "otherUserAvatarUrl")
    @Mapping(target = "conversationCreated", ignore = true)
    ConversationResponse toResponse(Conversation conversation, User otherUser);
}
