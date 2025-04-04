package com.doan.backend.mapper;

import com.doan.backend.dto.request.ProductRequest;
import com.doan.backend.dto.response.ProductResponse;
import com.doan.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface ProductMapper {

    @Mapping(source = "productRequest.categoryId", target = "category.id")
    Product toProduct(ProductRequest productRequest);

    @Mapping(source = "category", target = "categoryResponse")
    ProductResponse toProductResponse(Product product);
}
