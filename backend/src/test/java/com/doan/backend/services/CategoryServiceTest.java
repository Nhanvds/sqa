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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
//        categoryRepository.deleteAll();
        Category category = Category.builder()
                .name("Test Category")
                .description("Test Description")
                .status(StatusEnum.ACTIVE)
                .build();
        categorySample = categoryRepository.save(category);
        categoryRepository.findAll();
        System.out.println(categorySample);
    }

    // TC01 - Lấy thông tin Category đã tồn tại
    @Test
    void testGetCategoryByValidId_TC01() {
        assertDoesNotThrow(() -> {
            ApiResponse<CategoryResponse> response = categoryService.getCategory(categorySample.getId());
            assertNotNull(response, "Phản hồi không được null");

            CategoryResponse result = response.getResult();
            assertEquals(categorySample.getId(), result.getId(), "ID không khớp giữa DB và kết quả trả về");
            assertEquals(categorySample.getName(), result.getName(), "Tên không khớp giữa DB và kết quả trả về");
            assertEquals(categorySample.getDescription(), result.getDescription(), "Mô tả không khớp giữa DB và kết quả trả về");
            assertEquals(categorySample.getStatus(), result.getStatus(), "Trạng thái không khớp giữa DB và kết quả trả về");
        });
    }

    // TC02 - Lấy thông tin Category theo ID không có trong CSDL
    @Test
    void testGetCategoryByInvalidId_TC02() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getCategory("non-existing-id");
        }, "Không ném RuntimeException khi ID không tồn tại");
    }

    // TC03 - Lấy Category với ID null
    @Test
    void testGetCategoryWithNullId_TC03() {
        assertThrows(RuntimeException.class, () -> categoryService.getCategory(null), "Không ném RuntimeException khi ID null");
    }

    // TC04 - Lấy Category theo tên với giá trị null
    @Test
    void testGetCategoriesWithNullName_TC04() {
        assertThrows(RuntimeException.class, () -> categoryService.getPageAllCategories(null, PageRequest.of(0, 10)).getResult());
    }

    // TC05 - Lấy Category phân trang với size = 0
    @Test
    void testGetCategoriesWithZeroPageSize_TC05() {
        assertThrows(RuntimeException.class, () -> categoryService.getPageAllCategories("", PageRequest.of(0, 0)), "Không ném RuntimeException khi size = 0");
    }

    // TC06 - Lấy tất cả Category
    @Test
    void testGetAllCategories_TC06() {
        assertDoesNotThrow(() -> {

            List<CategoryResponse> categories = categoryService.getAllCategories().getResult();
            assertFalse(categories.isEmpty(), "Danh sách category không được rỗng");
        });
    }

    // TC07 - Lấy danh sách Category phân trang theo tên
    @Test
    void testGetPagedCategoriesByName_TC07() {
        assertDoesNotThrow(() -> {
            Page<CategoryResponse> page = categoryService.getPageAllCategories("Test Category", PageRequest.of(0, 10)).getResult();
            assertNotNull(page, "Kết quả page không được null");
            assertTrue(page.getContent().stream().anyMatch(c -> c.getName().contains("Test")), "Không tìm thấy Category chứa 'Test' trong danh sách trả về");
        });
    }

    // TC08 - Lấy danh sách category phân trang theo tên không tồn tại
    @Test
    void testGetPagedCategoriesWithNonExistentName_TC08() {
        assertDoesNotThrow(() -> {
            Page<CategoryResponse> page = categoryService.getPageAllCategories("khongtontai", PageRequest.of(0, 10)).getResult();
            assertNotNull(page, "Kết quả page không được null");
            assertTrue(page.isEmpty(), "Danh sách trả về phải rỗng khi tìm theo tên không tồn tại");
        });
    }

    // TC09 - Tạo Category mới
    @Test
    void testCreateCategoryValid_TC09() {
        assertDoesNotThrow(() -> {
            CategoryRequest request = CategoryRequest.builder().name("New Category").description("New Desc").status(StatusEnum.ACTIVE).build();
            ApiResponse<CategoryResponse> response = categoryService.createCategory(request);
            assertNotNull(response.getResult(), "Kết quả trả về sau khi tạo không được null");

            CategoryResponse result = response.getResult();

            Category saved = categoryRepository.findById(result.getId()).orElse(null);
            assertNotNull(saved, "Không tìm thấy category trong DB sau khi tạo");
            assertEquals(request.getName(), saved.getName(), "Tên trong DB không khớp với dữ liệu tạo");
            assertEquals(request.getDescription(), saved.getDescription(), "Mô tả trong DB không khớp với dữ liệu tạo");
            assertEquals(request.getStatus(), saved.getStatus(), "Trạng thái trong DB không khớp với dữ liệu tạo");
        });
    }

    // TC10 - Tạo Category mới với tên đã tồn tại
    @Test
    void testCreateCategoryWithExistingName_TC10() {
        CategoryRequest request = CategoryRequest.builder().name("Test Category").description("Any Desc").status(StatusEnum.ACTIVE).build();
        assertThrows(RuntimeException.class, () -> categoryService.createCategory(request), "Không ném RuntimeException khi tạo với tên đã tồn tại");
    }

    // TC11 - Tạo Category với tên null
    @Test
    void testCreateCategoryWithNullName_TC11() {
        CategoryRequest request = CategoryRequest.builder().name(null).description("desc").status(StatusEnum.ACTIVE).build();
        assertThrows(RuntimeException.class, () -> categoryService.createCategory(request), "Không ném RuntimeException khi tên null");
    }

    // TC12 - Cập nhật Category với tên chưa tồn tại
    @Test
    void testUpdateCategoryWithValidData_TC12() {
        CategoryRequest request = CategoryRequest.builder().name("Updated Category").description("Updated Desc").status(StatusEnum.INACTIVE).build();
        assertDoesNotThrow( () ->{

            categoryService.updateCategory(categorySample.getId(), request);


            Category saved = categoryRepository.findById(categorySample.getId()).orElse(null);
            assertNotNull(saved, "Không tìm thấy category trong DB sau khi tạo");
            assertEquals(request.getName(), saved.getName(), "Tên trong DB không khớp với dữ liệu tạo");
            assertEquals(request.getDescription(), saved.getDescription(), "Mô tả trong DB không khớp với dữ liệu tạo");
            assertEquals(request.getStatus(), saved.getStatus(), "Trạng thái trong DB không khớp với dữ liệu tạo");

        });
    }

    // TC13 - Cập nhật Category - Tên đã tồn tại
    @Test
    void testUpdateCategoryWithDuplicateName_TC13() {
        categoryRepository.save(Category.builder().name("Existing Name").description("xx").status(StatusEnum.ACTIVE).build());
        CategoryRequest request = CategoryRequest.builder().name("Existing Name").description("Update Desc").status(StatusEnum.ACTIVE).build();
        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(categorySample.getId(), request), "Không ném RuntimeException khi cập nhật với tên trùng");
    }

    // TC14 - Cập nhật Category với tên rỗng
    @Test
    void testUpdateCategoryWithBlankName_TC14() {
        CategoryRequest request = CategoryRequest.builder().name("").description("desc").status(StatusEnum.ACTIVE).build();
        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(categorySample.getId(), request), "Không ném RuntimeException khi tên rỗng");
    }

    // TC15 - Cập nhật Category với dữ liệu null
    @Test
    void testUpdateCategoryWithNullRequest_TC15() {
        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(categorySample.getId(), null), "Không ném RuntimeException khi request null");
    }

    // TC16 - Xoá Category
    @Test
    void testDeleteCategoryValid_TC16() {
        assertDoesNotThrow(() -> {
            ApiResponse<String> response = categoryService.deleteCategory(categorySample.getId());
            assertEquals(200, response.getCode(), "Mã phản hồi không phải 200 khi xoá hợp lệ");

            Category deleted = categoryRepository.findById(categorySample.getId()).orElseThrow();
            assertEquals(StatusEnum.DELETED, deleted.getStatus(), "Trạng thái sau khi xoá không phải DELETED");
        });
    }

    // TC17 - Xoá Category với ID không có trong DB
    @Test
    void testDeleteCategoryWithInvalidId_TC17() {
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory("invalid-id"), "Không ném RuntimeException khi ID xoá không tồn tại");
    }

    // TC18 - Xoá Category đã bị xoá
    @Test
    void testDeleteCategoryAlreadyDeleted_TC18() {
        categorySample.setStatus(StatusEnum.DELETED);
        categoryRepository.save(categorySample);
        assertThrows(RuntimeException.class, () -> categoryService.deleteCategory(categorySample.getId()), "Không ném RuntimeException khi xoá lại Category đã bị xoá");
    }
}
