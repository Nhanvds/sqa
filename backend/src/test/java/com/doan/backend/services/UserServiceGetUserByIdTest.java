package com.doan.backend.services;

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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceGetUserByIdTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        String id = "1";
        User user = new User();
        UserResponse userResponse = new UserResponse();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        ApiResponse<UserResponse> response = userService.getUserById(id);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Get user successfully", response.getMessage());
        assertEquals(userResponse, response.getResult());
        verify(userRepository, times(1)).findById(id);
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String id = "1";
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(id);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(id);
        verifyNoInteractions(userMapper);
    }
}