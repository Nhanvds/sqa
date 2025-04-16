package com.doan.backend.test;  // Đổi package thành com.doan.backend.test

import com.doan.backend.dto.request.RegisterRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.User;
import com.doan.backend.enums.RoleEnum;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.UserMapper;
import com.doan.backend.repositories.UserRepository;
import com.doan.backend.services.AuthService;
import com.doan.backend.services.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegisterWithEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerWithEmail_ShouldRegisterUser_WhenEmailIsNotTaken() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "Test User", null);
        User newUser = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .name(request.getName())
                .roles(Set.of(RoleEnum.CUSTOMER))
                .status(StatusEnum.INACTIVE)
                .verificationToken(anyString())
                .build();
        User savedUser = User.builder()
                .email(request.getEmail())
                .password("encodedPassword")
                .name(request.getName())
                .roles(Set.of(RoleEnum.CUSTOMER))
                .status(StatusEnum.INACTIVE)
                .verificationToken(anyString())
                .build();
        UserResponse userResponse = new UserResponse();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(userResponse);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

        // Act
        ApiResponse<UserResponse> response = authService.registerWithEmail(request);

        // Assert
        assertNotNull(response);
        assertEquals(201, response.getCode());
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(userResponse, response.getResult());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toUserResponse(savedUser);
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    void registerWithEmail_ShouldThrowIllegalArgumentException_WhenEmailIsTaken() {
        // Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "Test User", null);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.registerWithEmail(request);
        });
        assertEquals("Email is already taken", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder, userRepository, userMapper, emailService);
    }
}