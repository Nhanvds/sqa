package com.doan.backend.controllers;

import com.doan.backend.dto.request.ProductIdsRequest;
import com.doan.backend.dto.request.ProductInventoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductInventoryResponse;
import com.doan.backend.services.ProductInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-inventory")
public class ProductInventoryController {
    @Autowired
    private ProductInventoryService productInventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> createProductInventory(@RequestBody @Validated ProductInventoryRequest productInventoryRequest) {
        return productInventoryService.createProductInventory(productInventoryRequest);
    }

    @GetMapping
    public ApiResponse<?> getProductInventoryByProductId(@RequestParam String productId) {
        return productInventoryService.getProductInventoryByProductId(productId);
    }

    @PostMapping("/get-by-list-product-id")
    public ApiResponse<Iterable<ProductInventoryResponse>> getProductInventoryByListProductId(@RequestBody ProductIdsRequest productIdsRequest) {
        return productInventoryService.getProductInventoryByListProductId(productIdsRequest.getProductIds());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<?> updateProductInventory(@PathVariable String id, @RequestBody @Validated ProductInventoryRequest productInventoryRequest) {
        return productInventoryService.updateProductInventory(id, productInventoryRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProductInventory(@PathVariable String id) {
        return productInventoryService.deleteProductInventory(id);
    }
}
