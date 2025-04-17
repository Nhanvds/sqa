package com.doan.backend.services;

import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.entity.User;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.UserMapper;
import com.doan.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceGetAllUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUser_ShouldReturnPagedUsers_WhenUsersExist() {
        // Arrange
        String name = "test";
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        UserResponse userResponse = new UserResponse();
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));
        Page<UserResponse> userResponsePage = new PageImpl<>(Collections.singletonList(userResponse));

        when(userRepository.findByNameContainingIgnoreCaseAndStatusNot(name, StatusEnum.DELETED, pageable)).thenReturn(userPage);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // Act
        ApiResponse<Page<UserResponse>> response = userService.getAllUser(name, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Get all user successfully", response.getMessage());
        assertEquals(userResponsePage.getContent(), response.getResult().getContent());
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseAndStatusNot(name, StatusEnum.DELETED, pageable);
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void getAllUser_ShouldReturnEmptyPage_WhenNoUsersExist() {
        // Arrange
        String name = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());

        when(userRepository.findByNameContainingIgnoreCaseAndStatusNot(name, StatusEnum.DELETED, pageable)).thenReturn(emptyPage);

        // Act
        ApiResponse<Page<UserResponse>> response = userService.getAllUser(name, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Get all user successfully", response.getMessage());
        assertTrue(response.getResult().isEmpty());
        verify(userRepository, times(1)).findByNameContainingIgnoreCaseAndStatusNot(name, StatusEnum.DELETED, pageable);
        verifyNoInteractions(userMapper);
    }
}