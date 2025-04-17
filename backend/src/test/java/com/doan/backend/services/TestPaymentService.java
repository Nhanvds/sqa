package com.doan.backend.services;

import com.doan.backend.dto.request.PaymentRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.entity.Invoice;
import com.doan.backend.entity.Payment;
import com.doan.backend.enums.InvoiceStatusEnum;
import com.doan.backend.enums.PaymentMethodEnum;
import com.doan.backend.enums.PaymentStatusEnum;
import com.doan.backend.repositories.InvoiceRepository;
import com.doan.backend.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.times;

@SpringBootTest
@Transactional
public class TestPaymentService {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private PayOS payOS;

    @Value("${app.client-url}")
    private String clientUrl;

    @InjectMocks
    private PaymentService paymentService;  // Service cần test
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Khởi tạo mock
    }

    @Test
    @DisplayName("TC_PAYMENT_006: Payment không tồn tại - paymentId không tồn tại - Báo lỗi 'Payment not found' - Kiểm tra paymentId có trong DB không")
    void testUpdatePayment_PaymentNotFound() {
        // Arrange
        String paymentId = "non-existent-payment-id";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentStatus(PaymentStatusEnum.COMPLETED);

        // Mock behavior
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty()); // Trả về Optional.empty() nếu không tìm thấy Payment

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> paymentService.updatePayment(paymentId, paymentRequest));
        assertEquals("Payment not found", exception.getMessage()); // Kiểm tra thông báo lỗi

        // Verify behavior of mocks
        verify(paymentRepository).findById(paymentId);  // Kiểm tra phương thức findById đã được gọi đúng một lần
    }

    @Test
    @DisplayName("TC_PAYMENT_007: Thiếu paymentMethod hoặc amount khi paymentStatus = COMPLETED - Báo lỗi 'Payment status and amount are required' - Validate trường bắt buộc khi hoàn thành thanh toán")
    void TC_PAYMENT_007_MissingPaymentMethodOrAmount() {
        // Arrange
        String paymentId = "valid-payment-id";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentStatus(PaymentStatusEnum.COMPLETED);
        // Lúc này paymentMethod và amount chưa được set nên sẽ gây lỗi

        // Mock behavior
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(new Payment())); // Trả về Payment hợp lệ

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> paymentService.updatePayment(paymentId, paymentRequest));
        assertEquals("Payment status and amount are required", exception.getMessage()); // Kiểm tra thông báo lỗi

        // Verify behavior of mocks
        verify(paymentRepository).findById(paymentId);  // Kiểm tra phương thức findById đã được gọi đúng một lần
    }

    @Test
    @DisplayName("TC_PAYMENT_008: Update COMPLETED thành công - paymentRequest đầy đủ paymentStatus, paymentMethod, amount - Cập nhật thành công payment và invoice thành PAID")
    void TC_PAYMENT_008_UpdateCompletedSuccessfully() {
        // Arrange
        String paymentId = "valid-payment-id";

        // Tạo PaymentRequest đầy đủ
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(PaymentMethodEnum.TRANSFER);  // Cung cấp paymentMethod
        paymentRequest.setAmount(new BigDecimal("100.00"));  // Cung cấp amount
        paymentRequest.setPaymentStatus(PaymentStatusEnum.COMPLETED);  // Cập nhật paymentStatus thành COMPLETED

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatusEnum.PENDING);  // Trạng thái ban đầu là PENDING
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatusEnum.UNPAID);  // Trạng thái invoice ban đầu là UNPAID
        payment.setInvoice(invoice);  // Gắn invoice cho payment

        // Mock behavior
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));  // Trả về Payment hợp lệ
        when(invoiceRepository.save(invoice)).thenReturn(invoice);  // Khi gọi save cho invoice, trả về invoice đã cập nhật

        // Act
        ApiResponse<Void> response = paymentService.updatePayment(paymentId, paymentRequest);  // Thực hiện cập nhật payment

        // Assert
        assertEquals(200, response.getCode());  // Kiểm tra mã phản hồi trả về là 200 (thành công)
        assertEquals("Success", response.getMessage());  // Kiểm tra thông điệp trả về là "Success"

        // Kiểm tra trạng thái của invoice đã được thay đổi thành PAID
        assertEquals(InvoiceStatusEnum.PAID, payment.getInvoice().getStatus());

        // Verify behavior of mocks
        verify(paymentRepository).findById(paymentId);  // Kiểm tra phương thức findById đã được gọi đúng một lần
        verify(paymentRepository).save(payment);  // Kiểm tra phương thức save đã được gọi đúng một lần
        verify(invoiceRepository).save(invoice);  // Kiểm tra phương thức save đã được gọi cho invoice
    }

    @Test
    @DisplayName("TC_PAYMENT_009: Update trạng thái khác thành công - paymentStatus != COMPLETED - Cập nhật payment thành công")
    void TC_PAYMENT_009_UpdateStatusSuccessfully() {
        // Arrange
        String paymentId = "valid-payment-id";

        // Tạo PaymentRequest với paymentStatus khác COMPLETED (ví dụ: PENDING)
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod(PaymentMethodEnum.TRANSFER);  // Cung cấp paymentMethod
        paymentRequest.setAmount(new BigDecimal("100.00"));  // Cung cấp amount
        paymentRequest.setPaymentStatus(PaymentStatusEnum.PENDING);  // paymentStatus khác COMPLETED, ở đây là PENDING

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setPaymentStatus(PaymentStatusEnum.PENDING);  // Trạng thái ban đầu là PENDING
        Invoice invoice = new Invoice();
        invoice.setStatus(InvoiceStatusEnum.UNPAID);  // Trạng thái invoice ban đầu là UNPAID
        payment.setInvoice(invoice);  // Gắn invoice cho payment

        // Mock behavior
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));  // Trả về Payment hợp lệ

        // Act
        ApiResponse<Void> response = paymentService.updatePayment(paymentId, paymentRequest);  // Thực hiện cập nhật payment

        // Assert
        assertEquals(200, response.getCode());  // Kiểm tra mã phản hồi trả về là 200 (thành công)
        assertEquals("Success", response.getMessage());  // Kiểm tra thông điệp trả về là "Success"

        // Kiểm tra trạng thái của invoice không thay đổi (vẫn là UNPAID)
        assertEquals(InvoiceStatusEnum.UNPAID, payment.getInvoice().getStatus());

        // Verify behavior of mocks
        verify(paymentRepository).findById(paymentId);  // Kiểm tra phương thức findById đã được gọi đúng một lần
        verify(paymentRepository).save(payment);  // Kiểm tra phương thức save đã được gọi đúng một lần
        verify(invoiceRepository, never()).save(any(Invoice.class));  // Không gọi save cho invoice (invoice không được thay đổi)
    }


    @Test
    @DisplayName("TC_PAYMENT_001: Invoice không tồn tại - invoiceId không tồn tại - Báo lỗi 'Invoice not found'")
    void TC_PAYMENT_001_InvoiceNotFound() {
        // Arrange
        String invoiceId = "non-existing-invoice-id";

        // Mock behavior: Khi gọi findById, trả về Optional.empty() vì không có invoice
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPaymentLink(invoiceId);  // Gọi phương thức tạo payment link
        });

        // Kiểm tra thông báo lỗi là "Invoice not found"
        assertEquals("Invoice not found", thrown.getMessage());

        // Verify that the invoiceRepository's findById method was called once
        verify(invoiceRepository).findById(invoiceId);
    }




}
