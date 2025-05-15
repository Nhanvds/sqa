package com.doan.backend.dto.response;

import com.doan.backend.enums.InvoiceStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceResponse {
    String id;

    PaymentResponse payment;

    BigDecimal totalAmount;

    String invoiceNumber;

    InvoiceStatusEnum status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
