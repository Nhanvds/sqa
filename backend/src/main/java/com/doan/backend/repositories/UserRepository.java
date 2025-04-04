package com.doan.backend.repositories;

import com.doan.backend.entity.User;
import com.doan.backend.enums.RoleEnum;
import com.doan.backend.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    Boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    List<User> findByRoles(RoleEnum role);

    Page<User> findByNameContainingIgnoreCaseAndStatusNot(String name, StatusEnum status, Pageable pageable);

    List<User> findByStatusNot(StatusEnum status);
}