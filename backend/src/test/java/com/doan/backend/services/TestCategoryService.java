package com.doan.backend.services;

import com.doan.backend.dto.request.CategoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CategoryResponse;
import com.doan.backend.entity.Category;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.mapper.CategoryMapper;
import com.doan.backend.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional  // Mỗi test sẽ được rollback lại sau khi chạy xong
public class TestCategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;  // Class chứa phương thức createCategory

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    public void testCreateCategory_Success() {
        // Chuẩn bị dữ liệu request
        CategoryRequest request = CategoryRequest.builder()
                .name("CategoryTest")
                .description("Mô tả danh mục test")
                .status(StatusEnum.ACTIVE)
                .build();

        // Gọi service tạo danh mục
        ApiResponse<CategoryResponse> response = categoryService.createCategory(request);

        // Kiểm tra thông tin trả về
        assertNotNull(response, "Response không được null");
        assertEquals(200, response.getCode(), "Code trả về không đúng");
        assertEquals("Category created successfully", response.getMessage(), "Message trả về không đúng");
        assertNotNull(response.getResult(), "Result không được null");

        // Kiểm tra dữ liệu trong database
        Optional<Category> savedCategory = categoryRepository.findById(response.getResult().getId());
        assertTrue(savedCategory.isPresent(), "Danh mục mới tạo không có trong DB");
        assertEquals(request.getName(), savedCategory.get().getName(), "Tên danh mục không khớp");
        // Có thể kiểm tra thêm các trường khác nếu cần
    }

    @Test
    public void testCreateCategory_Failure_CategoryAlreadyExists() {
        // Tạo sẵn danh mục trong DB với cùng tên để kích hoạt rẽ nhánh exception trong service
        Category existingCategory = Category.builder()
                .name("CategoryExists")
                .description("Danh mục đã tồn tại")
                .status(StatusEnum.ACTIVE)
                .build();
        categoryRepository.save(existingCategory);

        // Chuẩn bị request với tên danh mục trùng
        CategoryRequest request = CategoryRequest.builder()
                .name("CategoryExists")
                .description("Danh mục test")
                .status(StatusEnum.ACTIVE)
                .build();

        // Kiểm tra xem có exception được ném ra với thông báo phù hợp không
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class,
                () -> categoryService.createCategory(request),
                "Không ném exception khi tên danh mục đã tồn tại");

        assertEquals("Category name already exists", thrownException.getMessage(), "Thông báo exception không khớp");
    }
}
