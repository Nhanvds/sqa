package com.doan.backend.mapper;

import com.doan.backend.dto.request.ReviewRequest;
import com.doan.backend.dto.response.ReviewResponse;
import com.doan.backend.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReviewMapper {
    @Mapping(source = "productId", target = "product.id")
    @Mapping(source = "orderId", target = "order.id")
    Review toReview(ReviewRequest reviewRequest);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "user", target = "userReviewResponse")
    @Mapping(source = "order.id", target = "orderId")
    ReviewResponse toReviewResponse(Review review);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "user", target = "userReviewResponse")
    @Mapping(source = "order.id", target = "orderId")
    Iterable<ReviewResponse> toReviewResponseIterable(Iterable<Review> reviews);
}
