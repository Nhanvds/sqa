package com.doan.backend.repositories;

import com.doan.backend.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductInventoryRepository extends JpaRepository<ProductInventory, String> {
    Iterable<ProductInventory> findByProductId(String productId);

    Iterable<ProductInventory> findByProductIdIn(List<String> productIds);

    Boolean existsByProductIdAndSizeId(String productId, String sizeId);

    Optional<ProductInventory> findByProductIdAndSizeId(String productId, String sizeId);
}
