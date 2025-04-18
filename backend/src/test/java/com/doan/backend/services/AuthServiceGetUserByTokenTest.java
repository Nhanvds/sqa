package com.doan.backend.services;

import com.doan.backend.entity.User;
import com.doan.backend.exception.Unauthorized;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceGetUserByTokenTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Test
    void getUserByToken_ShouldReturnUser_WhenUserIsAuthenticated() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .email(email)
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        SecurityContextHolder.setContext(securityContext);

        // Act
        User result = authService.getUserByToken();

        // Assert
        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getUserByToken_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        String email = "test@example.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.getUserByToken();
        });
        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void getUserByToken_ShouldThrowUnauthorized_WhenNotAuthenticated() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        Unauthorized exception = assertThrows(Unauthorized.class, () -> {
            authService.getUserByToken();
        });
        assertEquals("Unauthorized access", exception.getMessage());
        verifyNoInteractions(userRepository);
    }
}