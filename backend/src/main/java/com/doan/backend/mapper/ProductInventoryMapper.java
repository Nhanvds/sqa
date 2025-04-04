package com.doan.backend.mapper;

import com.doan.backend.dto.request.ProductInventoryRequest;
import com.doan.backend.dto.response.ProductInventoryResponse;
import com.doan.backend.entity.ProductInventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, SizeMapper.class})
public interface ProductInventoryMapper {

    @Mapping(source = "idProduct", target = "product.id")
    @Mapping(source = "idSize", target = "size.id")
    ProductInventory toProductInventory(ProductInventoryRequest productInventoryRequest);

    @Mapping(source = "product.id", target = "idProduct")
    ProductInventoryResponse toProductInventoryResponse(ProductInventory productInventory);

    @Mapping(source = "product.id", target = "idProduct")
    @Mapping(source = "size.id", target = "idSize")
    Iterable<ProductInventoryResponse> toProductInventoryResponse(Iterable<ProductInventory> productInventories);
}
