package com.doan.backend.repositories;

import com.doan.backend.dto.response.CategoryStatistics.CategoryStatisticResponse;
import com.doan.backend.dto.response.CustomerStatistics.CustomerStatisticResponse;
import com.doan.backend.dto.response.ProductStatistics.ProductStatisticResponse;
import com.doan.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    Iterable<OrderItem> findByOrderId(String orderId);


    @Query("""
                SELECT new com.doan.backend.dto.response.ProductStatistics.ProductStatisticResponse(
                    p.id,
                    p.name,
                    o.updatedAt,
                    s.name,
                    oi.price,
                    oi.quantity,
                    COALESCE(pr.discountPercentage, 0) 
                )
                FROM OrderItem oi
                JOIN oi.order o
                JOIN oi.product p
                JOIN oi.size s
                LEFT JOIN oi.promotion pr
                WHERE (o.updatedAt BETWEEN :startDate AND :endDate)
                AND o.status=com.doan.backend.enums.OrderStatusEnum.COMPLETED
            """)
    List<ProductStatisticResponse> getProductRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
                SELECT new com.doan.backend.dto.response.CategoryStatistics.CategoryStatisticResponse(
                    c.id,
                    c.name,
                    p.name,
                    o.updatedAt,
                    s.name,
                    oi.price,
                    oi.quantity,
                    COALESCE(pr.discountPercentage, 0)
                )
                FROM OrderItem oi
                JOIN oi.order o
                JOIN oi.product p
                JOIN p.category c ON p.category.id = c.id
                JOIN oi.size s
                LEFT JOIN oi.promotion pr
                WHERE o.updatedAt BETWEEN :startDate AND :endDate
                AND o.status=com.doan.backend.enums.OrderStatusEnum.COMPLETED
            """)
    List<CategoryStatisticResponse> getCategoryRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
            SELECT new com.doan.backend.dto.response.CustomerStatistics.CustomerStatisticResponse(
                u.id,
                u.name,
                u.email,
                o.id,
                o.totalPriceAfterDiscount,
                o.updatedAt)
            FROM Invoice i
            INNER JOIN i.order o
            INNER JOIN o.user u
            INNER JOIN u.roles r
            WHERE o.status = com.doan.backend.enums.OrderStatusEnum.COMPLETED AND r = 'CUSTOMER'
                  AND (o.updatedAt BETWEEN :startDate AND :endDate)
            """)
    List<CustomerStatisticResponse> getCustomerRevenue(LocalDateTime startDate, LocalDateTime endDate);
}