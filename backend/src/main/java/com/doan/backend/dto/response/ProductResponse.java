package com.doan.backend.dto.response;

import com.doan.backend.enums.StatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
public class ProductResponse {
    String id;
    String name;
    String description;
    BigDecimal price;
    CategoryResponse categoryResponse;
    PromotionResponse promotionResponse;
    List<PromotionResponse> promotions;
    Double rating;
    StatusEnum status;
    BigDecimal discountPercentage;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String mainImage;
}
