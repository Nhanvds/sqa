package com.doan.backend.dto.response;

import com.doan.backend.enums.InvoiceStatusEnum;
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
public class InvoiceResponse {
    String id;

    PaymentResponse payment;

    BigDecimal totalAmount;

    String invoiceNumber;

    InvoiceStatusEnum status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
