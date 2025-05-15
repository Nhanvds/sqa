package com.doan.backend.services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestReviewService {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private AuthService authService;
    @Mock private UserRepository userRepository;
    @Mock private ReviewMapper reviewMapper;
    @InjectMocks private ReviewService reviewService;

    // ===============================================================
    // TC-RV-001: Tạo review thành công
    // Mục tiêu: Đảm bảo createReview lưu review và cập nhật rating sản phẩm
    // Input: ReviewRequest hợp lệ, user đã hoàn thành đơn chứa sản phẩm
    // Expected: ApiResponse.message "Create review successfully", trả về ReviewResponse, và product.rating được cập nhật
    // ===============================================================
    @Test
    public void testCreateReviewSuccess() {
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);

        Product product = new Product(); product.setId("p1");
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        Order order = new Order(); order.setId("o1"); order.setUser(user);
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.existOrderCompletedByUserId("u1", "p1")).thenReturn(true);

        ReviewRequest req = ReviewRequest.builder()
                .productId("p1")
                .orderId("o1")
                .rating(5.0)
                .content("Great!")
                .build();

        Review reviewEntity = new Review();
        when(reviewMapper.toReview(req)).thenReturn(reviewEntity);
        Review savedReview = new Review(); savedReview.setId("r1");
        when(reviewRepository.save(reviewEntity)).thenReturn(savedReview);

        ReviewResponse respDto = ReviewResponse.builder().id("r1").build();
        when(reviewMapper.toReviewResponse(savedReview)).thenReturn(respDto);

        when(reviewRepository.calculateAverageRatingByProductId("p1")).thenReturn(4.2);

        ApiResponse<ReviewResponse> resp = reviewService.createReview(req);

        assertEquals("Create review successfully", resp.getMessage());
        assertEquals(respDto, resp.getResult());
        verify(productRepository).save(product);
        assertEquals(4.2, product.getRating());
    }

    // ===============================================================
    // TC-RV-002: Tạo review thất bại khi product không tồn tại
    // Mục tiêu: Khi productRepository.findById trả về empty, ném RuntimeException
    // Input: ReviewRequest với productId không tồn tại
    // Expected: RuntimeException("Product not found")
    // ===============================================================
    @Test
    public void testCreateReview_ProductNotFound() {
        when(authService.getUserByToken()).thenReturn(new User());
        when(productRepository.findById("pX")).thenReturn(Optional.empty());

        ReviewRequest req = ReviewRequest.builder()
                .productId("pX")
                .orderId("o1")
                .rating(4.0)
                .content("N/A")
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(req));
        assertEquals("Product not found", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-003: Tạo review thất bại khi order không tồn tại
    // Mục tiêu: Khi orderRepository.findById trả về empty, ném RuntimeException
    // Input: ReviewRequest với orderId không tồn tại
    // Expected: RuntimeException("Order not found")
    // ===============================================================
    @Test
    public void testCreateReview_OrderNotFound() {
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);
        when(productRepository.findById("p1")).thenReturn(Optional.of(new Product()));
        when(orderRepository.findById("oX")).thenReturn(Optional.empty());

        ReviewRequest req = ReviewRequest.builder()
                .productId("p1")
                .orderId("oX")
                .rating(3.0)
                .content("N/A")
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(req));
        assertEquals("Order not found", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-004: Tạo review thất bại khi không phải chủ đơn
    // Mục tiêu: Nếu order.user.id khác auth user id, ném RuntimeException
    // Input: order.user.id != user.id
    // Expected: RuntimeException("You are not allowed to review this product")
    // ===============================================================
    @Test
    public void testCreateReview_NotOrderOwner() {
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);
        when(productRepository.findById("p1")).thenReturn(Optional.of(new Product()));

        User other = new User(); other.setId("u2");
        Order order = new Order(); order.setId("o1"); order.setUser(other);
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        ReviewRequest req = ReviewRequest.builder()
                .productId("p1")
                .orderId("o1")
                .rating(4.0)
                .content("N/A")
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(req));
        assertEquals("You are not allowed to review this product", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-005: Tạo review thất bại khi order chưa hoàn thành
    // Mục tiêu: Nếu existOrderCompletedByUserId trả về false, ném RuntimeException
    // Input: existOrderCompletedByUserId = false
    // Expected: RuntimeException("Product not in any paid order of this customer")
    // ===============================================================
    @Test
    public void testCreateReview_OrderNotCompleted() {
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);
        when(productRepository.findById("p1")).thenReturn(Optional.of(new Product()));
        Order order = new Order(); order.setId("o1"); order.setUser(user);
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.existOrderCompletedByUserId("u1", "p1")).thenReturn(false);

        ReviewRequest req = ReviewRequest.builder()
                .productId("p1")
                .orderId("o1")
                .rating(2.0)
                .content("N/A")
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.createReview(req));
        assertEquals("Product not in any paid order of this customer", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-006: Cập nhật review thành công
    // Mục tiêu: Đảm bảo updateReview cho phép chủ review cập nhật và cập nhật rating
    // Input: existing review thuộc về auth user
    // Expected: ApiResponse.message "Update review successfully" và product.rating cập nhật
    // ===============================================================
    @Test
    public void testUpdateReviewSuccess() {
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);

        Review existing = new Review();
        existing.setId("r1");
        existing.setUser(user);
        Product product = new Product(); product.setId("p1");
        existing.setProduct(product);
        when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

        ReviewRequest req = ReviewRequest.builder()
                .rating(3.5)
                .content("Okay")
                .build();
        when(reviewRepository.save(existing)).thenReturn(existing);
        when(reviewRepository.calculateAverageRatingByProductId("p1")).thenReturn(3.5);

        ReviewResponse dto = ReviewResponse.builder().id("r1").build();
        when(reviewMapper.toReviewResponse(existing)).thenReturn(dto);

        ApiResponse<ReviewResponse> resp = reviewService.updateReview("r1", req);

        assertEquals("Update review successfully", resp.getMessage());
        assertEquals(dto, resp.getResult());
        verify(productRepository).save(product);
        assertEquals(3.5, product.getRating());
    }

    // ===============================================================
    // TC-RV-007: Cập nhật review thất bại khi review không tồn tại
    // Mục tiêu: Khi findById trả về empty, ném RuntimeException
    // Input: reviewId không tồn tại
    // Expected: RuntimeException("Review not found")
    // ===============================================================
    @Test
    public void testUpdateReview_NotFound() {
        when(authService.getUserByToken()).thenReturn(new User());
        when(reviewRepository.findById("x")).thenReturn(Optional.empty());

        ReviewRequest req = ReviewRequest.builder()
                .productId("p").orderId("o").rating(1.0).content("").build();
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.updateReview("x", req));
        assertEquals("Review not found", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-008: Cập nhật review thất bại khi không phải chủ review
    // Mục tiêu: Nếu auth user id khác review.user.id, ném RuntimeException
    // Input: existing.review.user.id != auth user id
    // Expected: RuntimeException("You are not allowed to update this review")
    // ===============================================================
    @Test
    public void testUpdateReview_NotOwner() {
        User user = new User(); user.setId("u1");
        User other = new User(); other.setId("u2");
        when(authService.getUserByToken()).thenReturn(user);
        Review existing = new Review(); existing.setId("r1"); existing.setUser(other);
        when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

        ReviewRequest req = ReviewRequest.builder()
                .productId("p").orderId("o").rating(1.0).content("").build();
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.updateReview("r1", req));
        assertEquals("You are not allowed to update this review", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-009: Lấy review theo productId
    // Mục tiêu: Đảm bảo getReviewByProductId trả về đúng danh sách ReviewResponse
    // Input: repository trả về 2 Review
    // Expected: ApiResponse.code 200, message "Review by productId", result size 2
    // ===============================================================
    @Test
    public void testGetReviewByProductId() {
        Review r1 = new Review(), r2 = new Review();
        when(reviewRepository.findByProductId("p1")).thenReturn(List.of(r1, r2));
        ReviewResponse dto1 = ReviewResponse.builder().id("r1").build();
        ReviewResponse dto2 = ReviewResponse.builder().id("r2").build();
        when(reviewMapper.toReviewResponseIterable(List.of(r1, r2)))
                .thenReturn(List.of(dto1, dto2));

        ApiResponse<Iterable<ReviewResponse>> resp =
                reviewService.getReviewByProductId("p1");

        List<ReviewResponse> list = StreamSupport.stream(resp.getResult().spliterator(), false)
                .collect(Collectors.toList());
        assertEquals(2, list.size());
        assertEquals("Review by productId", resp.getMessage());
        assertEquals(200, resp.getCode());
    }

    // ===============================================================
    // TC-RV-010: Lấy review theo productId khi không có review
    // Mục tiêu: Khi repository trả về rỗng, result cũng rỗng
    // Input: productId không có review
    // Expected: ApiResponse.result.size() == 0
    // ===============================================================
    @Test
    public void testGetReviewByProductId_Empty() {
        when(reviewRepository.findByProductId("pEmpty")).thenReturn(List.of());
        when(reviewMapper.toReviewResponseIterable(List.of())).thenReturn(List.of());

        ApiResponse<Iterable<ReviewResponse>> resp =
                reviewService.getReviewByProductId("pEmpty");

        List<ReviewResponse> list = StreamSupport.stream(resp.getResult().spliterator(), false)
                .collect(Collectors.toList());
        assertTrue(list.isEmpty());
        assertEquals(200, resp.getCode());
    }

    // ===============================================================
    // TC-RV-011: Xóa review thành công
    // Mục tiêu: Đảm bảo deleteReview xóa review và cập nhật rating
    // Input: existing.review.user.id == auth user id
    // Expected: ApiResponse.message "Delete review successfully", cập nhật product.rating
    // ===============================================================
    @Test
    public void testDeleteReviewSuccess() {
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);

        Review existing = new Review();
        existing.setId("r1");
        existing.setUser(user);
        Product prod = new Product(); prod.setId("p1");
        existing.setProduct(prod);
        when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

        when(reviewRepository.calculateAverageRatingByProductId("p1")).thenReturn(4.0);
        ReviewResponse dto = ReviewResponse.builder().id("r1").build();
        when(reviewMapper.toReviewResponse(existing)).thenReturn(dto);

        ApiResponse<ReviewResponse> resp = reviewService.deleteReview("r1");

        assertEquals("Delete review successfully", resp.getMessage());
        verify(reviewRepository).delete(existing);
        verify(productRepository).save(prod);
        assertEquals(4.0, prod.getRating());
    }

    // ===============================================================
    // TC-RV-012: Xóa review thất bại khi review không tồn tại
    // Mục tiêu: Khi findById trả về empty, ném RuntimeException
    // Input: reviewId không tồn tại
    // Expected: RuntimeException("Review not found")
    // ===============================================================
    @Test
    public void testDeleteReview_NotFound() {
        when(reviewRepository.findById("x")).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.deleteReview("x"));
        assertEquals("Review not found", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-013: Xóa review thất bại khi không phải chủ review
    // Mục tiêu: Nếu auth user id khác review.user.id, ném RuntimeException
    // Input: existing.review.user.id != auth user id
    // Expected: RuntimeException("You are not allowed to delete this review")
    // ===============================================================
    @Test
    public void testDeleteReview_NotOwner() {
        User user = new User(); user.setId("u1");
        User other = new User(); other.setId("u2");
        when(authService.getUserByToken()).thenReturn(user);
        Review existing = new Review(); existing.setId("r1"); existing.setUser(other);
        when(reviewRepository.findById("r1")).thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                reviewService.deleteReview("r1"));
        assertEquals("You are not allowed to delete this review", ex.getMessage());
    }

    // ===============================================================
    // TC-RV-014: Lấy review theo orderId
    // Mục tiêu: Đảm bảo getReviewByOrderId trả về đúng danh sách
    // Input: repository trả về 1 Review
    // Expected: ApiResponse.code 200, message "Review by productId and orderId", result size 1
    // ===============================================================
    @Test
    public void testGetReviewByOrderId() {
        Review r = new Review();
        when(reviewRepository.findByOrderId("o1")).thenReturn(List.of(r));
        ReviewResponse dto = ReviewResponse.builder().id("r").build();
        when(reviewMapper.toReviewResponseIterable(List.of(r)))
                .thenReturn(List.of(dto));

        ApiResponse<Iterable<ReviewResponse>> resp =
                reviewService.getReviewByOrderId("o1");

        List<ReviewResponse> list = StreamSupport.stream(resp.getResult().spliterator(), false)
                .collect(Collectors.toList());
        assertEquals(1, list.size());
        assertEquals("Review by productId and orderId", resp.getMessage());
        assertEquals(200, resp.getCode());
    }

    // ===============================================================
    // TC-RV-015: Tính và cập nhật rating khi không có review (average null)
    // Mục tiêu: Khi calculateAverageRatingByProductId trả về null,
    //          product.rating được set null và save
    // Input: product.id = "pNull", repository trả về null
    // Expected: product.getRating() == null, productRepository.save được gọi
    // ===============================================================
    @Test
    public void testCalculateAndUpdateProductRating_NullAverage() {
        Product product = new Product(); product.setId("pNull");
        when(reviewRepository.calculateAverageRatingByProductId("pNull")).thenReturn(null);

        reviewService.calculateAndUpdateProductRating(product);

        assertNull(product.getRating());
        verify(productRepository).save(product);
    }

    // ===============================================================
    // TC-RV-016: Tính và cập nhật rating bình quân
    // Mục tiêu: Khi calculateAverageRatingByProductId trả về giá trị,
    //          product.rating được set đúng và save
    // Input: product.id = "pAvg", repository trả về 3.75
    // Expected: product.getRating() == 3.75, save được gọi
    // ===============================================================
    @Test
    public void testCalculateAndUpdateProductRating_WithValue() {
        Product product = new Product(); product.setId("pAvg");
        when(reviewRepository.calculateAverageRatingByProductId("pAvg")).thenReturn(3.75);

        reviewService.calculateAndUpdateProductRating(product);

        assertEquals(3.75, product.getRating());
        verify(productRepository).save(product);
    }

    // ===============================================================
    // TC-RV-017: Gán đúng các thuộc tính lên Review trước khi lưu
    // Mục tiêu: Đảm bảo createReview thiết lập đúng product, user, order, rating, content trên entity
    // Input: ReviewRequest hợp lệ với productId="p1", orderId="o1", rating=4.5, content="Nice"
    // Expected: reviewRepository.save được gọi với Review có các trường tương ứng
    // ===============================================================
    @Test
    public void testCreateReview_EntityFieldsSetCorrectly() {
        // Arrange
        User user = new User(); user.setId("u1");
        when(authService.getUserByToken()).thenReturn(user);
        Product product = new Product(); product.setId("p1");
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        Order order = new Order(); order.setId("o1"); order.setUser(user);
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.existOrderCompletedByUserId("u1", "p1")).thenReturn(true);

        ReviewRequest req = ReviewRequest.builder()
                .productId("p1")
                .orderId("o1")
                .rating(4.5)
                .content("Nice")
                .build();

        Review toSave = new Review();
        when(reviewMapper.toReview(req)).thenReturn(toSave);
        when(reviewRepository.save(any(Review.class))).thenReturn(toSave);
        when(reviewMapper.toReviewResponse(toSave)).thenReturn(ReviewResponse.builder().id("r1").build());
        when(reviewRepository.calculateAverageRatingByProductId("p1")).thenReturn(4.5);

        // Act
        reviewService.createReview(req);

        // Assert
        ArgumentCaptor<Review> cap = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(cap.capture());
        Review saved = cap.getValue();
        assertEquals(product, saved.getProduct());
        assertEquals(user, saved.getUser());
        assertEquals(order, saved.getOrder());
        assertEquals(4.5, saved.getRating());
        assertEquals("Nice", saved.getContent());
    }



}
