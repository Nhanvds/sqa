package com.doan.backend.mapper;

import com.doan.backend.dto.response.CartResponse;
import com.doan.backend.entity.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartResponse toCartResponse(Cart Cart);
}
