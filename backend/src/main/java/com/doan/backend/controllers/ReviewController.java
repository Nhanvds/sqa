package com.doan.backend.controllers;

import com.doan.backend.dto.request.ReviewRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ReviewResponse;
import com.doan.backend.services.AuthService;
import com.doan.backend.services.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ReviewController {
    ReviewService reviewService;
    AuthService authService;

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@RequestBody @Validated ReviewRequest reviewRequest) {
        return reviewService.createReview(reviewRequest);
    }

    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable String id,
            @RequestBody @Validated ReviewRequest reviewRequest) {
        return reviewService.updateReview(id, reviewRequest);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<ReviewResponse> deleteReview(@PathVariable String id) {
        return reviewService.deleteReview(id);
    }

    @GetMapping()
    public ApiResponse<Iterable<ReviewResponse>> getReviewByProductId(@RequestParam String productId) {
        return reviewService.getReviewByProductId(productId);
    }

    @GetMapping("/order")
    public ApiResponse<Iterable<ReviewResponse>> getReviewByOrderId(@RequestParam String orderId) {
        return reviewService.getReviewByOrderId(orderId);
    }
}
