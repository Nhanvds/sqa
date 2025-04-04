package com.doan.backend.mapper;

import com.doan.backend.dto.request.UserRequest;
import com.doan.backend.dto.response.UserResponse;
import com.doan.backend.dto.response.UserReviewResponse;
import com.doan.backend.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserRequest userRequest);

    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    UserReviewResponse toUserReviewResponse(User user);
}
