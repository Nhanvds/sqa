package com.doan.backend.controllers;

import com.doan.backend.dto.request.CartItemRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CartItemResponse;
import com.doan.backend.dto.response.CartResponse;
import com.doan.backend.services.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/cart")
public class CartController {
    CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@RequestParam String userId) {
        return cartService.getCartByUserId(userId);
    }

    @PutMapping("/{id}")
    public ApiResponse<CartItemResponse> updateCartItem(@PathVariable String id, @RequestBody @Validated CartItemRequest cartItemRequest) {
        return cartService.updateCartItem(id, cartItemRequest);
    }

    @PostMapping
    public ApiResponse<CartItemResponse> createCartItem(@RequestBody @Validated CartItemRequest cartItemRequest) {
        return cartService.addCartItem(cartItemRequest);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCartItem(@PathVariable String id) {
        return cartService.deleteCartItem(id);
    }
}
