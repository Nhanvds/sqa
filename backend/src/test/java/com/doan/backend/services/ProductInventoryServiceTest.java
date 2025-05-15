package com.doan.backend.services;

import com.doan.backend.dto.request.ProductInventoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductInventoryResponse;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.ProductInventory;
import com.doan.backend.entity.Size;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.ProductInventoryMapper;
import com.doan.backend.repositories.ProductInventoryRepository;
import com.doan.backend.repositories.ProductRepository;
import com.doan.backend.repositories.SizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProductInventoryServiceTest {

    @Autowired
    private ProductInventoryService productInventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ProductInventoryMapper productInventoryMapper;

    private Product product;
    private Size size;

    @BeforeEach
    public void setUp() {
        // Tạo sản phẩm mới cho các test
        product = new Product();
        product.setName("Product 1");
        product.setDescription("Product Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStatus(StatusEnum.ACTIVE);
        product = productRepository.save(product);

        // Tạo kích thước mới cho các test
        Size newSize = new Size();
        newSize.setName("Test Size");
        size = sizeRepository.save(newSize);
    }

    // TC01 - Kiểm tra chức năng tạo mới ProductInventory thành công
    @Test
    public void createProductInventory_Success_TC01() {
        assertDoesNotThrow(() -> {
            // Chuẩn bị dữ liệu
            ProductInventoryRequest request = ProductInventoryRequest.builder()
                    .idProduct(product.getId())
                    .idSize(size.getId())
                    .quantity(10)
                    .build();

            // Thực hiện kiểm tra
            ApiResponse<ProductInventoryResponse> response = productInventoryService.createProductInventory(request);

            // Kiểm tra kết quả
            assertNotNull(response.getResult());
            assertEquals(request.getIdProduct(), response.getResult().getIdProduct());
            assertEquals(request.getIdSize(), response.getResult().getSize().getId());
            assertEquals(request.getQuantity(), response.getResult().getQuantity());
        });
    }

    // TC02 - Kiểm tra chức năng tạo mới ProductInventory khi tồn tại sản phẩm và size
    @Test
    public void createProductInventory_AlreadyExists_TC02() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize(size.getId())
                .quantity(10)
                .build();

        // Thực hiện lần 1: Lưu trực tiếp vào repository để giả lập trạng thái đã tồn tại
        ProductInventory inventory = new ProductInventory();
        inventory.setProduct(product);
        inventory.setSize(size);
        inventory.setQuantity(request.getQuantity());
        productInventoryRepository.save(inventory);

        // Thực hiện lần 2 (cùng productId và sizeId)
        assertThrows(RuntimeException.class, () -> {
            productInventoryService.createProductInventory(request);
        });

        // Kiểm tra exception
    }

    // TC03 - Kiểm tra lấy thông tin ProductInventory theo productId
    @Test
    public void getProductInventoryByProductId_Success_TC03() {
        assertDoesNotThrow(() -> {
            // Chuẩn bị dữ liệu
            ProductInventory inventory = new ProductInventory();
            inventory.setProduct(product);
            inventory.setSize(size);
            inventory.setQuantity(10);
            productInventoryRepository.save(inventory);

            // Lấy thông tin
            ApiResponse<Iterable<ProductInventoryResponse>> response = productInventoryService.getProductInventoryByProductId(product.getId());

            // Kiểm tra kết quả
            assertNotNull(response.getResult());
            ProductInventoryResponse result = response.getResult().iterator().next();
            assertEquals(product.getId(), result.getIdProduct());
            assertEquals(size.getId(), result.getSize().getId());
            assertEquals(10, result.getQuantity());
        });
    }

    // TC04 - Kiểm tra cập nhật ProductInventory thành công
    @Test
    public void updateProductInventory_Success_TC04() {
        assertDoesNotThrow(() -> {
            // Chuẩn bị dữ liệu
            ProductInventory inventory = new ProductInventory();
            inventory.setProduct(product);
            inventory.setSize(size);
            inventory.setQuantity(10);
            inventory = productInventoryRepository.save(inventory);

            // Cập nhật thông tin
            ProductInventoryRequest updateRequest = ProductInventoryRequest.builder()
                    .idProduct(product.getId())
                    .idSize(size.getId())
                    .quantity(20)
                    .build();

            ApiResponse<ProductInventoryResponse> updateResponse = productInventoryService.updateProductInventory(inventory.getId(), updateRequest);

            // Kiểm tra kết quả
            assertEquals(200, updateResponse.getCode());
            assertEquals(20, updateResponse.getResult().getQuantity());
            assertEquals(updateRequest.getIdProduct(), updateResponse.getResult().getIdProduct());
            assertEquals(updateRequest.getIdSize(), updateResponse.getResult().getSize().getId());
        });
    }

    // TC05 - Kiểm tra xóa ProductInventory thành công
    @Test
    public void deleteProductInventory_Success_TC05() {
        assertDoesNotThrow(() -> {
            // Chuẩn bị dữ liệu
            ProductInventory inventory = new ProductInventory();
            inventory.setProduct(product);
            inventory.setSize(size);
            inventory.setQuantity(10);
            inventory = productInventoryRepository.save(inventory);

            // Xóa ProductInventory
            productInventoryService.deleteProductInventory(inventory.getId());
            Optional<ProductInventory> productInventory = productInventoryRepository.findById(inventory.getId());

            assertTrue(productInventory.isEmpty());
            // Kiểm tra kết quả
        });
    }

    // TC06 - Kiểm tra lấy thông tin ProductInventory khi productId không tồn tại
    @Test
    public void getProductInventoryByProductId_NotFound_TC06() {
        assertThrows(RuntimeException.class,() -> {
            // Thực hiện lấy thông tin ProductInventory cho productId không tồn tại
            productInventoryService.getProductInventoryByProductId("nonExistentProductId");

        });
    }

    // TC07 - Cập nhập khi ProductInventory không tồn tại
    @Test
    public void updateProductInventory_NotFound_TC07() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize(size.getId())
                .quantity(10)
                .build();

        // Thực hiện cập nhật ProductInventory không tồn tại
        assertThrows(RuntimeException.class, () -> {
            productInventoryService.updateProductInventory("nonExistentId", request);
        });

    }

    // TC08 - Kiểm tra xóa ProductInventory không tồn tại
    @Test
    public void deleteProductInventory_NotFound_TC08() {
        // Thực hiện xóa ProductInventory không tồn tại
        assertThrows(RuntimeException.class, () -> {
            productInventoryService.deleteProductInventory("nonExistentId");
        });

    }

    // TC09 - Kiểm tra lấy thông tin ProductInventory khi danh sách productIds không tồn tại
    @Test
    public void getProductInventoryByListProductId_NotFound_TC09() {
        assertDoesNotThrow(() -> {
            // Thực hiện lấy thông tin ProductInventory cho danh sách productIds không tồn tại
            ApiResponse<Iterable<ProductInventoryResponse>> response = productInventoryService.getProductInventoryByListProductId(List.of("nonExistentProductId"));

            // Kiểm tra kết quả
            assertFalse(response.getResult().iterator().hasNext());
        });
    }

    // TC10 - Kiểm tra tạo ProductInventory với quantity bằng 0
    @Test
    public void createProductInventory_QuantityZero_TC10() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize(size.getId())
                .quantity(0) // Quantity = 0
                .build();

        // Thực hiện kiểm tra
        assertThrows(RuntimeException.class, () -> {
            productInventoryService.createProductInventory(request);
        });

    }
}