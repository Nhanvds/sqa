package com.doan.backend.repositories;

import com.doan.backend.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    Iterable<ProductImage> findAllByProductId(String productId);
}
