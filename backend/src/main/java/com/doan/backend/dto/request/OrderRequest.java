package com.doan.backend.dto.request;

import com.doan.backend.enums.OrderStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class OrderRequest {

    @NotBlank(message = "userId is required")
    String userId;

    String discountId;

    @NotBlank(message = "shippingAddressId is required")
    String shippingAddressId;

    OrderStatusEnum status;
}
