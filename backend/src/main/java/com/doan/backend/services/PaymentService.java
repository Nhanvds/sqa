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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class PaymentService {

    PayOS payOS;
    PaymentRepository paymentRepository;
    InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    @NonFinal
    @Value("${app.client-url}")
    String clientUrl;

    public String createPaymentLink(String invoiceId) {

        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));

            final String productName = "Pay bills " + invoice.getInvoiceNumber();
            final String description = invoice.getInvoiceNumber();
            final String returnUrl = clientUrl + "/account";
            final String cancelUrl = clientUrl + "/payment/cancel";
            int price = invoice.getTotalAmount().intValue();

            String invoiceNumberString = CodeUtils.removePrefix(invoice.getInvoiceNumber(), Constants.INVOICE_PREFIX);

            ItemData item = ItemData.builder().name(productName).price(price).quantity(1).build();
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(Long.parseLong(invoiceNumberString))
                    .description(description)
                    .amount(price)
                    .item(item)
                    .returnUrl(returnUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            CheckoutResponseData data = payOS.createPaymentLink(paymentData);
            return data.getCheckoutUrl();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public ObjectNode handlePaymentWebhook(Webhook webhookBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode response = objectMapper.createObjectNode();

        try {
            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);

            long orderCode = data.getOrderCode();
            String invoiceNumber = CodeUtils.generateUniqueCode(Constants.INVOICE_PREFIX, orderCode);
            Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                    .orElseThrow(() -> new RuntimeException("Invoice not found"));


            Payment payment = invoice.getPayment();
            payment.setCode(data.getCode());
            payment.setPaymentMethod(PaymentMethodEnum.TRANSFER);
            payment.setPaymentStatus(PaymentStatusEnum.COMPLETED);
            payment.setQrCodeUrl(null);
            payment.setAmount(new BigDecimal(data.getAmount()));
            payment.setPaymentDate(LocalDateTime.parse(data.getTransactionDateTime(), Constants.formatter));
            paymentRepository.save(payment);

            invoiceService.updateInvoiceStatus(invoice, InvoiceStatusEnum.PAID);

            response.put("error", 0);
            response.put("message", "Webhook processed successfully");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", -1);
            response.put("message", e.getMessage());
            return response;
        }
    }

    @Transactional
    public ApiResponse<Void> updatePayment(String id, PaymentRequest paymentRequest) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        if (paymentRequest.getPaymentStatus() == PaymentStatusEnum.COMPLETED) {

            if (paymentRequest.getPaymentMethod() == null || paymentRequest.getAmount() == null) {
                throw new RuntimeException("Payment status and amount are required");
            }
            payment.setPaymentStatus(paymentRequest.getPaymentStatus());
            paymentRepository.save(payment);

            Invoice invoice = payment.getInvoice();
            invoiceService.updateInvoiceStatus(invoice, InvoiceStatusEnum.PAID);
        } else {
            payment.setPaymentStatus(paymentRequest.getPaymentStatus());
            paymentRepository.save(payment);
        }

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Success")
                .build();
    }
}
