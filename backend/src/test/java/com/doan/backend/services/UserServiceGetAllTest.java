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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceGetAllTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getAll_ShouldReturnUsers_WhenUsersExist() {
        // Arrange
        User user = new User();
        List<User> users = Collections.singletonList(user);
        List<UserResponse> userResponses = Collections.singletonList(new UserResponse());

        when(userRepository.findByStatusNot(StatusEnum.DELETED)).thenReturn(users);
        when(userMapper.toUserResponseList(users)).thenReturn(userResponses);

        // Act
        ApiResponse<List<UserResponse>> response = userService.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Get user successfully", response.getMessage());
        assertEquals(userResponses, response.getResult());
        verify(userRepository, times(1)).findByStatusNot(StatusEnum.DELETED);
        verify(userMapper, times(1)).toUserResponseList(users);
    }

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoUsersExist() {
        // Arrange
        when(userRepository.findByStatusNot(StatusEnum.DELETED)).thenReturn(Collections.emptyList());
        when(userMapper.toUserResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<UserResponse>> response = userService.getAll();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Get user successfully", response.getMessage());
        assertTrue(response.getResult().isEmpty());
        verify(userRepository, times(1)).findByStatusNot(StatusEnum.DELETED);
        verify(userMapper, times(1)).toUserResponseList(Collections.emptyList());
    }
}