package com.doan.backend.services;

import com.doan.backend.dto.request.ProductInventoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ProductInventoryResponse;
import com.doan.backend.entity.Product;
import com.doan.backend.entity.ProductInventory;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.ProductInventoryMapper;
import com.doan.backend.repositories.ProductInventoryRepository;
import com.doan.backend.repositories.ProductRepository;
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
    private ProductInventoryMapper productInventoryMapper;

    private Product product;

    @BeforeEach
    public void setUp() {
        // Tạo sản phẩm mới cho các test
        product = new Product();
        product.setName("Product 1");
        product.setDescription("Product Description");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStatus(StatusEnum.ACTIVE);
        productRepository.save(product);
    }

    // TC01 - Kiểm tra chức năng tạo mới ProductInventory thành công
    @Test
    public void createProductInventory_Success_TC01() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(10)
                .build();

        // Thực hiện kiểm tra
        ApiResponse<ProductInventoryResponse> response = productInventoryService.createProductInventory(request);

        // Kiểm tra kết quả
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
        assertEquals("Create product inventory successfully", response.getMessage());
    }

    // TC02 - Kiểm tra chức năng tạo mới ProductInventory khi tồn tại sản phẩm và size
    @Test
    public void createProductInventory_AlreadyExists_TC02() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(10)
                .build();

        // Thực hiện lần 1
        productInventoryService.createProductInventory(request);

        // Thực hiện lần 2 (cùng productId và sizeId)
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productInventoryService.createProductInventory(request);
        });

        // Kiểm tra exception
        assertEquals("Product inventory already exists", exception.getMessage());
    }

    // TC03 - Kiểm tra lấy thông tin ProductInventory theo productId
    @Test
    public void getProductInventoryByProductId_Success_TC03() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(10)
                .build();

        // Thực hiện tạo ProductInventory
        productInventoryService.createProductInventory(request);

        // Lấy thông tin
        ApiResponse<Iterable<ProductInventoryResponse>> response = productInventoryService.getProductInventoryByProductId(product.getId());

        // Kiểm tra kết quả
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
        assertTrue(response.getResult().iterator().hasNext());
    }

    // TC04 - Kiểm tra cập nhật ProductInventory thành công
    @Test
    public void updateProductInventory_Success_TC04() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest createRequest = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(10)
                .build();

        // Thực hiện tạo ProductInventory
        ApiResponse<ProductInventoryResponse> createResponse = productInventoryService.createProductInventory(createRequest);

        // Cập nhật thông tin
        ProductInventoryRequest updateRequest = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(20)
                .build();

        ApiResponse<ProductInventoryResponse> updateResponse = productInventoryService.updateProductInventory(createResponse.getResult().getId(), updateRequest);

        // Kiểm tra kết quả
        assertEquals(200, updateResponse.getCode());
        assertEquals(20, updateResponse.getResult().getQuantity());
    }

    // TC05 - Kiểm tra xóa ProductInventory thành công
    @Test
    public void deleteProductInventory_Success_TC05() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(10)
                .build();

        // Thực hiện tạo ProductInventory
        ApiResponse<ProductInventoryResponse> createResponse = productInventoryService.createProductInventory(request);

        // Xóa ProductInventory
        ApiResponse<String> deleteResponse = productInventoryService.deleteProductInventory(createResponse.getResult().getId());

        // Kiểm tra kết quả
        assertEquals(200, deleteResponse.getCode());
        assertEquals("Delete product inventory successfully", deleteResponse.getMessage());
    }

    // TC06 - Kiểm tra lấy thông tin ProductInventory khi productId không tồn tại
    @Test
    public void getProductInventoryByProductId_NotFound_TC06() {
        // Thực hiện lấy thông tin ProductInventory cho productId không tồn tại
        ApiResponse<Iterable<ProductInventoryResponse>> response = productInventoryService.getProductInventoryByProductId("nonExistentProductId");

        // Kiểm tra kết quả
        assertEquals(200, response.getCode());
        assertFalse(response.getResult().iterator().hasNext());
    }

    // TC07 - Kiểm tra khi ProductInventory không tồn tại
    @Test
    public void updateProductInventory_NotFound_TC07() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(10)
                .build();

        // Thực hiện cập nhật ProductInventory không tồn tại
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productInventoryService.updateProductInventory("nonExistentId", request);
        });

        // Kiểm tra exception
        assertEquals("Product inventory not found", exception.getMessage());
    }

    // TC08 - Kiểm tra xóa ProductInventory không tồn tại
    @Test
    public void deleteProductInventory_NotFound_TC08() {
        // Thực hiện xóa ProductInventory không tồn tại
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productInventoryService.deleteProductInventory("nonExistentId");
        });

        // Kiểm tra exception
        assertEquals("Product inventory not found", exception.getMessage());
    }

    // TC09 - Kiểm tra lấy thông tin ProductInventory khi danh sách productIds không tồn tại
    @Test
    public void getProductInventoryByListProductId_NotFound_TC09() {
        // Thực hiện lấy thông tin ProductInventory cho danh sách productIds không tồn tại
        ApiResponse<Iterable<ProductInventoryResponse>> response = productInventoryService.getProductInventoryByListProductId(List.of("nonExistentProductId"));

        // Kiểm tra kết quả
        assertEquals(200, response.getCode());
        assertFalse(response.getResult().iterator().hasNext());
    }

    // TC10 - Kiểm tra tạo ProductInventory với quantity bằng 0
    @Test
    public void createProductInventory_QuantityZero_TC10() {
        // Chuẩn bị dữ liệu
        ProductInventoryRequest request = ProductInventoryRequest.builder()
                .idProduct(product.getId())
                .idSize("sizeId1")
                .quantity(0) // Quantity = 0
                .build();

        // Thực hiện kiểm tra
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productInventoryService.createProductInventory(request);
        });

        // Kiểm tra exception
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }
}
