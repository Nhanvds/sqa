package com.doan.backend.dto.response;

import com.doan.backend.enums.OrderStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    String id;

    UserResponse user;

    UserDiscountResponse userDiscount;

    ShippingAddressResponse shippingAddress;

    OrderStatusEnum status;

    BigDecimal totalPriceBeforeDiscount;

    BigDecimal totalPriceAfterDiscount;

    List<OrderItemResponse> orderItems;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
