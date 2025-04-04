package com.doan.backend.repositories;

import com.doan.backend.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, String> {
    Boolean existsByCode(String code);

    Optional<Discount> findByCode(String code);

    Iterable<Discount> findByAutoApplyTrueAndExpiryDateAfter(LocalDateTime now);

    Optional<Discount> findByCodeAndExpiryDateAfter(String code, LocalDateTime now);

    @Query("SELECT d FROM Discount d WHERE (:code IS NULL OR d.code LIKE %:code%)")
    Page<Discount> findByCodeContaining(@Param("code") String code, Pageable pageable);

    @Query("SELECT d FROM Discount d WHERE d.startDate <= :currentDate AND d.expiryDate >= :currentDate" +
            " AND d.maxUses > d.usedCount")
    Iterable<Discount> getCurrentDiscounts(@Param("currentDate") LocalDateTime currentDate);
}
