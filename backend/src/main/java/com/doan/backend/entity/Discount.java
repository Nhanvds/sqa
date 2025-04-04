package com.doan.backend.entity;

import com.doan.backend.enums.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "discounts",
        uniqueConstraints = @UniqueConstraint(columnNames = "code"),
        indexes = @Index(name = "idx_code", columnList = "code")
)
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Size(min = 6, max = 20, message = "Code must be between 6 and 20 characters")
    @Column(name = "code", nullable = false, length = 20)
    String code;

    @Column(name = "discount_type", nullable = false)
    DiscountType discountType;

    @DecimalMin(value = "0.0", message = "Discount percentage must be greater than or equal to 0")
    @Column(name = "discount_percentage")
    BigDecimal discountPercentage;

    @DecimalMin(value = "0.0", message = "Discount value must be greater than or equal to 0")
    @Column(name = "discount_value")
    BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Max discount value must be greater than or equal to 0")
    @Column(name = "max_discount_value", nullable = false)
    BigDecimal maxDiscountValue;

    @Column(name = "min_order_value", nullable = false)
    BigDecimal minOrderValue;

    @Column(name = "max_uses", nullable = false)
    Integer maxUses;

    @Column(name = "used_count", nullable = false)
    Integer usedCount = 0;

    @Column(name = "expiry_date")
    LocalDateTime expiryDate;

    @Column(name = "start_date")
    LocalDateTime startDate;

    @Column(name = "auto-apply")
    Boolean autoApply;

    @Column(name = "created_at")
    @CreationTimestamp
    LocalDateTime createdAt;
}
