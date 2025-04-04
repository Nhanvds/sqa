package com.doan.backend.dto.response;

import com.doan.backend.enums.ChatRoomStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatRoomResponse {
    String id;
    String customerId;
    ChatRoomStatus status;
    List<MessageResponse> messages;
    List<UserResponse> userInChatRoom;
}
