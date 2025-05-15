package com.doan.backend.services;

import com.doan.backend.dto.request.CartItemRequest;
import com.doan.backend.dto.response.*;
import com.doan.backend.entity.*;
import com.doan.backend.enums.InvoiceStatusEnum;
import com.doan.backend.mapper.CartItemMapper;
import com.doan.backend.mapper.CartMapper;
import com.doan.backend.mapper.InvoiceMapper;
import com.doan.backend.repositories.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
@SpringBootTest
@Transactional
public class TestInvoiceService {

    @MockBean
    private InvoiceRepository invoiceRepository;  // Mock repository

    @MockBean
    private InvoiceMapper invoiceMapper;  // Mock mapper

    @Autowired
    private InvoiceService invoiceService;  // Inject service

    @Test
    @DisplayName("TC_INVOICE_GET_BY_ORDER_01 - Lấy hóa đơn thành công (orderId hợp lệ)")
    void testGetInvoiceByOrderId_Success() {
        // Arrange
        String orderId = "order123";
        Invoice invoice = new Invoice();
        invoice.setId("invoice123");
        invoice.setTotalAmount(BigDecimal.valueOf(100000));
        invoice.setInvoiceNumber("INV-0001");
        invoice.setStatus(InvoiceStatusEnum.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoice.setPayment(new Payment());

        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.of(invoice));
        InvoiceResponse expectedResponse = new InvoiceResponse();
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(expectedResponse);

        // Act
        ApiResponse<InvoiceResponse> actualResponse = invoiceService.getInvoiceByOrderId(orderId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("Success");
        assertThat(actualResponse.getResult()).isEqualTo(expectedResponse);

        // Verify behavior
        verify(invoiceRepository, times(1)).findByOrderId(orderId);
        verify(invoiceMapper, times(1)).toInvoiceResponse(invoice);
    }

    @Test
    @DisplayName("TC_INVOICE_GET_BY_ORDER_02 - Không tìm thấy hóa đơn (orderId không hợp lệ)")
    void testGetInvoiceByOrderId_NotFound() {
        // Arrange
        String orderId = "invalidOrderId";

        // Mock repository để trả về Optional.empty() khi tìm kiếm bằng orderId không hợp lệ
        when(invoiceRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> invoiceService.getInvoiceByOrderId(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invoice not found");

        // Verify behavior
        verify(invoiceRepository, times(1)).findByOrderId(orderId);
    }
    @Test
    @DisplayName("TC_INVOICE_GET_BY_ID_01 - Lấy hóa đơn thành công (invoiceId hợp lệ)")
    void testGetInvoiceById_Success() {
        // Arrange
        String invoiceId = "invoice123";
        Invoice invoice = new Invoice();
        invoice.setId(invoiceId);
        invoice.setTotalAmount(BigDecimal.valueOf(100000));
        invoice.setInvoiceNumber("INV-0001");
        invoice.setStatus(InvoiceStatusEnum.PAID);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoice.setPayment(new Payment());

        // Mock repository để trả về invoice khi tìm kiếm theo invoiceId hợp lệ
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // Mock mapper để chuyển đổi từ Invoice sang InvoiceResponse
        InvoiceResponse expectedResponse = new InvoiceResponse();
        when(invoiceMapper.toInvoiceResponse(invoice)).thenReturn(expectedResponse);

        // Act
        ApiResponse<InvoiceResponse> actualResponse = invoiceService.getInvoiceById(invoiceId);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("Success");
        assertThat(actualResponse.getResult()).isEqualTo(expectedResponse);

        // Verify behavior
        verify(invoiceRepository, times(1)).findById(invoiceId);
        verify(invoiceMapper, times(1)).toInvoiceResponse(invoice);
    }

    @Test
    @DisplayName("TC_INVOICE_GET_BY_ID_02 - Không tìm thấy hóa đơn (invoiceId không hợp lệ)")
    void testGetInvoiceById_NotFound() {
        // Arrange
        String invoiceId = "invalidInvoiceId";

        // Mock repository để trả về Optional.empty() khi tìm kiếm bằng invoiceId không hợp lệ
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> invoiceService.getInvoiceById(invoiceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invoice not found");

        // Verify behavior
        verify(invoiceRepository, times(1)).findById(invoiceId);
    }

    @Test
    @DisplayName("TC_INVOICE_SEARCH_EMAIL_01 - Tìm kiếm hóa đơn theo email thành công (customerEmail hợp lệ, pageable hợp lệ)")
    void testGetAllInvoiceSearchEmail_Success() {
        // Arrange
        String customerEmail = "customer@example.com";
        Pageable pageable = PageRequest.of(0, 10); // giả sử trang đầu tiên và mỗi trang có 10 phần tử

        // Tạo danh sách hóa đơn giả
        Invoice invoice1 = new Invoice();
        invoice1.setId("invoice1");
        invoice1.setTotalAmount(BigDecimal.valueOf(100000));
        invoice1.setInvoiceNumber("INV-0001");
        invoice1.setStatus(InvoiceStatusEnum.PAID);
        invoice1.setCreatedAt(LocalDateTime.now());
        invoice1.setUpdatedAt(LocalDateTime.now());
        invoice1.setPayment(new Payment());

        Invoice invoice2 = new Invoice();
        invoice2.setId("invoice2");
        invoice2.setTotalAmount(BigDecimal.valueOf(200000));
        invoice2.setInvoiceNumber("INV-0002");
        invoice2.setStatus(InvoiceStatusEnum.UNPAID);
        invoice2.setCreatedAt(LocalDateTime.now());
        invoice2.setUpdatedAt(LocalDateTime.now());
        invoice2.setPayment(new Payment());

        // Mock repository để trả về Page<Invoice> khi tìm kiếm theo email
        Page<Invoice> invoicesPage = new PageImpl<>(List.of(invoice1, invoice2));
        when(invoiceRepository.findByAllSearchEmail(customerEmail, pageable)).thenReturn(invoicesPage);

        // Mock mapper để chuyển đổi từ Invoice sang InvoiceResponse
        InvoiceResponse invoiceResponse1 = new InvoiceResponse();
        InvoiceResponse invoiceResponse2 = new InvoiceResponse();
        when(invoiceMapper.toInvoiceResponse(invoice1)).thenReturn(invoiceResponse1);
        when(invoiceMapper.toInvoiceResponse(invoice2)).thenReturn(invoiceResponse2);

        // Act
        ApiResponse<Page<InvoiceResponse>> actualResponse = invoiceService.getAllInvoiceSearchEmail(customerEmail, pageable);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getCode()).isEqualTo(200);
        assertThat(actualResponse.getMessage()).isEqualTo("Success");

        // Kiểm tra xem kết quả có đúng như mong đợi không
        Page<InvoiceResponse> result = actualResponse.getResult();
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactly(invoiceResponse1, invoiceResponse2);

        // Verify behavior
        verify(invoiceRepository, times(1)).findByAllSearchEmail(customerEmail, pageable);
        verify(invoiceMapper, times(1)).toInvoiceResponse(invoice1);
        verify(invoiceMapper, times(1)).toInvoiceResponse(invoice2);
    }

    @Test
    @DisplayName("TC_INVOICE_UPDATE_STATUS_01 - Cập nhật trạng thái hóa đơn thành công (invoice hợp lệ, trạng thái mới)")
    void testUpdateInvoiceStatus_Success() {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setId("invoice123");
        invoice.setTotalAmount(BigDecimal.valueOf(100000));
        invoice.setInvoiceNumber("INV-0001");
        invoice.setStatus(InvoiceStatusEnum.UNPAID); // Trạng thái ban đầu là UNPAID
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());
        invoice.setPayment(new Payment());

        InvoiceStatusEnum newStatus = InvoiceStatusEnum.PAID; // Trạng thái mới là PAID

        // Mock repository để trả về hóa đơn đã cập nhật trạng thái
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        // Act
        Invoice actualInvoice = invoiceService.updateInvoiceStatus(invoice, newStatus);

        // Assert
        assertThat(actualInvoice).isNotNull();
        assertThat(actualInvoice.getStatus()).isEqualTo(newStatus);
        assertThat(actualInvoice.getId()).isEqualTo(invoice.getId());
        assertThat(actualInvoice.getTotalAmount()).isEqualTo(invoice.getTotalAmount());

        // Verify behavior
        verify(invoiceRepository, times(1)).save(invoice);
    }

}
