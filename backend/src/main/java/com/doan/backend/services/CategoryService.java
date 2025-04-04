package com.doan.backend.services;

import com.doan.backend.dto.request.CategoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CategoryResponse;
import com.doan.backend.entity.Category;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.CategoryMapper;
import com.doan.backend.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class CategoryService {
    CategoryMapper categoryMapper;
    CategoryRepository categoryRepository;

    public ApiResponse<String> deleteCategory(String id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));

        category.setStatus(StatusEnum.DELETED);
        categoryRepository.save(category);
        return ApiResponse.<String>builder()
                .code(200)
                .result(category.getId())
                .message("Category deleted successfully")
                .build();
    }

    public ApiResponse<String> updateCategory(String id, CategoryRequest categoryRequest) {
        Optional<Category> categoryName = categoryRepository.findByNameAndStatusNot(categoryRequest.getName(), StatusEnum.DELETED);

        if (categoryName.isPresent() && !categoryName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Category name already exists");
        }
        Category category = categoryMapper.toCategory(categoryRequest);
        Optional<Category> categoryOptional = categoryRepository.findById(id);

        if (categoryOptional.isPresent()) {
            Category categoryToUpdate = categoryOptional.get();
            categoryToUpdate.setName(category.getName());
            categoryToUpdate.setDescription(category.getDescription());
            categoryToUpdate.setStatus(category.getStatus());
            categoryRepository.save(categoryToUpdate);
            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Category updated successfully")
                    .build();
        } else {
            throw new RuntimeException("Category not found");
        }
    }

    public ApiResponse<CategoryResponse> createCategory(CategoryRequest categoryRequest) {
        boolean exists = categoryRepository.existsByNameAndStatusNot(categoryRequest.getName(), StatusEnum.DELETED);

        if (exists) {
            throw new IllegalArgumentException("Category name already exists");
        } else {
            Category newCategory = categoryRepository.save(categoryMapper.toCategory(categoryRequest));
            return ApiResponse.<CategoryResponse>builder()
                    .code(200)
                    .message("Category created successfully")
                    .result(categoryMapper.toCategoryResponse(newCategory))
                    .build();
        }
    }

    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        List<Category> categories = categoryRepository.findByStatusNot(StatusEnum.DELETED);
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(200)
                .message("Categories retrieved successfully")
                .result(categoryMapper.toCategoryResponseList(categories))
                .build();
    }

    public ApiResponse<CategoryResponse> getCategory(String id) {
        Optional<Category> categoryOptional = categoryRepository.findById(id);

        if (categoryOptional.isPresent()) {
            return ApiResponse.<CategoryResponse>builder()
                    .code(200)
                    .message("Category retrieved successfully")
                    .result(categoryMapper.toCategoryResponse(categoryOptional.get()))
                    .build();
        } else {
            throw new RuntimeException("Category not found");
        }
    }

    public ApiResponse<Page<CategoryResponse>> getPageAllCategories(String name, Pageable pageable) {
        Page<Category> categoryPage = categoryRepository.findByNameContainingIgnoreCaseAndStatusNot(name, StatusEnum.DELETED, pageable);

        Page<CategoryResponse> categoryResponsePage = categoryPage.map(categoryMapper::toCategoryResponse);

        return ApiResponse.<Page<CategoryResponse>>builder()
                .code(200)
                .message("Categories retrieved successfully")
                .result(categoryResponsePage)
                .build();
    }
}
