package com.doan.backend.mapper;

import com.doan.backend.dto.response.ProductImageResponse;
import com.doan.backend.entity.ProductImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
    Iterable<ProductImageResponse> toProductImageResponses(Iterable<ProductImage> productImages);
}