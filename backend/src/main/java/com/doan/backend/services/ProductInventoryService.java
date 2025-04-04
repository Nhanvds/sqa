package com.doan.backend.services;

import com.doan.backend.dto.request.ProductInventoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductInventoryResponse;
import com.doan.backend.entity.ProductInventory;
import com.doan.backend.mapper.ProductInventoryMapper;
import com.doan.backend.repositories.ProductInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ProductInventoryService {
    ProductInventoryRepository productInventoryRepository;
    ProductInventoryMapper productInventoryMapper;

    public ApiResponse<ProductInventoryResponse> createProductInventory(ProductInventoryRequest productInventoryRequest) {
        if (productInventoryRepository.existsByProductIdAndSizeId(productInventoryRequest.getIdProduct(), productInventoryRequest.getIdSize())) {
            throw new RuntimeException("Product inventory already exists");
        }
        ProductInventoryResponse productInventoryResponse = productInventoryMapper.toProductInventoryResponse(productInventoryRepository.save(productInventoryMapper.toProductInventory(productInventoryRequest)));
        return ApiResponse.<ProductInventoryResponse>builder()
                .code(200)
                .message("Create product inventory successfully")
                .result(productInventoryResponse)
                .build();

    }

    public ApiResponse<Iterable<ProductInventoryResponse>> getProductInventoryByProductId(String productId) {
        Iterable<ProductInventoryResponse> productInventoryResponses = productInventoryMapper.toProductInventoryResponse(productInventoryRepository.findByProductId(productId));
        return ApiResponse.<Iterable<ProductInventoryResponse>>builder()
                .code(200)
                .message("Get product inventory by product id successfully")
                .result(productInventoryResponses)
                .build();
    }

    public ApiResponse<ProductInventoryResponse> updateProductInventory(String id, ProductInventoryRequest productInventoryRequest) {
        Optional<ProductInventory> productInventoryOptional = productInventoryRepository.findById(id);
        if (!productInventoryOptional.isPresent()) {
            throw new RuntimeException("Product inventory not found");
        }

        ProductInventory productInventory = productInventoryOptional.get();
        if (!productInventory.getProduct().getId().equals(productInventoryRequest.getIdProduct()) || !productInventory.getSize().getId().equals(productInventoryRequest.getIdSize())) {
            throw new RuntimeException("Product inventory already exists");
        }

        productInventory.setQuantity(productInventoryRequest.getQuantity());

        ProductInventoryResponse productInventoryResponse = productInventoryMapper.toProductInventoryResponse(productInventoryRepository.save(productInventory));
        return ApiResponse.<ProductInventoryResponse>builder()
                .code(200)
                .message("Update product inventory successfully")
                .result(productInventoryResponse)
                .build();
    }

    public ApiResponse<String> deleteProductInventory(String id) {
        productInventoryRepository.deleteById(id);
        return ApiResponse.<String>builder()
                .code(200)
                .message("Delete product inventory successfully")
                .result(id)
                .build();
    }

    public ApiResponse<Iterable<ProductInventoryResponse>> getProductInventoryByListProductId(List<String> productIds) {
        Iterable<ProductInventoryResponse> productInventoryResponses = productInventoryMapper.toProductInventoryResponse(productInventoryRepository.findByProductIdIn(productIds));
        return ApiResponse.<Iterable<ProductInventoryResponse>>builder()
                .code(200)
                .message("Get product inventory by list product id successfully")
                .result(productInventoryResponses)
                .build();
    }
}
