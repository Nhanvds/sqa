package com.doan.backend.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class AddressRequest {
    String addressDetail;

    String city;

    String district;

    String ward;

    BigDecimal value;
}
