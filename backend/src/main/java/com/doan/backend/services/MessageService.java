//package com.doan.backend.services;
//
//import com.doan.backend.dto.request.BotRequest;
//import com.doan.backend.dto.request.MessageRequest;
//import com.doan.backend.dto.response.ApiResponse;
//import com.doan.backend.dto.response.BotResponse;
//import com.doan.backend.dto.response.MessageResponse;
//import com.doan.backend.entity.ChatRoom;
//import com.doan.backend.entity.Message;
//import com.doan.backend.entity.User;
//import com.doan.backend.enums.RoleEnum;
//import com.doan.backend.mapper.MessageMapper;
//import com.doan.backend.repositories.ChatRoomUserRepository;
//import com.doan.backend.repositories.MessageRepository;
//import com.doan.backend.repositories.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//import java.util.Set;
//
//@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
//@RequiredArgsConstructor
//@Service
//public class MessageService {
//    MessageRepository messageRepository;
//    MessageMapper messageMapper;
//    AuthService authService;
//    SimpMessagingTemplate messagingTemplate;
//    RestTemplate restTemplate;
//    UserRepository userRepository;
//    ChatRoomUserRepository chatRoomUserRepository;
//
//    @Value("${chatbot.url}")
//    String BOT_API_URL = null;
//
//    public ApiResponse<Void> sendMessage(MessageRequest messageRequest) {
//        User user = authService.getUserByToken();
//
//        Message message = messageMapper.toMessage(messageRequest);
//        message.setSender(user);
//
//        MessageResponse messageResponse = messageMapper.toMessageResponse(messageRepository.save(message));
//        messagingTemplate.convertAndSend("/topic/chatRoom/" + message.getChatRoom().getId(), messageResponse);
//
//        if (user.getRoles().contains(RoleEnum.CUSTOMER)) {
//            Set<RoleEnum> rolesToCheck = Set.of(RoleEnum.ADMIN, RoleEnum.STAFF);
//            boolean hasStaffOrAdmin = chatRoomUserRepository.existsByChatRoomIdAndUser_RolesIn(messageRequest.getChatRoomId(), rolesToCheck);
//
//            if (!hasStaffOrAdmin) {
//                handleBotResponse(message.getContent(), message.getChatRoom());
//            }
//        }
//
//        return ApiResponse.<Void>builder().build();
//    }
//
//    public List<MessageResponse> getMessages(String chatRoomId) {
//        return messageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId).stream().map(messageMapper::toMessageResponse).toList();
//    }
//
//    private void handleBotResponse(String userMessage, ChatRoom chatRoom) {
//        BotRequest botRequest = new BotRequest(userMessage);
//        User chatBot = authService.getChatBotUser();
//        try {
//            BotResponse botResponse = restTemplate.postForObject(BOT_API_URL, botRequest, BotResponse.class);
//
//            if (botResponse != null && botResponse.getResponse() != null) {
//
//                Message botMessage = new Message();
//                botMessage.setChatRoom(chatRoom);
//                botMessage.setContent(botResponse.getResponse());
//                botMessage.setSender(chatBot);
//
//                Message savedBotMessage = messageRepository.save(botMessage);
//
//                MessageResponse botMessageResponse = messageMapper.toMessageResponse(savedBotMessage);
//
//                messagingTemplate.convertAndSend("/topic/chatRoom/" + chatRoom.getId(), botMessageResponse);
//            }
//        } catch (Exception e) {
//            System.err.println("Error " + e.getMessage());
//            Message errorMessage = new Message();
//            errorMessage.setChatRoom(chatRoom);
//            errorMessage.setContent("Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này.");
//            errorMessage.setSender(chatBot);
//
//            Message savedErrorMessage = messageRepository.save(errorMessage);
//            MessageResponse errorMessageResponse = messageMapper.toMessageResponse(savedErrorMessage);
//            messagingTemplate.convertAndSend("/topic/chatRoom/" + chatRoom.getId(), errorMessageResponse);
//        }
//    }
//}
