package com.doan.backend.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
public class OrderItemResponse {
    String id;

    ProductResponse productResponse;

    SizeResponse size;

    Integer quantity;

    PromotionResponse promotion;

    BigDecimal price;
}
