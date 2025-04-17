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
class AuthServiceVerifyAccountTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void verifyAccount_ShouldVerifyUser_WhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        User user = User.builder()
                .id("user-id")
                .email("test@example.com")
                .status(StatusEnum.INACTIVE)
                .verificationToken(token)
                .build();
        User savedUser = User.builder()
                .id("user-id")
                .email("test@example.com")
                .status(StatusEnum.ACTIVE)
                .verificationToken(null)
                .build();

        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        ApiResponse<String> response = authService.verifyAccount(token);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Account verified successfully", response.getMessage());
        assertEquals("user-id", response.getResult());
        verify(userRepository, times(1)).findByVerificationToken(token);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void verifyAccount_ShouldThrowIllegalArgumentException_WhenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";
        when(userRepository.findByVerificationToken(token)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyAccount(token);
        });
        assertEquals("Invalid verification token", exception.getMessage());
        verify(userRepository, times(1)).findByVerificationToken(token);
        verifyNoMoreInteractions(userRepository);
    }
}