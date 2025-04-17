package com.doan.backend.services;


import com.doan.backend.dto.request.CategoryRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CategoryResponse;
import com.doan.backend.entity.Category;
import com.doan.backend.enums.StatusEnum;
import com.doan.backend.repositories.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.data.domain.PageRequest;


@SpringBootTest
@Transactional
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Category categorySample;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("Test Category")
                .description("Test Description")
                .status(StatusEnum.ACTIVE)
                .build();
        categorySample = categoryRepository.save(category);
    }

    // TC01 - Lấy thông tin Category đã tồn tại
    @Test
    void testGetCategoryByValidId_TC01() {
        ApiResponse<CategoryResponse> response = categoryService.getCategory(categorySample.getId());
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(categorySample.getName(), response.getResult().getName());
    }

    // TC02 - Lấy thông tin Category theo ID không có trong CSDL
    @Test
    void testGetCategoryByInvalidId_TC02() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getCategory("non-existing-id");
        });
        assertTrue(exception.getMessage().contains("Category not found"));
    }

    // TC03 - Lấy tất cả Category
    @Test
    void testGetAllCategories_TC03() {
        List<CategoryResponse> categories = categoryService.getAllCategories().getResult();
        assertFalse(categories.isEmpty());
        assertTrue(categories.stream().allMatch(c -> c.getStatus() != StatusEnum.DELETED));
    }

    // TC04 - Lấy danh sách Category phân trang theo tên
    @Test
    void testGetPagedCategoriesByName_TC04() {
        Page<CategoryResponse> page = categoryService.getPageAllCategories("Test", PageRequest.of(0, 10)).getResult();
        assertNotNull(page);
        assertTrue(page.getContent().stream().anyMatch(c -> c.getName().contains("Test")));
    }

    // TC05 - Tạo Category mới
    @Test
    void testCreateCategoryValid_TC05() {
        CategoryRequest request = CategoryRequest.builder().name("New Category").description("New Desc").status(StatusEnum.ACTIVE).build();
        ApiResponse<CategoryResponse> response = categoryService.createCategory(request);
        assertEquals(200, response.getCode());
        assertEquals("New Category", response.getResult().getName());
    }

    // TC06 - Tạo Category mới với tên đã tồn tại
    @Test
    void testCreateCategoryWithExistingName_TC06() {
        CategoryRequest request = CategoryRequest.builder().name("Test Category").description("Any Desc").status(StatusEnum.ACTIVE).build();
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(request));
    }

    // TC07 - Cập nhật Category với tên chưa tồn tại
    @Test
    void testUpdateCategoryWithValidData_TC07() {
        CategoryRequest request = CategoryRequest.builder().name("Updated Category").description("Updated Desc").status(StatusEnum.INACTIVE).build();
        ApiResponse<String> response = categoryService.updateCategory(categorySample.getId(), request);
        assertEquals(200, response.getCode());
    }

    // TC08 - Cập nhật Category - Tên đã tồn tại
    @Test
    void testUpdateCategoryWithDuplicateName_TC08() {
        Category duplicate = categoryRepository.save(Category.builder().name("Existing Name").description("xx").status(StatusEnum.ACTIVE).build());
        CategoryRequest request = CategoryRequest.builder().name("Existing Name").description("Update Desc").status(StatusEnum.ACTIVE).build();
        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(categorySample.getId(), request));
    }

    // TC09 - Xoá Category
    @Test
    void testDeleteCategoryValid_TC09() {
        ApiResponse<String> response = categoryService.deleteCategory(categorySample.getId());
        assertEquals(200, response.getCode());
        Category deleted = categoryRepository.findById(categorySample.getId()).orElseThrow();
        assertEquals(StatusEnum.DELETED, deleted.getStatus());
    }

    // TC10 - Xoá Category với ID không có trong DB
    @Test
    void testDeleteCategoryWithInvalidId_TC10() {
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory("invalid-id"));
    }

    // TC11 - Xoá Category đã bị xoá
    @Test
    void testDeleteCategoryAlreadyDeleted_TC11() {
        categorySample.setStatus(StatusEnum.DELETED);
        categoryRepository.save(categorySample);
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(categorySample.getId()));
    }

    // TC12 - Cập nhật Category với dữ liệu null
    @Test
    void testUpdateCategoryWithNullRequest_TC12() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> categoryService.updateCategory(categorySample.getId(), null));
    }

    // TC13 - Cập nhật Category với tên rỗng
    @Test
    void testUpdateCategoryWithBlankName_TC13() {
        CategoryRequest request = CategoryRequest.builder().name("").description("desc").status(StatusEnum.ACTIVE).build();
        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(categorySample.getId(), request));
    }

    // TC14 - Tạo Category với tên null
    @Test
    void testCreateCategoryWithNullName_TC14() {
        CategoryRequest request = CategoryRequest.builder().name(null).description("desc").status(StatusEnum.ACTIVE).build();
        assertThrows(Exception.class, () -> categoryService.createCategory(request));
    }

    // TC15 - Lấy Category với ID null
    @Test
    void testGetCategoryWithNullId_TC15() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> categoryService.getCategory(null));
    }

    // TC16 - Lấy Category theo tên với giá trị null
    @Test
    void testGetCategoriesWithNullName_TC16() {
        Page<CategoryResponse> page = categoryService.getPageAllCategories(null, PageRequest.of(0, 10)).getResult();
        assertNotNull(page);
    }

    // TC17 - Lấy Category phân trang với size = 0
    @Test
    void testGetCategoriesWithZeroPageSize_TC17() {
        assertThrows(IllegalArgumentException.class, () -> categoryService.getPageAllCategories("", PageRequest.of(0, 0)));
    }
}