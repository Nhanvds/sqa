package com.doan.backend.services;

import com.doan.backend.dto.request.DeleteProductImageRequest;
import com.doan.backend.dto.request.ProductImageRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductImageResponse;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.ProductImage;
import com.doan.backend.mapper.ProductImageMapper;
import com.doan.backend.repositories.ProductImageRepository;
import com.doan.backend.repositories.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductImageService {
    ProductImageRepository productImageRepository;
    ProductRepository productRepository;
    ProductImageMapper productImageMapper;

    public ApiResponse<Void> createProductImage(ProductImageRequest productImageRequest) {
        Product product = productRepository.findById(productImageRequest.getIdProduct())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        for (String url : productImageRequest.getImageUrl()) {
            ProductImage productImage = new ProductImage();
            productImage.setImageUrl(url);
            productImage.setProduct(product);
            productImageRepository.save(productImage);
        }

        return ApiResponse.<Void>builder()
                .message("Create product image successfully")
                .code(200)
                .build();
    }

    public ApiResponse<Void> deleteProductImage(DeleteProductImageRequest deleteProductImageRequest) {
        productImageRepository.deleteAllById(deleteProductImageRequest.getIds());
        return ApiResponse.<Void>builder()
                .message("Delete product image successfully")
                .code(200)
                .build();
    }

    public ApiResponse<Iterable<ProductImageResponse>> getProductImagesByProductId(String productId) {
        Iterable<ProductImage> productImages = productImageRepository.findAllByProductId(productId);
        return ApiResponse.<Iterable<ProductImageResponse>>builder()
                .result(productImageMapper.toProductImageResponses(productImages))
                .message("Get product images successfully")
                .code(200)
                .build();
    }
}
