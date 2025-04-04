package com.doan.backend.dto.response.ProductStatistics;

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
public class ProductStatistic {
    LocalDateTime date;
    String size;
    BigDecimal price;
    Integer quantity;
    BigDecimal discountPercentage;
}
