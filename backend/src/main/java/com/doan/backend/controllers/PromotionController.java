package com.doan.backend.controllers;

import com.doan.backend.dto.request.PromotionRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.PromotionResponse;
import com.doan.backend.services.PromotionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotion")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PromotionController {
    PromotionService promotionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<PromotionResponse> createPromotion(@RequestBody @Validated PromotionRequest promotionRequest) {
        return promotionService.createPromotion(promotionRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<PromotionResponse> updatePromotion(
            @PathVariable String id,
            @RequestBody @Validated PromotionRequest promotionRequest) {
        return promotionService.updatePromotion(id, promotionRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromotion(@PathVariable String id) {
        return promotionService.deletePromotion(id);

    }

    @GetMapping("/client/{id}")
    public ApiResponse<PromotionResponse> getPromotionById(@PathVariable String id) {
        return promotionService.getPromotionById(id);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<PromotionResponse>> getAllPromotions(
            @RequestParam(required = false) String name,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return promotionService.getAllPromotions(name, pageable);
    }

    @GetMapping("/get-excluded-apply-to-all")
    public ApiResponse<List<PromotionResponse>> getCurrentPromotionsExcludeApplyToAll() {
        return promotionService.getCurrentPromotionsExcludeApplyToAll();
    }
}
