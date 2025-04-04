package com.doan.backend.mapper;

import com.doan.backend.dto.request.PromotionRequest;
import com.doan.backend.dto.response.PromotionResponse;
import com.doan.backend.entity.Promotion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PromotionMapper {
    Promotion toPromotion(PromotionRequest promotionRequest);

    PromotionResponse toPromotionResponse(Promotion promotion);
}
