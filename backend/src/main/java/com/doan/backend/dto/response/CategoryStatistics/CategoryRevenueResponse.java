package com.doan.backend.dto.response.CategoryStatistics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
public class CategoryRevenueResponse {
    String categoryName;
    List<CategoryStatistic> statistics;
}
