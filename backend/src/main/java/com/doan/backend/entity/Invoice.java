package com.doan.backend.entity;

import com.doan.backend.enums.InvoiceStatusEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "invoices",
        indexes = {
                @Index(name = "idx_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_id", columnList = "payment_id"),
                @Index(name = "idx_invoice_number", columnList = "invoice_number")
        }
)
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @OneToOne(fetch = FetchType.LAZY)
    Payment payment;

    @Column(name = "total_amount", nullable = false)
    BigDecimal totalAmount;

    @Column(name = "invoice_number", unique = true, nullable = false)
    String invoiceNumber;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    InvoiceStatusEnum status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
