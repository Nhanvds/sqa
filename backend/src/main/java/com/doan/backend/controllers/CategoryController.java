package com.doan.backend.controllers;


import com.doan.backend.dto.request.CategoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CategoryResponse;
import com.doan.backend.services.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @GetMapping("/{id}")
    ApiResponse<CategoryResponse> getCategory(@PathVariable String id) {
        return categoryService.getCategory(id);
    }

    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/page")
    ApiResponse<Page<CategoryResponse>> getPageCategoriesByName(@RequestParam String name, Pageable pageable) {
        return categoryService.getPageAllCategories(name, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    ApiResponse<CategoryResponse> createCategory(
            @RequestBody @Validated CategoryRequest categoryRequest) {
        return categoryService.createCategory(categoryRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    ApiResponse<String> updateCategory(
            @PathVariable String id,
            @RequestBody @Validated CategoryRequest categoryRequest) {
        return categoryService.updateCategory(id, categoryRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    ApiResponse<String> deleteCategory(@PathVariable String id) {
        return categoryService.deleteCategory(id);
    }
}
