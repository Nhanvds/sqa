package com.doan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class CartItemRequest {
    @NotBlank(message = "cartId is required")
    String cartId;

    @NotBlank(message = "productId is required")
    String productId;

    @NotBlank(message = "sizeId is required")
    String sizeId;

    @Positive(message = "quantity must be greater than 0")
    Integer quantity;
}
