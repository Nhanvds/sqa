package com.doan.backend.controllers;

import com.doan.backend.dto.request.OrderRequest;
import com.doan.backend.dto.request.UpdateOrderRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.OrderResponse;
import com.doan.backend.enums.OrderStatusEnum;
import com.doan.backend.services.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;

    @PostMapping("/create")
    public ApiResponse<OrderResponse> createOrderFromCart(@RequestBody @Validated OrderRequest orderRequest) {
        return orderService.createOrderFromCart(orderRequest);
    }

    @PutMapping("/client/edit")
    public ApiResponse<OrderResponse> clientEditOrder(@RequestBody @Validated UpdateOrderRequest clientUpdateOrderRequest) {
        return orderService.clientEditOrder(clientUpdateOrderRequest);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<Iterable<OrderResponse>> getOrdersByUserId(@PathVariable String userId) {
        return orderService.getOrderByUserId(userId);
    }

    @PutMapping("/admin/edit/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> adminEditOrder(
            @PathVariable String orderId,
            @RequestBody OrderRequest orderRequest) {
        return orderService.adminEditOrder(orderId, orderRequest);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderResponse>> getOrdersForAdmin(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) OrderStatusEnum status,
            Pageable pageable
    ) {
        return orderService.getOrdersForAdmin(productName, customerEmail, status, pageable);
    }
}
