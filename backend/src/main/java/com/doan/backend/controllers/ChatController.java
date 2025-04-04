//package com.doan.backend.controllers;
//
//import com.doan.backend.dto.request.MessageRequest;
//import com.doan.backend.dto.response.ApiResponse;
//import com.doan.backend.dto.response.ChatRoomResponse;
//import com.doan.backend.services.ChatRoomService;
//import com.doan.backend.services.MessageService;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.web.bind.annotation.*;
//
//@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/chat")
//public class ChatController {
//    MessageService messageService;
//    ChatRoomService chatRoomService;
//
//    @PostMapping("/sendMessage")
//    public ApiResponse<Void> sendMessage(@RequestBody MessageRequest messageRequest) {
//        return messageService.sendMessage(messageRequest);
//    }
//
//    @GetMapping("/getChatRoom/{customerId}")
//    public ApiResponse<ChatRoomResponse> getChatRoom(@PathVariable String customerId) {
//        return chatRoomService.getChatRoom(customerId);
//    }
//
//    @GetMapping("/joinChatRoom/{chatRoomId}")
//    public ApiResponse<String> joinChat(@PathVariable String chatRoomId) {
//        return chatRoomService.userJoinChatRoom(chatRoomId);
//    }
//
//    @GetMapping("/leaveChatRoom/{chatRoomId}")
//    public ApiResponse<String> leaveChat(@PathVariable String chatRoomId) {
//        return chatRoomService.userLeaveChatRoom(chatRoomId);
//    }
//}
