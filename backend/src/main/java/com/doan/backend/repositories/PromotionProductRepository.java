package com.doan.backend.repositories;

import com.doan.backend.entity.Promotion;
import com.doan.backend.entity.PromotionProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionProductRepository extends JpaRepository<PromotionProduct, String> {
    @Query("SELECT pp.promotion FROM PromotionProduct pp " +
            "WHERE pp.product.id = :productId " +
            "AND pp.promotion.isActive = true " +
            "AND pp.promotion.startDate <= :currentDate " +
            "AND pp.promotion.endDate >= :currentDate " +
            "ORDER BY pp.promotion.startDate DESC LIMIT 1")
    Optional<Promotion> findActivePromotionByProductId(
            @Param("productId") String productId,
            @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p " +
            "LEFT JOIN PromotionProduct pp ON pp.promotion.id = p.id " +
            "WHERE (pp.product.id = :productId AND p.isActive = true " +
            "       AND p.startDate <= :currentDate AND p.endDate >= :currentDate) " +
            "   OR (p.applyToAll = true AND p.isActive = true " +
            "       AND p.startDate <= :currentDate AND p.endDate >= :currentDate) " +
            "ORDER BY CASE WHEN pp.product.id IS NOT NULL THEN 0 ELSE 1 END, p.applyToAll ASC")
    List<Promotion> findPromotionApplyByProductId(
            @Param("productId") String productId,
            @Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT pp FROM PromotionProduct pp " +
            "WHERE pp.product.id = :productId " +
            "   OR pp.promotion.applyToAll = true")
    List<PromotionProduct> findPromotionProductsByProductId(
            @Param("productId") String productId);

    @Query("SELECT p FROM Promotion p " +
            "LEFT JOIN PromotionProduct pp ON pp.promotion.id = p.id " +
            "WHERE (pp.product.id = :productId AND p.isActive = true) " +
            "OR (p.applyToAll = true AND p.isActive = true)")
    List<Promotion> findAllPromotionByProductId(@Param("productId") String productId);
}