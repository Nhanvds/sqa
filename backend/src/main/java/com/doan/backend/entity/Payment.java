package com.doan.backend.entity;

import com.doan.backend.enums.PaymentMethodEnum;
import com.doan.backend.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "code")
    String code;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    Invoice invoice;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    PaymentMethodEnum paymentMethod;

    @Column(name = "qr_code_url")
    String qrCodeUrl;

    @Column(name = "amount")
    BigDecimal amount;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    PaymentStatusEnum paymentStatus;

    @Column(name = "payment_date")
    LocalDateTime paymentDate;
}
