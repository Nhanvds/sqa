package com.doan.backend.services;

import com.doan.backend.dto.request.ReviewRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ReviewResponse;
import com.doan.backend.entity.Order;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.Review;
import com.doan.backend.entity.User;
import com.doan.backend.mapper.ReviewMapper;
import com.doan.backend.repositories.OrderRepository;
import com.doan.backend.repositories.ProductRepository;
import com.doan.backend.repositories.ReviewRepository;
import com.doan.backend.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class ReviewService {
    ReviewRepository reviewRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    OrderRepository orderRepository;
    ReviewMapper reviewMapper;
    AuthService authService;

    public ApiResponse<ReviewResponse> createReview(ReviewRequest reviewRequest) {
        User user = authService.getUserByToken();
        Product product = productRepository.findById(reviewRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));
        Order order = orderRepository.findById(reviewRequest.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to review this product");
        }

        boolean isCompletedOrder = orderRepository.existOrderCompletedByUserId(
                user.getId(), reviewRequest.getProductId());

        if (!isCompletedOrder) {
            throw new RuntimeException("Product not in any paid order of this customer");
        }

        Review review = reviewMapper.toReview(reviewRequest);
        review.setProduct(product);
        review.setUser(user);
        review.setOrder(order);
        review.setRating(reviewRequest.getRating());
        review.setContent(reviewRequest.getContent());

        Review savedReview = reviewRepository.save(review);
        calculateAndUpdateProductRating(product);

        return ApiResponse.<ReviewResponse>builder()
                .message("Create review successfully")
                .result(reviewMapper.toReviewResponse(savedReview))
                .build();
    }

    public ApiResponse<ReviewResponse> updateReview(String reviewId, ReviewRequest reviewRequest) {
        User user = authService.getUserByToken();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!user.getId().equals(review.getUser().getId())) {
            throw new RuntimeException("You are not allowed to update this review");
        }

        review.setContent(reviewRequest.getContent());
        review.setRating(reviewRequest.getRating());

        Review updatedReview = reviewRepository.save(review);

        calculateAndUpdateProductRating(review.getProduct());
        return ApiResponse.<ReviewResponse>builder()
                .message("Update review successfully")
                .code(200)
                .result(reviewMapper.toReviewResponse(updatedReview))
                .build();
    }

    public ApiResponse<Iterable<ReviewResponse>> getReviewByProductId(String productId) {
        Iterable<Review> reviews = reviewRepository.findByProductId(productId);
        return ApiResponse.<Iterable<ReviewResponse>>builder()
                .code(200)
                .message("Review by productId")
                .result(reviewMapper.toReviewResponseIterable(reviews))
                .build();
    }

    public ApiResponse<ReviewResponse> deleteReview(String reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        User user = authService.getUserByToken();

        if (!user.getId().equals(review.getUser().getId())) {
            throw new RuntimeException("You are not allowed to delete this review");
        }

        reviewRepository.delete(review);
        calculateAndUpdateProductRating(review.getProduct());

        return ApiResponse.<ReviewResponse>builder()
                .message("Delete review successfully")
                .code(200)
                .result(reviewMapper.toReviewResponse(review))
                .build();
    }

    public void calculateAndUpdateProductRating(Product product) {
        Double averageRating = reviewRepository.calculateAverageRatingByProductId(product.getId());
        product.setRating(averageRating);
        productRepository.save(product);
    }

    public ApiResponse<Iterable<ReviewResponse>> getReviewByOrderId(String orderId) {
        Iterable<Review> reviews = reviewRepository.findByOrderId(orderId);
        return ApiResponse.<Iterable<ReviewResponse>>builder()
                .code(200)
                .message("Review by productId and orderId")
                .result(reviewMapper.toReviewResponseIterable(reviews))
                .build();
    }
}
