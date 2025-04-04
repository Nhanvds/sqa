package com.doan.backend.mapper;

import com.doan.backend.dto.response.UserDiscountResponse;
import com.doan.backend.entity.UserDiscount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDiscountMapper {
    UserDiscountResponse toUserDiscountResponse(UserDiscount userDiscount);
}
