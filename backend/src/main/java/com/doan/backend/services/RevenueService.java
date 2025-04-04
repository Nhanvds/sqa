package com.doan.backend.services;

import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CategoryStatistics.CategoryRevenueResponse;
import com.doan.backend.dto.response.CategoryStatistics.CategoryStatistic;
import com.doan.backend.dto.response.CategoryStatistics.CategoryStatisticResponse;
import com.doan.backend.dto.response.CustomerStatistics.CustomerRevenueResponse;
import com.doan.backend.dto.response.CustomerStatistics.CustomerStatistic;
import com.doan.backend.dto.response.CustomerStatistics.CustomerStatisticResponse;
import com.doan.backend.dto.response.ProductStatistics.ProductRevenueResponse;
import com.doan.backend.dto.response.ProductStatistics.ProductStatistic;
import com.doan.backend.dto.response.ProductStatistics.ProductStatisticResponse;
import com.doan.backend.repositories.OrderItemRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RevenueService {
    OrderItemRepository orderItemRepository;

    public ApiResponse<List<ProductRevenueResponse>> getProductRevenue(LocalDate startDate, LocalDate endDate) {
        List<ProductStatisticResponse> responses = orderItemRepository.getProductRevenue(startDate.atStartOfDay(), endDate.atStartOfDay());

        List<ProductRevenueResponse> productRevenueResponses = responses.stream()
                .collect(Collectors.groupingBy(ProductStatisticResponse::getId))
                .values().stream()
                .map(productStatisticResponse -> {
                    String name = productStatisticResponse.getFirst().getProductName();
                    List<ProductStatistic> statistics = productStatisticResponse.stream()
                            .map(product -> new ProductStatistic(
                                    product.getDate(),
                                    product.getSize(),
                                    product.getPrice(),
                                    product.getQuantity(),
                                    product.getDiscountPercentage()
                            ))
                            .collect(Collectors.toList());
                    return new ProductRevenueResponse(name, statistics);
                }).toList();
        return ApiResponse.<List<ProductRevenueResponse>>builder()
                .code(200)
                .result(productRevenueResponses)
                .build();
    }

    public ApiResponse<List<CategoryRevenueResponse>> getCategoryRevenue(LocalDate startDate, LocalDate endDate) {
        List<CategoryStatisticResponse> responses = orderItemRepository.getCategoryRevenue(startDate.atStartOfDay(), endDate.atStartOfDay());

        List<CategoryRevenueResponse> categoryRevenueResponses = responses.stream()
                .collect(Collectors.groupingBy(CategoryStatisticResponse::getId))
                .values().stream()
                .map(categoryStatisticResponse -> {
                    String name = categoryStatisticResponse.getFirst().getCategoryName();
                    List<CategoryStatistic> statistics = categoryStatisticResponse.stream()
                            .map(category -> new CategoryStatistic(
                                    category.getProductName(),
                                    category.getDate(),
                                    category.getSize(),
                                    category.getPrice(),
                                    category.getQuantity(),
                                    category.getDiscountPercentage()
                            ))
                            .collect(Collectors.toList());
                    return new CategoryRevenueResponse(name, statistics);
                }).toList();
        return ApiResponse.<List<CategoryRevenueResponse>>builder()
                .code(200)
                .result(categoryRevenueResponses)
                .build();
    }

    public ApiResponse<List<CustomerRevenueResponse>> getCustomerRevenue(LocalDate startDate, LocalDate endDate) {
        List<CustomerStatisticResponse> responses = orderItemRepository.getCustomerRevenue(startDate.atStartOfDay(), endDate.atStartOfDay());

        List<CustomerRevenueResponse> customerRevenueResponses = responses.stream()
                .collect(Collectors.groupingBy(CustomerStatisticResponse::getUserId))
                .values().stream()
                .map(customerStatisticResponse -> {
                    String name = customerStatisticResponse.getFirst().getName();
                    String email = customerStatisticResponse.getFirst().getEmail();
                    Integer totalOrder = customerStatisticResponse.size();
                    BigDecimal totalValue = customerStatisticResponse.stream()
                            .map(CustomerStatisticResponse::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    List<CustomerStatistic> statistics = customerStatisticResponse.stream()
                            .map(order -> new CustomerStatistic(
                                    order.getOrderId(),
                                    order.getValue(),
                                    order.getDate()
                            ))
                            .collect(Collectors.toList());
                    return new CustomerRevenueResponse(name, email, totalOrder, totalValue, statistics);
                }).toList();

        return ApiResponse.<List<CustomerRevenueResponse>>builder()
                .code(200)
                .result(customerRevenueResponses)
                .build();
    }
}
