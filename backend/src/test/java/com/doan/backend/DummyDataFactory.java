package com.doan.backend;

import com.doan.backend.dto.request.CartItemRequest;
import com.doan.backend.dto.response.*;
import com.doan.backend.entity.*;
import com.doan.backend.enums.StatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class DummyDataFactory {

    public static CartItemRequest dummyCartItemRequest() {
        CartItemRequest request = new CartItemRequest();
        request.setCartId(UUID.randomUUID().toString());
        request.setProductId(UUID.randomUUID().toString());
        request.setSizeId(UUID.randomUUID().toString());
        request.setQuantity(2);
        return request;
    }

    public static ProductInventory dummyProductInventory() {
        ProductInventory inventory = new ProductInventory();
        inventory.setId(UUID.randomUUID().toString());
        inventory.setProduct(dummyProduct());
        inventory.setSize(dummySize());
        inventory.setQuantity(100);
        return inventory;
    }

    public static Product dummyProduct() {
        Product product = new Product();
        product.setId(UUID.randomUUID().toString());
        product.setName("Sample Product");
        product.setDescription("This is a sample product description.");
        product.setPrice(new BigDecimal("199.99"));
        product.setCategory(dummyCategory());
        product.setRating(4.5);
        product.setMainImage("https://example.com/product-image.jpg");
        product.setStatus(StatusEnum.ACTIVE);
        product.setCreatedAt(LocalDateTime.now().minusDays(5));
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    public static Category dummyCategory() {
        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setName("Sample Category");
        category.setDescription("This is a sample category description.");
        category.setStatus(StatusEnum.ACTIVE);
        category.setCreatedAt(LocalDateTime.now().minusMonths(1));
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    public static Size dummySize() {
        Size size = new Size();
        size.setId(UUID.randomUUID().toString());
        size.setName("Size M");
        return size;
    }

    public static Cart dummyCart() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID().toString());
        cart.setUser(dummyUser());
        cart.setCreatedAt(LocalDateTime.now().minusDays(2));
        cart.setUpdatedAt(LocalDateTime.now());
        return cart;
    }

    public static CartItemResponse dummyCartItemResponse() {
        CartItemResponse response = new CartItemResponse();
        response.setId(UUID.randomUUID().toString());
        response.setProduct(dummyProductResponse());
        response.setSize(dummySizeResponse());
        response.setQuantity(2);
        return response;
    }

    public static ProductResponse dummyProductResponse() {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(UUID.randomUUID().toString());
        productResponse.setName("Sample Product Response");
        productResponse.setDescription("This is a product response description.");
        productResponse.setPrice(new BigDecimal("150.00"));
        productResponse.setCategoryResponse(dummyCategoryResponse());
        productResponse.setPromotionResponse(dummyPromotionResponse());
        productResponse.setPromotions(List.of(dummyPromotionResponse()));
        productResponse.setRating(4.7);
        productResponse.setStatus(StatusEnum.ACTIVE);
        productResponse.setDiscountPercentage(new BigDecimal("10.0"));
        productResponse.setCreatedAt(LocalDateTime.now().minusDays(3));
        productResponse.setUpdatedAt(LocalDateTime.now());
        productResponse.setMainImage("https://example.com/response-product-image.jpg");
        return productResponse;
    }

    public static SizeResponse dummySizeResponse() {
        SizeResponse sizeResponse = new SizeResponse();
        sizeResponse.setId(UUID.randomUUID().toString());
        sizeResponse.setName("Size L");
        return sizeResponse;
    }

    public static CategoryResponse dummyCategoryResponse() {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(UUID.randomUUID().toString());
        categoryResponse.setName("Category Response");
        categoryResponse.setDescription("Category Response Description");
        categoryResponse.setStatus(StatusEnum.ACTIVE);
        return categoryResponse;
    }

    public static PromotionResponse dummyPromotionResponse() {
        PromotionResponse promotion = new PromotionResponse();
        promotion.setId(UUID.randomUUID().toString());
        promotion.setName("Summer Sale");
        promotion.setDiscountPercentage(new BigDecimal("15.00"));
        return promotion;
    }

    private static User dummyUser() {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail("testuser@example.com");
        // bổ sung các trường cần thiết khác nếu User có
        return user;
    }

    public static CartItem dummyCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(UUID.randomUUID().toString());
        cartItem.setCart(dummyCart());
        cartItem.setProduct(dummyProduct());
        cartItem.setSize(dummySize());
        cartItem.setQuantity(3);
        return cartItem;
    }
}
