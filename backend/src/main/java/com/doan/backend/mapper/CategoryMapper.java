package com.doan.backend.mapper;

import com.doan.backend.dto.request.CategoryRequest;
import com.doan.backend.dto.response.CategoryResponse;
import com.doan.backend.entity.Category;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest categoryRequest);
    CategoryResponse toCategoryResponse(Category category);
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);
}
