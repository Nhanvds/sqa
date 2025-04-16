package com.doan.backend.test;

import com.doan.backend.dto.request.UserRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.User;
import com.doan.backend.mapper.UserMapper;
import com.doan.backend.repositories.UserRepository;
import com.doan.backend.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUpdateUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUser_ShouldUpdateUser_WhenUserExists() {
        // Arrange
        String id = "1";
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Updated Name");
        userRequest.setEmail("updated@example.com");
        User user = new User();
        UserResponse userResponse = new UserResponse();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        ApiResponse<UserResponse> response = userService.updateUser(id, userRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Update user successfully", response.getMessage());
        assertEquals(userResponse, response.getResult());
        assertEquals("Updated Name", user.getName());
        assertEquals("updated@example.com", user.getEmail());
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String id = "1";
        UserRequest userRequest = new UserRequest();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(id, userRequest);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(id);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userMapper);
    }
}