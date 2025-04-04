package com.doan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class UpdateOrderRequest {
    @NotBlank(message = "User is required")
    String userId;
    @NotBlank(message = "Order is required")
    String orderId;
    String shippingAddressId;
}
