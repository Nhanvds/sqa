package com.doan.backend.repositories;

import com.doan.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") String productId);

    @Query("SELECT r FROM Review r WHERE r.product.id = :productId")
    Iterable<Review> findByProductId(@Param("productId") String productId);

    Iterable<Review> findByOrderId(String orderId);
}
