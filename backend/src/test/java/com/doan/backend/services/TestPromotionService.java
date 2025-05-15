package com.doan.backend.services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import com.doan.backend.dto.request.PromotionRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.PromotionResponse;
import com.doan.backend.entity.Promotion;
import com.doan.backend.entity.Product;
import com.doan.backend.mapper.PromotionMapper;
import com.doan.backend.repositories.PromotionProductRepository;
import com.doan.backend.repositories.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
public class TestPromotionService {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionProductRepository promotionProductRepository;

    @Mock
    private PromotionMapper promotionMapper;

    @InjectMocks
    private PromotionService promotionService;

    // ===============================================================
    // TC-PS-001: Tạo promotion thành công
    // Mục tiêu: Đảm bảo createPromotion lưu entity và trả về ApiResponse đúng
    // Input: Một PromotionRequest hợp lệ
    // Expected: ApiResponse với code 200, message "Promotion created successfully",
    //           và PromotionResponse trùng khớp
    // ===============================================================
    @Test
    public void testCreatePromotionSuccess() {
        // Arrange
        PromotionRequest req = PromotionRequest.builder()
                .name("New Year Sale")
                .description("10% off")
                .discountPercentage(new BigDecimal("10"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .applyToAll(true)
                .isActive(true)
                .build();

        Promotion promoEntity = new Promotion();
        promoEntity.setName(req.getName());
        promoEntity.setDescription(req.getDescription());
        promoEntity.setDiscountPercentage(req.getDiscountPercentage());

        PromotionResponse resp = PromotionResponse.builder()
                .name(req.getName())
                .description(req.getDescription())
                .discountPercentage(req.getDiscountPercentage())
                .build();

        when(promotionMapper.toPromotion(req)).thenReturn(promoEntity);
        when(promotionRepository.save(promoEntity)).thenReturn(promoEntity);
        when(promotionMapper.toPromotionResponse(promoEntity)).thenReturn(resp);

        // Act
        ApiResponse<PromotionResponse> result = promotionService.createPromotion(req);

        // Assert
        assertNotNull(result);
        assertEquals(201, result.getCode());
        assertEquals("Promotion created successfully", result.getMessage());
        assertEquals(resp, result.getResult());
    }

    // ===============================================================
    // TC-PS-002: Cập nhật promotion thành công
    // Mục tiêu: Đảm bảo updatePromotion cập nhật đúng các trường
    // Input: Một id tồn tại và PromotionRequest chứa thông tin mới
    // Expected: ApiResponse với code 200, message "Promotion update successfully",
    //           và PromotionResponse trùng khớp
    // ===============================================================
    @Test
    public void testUpdatePromotionSuccess() {
        // Arrange
        String id = "promo123";
        PromotionRequest req = PromotionRequest.builder()
                .name("Spring Sale")
                .description("15% off")
                .discountPercentage(new BigDecimal("15"))
                .startDate(LocalDateTime.now().minusDays(2))
                .endDate(LocalDateTime.now().plusDays(2))
                .applyToAll(false)
                .isActive(true)
                .build();

        Promotion existing = new Promotion();
        existing.setId(id);

        PromotionResponse resp = PromotionResponse.builder()
                .id(id)
                .name(req.getName())
                .description(req.getDescription())
                .discountPercentage(req.getDiscountPercentage())
                .build();

        when(promotionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(promotionMapper.toPromotionResponse(existing)).thenReturn(resp);

        // Act
        ApiResponse<PromotionResponse> result = promotionService.updatePromotion(id, req);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Promotion update successfully", result.getMessage());
        assertEquals(resp, result.getResult());
        // verify that fields were set
        assertEquals("Spring Sale", existing.getName());
        assertEquals("15% off", existing.getDescription());
        assertEquals(req.getDiscountPercentage(), existing.getDiscountPercentage());
    }

    // ===============================================================
    // TC-PS-003: Cập nhật promotion thất bại khi id không tồn tại
    // Mục tiêu: Khi id không tìm thấy, ném RuntimeException "Promotion not found"
    // Input: id không tồn tại
    // Expected: RuntimeException với thông báo "Promotion not found"
    // ===============================================================
    @Test
    public void testUpdatePromotion_Failure_NotFound() {
        // Arrange
        String id = "notExist";
        when(promotionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            promotionService.updatePromotion(id, PromotionRequest.builder().build());
        });
        assertEquals("Promotion not found", ex.getMessage());
    }

    // ===============================================================
    // TC-PS-004: Xóa promotion thành công
    // Mục tiêu: Đảm bảo deletePromotion gọi repository và trả về ApiResponse đúng
    // Input: Một id hợp lệ
    // Expected: ApiResponse với code 200, message "Promotion deleted successfully"
    // ===============================================================
    @Test
    public void testDeletePromotionSuccess() {
        // Arrange
        String id = "promoDel";
        doNothing().when(promotionRepository).deleteById(id);

        // Act
        ApiResponse<Void> result = promotionService.deletePromotion(id);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Promotion deleted successfully", result.getMessage());
        verify(promotionRepository).deleteById(id);
    }

    // ===============================================================
    // TC-PS-005: Lấy promotion theo id thành công
    // Mục tiêu: Đảm bảo getPromotionById trả về PromotionResponse khi tồn tại
    // Input: Một id hợp lệ
    // Expected: ApiResponse với code 200, message "Promotion retrieved successfully",
    //           và PromotionResponse trùng khớp
    // ===============================================================
    @Test
    public void testGetPromotionByIdSuccess() {
        // Arrange
        String id = "promoGet";
        Promotion entity = new Promotion();
        entity.setId(id);
        PromotionResponse resp = PromotionResponse.builder().id(id).build();

        when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));
        when(promotionMapper.toPromotionResponse(entity)).thenReturn(resp);

        // Act
        ApiResponse<PromotionResponse> result = promotionService.getPromotionById(id);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Promotion retrieved successfully", result.getMessage());
        assertEquals(resp, result.getResult());
    }

    // ===============================================================
    // TC-PS-006: Lấy promotion theo id thất bại khi không tồn tại
    // Mục tiêu: Khi id không tìm thấy, ném RuntimeException "Promotion not found"
    // Input: id không tồn tại
    // Expected: RuntimeException với thông báo "Promotion not found"
    // ===============================================================
    @Test
    public void testGetPromotionById_Failure_NotFound() {
        // Arrange
        String id = "noPromo";
        when(promotionRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            promotionService.getPromotionById(id);
        });
        assertEquals("Promotion not found", ex.getMessage());
    }

    // ===============================================================
    // TC-PS-007: Lấy tất cả promotions theo tên filter
    // Mục tiêu: getAllPromotions trả về Page<PromotionResponse> tương ứng
    // Input: name = "Sale", pageable (0,10)
    // Expected: ApiResponse với code 200, message "Promotions retrieved successfully",
    //           và Page chứa đúng số lượng promotionResponses
    // ===============================================================
    @Test
    public void testGetAllPromotions_WithNameFilter() {
        // Arrange
        String name = "Sale";
        Pageable pageable = PageRequest.of(0, 10);
        Promotion p = new Promotion();
        p.setName("Sale Event");
        List<Promotion> list = Collections.singletonList(p);
        Page<Promotion> page = new PageImpl<>(list, pageable, 1);
        PromotionResponse resp = PromotionResponse.builder().name("Sale Event").build();
        Page<PromotionResponse> mappedPage = page.map(pr -> resp);

        when(promotionRepository.findByNameContaining(name, pageable)).thenReturn(page);

        // Act
        ApiResponse<Page<PromotionResponse>> result = promotionService.getAllPromotions(name, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Promotions retrieved successfully", result.getMessage());
        assertEquals(mappedPage.getContent().size(), result.getResult().getContent().size());
    }

    // ===============================================================
    // TC-PS-008: Lấy tất cả promotions khi name = null
    // Mục tiêu: Khi name null, findByNameContaining trả về tất cả promotions
    // Input: name = null, pageable (0,10)
    // Expected: ApiResponse với Page chứa tất cả promotions
    // ===============================================================
    @Test
    public void testGetAllPromotions_NameNull() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Promotion p = new Promotion();
        p.setName("Any");
        List<Promotion> list = Collections.singletonList(p);
        Page<Promotion> page = new PageImpl<>(list, pageable, 1);

        when(promotionRepository.findByNameContaining(null, pageable)).thenReturn(page);

        // Act
        ApiResponse<Page<PromotionResponse>> result = promotionService.getAllPromotions(null, pageable);

        // Assert
        assertEquals(1, result.getResult().getContent().size());
        assertEquals(200, result.getCode());
    }

    // ===============================================================
    // TC-PS-009: Áp dụng promotion vào product thành công
    // Mục tiêu: applyPromotionToProduct trả về giá đã trừ đúng theo discountPercentage
    // Input: product.price = 100, repository trả về Promotion với discountPercentage = 10
    // Expected: Giá trả về = 90
    // ===============================================================
    @Test
    public void testApplyPromotionToProduct_Success() {
        // Arrange
        Product prod = new Product();
        prod.setId("prod1");
        prod.setPrice(new BigDecimal("100"));

        Promotion promo = new Promotion();
        promo.setDiscountPercentage(new BigDecimal("10"));

        when(promotionProductRepository
                .findActivePromotionByProductId(eq("prod1"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(promo));

        // Act
        BigDecimal result = promotionService.applyPromotionToProduct(prod);

        // Assert
        assertEquals(new BigDecimal("90.0"), result);
    }

    // ===============================================================
    // TC-PS-010: Áp dụng promotion thất bại khi không có promotion nào
    // Mục tiêu: Khi repository không trả về promotion, giá không thay đổi
    // Input: product.price = 100, repository trả về Optional.empty()
    // Expected: Giá trả về = 100
    // ===============================================================
    @Test
    public void testApplyPromotionToProduct_NoPromotion() {
        // Arrange
        Product prod = new Product();
        prod.setId("prod2");
        prod.setPrice(new BigDecimal("100"));

        when(promotionProductRepository
                .findActivePromotionByProductId(eq("prod2"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        // Act
        BigDecimal result = promotionService.applyPromotionToProduct(prod);

        // Assert
        assertEquals(new BigDecimal("100"), result);
    }

    // ===============================================================
    // TC-PS-011: Lấy promotions hiện hành exclude applyToAll có dữ liệu
    // Mục tiêu: getCurrentPromotionsExcludeApplyToAll trả về danh sách PromotionResponse
    // Input: repository trả về List<Promotion> có 1 item
    // Expected: ApiResponse với code 200, message và danh sách size = 1
    // ===============================================================
    @Test
    public void testGetCurrentPromotionsExcludeApplyToAll_Success() {
        // Arrange
        Promotion p = new Promotion();
        p.setName("Exclusive");
        List<Promotion> list = Collections.singletonList(p);
        PromotionResponse resp = PromotionResponse.builder().name("Exclusive").build();

        when(promotionRepository.findActiveCurrentPromotionsExcludeApplyToAll(any(LocalDateTime.class)))
                .thenReturn(list);
        when(promotionMapper.toPromotionResponse(p)).thenReturn(resp);

        // Act
        ApiResponse<List<PromotionResponse>> result =
                promotionService.getCurrentPromotionsExcludeApplyToAll();

        // Assert
        assertEquals(200, result.getCode());
        assertEquals(1, result.getResult().size());
        assertEquals("Exclusive", result.getResult().get(0).getName());
    }

    // ===============================================================
    // TC-PS-012: Lấy promotions hiện hành exclude applyToAll trả về rỗng
    // Mục tiêu: Khi repository trả về danh sách rỗng, ApiResponse.result cũng rỗng
    // Input: repository trả về empty list
    // Expected: ApiResponse result size = 0
    // ===============================================================
    @Test
    public void testGetCurrentPromotionsExcludeApplyToAll_Empty() {
        // Arrange
        when(promotionRepository.findActiveCurrentPromotionsExcludeApplyToAll(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<PromotionResponse>> result =
                promotionService.getCurrentPromotionsExcludeApplyToAll();

        // Assert
        assertTrue(result.getResult().isEmpty());
        assertEquals(200, result.getCode());
    }


    // ===============================================================
    // TC-PS-013: Áp dụng promotion khi endDate == now
    // Mục tiêu: Đảm bảo rằng nếu một Promotion có endDate chính xác bằng thời điểm hiện tại,
    //          applyPromotionToProduct vẫn tính giảm giá đúng.
    // Input: Product.price = 200, repository trả về Promotion.discountPercentage = 50,
    //        Promotion.endDate = LocalDateTime.now()
    // Expected: Giá trả về = 100 (200 - 50%)
    // ===============================================================
    @Test
    public void testApplyPromotionToProduct_ExactEndDate() {
        // Arrange
        Product prod = new Product();
        prod.setId("p");
        prod.setPrice(new BigDecimal("200"));
        Promotion promo = new Promotion();
        promo.setDiscountPercentage(new BigDecimal("50"));
        // Giả lập repository trả về promo có endDate = now
        when(promotionProductRepository.findActivePromotionByProductId(eq("p"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(promo));

        // Act
        BigDecimal result = promotionService.applyPromotionToProduct(prod);

        // Assert
        assertEquals(new BigDecimal("100.0"), result);
    }

    // ===============================================================
    // TC-PS-014: Không áp dụng promotion khi isActive = false
    // Mục tiêu: Đảm bảo rằng một Promotion inactive (isActive=false) không được áp dụng
    // Input: Product.price = 300, repository trả về Promotion.isActive = false
    // Expected: Giá trả về = 300 (không thay đổi)
    // ===============================================================
    @Test
    public void testApplyPromotionToProduct_InactivePromotion() {
        // Arrange
        Product prod = new Product();
        prod.setId("x");
        prod.setPrice(new BigDecimal("300"));
        Promotion promo = new Promotion();
        promo.setDiscountPercentage(new BigDecimal("20"));
        promo.setIsActive(false);
        // findActivePromotionByProductId chỉ trả về promo active, nhưng giả lập nhầm
        when(promotionProductRepository.findActivePromotionByProductId(eq("x"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(promo));

        // Act
        BigDecimal result = promotionService.applyPromotionToProduct(prod);

        // Assert
        assertEquals(new BigDecimal("300"), result);
    }

    // ===============================================================
    // TC-PS-015: Chọn promotion mới nhất khi có nhiều promotion
    // Mục tiêu: Khi repository có nhiều Promotion, applyPromotionToProduct phải lấy Promotion mới nhất
    // Input: Product.price = 100, repository trả về Optional.of(latestPromotion)
    // Expected: Giá trả về = 80 (100 - 20%), với latestPromotion.discountPercentage = 20
    // ===============================================================
    @Test
    public void testApplyPromotionToProduct_PicksMostRecent() {
        // Arrange
        Product prod = new Product();
        prod.setId("p2");
        prod.setPrice(new BigDecimal("100"));
        Promotion oldP = new Promotion();
        oldP.setDiscountPercentage(new BigDecimal("10"));
        oldP.setStartDate(LocalDateTime.now().minusDays(5));
        Promotion newP = new Promotion();
        newP.setDiscountPercentage(new BigDecimal("20"));
        newP.setStartDate(LocalDateTime.now().minusDays(1));
        // Giả lập repository luôn trả newP (mới nhất)
        when(promotionProductRepository.findActivePromotionByProductId(eq("p2"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(newP));

        // Act
        BigDecimal result = promotionService.applyPromotionToProduct(prod);

        // Assert
        assertEquals(new BigDecimal("80.0"), result);
    }

    // ===============================================================
    // TC-PS-016: Lấy promotions hiện hành exclude applyToAll chỉ trả về promotions riêng
    // Mục tiêu: Đảm bảo getCurrentPromotionsExcludeApplyToAll chỉ trả về Promotion.applyToAll=false
    // Input: repository trả về danh sách chứa cả applyToAll=true và applyToAll=false
    // Expected: ApiResponse.result chỉ gồm các PromotionResponse với applyToAll=false
    // ===============================================================
    @Test
    public void testGetCurrentPromotionsExcludeApplyToAll_FiltersApplyToAll() {
        // Arrange
        Promotion pAll = new Promotion();
        pAll.setApplyToAll(true);
        pAll.setIsActive(true);
        Promotion pSingle = new Promotion();
        pSingle.setApplyToAll(false);
        pSingle.setIsActive(true);
        List<Promotion> mix = Arrays.asList(pAll, pSingle);
        when(promotionRepository.findActiveCurrentPromotionsExcludeApplyToAll(any(LocalDateTime.class)))
                .thenReturn(mix);
        when(promotionMapper.toPromotionResponse(pAll))
                .thenReturn(PromotionResponse.builder().applyToAll(true).build());
        when(promotionMapper.toPromotionResponse(pSingle))
                .thenReturn(PromotionResponse.builder().applyToAll(false).build());

        // Act
        List<PromotionResponse> result = promotionService.getCurrentPromotionsExcludeApplyToAll().getResult();

        // Assert
        // Theo query, chỉ pSingle (applyToAll=false) đúng, nên result chỉ chứa 1 phần tử
        assertEquals(1, result.size());
        assertFalse(result.get(0).getApplyToAll());
    }

    // ===============================================================
    // TC-PS-017: Repository trả về null khiến applyPromotionToProduct => NullPointerException
    // Mục tiêu: Kiểm tra tình huống không mong muốn khi repository trả về null thay vì Optional.empty()
    // Input: repository trả về null
    // Expected: Phát hiện NullPointerException (biết rằng code cần bảo vệ null)
    // ===============================================================
    @Test
    public void testApplyPromotionToProduct_RepositoryReturnsNull() {
        // Arrange
        Product prod = new Product();
        prod.setId("err");
        prod.setPrice(new BigDecimal("100"));
        when(promotionProductRepository.findActivePromotionByProductId(eq("err"), any(LocalDateTime.class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> promotionService.applyPromotionToProduct(prod));
    }

}

