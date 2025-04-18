package com.doan.backend.services;

import com.doan.backend.dto.request.ProductRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductResponse;
import com.doan.backend.entity.Category;
import com.doan.backend.entity.Product;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.repositories.CategoryRepository;
import com.doan.backend.repositories.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
    }

    // TC01 - Lấy thông tin Product đã tồn tại
    @Test
    void testGetProductByValidId_TC01() {
        ApiResponse<ProductResponse> response = productService.getProductById(sampleProduct.getId());
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(sampleProduct.getName(), response.getResult().getName());
    }

    // TC02 - Lấy thông tin Product không tồn tại
    @Test
    void testGetProductByInvalidId_TC02() {
        assertThrows(RuntimeException.class, () -> productService.getProductById("invalid-id"));
    }

    // TC03 - Tạo Product mới hợp lệ
    @Test
    void testCreateProductValid_TC03() {
        ProductRequest request = ProductRequest.builder()
                .name("New Product")
                .description("New Product Desc")
                .price(BigDecimal.valueOf(150))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        ApiResponse<String> response = productService.createProduct(request);
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
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

    // TC05 - Cập nhật Product hợp lệ
    @Test
    void testUpdateProductValid_TC05() {
        ProductRequest request = ProductRequest.builder()
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(199))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.INACTIVE)
                .build();

        ApiResponse<String> response = productService.updateProduct(sampleProduct.getId(), request);
        assertEquals(200, response.getCode());
    }

    // TC06 - Cập nhật Product với ID không tồn tại
    @Test
    void testUpdateProductInvalidId_TC06() {
        ProductRequest request = ProductRequest.builder()
                .name("Product")
                .description("desc")
                .price(BigDecimal.valueOf(99))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(RuntimeException.class, () -> productService.updateProduct("invalid-id", request));
    }

    // TC07 - Xoá Product hợp lệ
    @Test
    void testDeleteProductValid_TC07() {
        ApiResponse<Void> response = productService.deleteProduct(sampleProduct.getId());
        assertEquals(200, response.getCode());
        Product deleted = productRepository.findById(sampleProduct.getId()).orElseThrow();
        assertEquals(StatusEnum.DELETED, deleted.getStatus());
    }

    // TC08 - Xoá Product không tồn tại
    @Test
    void testDeleteProductInvalidId_TC08() {
        assertThrows(RuntimeException.class, () -> productService.deleteProduct("invalid-id"));
    }

    // TC09 - Lấy danh sách Product có phân trang
    @Test
    void testSearchProductsWithPaging_TC09() {
        Page<ProductResponse> page = productService.searchProducts("Test", null, PageRequest.of(0, 10)).getResult();
        assertNotNull(page);
        assertTrue(page.getContent().stream().anyMatch(p -> p.getName().contains("Test")));
    }

    // TC10 - Lấy danh sách Product với tên không tồn tại
    @Test
    void testSearchProductsWithInvalidName_TC10() {
        Page<ProductResponse> page = productService.searchProducts("NoSuchName", null, PageRequest.of(0, 10)).getResult();
        assertTrue(page.getContent().isEmpty());
    }

    // TC11 - Lấy danh sách Product với Category ID không tồn tại
    @Test
    void testSearchProductsWithInvalidCategory_TC11() {
        Page<ProductResponse> page = productService.searchProducts(null, "invalid-category", PageRequest.of(0, 10)).getResult();
        assertTrue(page.getContent().isEmpty());
    }

    // TC12 - Tạo Product với tên null
    @Test
    void testCreateProductWithNullName_TC12() {
        ProductRequest request = ProductRequest.builder()
                .name(null)
                .description("desc")
                .price(BigDecimal.valueOf(100))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(Exception.class, () -> productService.createProduct(request));
    }

    // TC13 - Cập nhật Product với dữ liệu null
    @Test
    void testUpdateProductWithNullRequest_TC13() {
        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(sampleProduct.getId(), null));
    }

    // TC14 - Tạo Product với giá trị price âm
    @Test
    void testCreateProductWithNegativePrice_TC14() {
        ProductRequest request = ProductRequest.builder()
                .name("Negative Price")
                .description("desc")
                .price(BigDecimal.valueOf(-1))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(Exception.class, () -> productService.createProduct(request));
    }

    // TC15 - Cập nhật Product với tên rỗng
    @Test
    void testUpdateProductWithEmptyName_TC15() {
        ProductRequest request = ProductRequest.builder()
                .name("")
                .description("desc")
                .price(BigDecimal.valueOf(99))
                .categoryId(sampleCategory.getId())
                .status(StatusEnum.ACTIVE)
                .build();

        assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(sampleProduct.getId(), request));
    }

    // TC16 - Tìm kiếm Product với pageable null
    @Test
    void testSearchProductsWithNullPageable_TC16() {
        assertThrows(IllegalArgumentException.class, () -> productService.searchProducts("Test", null, null));
    }

    // TC17 - Tìm kiếm Product với page size = 0
    @Test
    void testSearchProductsWithZeroPageSize_TC17() {
        assertThrows(IllegalArgumentException.class, () -> productService.searchProducts("Test", null, PageRequest.of(0, 0)));
    }
}
