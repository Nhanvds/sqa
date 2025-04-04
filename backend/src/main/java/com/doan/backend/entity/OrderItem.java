package com.doan.backend.entity;

import com.doan.backend.dto.response.ProductStatistics.ProductRevenueResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@NamedStoredProcedureQuery(
        name = "GetRevenueByDay",
        procedureName = "GetRevenueByDay",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "startDate", type = Date.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "endDate", type = Date.class)
        },
        resultSetMappings = "ProductRevenueMapping"
)

@NamedStoredProcedureQuery(
        name = "GetRevenueByMonth",
        procedureName = "GetRevenueByMonth",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "month", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "year", type = Integer.class)
        },
        resultSetMappings = "ProductRevenueMapping"
)

@NamedStoredProcedureQuery(
        name = "GetRevenueByQuarter",
        procedureName = "GetRevenueByQuarter",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "quarter", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "year", type = Integer.class)
        },
        resultSetMappings = "ProductRevenueMapping"
)
@NamedStoredProcedureQuery(
        name = "GetRevenueByYear",
        procedureName = "GetRevenueByYear",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "year", type = Integer.class)
        },
        resultSetMappings = "ProductRevenueMapping"
)
@SqlResultSetMapping(
        name = "ProductRevenueMapping",
        classes = @ConstructorResult(
                targetClass = ProductRevenueResponse.class,
                columns = {
                        @ColumnResult(name = "product_name", type = String.class),
                        @ColumnResult(name = "total_quantity", type = Integer.class),
                        @ColumnResult(name = "total_revenue", type = BigDecimal.class)
                }
        )
)
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_product_id", columnList = "product_id"),
        @Index(name = "idx_promotion_id", columnList = "promotion_id")
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false)
    Size size;

    @Column(name = "quantity", nullable = false)
    Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    Promotion promotion;

    @Column(name = "price", nullable = false)
    BigDecimal price;
}
