package com.doan.backend.dto.response.CustomerStatistics;

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
public class CustomerStatisticResponse {
    String userId;
    String name;
    String email;
    String orderId;
    BigDecimal value;
    LocalDateTime date;
}
