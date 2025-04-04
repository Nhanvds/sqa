package com.doan.backend.dto.response.CategoryStatistics;

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
public class CategoryStatisticResponse {
    String id;
    String categoryName;
    String productName;
    LocalDateTime date;
    String size;
    BigDecimal price;
    Integer quantity;
    BigDecimal discountPercentage;
}
