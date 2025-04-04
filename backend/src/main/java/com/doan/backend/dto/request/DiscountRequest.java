package com.doan.backend.dto.request;

import com.doan.backend.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class DiscountRequest {

    @NotBlank(message = "Code is required")
    String code;

    @NotNull(message = "Discount type is required")
    DiscountType discountType;

    BigDecimal discountPercentage;

    BigDecimal discountValue;

    BigDecimal maxDiscountValue;

    @NotNull(message = "Min Order Value is required")
    BigDecimal minOrderValue;

    @NotNull(message = "Max uses is required")
    Integer maxUses;

    @NotNull(message = "Auto apply is required")
    Boolean autoApply;

    @NotNull(message = "Expiry date is required")
    LocalDateTime expiryDate;

    @NotNull(message = "Start date is required")
    LocalDateTime startDate;
}
