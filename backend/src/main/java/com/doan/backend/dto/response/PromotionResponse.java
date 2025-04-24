package com.doan.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {
    String id;
    String name;
    String description;
    BigDecimal discountPercentage;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Boolean applyToAll;
    Boolean isActive;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
