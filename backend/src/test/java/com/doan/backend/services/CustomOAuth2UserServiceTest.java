package com.doan.backend.services;

import com.doan.backend.entity.User;
import com.doan.backend.enums.RoleEnum;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.repositories.UserRepository;
import com.doan.backend.services.oauth2.CustomOAuth2UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Mock
    private ClientRegistration clientRegistration;

    @Mock
    private ClientRegistration.ProviderDetails providerDetails;

    @Test
    void loadUser_ShouldReturnOAuth2User_WhenUserExists() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        attributes.put("name", "Test User");
        attributes.put("sub", "google-id");

        User existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setName("Test User");
        existingUser.setRoles(Set.of(RoleEnum.CUSTOMER));

        // Mock ClientRegistration and ProviderDetails
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(mock(ClientRegistration.ProviderDetails.UserInfoEndpoint.class));

        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof DefaultOAuth2User);
        assertEquals("test@example.com", result.getAttribute("email"));
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        verify(defaultOAuth2UserService, times(1)).loadUser(userRequest);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_ShouldCreateAndReturnOAuth2User_WhenUserDoesNotExist() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "newuser@example.com");
        attributes.put("name", "New User");
        attributes.put("sub", "google-id");

        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setName("New User");
        newUser.setStatus(StatusEnum.ACTIVE);
        newUser.setRoles(Set.of(RoleEnum.CUSTOMER));
        newUser.setGoogleId("google-id");

        // Mock ClientRegistration and ProviderDetails
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getProviderDetails()).thenReturn(providerDetails);
        when(providerDetails.getUserInfoEndpoint()).thenReturn(mock(ClientRegistration.ProviderDetails.UserInfoEndpoint.class));

        when(defaultOAuth2UserService.loadUser(userRequest)).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof DefaultOAuth2User);
        assertEquals("newuser@example.com", result.getAttribute("email"));
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        verify(defaultOAuth2UserService, times(1)).loadUser(userRequest);
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }
}