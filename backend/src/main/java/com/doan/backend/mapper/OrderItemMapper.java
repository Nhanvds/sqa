package com.doan.backend.mapper;

import com.doan.backend.dto.response.OrderItemResponse;
import com.doan.backend.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, SizeMapper.class, PromotionMapper.class})
public interface OrderItemMapper {

    @Mapping(source = "product", target = "productResponse")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "promotion", target = "promotion")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(source = "product", target = "productResponse")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "promotion", target = "promotion")
    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);
}
