package com.doan.backend.dto.response.CustomerStatistics;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRevenueResponse {
    private String name;
    private String email;
    private Integer totalOrder;
    private BigDecimal totalValue;
    private List<CustomerStatistic> statistics = new ArrayList<>();
}


