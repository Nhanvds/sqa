package com.doan.backend.repositories;

import com.doan.backend.entity.Category;
import com.doan.backend.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Boolean existsByNameAndStatusNot(String name, StatusEnum status);

    Optional<Category> findByNameAndStatusNot(String name, StatusEnum status);

    List<Category> findByStatusNot(StatusEnum status);

    Page<Category> findByNameContainingIgnoreCaseAndStatusNot(String name, StatusEnum status, Pageable pageable);
}
