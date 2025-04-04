package com.doan.backend.controllers;

import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.InvoiceResponse;
import com.doan.backend.services.InvoiceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceController {

    InvoiceService invoiceService;

    @GetMapping("/order")
    public ApiResponse<?> getInvoicesByOrderId(@RequestParam String orderId) {
        return invoiceService.getInvoiceByOrderId(orderId);
    }

    @GetMapping("/get-id/{id}")
    public ApiResponse<?> getInvoiceById(@RequestParam String id) {
        return invoiceService.getInvoiceById(id);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<InvoiceResponse>> getAllInvoicesSearchName(@RequestParam String customerEmail, Pageable pageable) {
        return invoiceService.getAllInvoiceSearchEmail(customerEmail, pageable);
    }
}
