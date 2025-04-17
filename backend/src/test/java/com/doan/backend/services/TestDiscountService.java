package com.doan.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.doan.backend.dto.request.DiscountRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.DiscountResponse;
import com.doan.backend.entity.Discount;
import com.doan.backend.entity.UserDiscount;
import com.doan.backend.enums.DiscountType;
import com.doan.backend.mapper.DiscountMapper;
import com.doan.backend.repositories.DiscountRepository;
import com.doan.backend.repositories.UserDiscountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
public class TestDiscountService {

    // Mô phỏng các dependency cần thiết để test service
    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private UserDiscountRepository userDiscountRepository;

    @Mock
    private DiscountMapper discountMapper;

    @InjectMocks
    private DiscountService discountService;

    // ===============================================================
    // TC-DS-001: Tạo discount thành công với DiscountType.PERCENTAGE
    // Mục tiêu: Kiểm tra hàm createDiscount trả về ApiResponse với discount được tạo đúng
    // Input: DiscountRequest hợp lệ (discountType=PERCENTAGE, discountPercentage khác null, expiryDate hợp lệ)
    // Expected: ApiResponse chứa discount đã được save, với message và code 200
    // ===============================================================
    @Test
    public void testCreateDiscountSuccess_Percentage() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("DISCOUNT_PERCENT")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(new BigDecimal("10"))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .minOrderValue(new BigDecimal("100"))
                .maxUses(10)
                .autoApply(false)
                .startDate(LocalDateTime.now())
                .build();

        // Giả lập đối tượng Discount được chuyển đổi từ request
        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountPercentage(request.getDiscountPercentage());
        // Nếu discountValue không được set thì service tự set về ZERO
        discount.setDiscountValue(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("DISCOUNT_PERCENT")).thenReturn(false);
        when(discountRepository.save(discount)).thenReturn(discount);

        // Act
        ApiResponse<Discount> response = discountService.createDiscount(request);

        // Assert
        assertNotNull(response, "Response không được null");
        assertEquals(200, response.getCode(), "Mã phản hồi phải là 200");
        assertEquals("Discount created successfully", response.getMessage(), "Message phản hồi không đúng");
        assertEquals(discount, response.getResult(), "Discount trả về không khớp với mong đợi");
    }

    // ===============================================================
    // TC-DS-002: Tạo discount thành công với DiscountType.VALUE
    // Mục tiêu: Kiểm tra hàm createDiscount hoạt động đúng cho discount kiểu VALUE
    // Input: DiscountRequest hợp lệ (discountType=VALUE, discountValue khác null, discountPercentage có thể null)
    // Expected: ApiResponse chứa discount được tạo với discountValue đã có; nếu discountPercentage null -> được set ZERO
    // ===============================================================
    @Test
    public void testCreateDiscountSuccess_Value() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("DISCOUNT_VALUE")
                .discountType(DiscountType.VALUE)
                .discountValue(new BigDecimal("25"))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .minOrderValue(new BigDecimal("50"))
                .maxUses(5)
                .autoApply(true)
                .startDate(LocalDateTime.now())
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        // Nếu discountPercentage chưa có thì được set về ZERO
        discount.setDiscountPercentage(BigDecimal.ZERO);
        discount.setDiscountValue(request.getDiscountValue());

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("DISCOUNT_VALUE")).thenReturn(false);
        when(discountRepository.save(discount)).thenReturn(discount);

        // Act
        ApiResponse<Discount> response = discountService.createDiscount(request);

        // Assert
        assertNotNull(response, "Response không được null");
        assertEquals(200, response.getCode(), "Mã phản hồi phải bằng 200");
        assertEquals("Discount created successfully", response.getMessage(), "Message phản hồi không khớp");
        assertEquals(discount, response.getResult(), "Discount trả về không đúng");
    }

    // ===============================================================
    // TC-DS-003: Tạo discount thất bại khi mã discount đã tồn tại
    // Mục tiêu: Kiểm tra exception khi tồn tại mã discount trùng
    // Input: DiscountRequest với code đã tồn tại
    // Expected: RuntimeException với thông báo "Discount code already exists"
    // ===============================================================
    @Test
    public void testCreateDiscount_Failure_CodeAlreadyExists() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("DUPLICATE")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(new BigDecimal("15"))
                .expiryDate(LocalDateTime.now().plusDays(1))
                .minOrderValue(new BigDecimal("100"))
                .maxUses(10)
                .autoApply(false)
                .startDate(LocalDateTime.now())
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountPercentage(request.getDiscountPercentage());
        discount.setDiscountValue(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("DUPLICATE")).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.createDiscount(request);
        });
        assertEquals("Discount code already exists", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-004: Tạo discount thất bại khi expiryDate đã qua
    // Mục tiêu: Kiểm tra exception khi expiryDate nhỏ hơn thời điểm hiện tại
    // Input: DiscountRequest với expiryDate trước hiện tại
    // Expected: RuntimeException với thông báo "Expiry date is before current date"
    // ===============================================================
    @Test
    public void testCreateDiscount_Failure_ExpiryDateBeforeNow() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("EXPIRED")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(new BigDecimal("10"))
                .expiryDate(LocalDateTime.now().minusDays(1))
                .minOrderValue(new BigDecimal("100"))
                .maxUses(10)
                .autoApply(false)
                .startDate(LocalDateTime.now())
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountPercentage(request.getDiscountPercentage());
        discount.setDiscountValue(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("EXPIRED")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.createDiscount(request);
        });
        assertEquals("Expiry date is before current date", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-005: Tạo discount thất bại khi discountPercentage bị null với DiscountType.PERCENTAGE
    // Mục tiêu: Khi discountType là PERCENTAGE thì discountPercentage phải khác null.
    // Input: DiscountRequest với discountPercentage null
    // Expected: RuntimeException với thông báo "Discount percentage is required"
    // ===============================================================
    @Test
    public void testCreateDiscount_Failure_DiscountPercentageRequired() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("NO_PERCENT")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(null)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .minOrderValue(new BigDecimal("100"))
                .maxUses(10)
                .autoApply(true)
                .startDate(LocalDateTime.now())
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountPercentage(null);
        discount.setDiscountValue(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("NO_PERCENT")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.createDiscount(request);
        });
        assertEquals("Discount percentage is required", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-006: Tạo discount thất bại khi discountValue bị null với DiscountType.VALUE
    // Mục tiêu: Khi discountType là VALUE thì discountValue phải khác null.
    // Input: DiscountRequest với discountValue null
    // Expected: RuntimeException với thông báo "Discount value is required"
    // ===============================================================
    @Test
    public void testCreateDiscount_Failure_DiscountValueRequired() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("NO_VALUE")
                .discountType(DiscountType.VALUE)
                .discountValue(null)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .minOrderValue(new BigDecimal("50"))
                .maxUses(5)
                .autoApply(true)
                .startDate(LocalDateTime.now())
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        // Với kiểu VALUE, nếu discountValue null thì exception sẽ xảy ra (không set default)
        discount.setDiscountValue(null);
        discount.setDiscountPercentage(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("NO_VALUE")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.createDiscount(request);
        });
        assertEquals("Discount value is required", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-007: Cập nhật discount thành công
    // Mục tiêu: Kiểm tra cập nhật discount với discountId hợp lệ và DiscountRequest chứa thông tin cập nhật
    // Input: discountId hợp lệ và DiscountRequest với thông tin mới (ví dụ cập nhật code)
    // Expected: ApiResponse trả về discount được cập nhật với message "Discount updated successfully"
    // ===============================================================
    @Test
    public void testUpdateDiscountSuccess() {
        // Arrange
        String discountId = "discount123";
        DiscountRequest updateRequest = DiscountRequest.builder()
                .code("UPDATED_CODE")
                .minOrderValue(new BigDecimal("150"))
                .build(); // chỉ cập nhật một số trường

        Discount existingDiscount = new Discount();
        existingDiscount.setId(discountId);
        existingDiscount.setCode("OLD_CODE");
        existingDiscount.setDiscountType(DiscountType.PERCENTAGE);
        existingDiscount.setDiscountPercentage(new BigDecimal("10"));
        existingDiscount.setDiscountValue(BigDecimal.ZERO);

        when(discountRepository.findById(discountId)).thenReturn(Optional.of(existingDiscount));
        when(discountRepository.save(existingDiscount)).thenReturn(existingDiscount);

        // Act
        ApiResponse<Discount> response = discountService.updateDiscount(discountId, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discount updated successfully", response.getMessage());
        assertEquals("UPDATED_CODE", existingDiscount.getCode());
    }

    // ===============================================================
    // TC-DS-008: Cập nhật discount thất bại khi discount không tồn tại
    // Mục tiêu: Nếu discountId không tồn tại, updateDiscount ném exception "Discount not found"
    // Input: discountId không tồn tại
    // Expected: RuntimeException với thông báo "Discount not found"
    // ===============================================================
    @Test
    public void testUpdateDiscount_Failure_NotFound() {
        // Arrange
        String discountId = "nonexistent";
        DiscountRequest updateRequest = DiscountRequest.builder().build();

        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.updateDiscount(discountId, updateRequest);
        });
        assertEquals("Discount not found", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-009: Xóa discount thành công
    // Mục tiêu: Kiểm tra deleteDiscount gọi deleteById và trả về ApiResponse thông báo thành công
    // Input: discountId hợp lệ
    // Expected: ApiResponse với code 200 và message "Discount deleted  successfully"
    // ===============================================================
    @Test
    public void testDeleteDiscount() {
        // Arrange
        String discountId = "del123";
        doNothing().when(discountRepository).deleteById(discountId);

        // Act
        ApiResponse<Void> response = discountService.deleteDiscount(discountId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discount deleted successfully", response.getMessage());
        verify(discountRepository, times(1)).deleteById(discountId);
    }

    // ===============================================================
    // TC-DS-010: Tìm kiếm discount theo code sử dụng phân trang
    // Mục tiêu: getDiscountSearchByCode trả về danh sách discount chứa code phù hợp
    // Input: code "TEST", pageable (trang 0, size 10)
    // Expected: ApiResponse chứa Page<Discount> với discount phù hợp
    // ===============================================================
    @Test
    public void testGetDiscountSearchByCode() {
        // Arrange
        String searchCode = "TEST";
        Pageable pageable = PageRequest.of(0, 10);
        Discount discount = new Discount();
        discount.setCode("TEST123");
        List<Discount> list = Collections.singletonList(discount);
        Page<Discount> pageResult = new PageImpl<>(list, pageable, list.size());

        when(discountRepository.findByCodeContaining(searchCode, pageable)).thenReturn(pageResult);

        // Act
        ApiResponse<Page<Discount>> response = discountService.getDiscountSearchByCode(searchCode, pageable);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discount retrieved successfully", response.getMessage());
        assertEquals(pageResult, response.getResult());
    }

    // ===============================================================
    // TC-DS-011: Lấy discount thành công khi người dùng chưa sử dụng discount
    // Mục tiêu: Khi user chưa dùng discount và discount còn hiệu lực, getDiscount trả về discount phù hợp.
    // Input: code "DISCOUNT1", userId "user1", discount có startDate <= now, expiryDate >= now, usedCount < maxUses
    // Expected: ApiResponse với discount và message "Discount retrieved successfully"
    // ===============================================================
    @Test
    public void testGetDiscountSuccess() {
        // Arrange
        String code = "DISCOUNT1";
        String userId = "user1";
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setMaxUses(5);
        discount.setUsedCount(2);

        when(userDiscountRepository.findByUserIdAndDiscount_Code(userId, code))
                .thenReturn(Optional.empty());
        when(discountRepository.findByCodeAndExpiryDateAfter(eq(code), any(LocalDateTime.class)))
                .thenReturn(Optional.of(discount));

        // Act
        ApiResponse<Discount> response = discountService.getDiscount(code, userId);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discount retrieved successfully", response.getMessage());
        assertEquals(discount, response.getResult());
    }

    // ===============================================================
    // TC-DS-012: Lỗi khi người dùng đã sử dụng discount
    // Mục tiêu: Nếu người dùng đã dùng discount, getDiscount ném exception "User has already used this discount."
    // Input: userDiscount đã có usesCount > 0 cho discount với code "DISCOUNT1" và userId "user1"
    // Expected: RuntimeException với thông báo "User has already used this discount."
    // ===============================================================
    @Test
    public void testGetDiscount_Failure_UserAlreadyUsed() {
        // Arrange
        String code = "DISCOUNT1";
        String userId = "user1";
        UserDiscount userDiscount = new UserDiscount();
        userDiscount.setUsesCount(1);  // Đã sử dụng discount

        when(userDiscountRepository.findByUserIdAndDiscount_Code(userId, code))
                .thenReturn(Optional.of(userDiscount));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.getDiscount(code, userId);
        });
        assertEquals("User has already used this discount.", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-013: Lỗi khi discount đã đạt tối đa lượt sử dụng
    // Mục tiêu: Nếu discount.usedCount >= discount.maxUses, getDiscount ném exception "Discount has reached its maximum uses"
    // Input: discount có usedCount = maxUses
    // Expected: RuntimeException với thông báo "Discount has reached its maximum uses"
    // ===============================================================
    @Test
    public void testGetDiscount_Failure_MaxUsesReached() {
        // Arrange
        String code = "DISCOUNT1";
        String userId = "user1";
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setStartDate(LocalDateTime.now().minusDays(1));
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setMaxUses(5);
        discount.setUsedCount(5);

        when(userDiscountRepository.findByUserIdAndDiscount_Code(userId, code))
                .thenReturn(Optional.empty());
        when(discountRepository.findByCodeAndExpiryDateAfter(eq(code), any(LocalDateTime.class)))
                .thenReturn(Optional.of(discount));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.getDiscount(code, userId);
        });
        assertEquals("Discount has reached its maximum uses", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-014: Lỗi khi discount không hợp lệ về thời gian (startDate > now)
    // Mục tiêu: Nếu discount chưa bắt đầu (startDate > now) thì getDiscount ném exception "Discount is not yet valid"
    // Input: discount có startDate trong tương lai
    // Expected: RuntimeException với thông báo "Discount is not yet valid"
    // ===============================================================
    @Test
    public void testGetDiscount_Failure_InvalidDate() {
        // Arrange
        String code = "DISCOUNT1";
        String userId = "user1";
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setStartDate(LocalDateTime.now().plusDays(1));  // chưa bắt đầu
        discount.setExpiryDate(LocalDateTime.now().plusDays(2));
        discount.setMaxUses(5);
        discount.setUsedCount(0);

        when(userDiscountRepository.findByUserIdAndDiscount_Code(userId, code))
                .thenReturn(Optional.empty());
        when(discountRepository.findByCodeAndExpiryDateAfter(eq(code), any(LocalDateTime.class)))
                .thenReturn(Optional.of(discount));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.getDiscount(code, userId);
        });
        assertEquals("Discount is not yet valid", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-015: Lấy danh sách discount auto-apply
    // Mục tiêu: getAllDiscountsByAutoApply trả về danh sách discount autoApply mà user chưa sử dụng và vẫn còn lượt sử dụng
    // Input: userId và danh sách discount autoApply trong DB
    // Expected: ApiResponse trả về danh sách discount phù hợp (trong ví dụ nếu user đã dùng hết thì trả về danh sách trống)
    // ===============================================================
    @Test
    public void testGetAllDiscountsByAutoApply() {
        // Arrange
        String userId = "user123";
        Discount d1 = new Discount();
        d1.setCode("AUTO1");
        d1.setAutoApply(true);
        d1.setExpiryDate(LocalDateTime.now().plusDays(1));
        d1.setMaxUses(5);
        d1.setUsedCount(1);

        Discount d2 = new Discount();
        d2.setCode("AUTO2");
        d2.setAutoApply(true);
        d2.setExpiryDate(LocalDateTime.now().plusDays(1));
        d2.setMaxUses(5);
        d2.setUsedCount(5); // hết lượt

        Iterable<Discount> autoDiscounts = Arrays.asList(d1, d2);
        when(discountRepository.findByAutoApplyTrueAndExpiryDateAfter(any(LocalDateTime.class)))
                .thenReturn(autoDiscounts);

        // Giả lập user đã dùng discount d1
        UserDiscount userDisc = new UserDiscount();
        userDisc.setUsesCount(1);
        userDisc.setDiscount(d1);
        when(userDiscountRepository.findByUserId(userId))
                .thenReturn(Collections.singletonList(userDisc));

        // Act
        ApiResponse<Iterable<Discount>> response = discountService.getAllDiscountsByAutoApply(userId);

        // Assert: Dù có 2 discount autoApply, vì user đã dùng d1 và d2 hết lượt nên danh sách trả về sẽ rỗng.
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discounts retrieved successfully", response.getMessage());
        List<Discount> resultList = (List<Discount>) response.getResult();
        assertTrue(resultList.isEmpty(), "Danh sách discount autoApply phải rỗng nếu user đã dùng hết");
    }

    // ===============================================================
    // TC-DS-016: Lấy danh sách discount hiện hành
    // Mục tiêu: getCurrentDiscount lấy danh sách discount theo thời gian hiện tại
    // Input: Không cần input đặc biệt
    // Expected: ApiResponse trả về danh sách DiscountResponse với message "Discounts retrieved successfully"
    // ===============================================================
    @Test
    public void testGetCurrentDiscount() {
        // Arrange
        Discount discount = new Discount();
        discount.setCode("CURRENT");
        Iterable<Discount> discountList = Collections.singletonList(discount);
        when(discountRepository.getCurrentDiscounts(any(LocalDateTime.class)))
                .thenReturn(discountList);

        DiscountResponse dr = DiscountResponse.builder()
                .code("CURRENT")
                .build();
        Iterable<DiscountResponse> responseList = Collections.singletonList(dr);
        when(discountMapper.toDiscountResponseIterable(discountList)).thenReturn(responseList);

        // Act
        ApiResponse<Iterable<DiscountResponse>> response = discountService.getCurrentDiscount();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discounts retrieved successfully", response.getMessage());
        assertEquals(responseList, response.getResult());
    }

    // ===============================================================
    // TC-DS-017: Lỗi khi discount không được tìm thấy
    // Mục tiêu: Nếu discountRepository.findByCodeAndExpiryDateAfter trả về Optional.empty(), hàm getDiscount sẽ ném exception "Discount not found"
    // Input: code "NOT_FOUND", userId "user1"
    // Expected: RuntimeException với thông báo "Discount not found"
    // ===============================================================
    @Test
    public void testGetDiscount_Failure_DiscountNotFound() {
        // Arrange
        String code = "NOT_FOUND";
        String userId = "user1";
        when(userDiscountRepository.findByUserIdAndDiscount_Code(userId, code))
                .thenReturn(Optional.empty());
        when(discountRepository.findByCodeAndExpiryDateAfter(eq(code), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.getDiscount(code, userId);
        });
        assertEquals("Discount not found", ex.getMessage());
    }

    // ===============================================================
    // TC-DS-018: Tìm kiếm discount với code null
    // Mục tiêu: Khi tham số code = null, hàm getDiscountSearchByCode trả về tất cả discount.
    // Input: code null, pageable (trang 0, size 10)
    // Expected: ApiResponse chứa Page<Discount> trả về tất cả discount, với message "Discount retrieved successfully"
    // ===============================================================
    @Test
    public void testGetDiscountSearchByCode_NullCode() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Discount discount = new Discount();
        discount.setCode("ALL_DISCOUNT");
        List<Discount> list = Collections.singletonList(discount);
        Page<Discount> pageResult = new PageImpl<>(list, pageable, list.size());
        when(discountRepository.findByCodeContaining(null, pageable)).thenReturn(pageResult);
        // Act
        ApiResponse<Page<Discount>> response = discountService.getDiscountSearchByCode(null, pageable);
        // Assert
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("Discount retrieved successfully", response.getMessage());
        assertEquals(pageResult, response.getResult());
    }

    // ===============================================================
    // TC-DS-019: Lấy danh sách discount autoApply khi user chưa sử dụng discount nào
    // Mục tiêu: Khi user chưa sử dụng discount nào, hàm getAllDiscountsByAutoApply sẽ trả về danh sách discount autoApply khả dụng.
    // Input: userId "newUser", trong DB có discount autoApply chưa được dùng.
    // Expected: ApiResponse chứa danh sách discount autoApply có sẵn.
    // ===============================================================
    @Test
    public void testGetAllDiscountsByAutoApply_UserNotUsedAny() {
        // Arrange
        String userId = "newUser";
        Discount discount = new Discount();
        discount.setCode("AUTO_AVAILABLE");
        discount.setAutoApply(true);
        discount.setExpiryDate(LocalDateTime.now().plusDays(1));
        discount.setMaxUses(5);
        discount.setUsedCount(2);
        // Trả về discount từ repository
        when(discountRepository.findByAutoApplyTrueAndExpiryDateAfter(any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(discount));
        // Giả lập rằng user chưa sử dụng discount nào
        when(userDiscountRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        // Act
        ApiResponse<Iterable<Discount>> response = discountService.getAllDiscountsByAutoApply(userId);
        // Assert: danh sách discount autoApply phải chứa discount khả dụng
        List<Discount> resultList = (List<Discount>) response.getResult();
        assertFalse(resultList.isEmpty(), "Danh sách discount autoApply không được rỗng khi user chưa sử dụng discount");
        assertEquals(1, resultList.size());
        assertEquals("AUTO_AVAILABLE", resultList.get(0).getCode());
    }


    // ===============================================================
    // TC-DS-020: Kiểm tra thất bại khi lấy Discount với ngày hết hạn bằng thời điểm hiện tại
    // Mục tiêu: Xác minh rằng phương thức getDiscount ném ra ngoại lệ khi ngày hết hạn của discount đúng bằng thời điểm hiện tại, vì logic sử dụng .isAfter(now) và loại trừ các discount hết hạn tại thời điểm hiện tại.
    // Input: Mã discount "EXACT_EXPIRY", userId "user1", discount có ngày hết hạn bằng thời điểm hiện tại.
    // Expected: Một RuntimeException được ném ra với thông báo "Discount not found".
    // ===============================================================
    @Test
    public void testGetDiscount_Failure_ExactExpiry() {
        // Arrange
        String code = "EXACT_EXPIRY";
        String userId = "user1";
        LocalDateTime now = LocalDateTime.now();
        Discount discount = new Discount();
        discount.setCode(code);
        discount.setStartDate(now.minusDays(1)); // discount đã bắt đầu
        discount.setExpiryDate(now);             // expiryDate = now
        discount.setMaxUses(5);
        discount.setUsedCount(0);

        // Giả lập rằng người dùng chưa sử dụng discount
        when(userDiscountRepository.findByUserIdAndDiscount_Code(userId, code)).thenReturn(Optional.empty());
        // Vì expiryDate equals now và logic dùng .isAfter(now), repository trả về Optional.empty()
        when(discountRepository.findByCodeAndExpiryDateAfter(eq(code), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.getDiscount(code, userId);
        });
        assertEquals("Discount not found", ex.getMessage(), "Nếu expiryDate bằng now, discount sẽ không được trả về");
    }

    // ===============================================================
    // TC-DS-021: Kiểm tra thất bại khi tạo Discount với phần trăm giảm giá bằng 0
    // Mục tiêu: Đảm bảo rằng phương thức createDiscount ném ra ngoại lệ khi cố gắng tạo một discount với giá trị phần trăm giảm bằng 0, vì logic nghiệp vụ yêu cầu phần trăm giảm phải là số dương.
    // Input: Một DiscountRequest với mã "ZERO_PERCENT", loại discount là PERCENTAGE, và phần trăm giảm giá được đặt bằng 0.
    // Expected: Một RuntimeException được ném ra với thông báo "Discount percentage must be greater than 0".
    // ===============================================================
    @Test
    public void testCreateDiscount_Failure_DiscountPercentageZero() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("ZERO_PERCENT")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(BigDecimal.ZERO)  // Giá trị giảm = 0
                .expiryDate(LocalDateTime.now().plusDays(1))
                .minOrderValue(new BigDecimal("100"))
                .maxUses(10)
                .autoApply(false)
                .startDate(LocalDateTime.now())
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountPercentage(BigDecimal.ZERO);
        // Nếu không được xử lý, discountValue được set về ZERO theo mặc định
        discount.setDiscountValue(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("ZERO_PERCENT")).thenReturn(false);

        // Act & Assert: Giả sử nghiệp vụ yêu cầu discountPercentage phải > 0.
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.createDiscount(request);
        });
        assertEquals("Discount percentage must be greater than 0", ex.getMessage(),
                "Discount percentage bằng 0 không hợp lệ theo nghiệp vụ");
    }

    // ===============================================================
    // TC-DS-022: Kiểm tra thất bại khi tạo Discount với ngày hết hạn trước ngày bắt đầu
    // Mục tiêu: Xác nhận rằng phương thức createDiscount ném ra ngoại lệ khi ngày hết hạn của discount được đặt trước ngày bắt đầu, vi phạm quy tắc nghiệp vụ rằng ngày bắt đầu phải trước ngày hết hạn.
    // Input: Một DiscountRequest với mã "INVALID_DATE", ngày hết hạn đặt là hôm nay, và ngày bắt đầu đặt là ngày mai.
    // Expected: Một RuntimeException được ném ra với thông báo "Expiry date is before current date".
    // ===============================================================
    @Test
    public void testCreateDiscount_Failure_InvalidDateOrder() {
        // Arrange
        DiscountRequest request = DiscountRequest.builder()
                .code("INVALID_DATE")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(new BigDecimal("10"))
                // expiryDate là hôm nay, nhưng startDate lại là ngày mai
                .expiryDate(LocalDateTime.now())
                .minOrderValue(new BigDecimal("100"))
                .maxUses(10)
                .autoApply(false)
                .startDate(LocalDateTime.now().plusDays(1))
                .build();

        Discount discount = new Discount();
        discount.setCode(request.getCode());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountPercentage(request.getDiscountPercentage());
        discount.setDiscountValue(BigDecimal.ZERO);

        when(discountMapper.toDiscount(request)).thenReturn(discount);
        when(discountRepository.existsByCode("INVALID_DATE")).thenReturn(false);

        // Act & Assert: Ném exception vì startDate > expiryDate
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            discountService.createDiscount(request);
        });
        assertEquals("Expiry date is before current date", ex.getMessage(),
                "Ngày bắt đầu phải trước ngày hết hạn");
    }












}

