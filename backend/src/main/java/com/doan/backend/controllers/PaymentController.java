package com.doan.backend.controllers;

import com.doan.backend.dto.request.PaymentRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.services.PaymentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.Webhook;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/payos")
    public ResponseEntity<ObjectNode> handlePayOSWebhook(@RequestBody Webhook webhookBody) {
        ObjectNode response = paymentService.handlePaymentWebhook(webhookBody);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<?> updatePaymentStatus(@PathVariable String id, @RequestBody @Validated PaymentRequest paymentRequest) {
        return paymentService.updatePayment(id, paymentRequest);
    }
}
