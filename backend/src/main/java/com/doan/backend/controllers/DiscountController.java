package com.doan.backend.controllers;

import com.doan.backend.dto.request.DiscountRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.DiscountResponse;
import com.doan.backend.entity.Discount;
import com.doan.backend.services.DiscountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discounts")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class DiscountController {

    DiscountService discountService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Discount> createDiscount(@RequestBody @Validated DiscountRequest discountRequest) {
        return discountService.createDiscount(discountRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<Discount> updateDiscount(@PathVariable String id, @RequestBody @Validated DiscountRequest discountRequest) {
        return discountService.updateDiscount(id, discountRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDiscount(@PathVariable String id) {
        return discountService.deleteDiscount(id);
    }

    @GetMapping("/get-code")
    public ApiResponse<Discount> getDiscountByCode(@RequestParam String code, @RequestParam String userId) {
        return discountService.getDiscount(code, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ApiResponse<Page<Discount>> getDiscountSearchByCode(
            @RequestParam(required = false) String code,
            Pageable pageable) {
        return discountService.getDiscountSearchByCode(code, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("get-current-discount")
    public ApiResponse<Iterable<DiscountResponse>> getCurrentDiscount() {
        return discountService.getCurrentDiscount();
    }

    @GetMapping("/auto-apply")
    public ApiResponse<Iterable<Discount>> getAllDiscountsByAutoApply(@RequestParam String userId) {
        return discountService.getAllDiscountsByAutoApply(userId);
    }

    @GetMapping("/get-discount")
    public ApiResponse<Discount> getDiscountById(@RequestParam String userId, @RequestParam String code) {
        return discountService.getDiscount(code, userId);
    }
}
