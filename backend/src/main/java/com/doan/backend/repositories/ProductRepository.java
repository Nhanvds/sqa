package com.doan.backend.repositories;

import com.doan.backend.entity.Product;
import com.doan.backend.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByIdAndStatusNot(String id, StatusEnum status);

    Page<Product> findByNameContainingIgnoreCaseAndStatusNot(String name, StatusEnum status, Pageable pageable);

    Page<Product> findByCategory_IdAndStatusNot(String categoryId, StatusEnum status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndCategory_IdAndStatusNot(String name, String categoryId, StatusEnum status, Pageable pageable);
}
