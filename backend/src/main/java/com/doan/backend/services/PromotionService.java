package com.doan.backend.services;

import com.doan.backend.dto.request.PromotionRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.PromotionResponse;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.Promotion;
import com.doan.backend.mapper.PromotionMapper;
import com.doan.backend.repositories.PromotionProductRepository;
import com.doan.backend.repositories.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PromotionService {
    PromotionRepository promotionRepository;
    PromotionProductRepository promotionProductRepository;
    PromotionMapper promotionMapper;

    public ApiResponse<PromotionResponse> createPromotion(PromotionRequest promotionRequest) {
        Promotion promotion = promotionMapper.toPromotion(promotionRequest);
        promotion = promotionRepository.save(promotion);
        return ApiResponse.<PromotionResponse>builder()
                .code(200)
                .message("Promotion created successfully")
                .result(promotionMapper.toPromotionResponse(promotion))
                .build();
    }

    public ApiResponse<PromotionResponse> updatePromotion(String id, PromotionRequest promotionRequest) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setName(promotionRequest.getName());
        promotion.setDescription(promotionRequest.getDescription());
        promotion.setStartDate(promotionRequest.getStartDate());
        promotion.setEndDate(promotionRequest.getEndDate());
        promotion.setApplyToAll(promotionRequest.getApplyToAll());
        promotion.setIsActive(promotionRequest.getIsActive());
        promotion.setDiscountPercentage(promotionRequest.getDiscountPercentage());

        promotionRepository.save(promotion);
        return ApiResponse.<PromotionResponse>builder()
                .code(200)
                .message("Promotion update successfully")
                .result(promotionMapper.toPromotionResponse(promotion))
                .build();
    }

    public ApiResponse<Void> deletePromotion(String id) {
        promotionRepository.deleteById(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Promotion deleted successfully")
                .build();
    }

    public ApiResponse<PromotionResponse> getPromotionById(String id) {
        PromotionResponse promotionResponse = promotionRepository.findById(id)
                .map(promotionMapper::toPromotionResponse)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        return ApiResponse.<PromotionResponse>builder()
                .code(200)
                .message("Promotion retrieved successfully")
                .result(promotionResponse)
                .build();
    }

    public ApiResponse<Page<PromotionResponse>> getAllPromotions(String name, Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findByNameContaining(name, pageable);
        Page<PromotionResponse> promotionResponses = promotions.map(promotionMapper::toPromotionResponse);

        return ApiResponse.<Page<PromotionResponse>>builder()
                .code(200)
                .message("Promotions retrieved successfully")
                .result(promotionResponses)
                .build();
    }

    public BigDecimal applyPromotionToProduct(Product product) {
        Optional<Promotion> promotionOptional = promotionProductRepository.findActivePromotionByProductId(product.getId(), LocalDateTime.now());

        if (promotionOptional.isPresent()) {
            Promotion promotion = promotionOptional.get();

            BigDecimal discount = product.getPrice().multiply(promotion.getDiscountPercentage().divide(BigDecimal.valueOf(100)));
            return product.getPrice().subtract(discount);
        }

        return product.getPrice();
    }

    public ApiResponse<List<PromotionResponse>> getCurrentPromotionsExcludeApplyToAll() {
        List<PromotionResponse> promotions = promotionRepository.findActiveCurrentPromotionsExcludeApplyToAll(LocalDateTime.now())
                .stream()
                .map(promotionMapper::toPromotionResponse)
                .toList();
        return ApiResponse.<List<PromotionResponse>>builder()
                .code(200)
                .message("Promotions retrieved successfully")
                .result(promotions)
                .build();
    }

}
