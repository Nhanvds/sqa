package com.doan.backend.mapper;

import com.doan.backend.dto.request.DiscountRequest;
import com.doan.backend.dto.response.DiscountResponse;
import com.doan.backend.entity.Discount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    Discount toDiscount(DiscountRequest discountRequest);

    Iterable<DiscountResponse> toDiscountResponseIterable(Iterable<Discount> discounts);
}
