package com.doan.backend.dto.request;

import com.doan.backend.enums.PaymentMethodEnum;
import com.doan.backend.enums.PaymentStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    PaymentMethodEnum paymentMethod;

    BigDecimal amount;

    @NotNull(message = "paymentStatus is required")
    PaymentStatusEnum paymentStatus;
}
