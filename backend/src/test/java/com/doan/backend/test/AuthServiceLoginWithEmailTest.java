package com.doan.backend.test;

import com.doan.backend.dto.request.LoginEmailRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.JwtResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.User;
import com.doan.backend.enums.RoleEnum;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.UserMapper;
import com.doan.backend.repositories.UserRepository;
import com.doan.backend.config.JwtTokenProvider;
import com.doan.backend.services.EmailService;
import com.doan.backend.services.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginWithEmailTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginWithEmail_ShouldReturnJwtResponse_WhenCredentialsAreValid() {
        // Arrange
        LoginEmailRequest request = new LoginEmailRequest("test@example.com", "password");
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .roles(Set.of(RoleEnum.CUSTOMER))
                .status(StatusEnum.ACTIVE)
                .build();
        UserResponse userResponse = new UserResponse();
        JwtResponse jwtResponse = new JwtResponse("token", userResponse);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenProvider.generateToken(user.getEmail(), Map.of("roles", user.getRoles()))).thenReturn("token");
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        ApiResponse<JwtResponse> response = authService.loginWithEmail(request);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Login successful", response.getMessage());
        assertEquals(jwtResponse, response.getResult());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
        verify(jwtTokenProvider, times(1)).generateToken(user.getEmail(), Map.of("roles", user.getRoles()));
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void loginWithEmail_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        LoginEmailRequest request = new LoginEmailRequest("test@example.com", "password");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loginWithEmail(request);
        });
        assertEquals("User not found with email: " + request.getEmail(), exception.getMessage());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder, jwtTokenProvider, userMapper);
    }

    @Test
    void loginWithEmail_ShouldThrowBadCredentialsException_WhenPasswordIsInvalid() {
        // Arrange
        LoginEmailRequest request = new LoginEmailRequest("test@example.com", "wrongPassword");
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .status(StatusEnum.ACTIVE)
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.loginWithEmail(request);
        });
        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
        verifyNoInteractions(jwtTokenProvider, userMapper);
    }

    @Test
    void loginWithEmail_ShouldThrowBadCredentialsException_WhenAccountIsInactive() {
        // Arrange
        LoginEmailRequest request = new LoginEmailRequest("test@example.com", "password");
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .status(StatusEnum.INACTIVE)
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.loginWithEmail(request);
        });
        assertEquals("Account is not activated", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
        verifyNoInteractions(jwtTokenProvider, userMapper);
    }

    @Test
    void loginWithEmail_ShouldThrowBadCredentialsException_WhenAccountIsDeleted() {
        // Arrange
        LoginEmailRequest request = new LoginEmailRequest("test@example.com", "password");
        User user = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .status(StatusEnum.DELETED)
                .build();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.loginWithEmail(request);
        });
        assertEquals("Account is deleted", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
        verifyNoInteractions(jwtTokenProvider, userMapper);
    }
}