package com.doan.backend.repositories;

import com.doan.backend.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {

    @Query("SELECT p FROM Promotion p WHERE p.applyToAll=false AND p.isActive = true AND p.startDate <= :currentDate AND p.endDate >= :currentDate")
    List<Promotion> findActiveCurrentPromotionsExcludeApplyToAll(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE p.applyToAll = true AND p.isActive = true " +
            "AND p.startDate <= :currentDate AND p.endDate >= :currentDate")
    List<Promotion> findApplyToAllPromotions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Promotion p WHERE (:name IS NULL OR p.name LIKE %:name%)")
    Page<Promotion> findByNameContaining(@Param("name") String name, Pageable pageable);
}
