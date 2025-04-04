package com.doan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class ReviewRequest {
    @NotBlank(message = "ProductId is required")
    String productId;

    @NotBlank(message = "OrderId is required")
    String orderId;
    @NotNull(message = "Rating is not null")
    Double rating;
    String content;
}
