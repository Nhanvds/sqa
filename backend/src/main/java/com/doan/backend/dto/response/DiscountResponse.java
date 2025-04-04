package com.doan.backend.dto.response;

import com.doan.backend.enums.DiscountType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
public class DiscountResponse {
    String id;

    String code;

    DiscountType discountType;

    BigDecimal discountPercentage;

    BigDecimal discountValue;

    BigDecimal maxDiscountValue;

    BigDecimal minOrderValue;

    Integer maxUses;

    Integer usedCount = 0;

    LocalDateTime expiryDate;

    LocalDateTime startDate;

    Boolean autoApply;

    LocalDateTime createdAt;
}
