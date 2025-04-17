package com.doan.backend.services;

import com.doan.backend.dto.request.SizeRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.entity.Size;
import com.doan.backend.repositories.SizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SizeServiceTest {

    @Autowired
    private SizeService sizeService;

    @Autowired
    private SizeRepository sizeRepository;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        sizeRepository.deleteAll();
    }


     //TC01 - Kiểm tra chức năng thêm một Size mới.

    @Test
    void testCreateSize_TC01() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Large");

        // When
        ApiResponse<Size> response = sizeService.createSize(sizeRequest);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
        assertEquals("Large", response.getResult().getName());
    }


     //TC02 - Kiểm tra chức năng thêm Size khi tên đã tồn tại.

    @Test
    void testCreateSizeAlreadyExists_TC02() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Medium");
        sizeService.createSize(sizeRequest);

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            sizeService.createSize(sizeRequest);
        });
        assertEquals("Size is already exist", thrown.getMessage());
    }


     //TC03 - Kiểm tra chức năng lấy tất cả Size.

    @Test
    void testGetAllSize_TC03() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Small");
        sizeService.createSize(sizeRequest);

        // When
        ApiResponse<List<Size>> response = sizeService.getAllSize();

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
        assertFalse(response.getResult().isEmpty());
        assertEquals("Small", response.getResult().get(0).getName());
    }


     //TC04 - Kiểm tra chức năng lấy Size theo ID.

    @Test
    void testGetSizeById_TC04() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Extra Large");
        ApiResponse<Size> createResponse = sizeService.createSize(sizeRequest);
        String id = createResponse.getResult().getId();

        // When
        ApiResponse<Size> response = sizeService.getSizeById(id);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
        assertEquals("Extra Large", response.getResult().getName());
    }


     //TC05 - Kiểm tra lấy Size theo ID khi không tồn tại.

    @Test
    void testGetSizeByIdNotFound_TC05() {
        // Given
        String id = "nonexistent-id";

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            sizeService.getSizeById(id);
        });
        assertEquals("Size not found", thrown.getMessage());
    }


     //TC06 - Kiểm tra chức năng cập nhật Size.

    @Test
    void testUpdateSize_TC06() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Small");
        ApiResponse<Size> createResponse = sizeService.createSize(sizeRequest);
        String id = createResponse.getResult().getId();

        // Update Size
        sizeRequest.setName("Medium");

        // When
        ApiResponse<Size> response = sizeService.updateSize(id, sizeRequest);

        // Then
        assertEquals(200, response.getCode());
        assertNotNull(response.getResult());
        assertEquals("Medium", response.getResult().getName());
    }


     //TC07 - Kiểm tra cập nhật Size khi không tồn tại.

    @Test
    void testUpdateSizeNotFound_TC07() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Small");
        String id = "nonexistent-id";

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            sizeService.updateSize(id, sizeRequest);
        });
        assertEquals("Size not found", thrown.getMessage());
    }


    //TC08 - Kiểm tra chức năng xóa Size.

    @Test
    void testDeleteSize_TC08() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Small");
        ApiResponse<Size> createResponse = sizeService.createSize(sizeRequest);
        String id = createResponse.getResult().getId();

        // When
        ApiResponse<Void> response = sizeService.deleteSize(id);

        // Then
        assertEquals(200, response.getCode());
        assertEquals("Delete size successfully", response.getMessage());
    }


     //TC09 - Kiểm tra xóa Size khi không tồn tại.

    @Test
    void testDeleteSizeNotFound_TC09() {
        // Given
        String id = "nonexistent-id";

        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            sizeService.deleteSize(id);
        });
        assertEquals("Size not found", thrown.getMessage());
    }
}
