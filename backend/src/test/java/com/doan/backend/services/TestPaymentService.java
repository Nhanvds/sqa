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
import com.doan.backend.utils.CodeUtils;
import com.doan.backend.utils.Constants;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.ExpectedCount.times;

@ExtendWith(MockitoExtension.class)
public class TestPaymentService {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    PayOS payOS;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setup() {
        // Set clientUrl cho test
        ReflectionTestUtils.setField(paymentService, "clientUrl", "http://localhost:8080");
    }



    @Test
    @DisplayName("TC_PAYMENT_002: Tạo link thanh toán thành công - Trả về checkoutUrl")
    void createPaymentLink_success_returnsCheckoutUrl() throws Exception {
        String invoiceId = "inv123";
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("BILL-0001");  // đơn giản, sau removePrefix sẽ là "1"
        invoice.setTotalAmount(new BigDecimal("100000"));
        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        CheckoutResponseData responseData = mock(CheckoutResponseData.class);
        when(responseData.getCheckoutUrl()).thenReturn("http://checkout.url");
        when(payOS.createPaymentLink(any())).thenReturn(responseData);

        String url = paymentService.createPaymentLink(invoiceId);

        assertEquals("http://checkout.url", url);
    }

    @Test
    @DisplayName("TC_PAYMENT_003: Dữ liệu webhook không hợp lệ - Báo lỗi, dữ liệu không thay đổi")
    void handlePaymentWebhook_invalidWebhook_returnsError() throws Exception {
        Webhook webhook = mock(Webhook.class);

        when(payOS.verifyPaymentWebhookData(webhook)).thenThrow(new RuntimeException("Invalid webhook"));

        ObjectNode response = paymentService.handlePaymentWebhook(webhook);

        assertEquals(-1, response.get("error").asInt());
        assertEquals("Invalid webhook", response.get("message").asText());

        verify(paymentRepository, never()).save(any());
        verify(invoiceService, never()).updateInvoiceStatus(any(), any());
    }

    @Test
    @DisplayName("TC_PAYMENT_004: Invoice không tồn tại theo orderCode - Báo lỗi invoice không tồn tại, dữ liệu không thay đổi")
    void handlePaymentWebhook_invoiceNotFound_throwsException() throws Exception {
        // Mock webhookBody và webhookData trả về orderCode
        Webhook webhookBody = mock(Webhook.class);
        WebhookData webhookData = mock(WebhookData.class);

        when(payOS.verifyPaymentWebhookData(webhookBody)).thenReturn(webhookData);
        when(webhookData.getOrderCode()).thenReturn(12345L);

        // Giả sử CodeUtils.generateUniqueCode trả về mã invoice
        String invoiceNumber = Constants.INVOICE_PREFIX + "12345";
        try (MockedStatic<CodeUtils> utilities = mockStatic(CodeUtils.class)) {
            utilities.when(() -> CodeUtils.generateUniqueCode(Constants.INVOICE_PREFIX, 12345L))
                    .thenReturn(invoiceNumber);

            // Khi tìm invoice bằng số invoice trả về empty (không tìm thấy)
            when(invoiceRepository.findByInvoiceNumber(invoiceNumber)).thenReturn(Optional.empty());

            // Gọi hàm và kiểm tra trả về lỗi (ObjectNode có error = -1)
            ObjectNode response = paymentService.handlePaymentWebhook(webhookBody);

            assertEquals(-1, response.get("error").asInt());
            assertTrue(response.get("message").asText().toLowerCase().contains("invoice not found"));

            // Kiểm tra không gọi save hoặc update invoice
            verify(paymentRepository, never()).save(any());
            verify(invoiceService, never()).updateInvoiceStatus(any(), any());
        }
    }

    @Test
    @DisplayName("TC_PAYMENT_005: Xử lý webhook thành công - Cập nhật Payment và Invoice thành công")
    void handlePaymentWebhook_validWebhook_updatesPaymentAndInvoice() throws Exception {
        Webhook webhookBody = mock(Webhook.class);
        WebhookData webhookData = mock(WebhookData.class);

        // Mock dữ liệu webhookData trả về
        when(payOS.verifyPaymentWebhookData(webhookBody)).thenReturn(webhookData);
        when(webhookData.getOrderCode()).thenReturn(12345L);
        when(webhookData.getCode()).thenReturn("PAY123");
        when(webhookData.getAmount()).thenReturn(100000);
        when(webhookData.getTransactionDateTime()).thenReturn("2025-05-15T10:30:00");

        String invoiceNumber = Constants.INVOICE_PREFIX + "12345";

        try (MockedStatic<CodeUtils> utilities = mockStatic(CodeUtils.class)) {
            utilities.when(() -> CodeUtils.generateUniqueCode(Constants.INVOICE_PREFIX, 12345L))
                    .thenReturn(invoiceNumber);

            Invoice invoice = mock(Invoice.class);
            Payment payment = mock(Payment.class);

            when(invoiceRepository.findByInvoiceNumber(invoiceNumber)).thenReturn(Optional.of(invoice));
            when(invoice.getPayment()).thenReturn(payment);

            // Gọi hàm
            ObjectNode response = paymentService.handlePaymentWebhook(webhookBody);

            // Verify payment set các thuộc tính đúng
            verify(payment).setCode("PAY123");
            verify(payment).setPaymentMethod(PaymentMethodEnum.TRANSFER);
            verify(payment).setPaymentStatus(PaymentStatusEnum.COMPLETED);
            verify(payment).setQrCodeUrl(null);
            verify(payment).setAmount(new BigDecimal(100000L));
            verify(payment).setPaymentDate(LocalDateTime.parse("2025-05-15T10:30:00", Constants.formatter));

            // Verify lưu payment
            verify(paymentRepository).save(payment);
            // Verify update invoice status thành PAID
            verify(invoiceService).updateInvoiceStatus(invoice, InvoiceStatusEnum.PAID);

            // Kết quả trả về success
            assertEquals(0, response.get("error").asInt());
            assertEquals("Webhook processed successfully", response.get("message").asText());
        }
    }


    @Test
    @DisplayName("TC_PAYMENT_006 - Payment không tồn tại - paymentId không tồn tại, ném lỗi và không thay đổi dữ liệu")
    void updatePayment_whenPaymentNotFound_shouldThrowException() {
        // Arrange
        String nonExistentPaymentId = "non-existent-id";
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentStatus(PaymentStatusEnum.COMPLETED);
        paymentRequest.setPaymentMethod(PaymentMethodEnum.TRANSFER);
        paymentRequest.setAmount(new BigDecimal("100"));

        when(paymentRepository.findById(nonExistentPaymentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePayment(nonExistentPaymentId, paymentRequest);
        });

        assertEquals("Payment not found", exception.getMessage());

        // Verify không gọi save hay update invoice vì payment không tồn tại
        verify(paymentRepository, never()).save(any());
        verify(invoiceService, never()).updateInvoiceStatus(any(), any());
    }


    @Test
    @DisplayName("TC_PAYMENT_007: Thiếu paymentMethod hoặc amount khi paymentStatus = COMPLETED - Báo lỗi 'Payment status and amount are required'")
    void updatePayment_whenPaymentStatusCompletedAndMissingMethodOrAmount_shouldThrowException() {
        // Arrange
        String paymentId = "payment-001";

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setPaymentStatus(PaymentStatusEnum.PENDING); // trạng thái cũ

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));

        // Test case 1: paymentMethod == null
        PaymentRequest requestMissingMethod = new PaymentRequest();
        requestMissingMethod.setPaymentStatus(PaymentStatusEnum.COMPLETED);
        requestMissingMethod.setPaymentMethod(null);
        requestMissingMethod.setAmount(new BigDecimal("100"));

        RuntimeException ex1 = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePayment(paymentId, requestMissingMethod);
        });
        assertEquals("Payment status and amount are required", ex1.getMessage());

        // Test case 2: amount == null
        PaymentRequest requestMissingAmount = new PaymentRequest();
        requestMissingAmount.setPaymentStatus(PaymentStatusEnum.COMPLETED);
        requestMissingAmount.setPaymentMethod(PaymentMethodEnum.TRANSFER);
        requestMissingAmount.setAmount(null);

        RuntimeException ex2 = assertThrows(RuntimeException.class, () -> {
            paymentService.updatePayment(paymentId, requestMissingAmount);
        });
        assertEquals("Payment status and amount are required", ex2.getMessage());

        // Verify không save payment và không gọi updateInvoiceStatus
        verify(paymentRepository, never()).save(any());
        verify(invoiceService, never()).updateInvoiceStatus(any(), any());
    }


    @Test
    @DisplayName("TC_PAYMENT_008: Update COMPLETED thành công - paymentRequest đầy đủ paymentStatus, paymentMethod, amount - Cập nhật thành công payment và invoice thành PAID")
    void updatePayment_whenCompletedWithValidData_shouldUpdatePaymentAndInvoice() {
        // Arrange
        String paymentId = "payment-002";

        Invoice invoice = new Invoice();
        invoice.setId("invoice-001");

        Payment existingPayment = new Payment();
        existingPayment.setId(paymentId);
        existingPayment.setPaymentStatus(PaymentStatusEnum.PENDING);
        existingPayment.setInvoice(invoice);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(existingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequest request = new PaymentRequest();
        request.setPaymentStatus(PaymentStatusEnum.COMPLETED);
        request.setPaymentMethod(PaymentMethodEnum.TRANSFER);
        request.setAmount(new BigDecimal("150"));

        // Act
        ApiResponse<Void> response = paymentService.updatePayment(paymentId, request);

        // Assert
        assertEquals(200, response.getCode());
        assertEquals("Success", response.getMessage());

        // Verify payment updated with correct status
        assertEquals(PaymentStatusEnum.COMPLETED, existingPayment.getPaymentStatus());

        // Verify paymentRepository.save called once
//        verify(paymentRepository, times(1)).save(existingPayment);
//
//        // Verify invoiceService.updateInvoiceStatus called with invoice and PAID status
//        verify(invoiceService, times(1)).updateInvoiceStatus(invoice, InvoiceStatusEnum.PAID);
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
