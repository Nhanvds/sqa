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
import java.util.Objects;
import java.util.Optional;

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
//        sizeRepository.deleteAll();
    }

    // TC01 - Kiểm tra chức năng thêm một Size mới
    @Test
    void testCreateSize_TC01() {
        assertDoesNotThrow(() -> {
            // Given
            SizeRequest sizeRequest = new SizeRequest();
            sizeRequest.setName("Large");

            // When
            ApiResponse<Size> response = sizeService.createSize(sizeRequest);

            // Then
            assertNotNull(response.getResult());
            assertEquals("Large", response.getResult().getName());
        });
    }

    // TC02 - Kiểm tra chức năng thêm Size khi tên đã tồn tại
    @Test
    void testCreateSizeAlreadyExists_TC02() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Medium");

        Size size = new Size();
        size.setName("Medium");
        sizeRepository.save(size);

        // When / Then
        assertThrows(RuntimeException.class, () -> {
            sizeService.createSize(sizeRequest);
        });
    }

    // TC03 - Kiểm tra chức năng lấy tất cả Size
    @Test
    void testGetAllSize_TC03() {
        assertDoesNotThrow(() -> {
            // Given
            Size size1 = new Size();
            size1.setName("Small");
            sizeRepository.save(size1);

            Size size2 = new Size();
            size2.setName("Medium");
            sizeRepository.save(size2);

            // When
            ApiResponse<List<Size>> response = sizeService.getAllSize();

            // Then
            assertNotNull(response.getResult());
            assertFalse(response.getResult().isEmpty());
        });
    }

    // TC04 - Kiểm tra chức năng lấy Size theo ID
    @Test
    void testGetSizeById_TC04() {
        assertDoesNotThrow(() -> {
            // Given
            Size size = new Size();
            size.setName("Extra Large");
            size = sizeRepository.save(size);
            String id = size.getId();

            // When
            ApiResponse<Size> response = sizeService.getSizeById(id);

            // Then
            assertNotNull(response.getResult());
            assertEquals("Extra Large", response.getResult().getName());
        });
    }

    // TC05 - Kiểm tra lấy Size theo ID khi không tồn tại
    @Test
    void testGetSizeByIdNotFound_TC05() {
        // Given
        String id = "nonexistent-id";

        // When / Then
        assertThrows(RuntimeException.class, () -> {
            sizeService.getSizeById(id);
        });
    }

    // TC06 - Kiểm tra chức năng cập nhật Size
    @Test
    void testUpdateSize_TC06() {
        assertDoesNotThrow(() -> {
            // Given
            Size size = new Size();
            size.setName("Small");
            size = sizeRepository.save(size);
            String id = size.getId();

            SizeRequest sizeRequest = new SizeRequest();
            sizeRequest.setName("Medium");

            // When
            ApiResponse<Size> response = sizeService.updateSize(id, sizeRequest);

            // Then
            assertNotNull(response.getResult());
            assertEquals("Medium", response.getResult().getName());
        });
    }

    // TC07 - Kiểm tra cập nhật Size khi không tồn tại
    @Test
    void testUpdateSizeNotFound_TC07() {
        // Given
        SizeRequest sizeRequest = new SizeRequest();
        sizeRequest.setName("Small");
        String id = "nonexistent-id";

        // When / Then
        assertThrows(RuntimeException.class, () -> {
            sizeService.updateSize(id, sizeRequest);
        });
    }

    // TC08 - Kiểm tra chức năng xóa Size
    @Test
    void testDeleteSize_TC08() {
        assertDoesNotThrow(() -> {
            // Given
            Size size = new Size();
            size.setName("Small");
            size = sizeRepository.save(size);
            String id = size.getId();

            // When
            ApiResponse<Void> response = sizeService.deleteSize(id);

            Optional<Size> size1 = sizeRepository.findById(id);

            assertTrue(size1.isEmpty());
        });
    }

    // TC09 - Kiểm tra xóa Size khi không tồn tại
    @Test
    void testDeleteSizeNotFound_TC09() {
        // Given
        String id = "nonexistent-id";

        // When / Then
        assertThrows(RuntimeException.class, () -> {
            sizeService.deleteSize(id);
        });
    }
}