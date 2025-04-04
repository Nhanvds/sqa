package com.doan.backend.dto.response;

import com.doan.backend.enums.PaymentMethodEnum;
import com.doan.backend.enums.PaymentStatusEnum;
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
public class PaymentResponse {
    String id;

    PaymentMethodEnum paymentMethod;

    String code;

    String qrCodeUrl;

    BigDecimal amount;

    PaymentStatusEnum paymentStatus;

    LocalDateTime paymentDate;
}
