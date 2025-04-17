package com.doan.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    String id;

    List<CartItemResponse> cartItems;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
