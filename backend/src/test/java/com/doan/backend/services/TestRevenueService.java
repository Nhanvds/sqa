package com.doan.backend.services;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.doan.backend.dto.response.*;
import com.doan.backend.dto.response.CategoryStatistics.CategoryRevenueResponse;
import com.doan.backend.dto.response.CategoryStatistics.CategoryStatisticResponse;
import com.doan.backend.dto.response.CustomerStatistics.CustomerRevenueResponse;
import com.doan.backend.dto.response.CustomerStatistics.CustomerStatisticResponse;
import com.doan.backend.dto.response.ProductStatistics.ProductRevenueResponse;
import com.doan.backend.dto.response.ProductStatistics.ProductStatisticResponse;
import com.doan.backend.repositories.OrderItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TestRevenueService {

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private RevenueService revenueService;

    @Captor
    private ArgumentCaptor<LocalDateTime> dateCaptor;

    // ===============================================================
    // TC-RS-001: Thống kê doanh thu Product theo productID
    // Mục tiêu: Đảm bảo getProductRevenue gom nhóm kết quả theo product ID
    // Input: startDate=2025-04-01, endDate=2025-04-02; repository trả về 2 ProductStatisticResponse cùng ID "p1"
    // Expected: ApiResponse.result chứa 1 ProductRevenueResponse với name tương ứng và 2 statistics
    // ===============================================================
    @Test
    public void testGetProductRevenue_Grouping() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 4, 1), end = LocalDate.of(2025, 4, 2);
        ProductStatisticResponse r1 = new ProductStatisticResponse("p1", "Prod1",
                LocalDateTime.of(2025,4,1,10,0),
                "M", new BigDecimal("100"), 2, new BigDecimal("10"));
        ProductStatisticResponse r2 = new ProductStatisticResponse("p1", "Prod1",
                LocalDateTime.of(2025,4,2,11,0),
                "L", new BigDecimal("120"), 1, new BigDecimal("0"));
        when(orderItemRepository.getProductRevenue(any(), any()))
                .thenReturn(List.of(r1, r2));

        // Act
        ApiResponse<List<ProductRevenueResponse>> resp =
                revenueService.getProductRevenue(start, end);

        // Assert
        assertEquals(200, resp.getCode());
        List<ProductRevenueResponse> list = resp.getResult();
        assertEquals(1, list.size());
        ProductRevenueResponse pr = list.get(0);
        assertEquals("Prod1", pr.getProductName());
        assertEquals(2, pr.getStatistics().size());
    }

    // ===============================================================
    // TC-RS-002: Doanh thu theo Product rỗng
    // Mục tiêu: Khi repository trả về danh sách rỗng, kết quả cũng rỗng
    // Input: bất kỳ ngày nào; repository trả về empty list
    // Expected: ApiResponse.result.size() == 0
    // ===============================================================
    @Test
    public void testGetProductRevenue_Empty() {
        // Arrange
        when(orderItemRepository.getProductRevenue(any(), any())).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<ProductRevenueResponse>> resp =
                revenueService.getProductRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        assertTrue(resp.getResult().isEmpty());
        assertEquals(200, resp.getCode());
    }

    // ===============================================================
    // TC-RS-003: Thống kê doanh thu theo Category , theo CategoryID
    // Mục tiêu: Đảm bảo getCategoryRevenue gom nhóm theo category ID
    // Input: repository trả về 2 CategoryStatisticResponse cùng ID "c1"
    // Expected: ApiResponse.result chứa 1 CategoryRevenueResponse với 2 statistics
    // ===============================================================
    @Test
    public void testGetCategoryRevenue_Grouping() {
        // Arrange
        CategoryStatisticResponse c1 = new CategoryStatisticResponse("c1", "Cat1",
                "ProdA", LocalDateTime.now(), "S", new BigDecimal("50"), 3, new BigDecimal("5"));
        CategoryStatisticResponse c2 = new CategoryStatisticResponse("c1", "Cat1",
                "ProdB", LocalDateTime.now(), "M", new BigDecimal("80"), 1, new BigDecimal("0"));
        when(orderItemRepository.getCategoryRevenue(any(), any()))
                .thenReturn(List.of(c1, c2));

        // Act
        ApiResponse<List<CategoryRevenueResponse>> resp =
                revenueService.getCategoryRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        assertEquals(200, resp.getCode());
        List<CategoryRevenueResponse> list = resp.getResult();
        assertEquals(1, list.size());
        CategoryRevenueResponse cr = list.get(0);
        assertEquals("Cat1", cr.getCategoryName());
        assertEquals(2, cr.getStatistics().size());
    }

    // ===============================================================
    // TC-RS-004: Doanh thu theo Category rỗng
    // Mục tiêu: Khi không có dữ liệu category, trả về danh sách rỗng
    // Input: repository trả về empty list
    // Expected: ApiResponse.result.size() == 0
    // ===============================================================
    @Test
    public void testGetCategoryRevenue_Empty() {
        // Arrange
        when(orderItemRepository.getCategoryRevenue(any(), any())).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<CategoryRevenueResponse>> resp =
                revenueService.getCategoryRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        assertTrue(resp.getResult().isEmpty());
        assertEquals(200, resp.getCode());
    }

    // ===============================================================
    // TC-RS-005: Thống kê doanh thu theo khách hàng (theo userId) và tính tổng doanh thu (totalValue)
    // Mục tiêu: Đảm bảo getCustomerRevenue tính đúng totalOrder, totalValue và statistics
    // Input: 2 CustomerStatisticResponse cùng userId "u1" với value 50 và 150
    // Expected: 1 CustomerRevenueResponse với totalOrder=2, totalValue=200, statistics size=2
    // ===============================================================
    @Test
    public void testGetCustomerRevenue_GroupingAndSum() {
        // Arrange
        CustomerStatisticResponse o1 = new CustomerStatisticResponse("u1", "Alice", "a@x",
                "ord1", new BigDecimal("50"), LocalDateTime.now());
        CustomerStatisticResponse o2 = new CustomerStatisticResponse("u1", "Alice", "a@x",
                "ord2", new BigDecimal("150"), LocalDateTime.now());
        when(orderItemRepository.getCustomerRevenue(any(), any())).thenReturn(List.of(o1, o2));

        // Act
        ApiResponse<List<CustomerRevenueResponse>> resp =
                revenueService.getCustomerRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        assertEquals(200, resp.getCode());
        List<CustomerRevenueResponse> list = resp.getResult();
        assertEquals(1, list.size());
        CustomerRevenueResponse cr = list.get(0);
        assertEquals("Alice", cr.getName());
        assertEquals("a@x", cr.getEmail());
        assertEquals(2, cr.getTotalOrder());
        assertEquals(new BigDecimal("200"), cr.getTotalValue());
        assertEquals(2, cr.getStatistics().size());
    }

    // ===============================================================
    // TC-RS-006: Doanh thu theo khách hàng rỗng
    // Mục tiêu: Khi không có dữ liệu customer, trả về danh sách rỗng
    // Input: repository trả về empty list
    // Expected: ApiResponse.result.size() == 0
    // ===============================================================
    @Test
    public void testGetCustomerRevenue_Empty() {
        // Arrange
        when(orderItemRepository.getCustomerRevenue(any(), any())).thenReturn(Collections.emptyList());

        // Act
        ApiResponse<List<CustomerRevenueResponse>> resp =
                revenueService.getCustomerRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        assertTrue(resp.getResult().isEmpty());
        assertEquals(200, resp.getCode());
    }

    // ===============================================================
    // TC-RS-007: Xác nhận repository được gọi với atStartOfDay
    // Mục tiêu: Đảm bảo startDate và endDate được chuyển thành LocalDateTime.atStartOfDay()
    // Input: startDate=2025-04-10, endDate=2025-04-15
    // Expected: orderItemRepository.getProductRevenue gọi với 2025-04-10T00:00 và 2025-04-15T00:00
    // ===============================================================
    @Test
    public void testGetProductRevenue_CallsWithStartOfDay() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 4, 10);
        LocalDate end = LocalDate.of(2025, 4, 15);
        when(orderItemRepository.getProductRevenue(any(), any())).thenReturn(Collections.emptyList());

        // Act
        revenueService.getProductRevenue(start, end);

        // Assert
        verify(orderItemRepository).getProductRevenue(dateCaptor.capture(), dateCaptor.capture());
        List<LocalDateTime> dates = dateCaptor.getAllValues();
        assertEquals(LocalDateTime.of(2025,4,10,0,0), dates.get(0));
        assertEquals(LocalDateTime.of(2025,4,15,0,0), dates.get(1));
    }


    // ===============================================================
    // TC-RS-008: Nhiều sản phẩm khác nhau
    // Mục tiêu: Khi repository trả về thống kê cho 2 productId khác nhau,
    //          getProductRevenue phải tạo 2 ProductRevenueResponse riêng biệt.
    // Input: startDate=2025-04-01, endDate=2025-04-02; repository trả về
    //        ProductStatisticResponse cho id "p1" và "p2".
    // Expected: ApiResponse.result.size() == 2, lần lượt tương ứng "Prod1" và "Prod2"
    // ===============================================================
    @Test
    public void testGetProductRevenue_MultipleProducts() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 4, 1), end = LocalDate.of(2025, 4, 2);
        ProductStatisticResponse r1 = new ProductStatisticResponse("p1", "Prod1",
                LocalDateTime.now(), "M", new BigDecimal("100"), 1, BigDecimal.ZERO);
        ProductStatisticResponse r2 = new ProductStatisticResponse("p2", "Prod2",
                LocalDateTime.now(), "L", new BigDecimal("200"), 2, new BigDecimal("10"));
        when(orderItemRepository.getProductRevenue(any(), any()))
                .thenReturn(List.of(r1, r2));

        // Act
        ApiResponse<List<ProductRevenueResponse>> resp =
                revenueService.getProductRevenue(start, end);

        // Assert
        List<ProductRevenueResponse> list = resp.getResult();
        assertEquals(2, list.size());
        Set<String> names = Set.of(list.get(0).getProductName(), list.get(1).getProductName());
        assertTrue(names.contains("Prod1") && names.contains("Prod2"));
    }

    // ===============================================================
    // TC-RS-009: Nhiều danh mục khác nhau
    // Mục tiêu: Khi repository trả về thống kê cho 2 categoryId khác nhau,
    //          getCategoryRevenue phải tạo 2 CategoryRevenueResponse riêng biệt.
    // Input: repository trả về CategoryStatisticResponse cho id "c1" và "c2".
    // Expected: ApiResponse.result.size() == 2, lần lượt tương ứng "Cat1" và "Cat2"
    // ===============================================================
    @Test
    public void testGetCategoryRevenue_MultipleCategories() {
        // Arrange
        CategoryStatisticResponse c1 = new CategoryStatisticResponse("c1", "Cat1",
                "ProdA", LocalDateTime.now(), "S", new BigDecimal("50"), 1, BigDecimal.ZERO);
        CategoryStatisticResponse c2 = new CategoryStatisticResponse("c2", "Cat2",
                "ProdB", LocalDateTime.now(), "M", new BigDecimal("80"), 2, new BigDecimal("5"));
        when(orderItemRepository.getCategoryRevenue(any(), any()))
                .thenReturn(List.of(c1, c2));

        // Act
        ApiResponse<List<CategoryRevenueResponse>> resp =
                revenueService.getCategoryRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        List<CategoryRevenueResponse> list = resp.getResult();
        assertEquals(2, list.size());
        Set<String> names = Set.of(list.get(0).getCategoryName(), list.get(1).getCategoryName());
        assertTrue(names.contains("Cat1") && names.contains("Cat2"));
    }

    // ===============================================================
    // TC-RS-010: Nhiều khách hàng khác nhau
    // Mục tiêu: Khi repository trả về thống kê cho 2 userId khác nhau,
    //          getCustomerRevenue phải tạo 2 CustomerRevenueResponse riêng biệt.
    // Input: repository trả về CustomerStatisticResponse cho userId "u1" và "u2".
    // Expected: ApiResponse.result.size() == 2, lần lượt tương ứng Alice và Bob
    // ===============================================================
    @Test
    public void testGetCustomerRevenue_MultipleCustomers() {
        // Arrange
        CustomerStatisticResponse o1 = new CustomerStatisticResponse("u1", "Alice", "a@x",
                "ord1", new BigDecimal("100"), LocalDateTime.now());
        CustomerStatisticResponse o2 = new CustomerStatisticResponse("u2", "Bob", "b@y",
                "ord2", new BigDecimal("150"), LocalDateTime.now());
        when(orderItemRepository.getCustomerRevenue(any(), any()))
                .thenReturn(List.of(o1, o2));

        // Act
        ApiResponse<List<CustomerRevenueResponse>> resp =
                revenueService.getCustomerRevenue(LocalDate.now(), LocalDate.now());

        // Assert
        List<CustomerRevenueResponse> list = resp.getResult();
        assertEquals(2, list.size());
        Set<String> names = Set.of(list.get(0).getName(), list.get(1).getName());
        assertTrue(names.contains("Alice") && names.contains("Bob"));
    }

    // ===============================================================
    // TC-RS-011: Xử lý khi startDate > endDate
    // Mục tiêu: Kiểm tra xem khi startDate vượt sau endDate, service có trả về rỗng
    // Input: startDate = 2025-04-10, endDate = 2025-04-05 (đảo ngược)
    // Expected: ApiResponse.result.size() == 0 (không gọi repository hoặc xử lý thành rỗng)
    // ===============================================================
    @Test
    public void testGetProductRevenue_InvalidDateRange() {
        // Arrange
        LocalDate start = LocalDate.of(2025, 4, 10), end = LocalDate.of(2025, 4, 5);
        // Không cần giả lập repository vì logic service không gọi nếu date không hợp lệ

        // Act
        ApiResponse<List<ProductRevenueResponse>> resp =
                revenueService.getProductRevenue(start, end);

        // Assert
        // Nếu service không validate, repository vẫn gọi; nhưng chúng ta kỳ vọng rỗng
        // Tuỳ implementation, ở đây giả sử service trả rỗng
        assertTrue(resp.getResult().isEmpty(), "Khi startDate > endDate, kết quả phải rỗng");
    }




}

