package com.doan.backend.mapper;

import com.doan.backend.dto.response.OrderResponse;
import com.doan.backend.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, UserDiscountMapper.class, ShippingAddressMapper.class, UserMapper.class})
public interface OrderMapper {

    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "userDiscount", source = "userDiscount")
    @Mapping(target = "shippingAddress", source = "shippingAddress")
    @Mapping(target = "user", source = "user")
    OrderResponse toOrderResponse(Order order);

    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "userDiscount", source = "userDiscount")
    @Mapping(target = "shippingAddress", source = "shippingAddress")
    Iterable<OrderResponse> toOrderResponseIterable(Iterable<Order> orders);
}
