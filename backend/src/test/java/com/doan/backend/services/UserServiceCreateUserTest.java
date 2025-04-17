package com.doan.backend.services;

import com.doan.backend.dto.request.UserRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.User;
import com.doan.backend.mapper.UserMapper;
import com.doan.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCreateUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_ShouldCreateUser_WhenRequestIsValid() {
        // Arrange
        UserRequest userRequest = new UserRequest();
        User user = new User();
        UserResponse userResponse = new UserResponse();

        when(userMapper.toUser(userRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        ApiResponse<UserResponse> response = userService.createUser(userRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Create user successfully", response.getMessage());
        assertEquals(userResponse, response.getResult());
        verify(userMapper, times(1)).toUser(userRequest);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserResponse(user);
    }
}