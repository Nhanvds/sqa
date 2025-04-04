package com.doan.backend.repositories;

import com.doan.backend.entity.Order;
import com.doan.backend.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Iterable<Order> findByUserId(String userId);

    Iterable<Order> findByStatus(OrderStatusEnum status);

    Iterable<Order> findByUserIdAndStatus(String userId, OrderStatusEnum status);

    Boolean existsByUserIdAndStatus(String userId, OrderStatusEnum status);

    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN o.orderItems oi " +
            "JOIN o.user u " +
            "WHERE (:productName IS NULL OR oi.product.name LIKE %:productName%) " +
            "AND (:customerEmail IS NULL OR u.email LIKE %:customerEmail%) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Order> findOrdersForAdmin(
            @Param("productName") String productName,
            @Param("customerEmail") String customerEmail,
            @Param("status") OrderStatusEnum status,
            Pageable pageable
    );

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o JOIN o.orderItems oi " +
            "WHERE o.user.id = :userId " +
            "AND o.status = 5 " +
            "AND oi.product.id = :productId")
    boolean existOrderCompletedByUserId(@Param("userId") String userId,
                                        @Param("productId") String productId);


}
