package com.doan.backend.services;

import com.doan.backend.dto.request.CartItemRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CartItemResponse;
import com.doan.backend.dto.response.CartResponse;
import com.doan.backend.entity.Cart;
import com.doan.backend.entity.CartItem;
import com.doan.backend.entity.ProductInventory;
import com.doan.backend.mapper.CartItemMapper;
import com.doan.backend.mapper.CartMapper;
import com.doan.backend.repositories.CartItemRepository;
import com.doan.backend.repositories.CartRepository;
import com.doan.backend.repositories.ProductInventoryRepository;
import com.doan.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class CartService {
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    ProductInventoryRepository productInventoryRepository;
    UserRepository userRepository;
    CartMapper cartMapper;
    CartItemMapper cartItemMapper;

    public ApiResponse<CartResponse> getCartByUserId(String userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);

        if (cartOptional.isPresent()) {
            CartResponse cartResponse = cartMapper.toCartResponse(cartOptional.get());
            cartResponse.setCartItems(cartItemMapper.tocartItemResponseList(cartItemRepository.findByCartId(cartOptional.get().getId())));
            return ApiResponse.<CartResponse>builder()
                    .code(200)
                    .message("get cart successfully")
                    .result(cartResponse)
                    .build();
        } else {
            return ApiResponse.<CartResponse>builder()
                    .code(200)
                    .message("get cart successfully")
                    .result(createCart(userId))
                    .build();
        }
    }

    private ProductInventory validateProductInventory(CartItemRequest cartItemRequest) {
        ProductInventory productInventory = productInventoryRepository.findByProductIdAndSizeId(
                        cartItemRequest.getProductId(), cartItemRequest.getSizeId())
                .orElseThrow(() -> new RuntimeException("Product inventory not found"));

        if (productInventory.getQuantity() < cartItemRequest.getQuantity()) {
            throw new RuntimeException("Insufficient stock for product: " + productInventory.getProduct().getName());
        }
        return productInventory;
    }

    public ApiResponse<CartItemResponse> addCartItem(CartItemRequest cartItemRequest) {
        ProductInventory productInventory = validateProductInventory(cartItemRequest);
        Cart cart = cartRepository.findById(cartItemRequest.getCartId()).orElseThrow(() -> new RuntimeException("Cart not found"));
        CartItem newCartItem = new CartItem();

        newCartItem.setCart(cart);
        newCartItem.setProduct(productInventory.getProduct());
        newCartItem.setSize(productInventory.getSize());
        newCartItem.setQuantity(cartItemRequest.getQuantity());

        CartItemResponse cartItemResponse = cartItemMapper.toCartItemResponse(cartItemRepository.save(newCartItem));

        return ApiResponse.<CartItemResponse>builder()
                .code(200)
                .message("Cart item added successfully")
                .result(cartItemResponse)
                .build();
    }

    public ApiResponse<CartItemResponse> updateCartItem(String cartItemId, CartItemRequest cartItemRequest) {
        ProductInventory productInventory = validateProductInventory(cartItemRequest);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItem.setQuantity(cartItemRequest.getQuantity());
        cartItem.setProduct(productInventory.getProduct());
        cartItem.setSize(productInventory.getSize());
        CartItemResponse cartItemResponse = cartItemMapper.toCartItemResponse(cartItemRepository.save(cartItem));
        return ApiResponse.<CartItemResponse>builder()
                .code(200)
                .message("Cart item updated successfully")
                .result(cartItemResponse)
                .build();
    }

    public ApiResponse<Void> deleteCartItem(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("delete cart item successfully")
                .build();
    }

    private CartResponse createCart(String userId) {
        Cart cart = new Cart();
        cart.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));

        CartResponse cartResponse = cartMapper.toCartResponse(cartRepository.save(cart));
        cartResponse.setCartItems(Collections.emptyList());
        return cartResponse;
    }
}
