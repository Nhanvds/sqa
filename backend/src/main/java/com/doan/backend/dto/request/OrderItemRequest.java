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
public class OrderItemRequest {
    @NotBlank(message = "productInventoryRequest is required")
    ProductInventoryRequest productInventoryRequest;

    @Positive(message = "quantity must be greater than 0")
    Integer quantity;
}
