package com.doan.backend.mapper;

import com.doan.backend.dto.request.MessageRequest;
import com.doan.backend.dto.response.MessageResponse;
import com.doan.backend.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MessageMapper {

    @Mapping(source = "chatRoomId", target = "chatRoom.id")
    Message toMessage(MessageRequest messageRequest);


    @Mapping(target = "sender", source = "sender")
    MessageResponse toMessageResponse(Message message);

}
