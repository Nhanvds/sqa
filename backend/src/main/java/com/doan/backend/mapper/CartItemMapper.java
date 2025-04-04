package com.doan.backend.mapper;

import com.doan.backend.dto.response.CartItemResponse;
import com.doan.backend.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, SizeMapper.class})
public interface CartItemMapper {
    @Mapping(source = "cartItem.product", target = "product")
    @Mapping(source = "cartItem.size", target = "size")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(source = "cartItem.product", target = "product")
    @Mapping(source = "cartItem.size", target = "size")
    List<CartItemResponse> tocartItemResponseList(List<CartItem> cartItems);
}
