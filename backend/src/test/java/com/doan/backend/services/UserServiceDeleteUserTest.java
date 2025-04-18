package com.doan.backend.services;

import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.entity.User;
import com.doan.backend.enums.StatusEnum;
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
class UserServiceDeleteUserTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        String id = "1";
        User user = new User();
        user.setStatus(StatusEnum.ACTIVE);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        ApiResponse<Void> response = userService.deleteUser(id);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Delete user successfully", response.getMessage());
        assertEquals(StatusEnum.DELETED, user.getStatus());
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        String id = "1";
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(id);
        });
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(id);
        verifyNoMoreInteractions(userRepository);
    }
}