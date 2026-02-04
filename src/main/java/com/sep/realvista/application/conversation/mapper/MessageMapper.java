package com.sep.realvista.application.conversation.mapper;

import com.sep.realvista.application.conversation.dto.MessageResponse;
import com.sep.realvista.application.conversation.dto.SenderInfo;
import com.sep.realvista.domain.conversation.Message;
import com.sep.realvista.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Message entity to DTO conversion.
 */
@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "message.messageId", target = "messageId")
    @Mapping(source = "message.conversationId", target = "conversationId")
    @Mapping(source = "message.replyToMessageId", target = "replyToMessageId")
    @Mapping(source = "message.messageType", target = "messageType")
    @Mapping(source = "message.content", target = "content")
    @Mapping(source = "message.metadata", target = "metadata")
    @Mapping(source = "message.createdAt", target = "createdAt")
    @Mapping(source = "sender", target = "sender")
    MessageResponse toResponse(Message message, SenderInfo sender);

    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "fullName", target = "name")
    @Mapping(source = "avatarUrl", target = "avatarUrl")
    SenderInfo toSenderInfo(User user);
}
