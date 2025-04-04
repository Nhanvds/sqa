package com.doan.backend.services;

import com.doan.backend.dto.request.UserRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.User;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.UserMapper;
import com.doan.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    public ApiResponse<Page<UserResponse>> getAllUser(String name, Pageable pageable) {

        Page<UserResponse> userResponses = userRepository.findByNameContainingIgnoreCaseAndStatusNot(name, StatusEnum.DELETED, pageable).map(userMapper::toUserResponse);
        return ApiResponse.<Page<UserResponse>>builder()
                .code(200)
                .message("Get all user successfully")
                .result(userResponses)
                .build();
    }

    public ApiResponse<UserResponse> getUserById(String id) {
        UserResponse userResponse = userMapper.toUserResponse(userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found")));
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Get user successfully")
                .result(userResponse)
                .build();
    }

    public ApiResponse<Void> deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(StatusEnum.DELETED);
        userRepository.save(user);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Delete user successfully")
                .build();
    }

    public ApiResponse<UserResponse> updateUser(String id, UserRequest userRequest) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setRoles(userRequest.getRoles());
        user.setLoyaltyTier(userRequest.getLoyaltyTier());
        userRepository.save(user);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Update user successfully")
                .result(userMapper.toUserResponse(user))
                .build();
    }

    public ApiResponse<UserResponse> createUser(UserRequest userRequest) {
        User user = userMapper.toUser(userRequest);
        userRepository.save(user);
        return ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Create user successfully")
                .result(userMapper.toUserResponse(user))
                .build();
    }

    public ApiResponse<List<UserResponse>> getAll() {
        List<User> users = userRepository.findByStatusNot(StatusEnum.DELETED);

        return ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .message("Get user successfully")
                .result(userMapper.toUserResponseList(users))
                .build();
    }
}
