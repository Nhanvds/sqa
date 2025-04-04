package com.doan.backend.repositories;

import com.doan.backend.entity.UserDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDiscountRepository extends JpaRepository<UserDiscount, String> {
    List<UserDiscount> findByUserId(String userId);

    Optional<UserDiscount> findByUserIdAndDiscount_Code(String userId, String code);

    Optional<UserDiscount> findByUserIdAndDiscount_Id(String userId, String discountId);
}
