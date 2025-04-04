package com.doan.backend.controllers;

import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CategoryStatistics.CategoryRevenueResponse;
import com.doan.backend.dto.response.CustomerStatistics.CustomerRevenueResponse;
import com.doan.backend.dto.response.ProductStatistics.ProductRevenueResponse;
import com.doan.backend.services.RevenueService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/revenue")
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RevenueController {
    RevenueService revenueService;

    @GetMapping("/product")
    public ApiResponse<List<ProductRevenueResponse>> getProductRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return revenueService.getProductRevenue(startDate,endDate);
    }

    @GetMapping("/category")
    public ApiResponse<List<CategoryRevenueResponse>> getCategoryRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return revenueService.getCategoryRevenue(startDate,endDate);
    }

    @GetMapping("/customer")
    public ApiResponse<List<CustomerRevenueResponse>> getCustomerRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return revenueService.getCustomerRevenue(startDate,endDate);
    }
}
