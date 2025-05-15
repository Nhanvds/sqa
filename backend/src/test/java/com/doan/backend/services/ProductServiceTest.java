package com.doan.backend.services;

import com.doan.backend.dto.request.ProductRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductResponse;
import com.doan.backend.entity.Category;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.Promotion;
import com.doan.backend.entity.PromotionProduct;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.repositories.CategoryRepository;
import com.doan.backend.repositories.ProductRepository;
import com.doan.backend.repositories.PromotionProductRepository;
import com.doan.backend.repositories.PromotionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private PromotionProductRepository promotionProductRepository;

    private Promotion samplePromotion;
    private Product sampleProduct;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = categoryRepository.save(Category.builder()
                .name("Test Category")
                .description("Sample Description")
                .status(StatusEnum.ACTIVE)
                .build());

        sampleProduct = productRepository.save(Product.builder()
                .name("Test Product")
                .description("Sample Product")
                .price(BigDecimal.valueOf(100))
                .status(StatusEnum.ACTIVE)
                .category(sampleCategory)
                .build());

        samplePromotion = promotionRepository.save(Promotion.builder()
                .name("Test Promotion")
                .description("10% off")
                .discountPercentage(BigDecimal.valueOf(10))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .applyToAll(false)
                .build());
    }

    // TC01 - Lấy thông tin Product đã tồn tại
    @Test
    void testGetProductByValidId_TC01() {
        assertDoesNotThrow(() -> {
            ApiResponse<ProductResponse> response = productService.getProductById(sampleProduct.getId());
            assertNotNull(response);
            ProductResponse result = response.getResult();
            assertEquals(sampleProduct.getName(), result.getName());
            assertEquals(sampleProduct.getDescription(), result.getDescription());
            assertEquals(sampleProduct.getPrice(), result.getPrice());
            assertEquals(sampleProduct.getId(), result.getId());
            assertEquals(sampleProduct.getStatus(), result.getStatus());
        });
    }

    // TC02 - Lấy thông tin Product không tồn tại
    @Test
    void testGetProductByInvalidId_TC02() {
        assertThrows(RuntimeException.class, () -> productService.getProductById("invalid-id"));
    }

    // TC03 - Tạo Product mới hợp lệ, có Categoy tồn tại
    @Test
    void testCreateProductValid_TC03() {
        assertDoesNotThrow(() -> {
            ProductRequest request = ProductRequest.builder()
                    .name("New Product")
                    .description("New Product Desc")
                    .price(BigDecimal.valueOf(150))
                    .categoryId(sampleCategory.getId())
                    .status(StatusEnum.ACTIVE)
                    .build();

            ApiResponse<String> response = productService.createProduct(request);
            assertNotNull(response.getResult());

            Product createdProduct = productRepository.findById(response.getResult()).orElseThrow();
            assertEquals(request.getName(), createdProduct.getName());
            assertEquals(request.getDescription(), createdProduct.getDescription());
            assertEquals(request.getPrice(), createdProduct.getPrice());
            assertEquals(request.getCategoryId(), createdProduct.getCategory().getId());
            assertEquals(request.getStatus(), createdProduct.getStatus());
        });
    }

    // TC04 - Tạo Product với Category không tồn tại
    @Test
    void testCreateProductWithInvalidCategory_TC04() {
        ProductRequest request = ProductRequest.builder()
                .name("Invalid Product")
                .description("desc")
                .price(BigDecimal.valueOf(150))
                .categoryId("non-existing-category")
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.createProduct(request));
    }

    // TC05 - Tạo Product với tên null
    @Test
    void testCreateProductWithNullName_TC05() {
        ProductRequest request = ProductRequest.builder()
                .name(null)
                .description("desc")
                .price(BigDecimal.valueOf(100))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.createProduct(request));
    }

    // TC06 - Tạo Product với giá trị price âm
    @Test
    void testCreateProductWithNegativePrice_TC06() {
        ProductRequest request = ProductRequest.builder()
                .name("Negative Price")
                .description("desc")
                .price(BigDecimal.valueOf(-1))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.createProduct(request));
    }

    // TC07 - Cập nhật Product hợp lệ
    @Test
    void testUpdateProductValid_TC07() {
        assertDoesNotThrow(() -> {
            ProductRequest request = ProductRequest.builder()
                    .name("Updated Name")
                    .description("Updated Description")
                    .price(BigDecimal.valueOf(199))
                    .categoryId(sampleCategory.getId())
                    .status(StatusEnum.INACTIVE)
                    .build();

            productService.updateProduct(sampleProduct.getId(), request);

            Product updatedProduct = productRepository.findById(sampleProduct.getId()).orElseThrow();
            assertEquals(request.getName(), updatedProduct.getName());
            assertEquals(request.getDescription(), updatedProduct.getDescription());
            assertEquals(request.getPrice(), updatedProduct.getPrice());
            assertEquals(request.getCategoryId(), updatedProduct.getCategory().getId());
            assertEquals(request.getStatus(), updatedProduct.getStatus());
        });
    }

    // TC08 - Cập nhật Product với ID không tồn tại
    @Test
    void testUpdateProductInvalidId_TC08() {
        ProductRequest request = ProductRequest.builder()
                .name("Product")
                .description("desc")
                .price(BigDecimal.valueOf(99))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.updateProduct("invalid-id", request));
    }

    // TC09 - Cập nhật Product với Category không tồn tại
    @Test
    void testUpdateProductWithInvalidCategory_TC09() {
        ProductRequest request = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199))
                .categoryId("non-existing-category")
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.updateProduct(sampleProduct.getId(), request));
    }

    // TC10 - Cập nhật Product với dữ liệu null
    @Test
    void testUpdateProductWithNullRequest_TC10() {
        assertThrows(RuntimeException.class, () -> productService.updateProduct(sampleProduct.getId(), null));
    }

    // TC11 - Cập nhật Product với tên rỗng
    @Test
    void testUpdateProductWithEmptyName_TC11() {
        ProductRequest request = ProductRequest.builder()
                .name("")
                .description("desc")
                .price(BigDecimal.valueOf(99))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.updateProduct(sampleProduct.getId(), request));
    }

    // TC12 - Xoá Product hợp lệ
    @Test
    void testDeleteProductValid_TC12() {
        assertDoesNotThrow(() -> {
            productService.deleteProduct(sampleProduct.getId());
            Product deleted = productRepository.findById(sampleProduct.getId()).orElseThrow();
            assertEquals(StatusEnum.DELETED, deleted.getStatus());
        });
    }

    // TC13 - Xoá Product không tồn tại
    @Test
    void testDeleteProductInvalidId_TC13() {
        assertThrows(RuntimeException.class, () -> productService.deleteProduct("invalid-id"));
    }

    // TC14 - Lấy danh sách Product có phân trang
    @Test
    void testSearchProductsWithPaging_TC14() {
        assertDoesNotThrow(() -> {
            Page<ProductResponse> page = productService.searchProducts("Test Category", null, PageRequest.of(0, 10)).getResult();
            assertNotNull(page);
            assertTrue(page.getContent().stream().anyMatch(p -> p.getName().contains("Test")));

        });
    }

    // TC15 - Lấy danh sách Product với CategoruId là null
    @Test
    void testSearchProductsWithInvalidName_TC15() {
        assertDoesNotThrow(() -> {
            Page<ProductResponse> page = productService.searchProducts("NoSuchName", null, PageRequest.of(0, 10)).getResult();
            assertEquals(page.getContent().size(), productRepository.findAll().size());
        });
    }

    // TC16 - Lấy danh sách Product với Category ID không tồn tại
    @Test
    void testSearchProductsWithInvalidCategory_TC16() {
        assertDoesNotThrow(() -> {
            Page<ProductResponse> page = productService.searchProducts(null, "invalid-category", PageRequest.of(0, 10)).getResult();
            assertTrue(page.getContent().isEmpty());
        });
    }

    // TC17 - Tìm kiếm Product với pageable null
    @Test
    void testSearchProductsWithNullPageable_TC17() {
        assertThrows(RuntimeException.class, () -> productService.searchProducts("Test", null, null));
    }

    // TC18 - Tìm kiếm Product với page size = 0
    @Test
    void testSearchProductsWithZeroPageSize_TC18() {
        assertThrows(RuntimeException.class, () -> productService.searchProducts("Test", null, PageRequest.of(0, 0)));
    }

    // TC19 - Áp dụng Promotion cho Product
    @Test
    void testApplyPromotionToProduct_TC19() {
        assertDoesNotThrow(() -> {
            PromotionProduct pp = new PromotionProduct();
            pp.setProduct(sampleProduct);
            pp.setPromotion(samplePromotion);
            promotionProductRepository.save(pp);

            ApiResponse<ProductResponse> response = productService.getProductById(sampleProduct.getId());
            ProductResponse result = response.getResult();
            assertTrue( result.getPromotions().stream().anyMatch(a -> a.getId().equals(samplePromotion.getId())));
        });
    }

    // TC20 - Tạo Product với Promotion không tồn tại
    @Test
    void testCreateProductWithInvalidPromotion_TC20() {
        ProductRequest request = ProductRequest.builder()
                .name("Invalid Promotion Product")
                .description("desc")
                .price(BigDecimal.valueOf(120))
                .categoryId(sampleCategory.getId())
                .promotionIds(List.of("invalid-id"))
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.createProduct(request));
    }

    // TC21 - Cập nhật Product với Promotion mới
    @Test
    void testUpdateProductWithPromotion_TC21() {
        assertDoesNotThrow(() -> {
            ProductRequest request = ProductRequest.builder()
                    .name("Update With Promotion")
                    .description("desc")
                    .price(BigDecimal.valueOf(110))
                    .categoryId(sampleCategory.getId())
                    .promotionIds(List.of(samplePromotion.getId()))
                    .status(StatusEnum.ACTIVE)
                    .build();

            productService.updateProduct(sampleProduct.getId(), request);
            productRepository.findById(sampleProduct.getId()).orElseThrow();
            List<PromotionProduct> list = promotionProductRepository.findPromotionProductsByProductId(sampleProduct.getId());
            assertTrue(list.stream().anyMatch(a -> (a.getPromotion().getId().equals(samplePromotion.getId()) && a.getProduct().getId().equals(a.getProduct().getId())) ));
        });
    }
}