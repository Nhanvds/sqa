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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceGetUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Test
    void getUser_ShouldReturnUserResponse_WhenUserIsAuthenticated() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .build();
        UserResponse userResponse = new UserResponse();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ApiResponse<UserResponse> response = authService.getUser();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(userResponse, response.getResult());
        verify(userRepository, times(1)).findByEmail(email);
        verify(userMapper, times(1)).toUserResponse(user);
    }
}