package com.doan.backend.mapper;

import com.doan.backend.dto.request.ShippingAddressRequest;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.entity.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShippingAddressMapper {

    @Mapping(source = "userId", target = "user.id")
    ShippingAddress toShippingAddress(ShippingAddressRequest shippingAddressRequest);

    ShippingAddressResponse toShippingAddressResponse(ShippingAddress shippingAddress);

    Iterable<ShippingAddressResponse> toShippingAddressResponseIterable(Iterable<ShippingAddress> shippingAddress);
}
