package com.doan.backend.controllers;

import com.doan.backend.dto.request.ShippingAddressRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.services.ShippingAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/shipping-address")
public class ShippingAddressController {
    @Autowired
    private ShippingAddressService shippingAddressService;

    @PostMapping
    public ApiResponse<ShippingAddressResponse> createShippingAddress(@RequestBody @Validated ShippingAddressRequest shippingAddressRequest) {
        return shippingAddressService.createShippingAddress(shippingAddressRequest);
    }

    @PutMapping("/{id}")
    public ApiResponse<ShippingAddressResponse> updateShippingAddress(@PathVariable String id, @RequestBody @Validated ShippingAddressRequest shippingAddressRequest) {
        return shippingAddressService.updateShippingAddress(id, shippingAddressRequest);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteShippingAddress(@PathVariable String id) {
        return shippingAddressService.deleteShippingAddress(id);
    }

    @GetMapping("/{userId}")
    public ApiResponse<Iterable<ShippingAddressResponse>> getShippingAddressByUserId(@PathVariable String userId) {
        return shippingAddressService.getShippingAddressByUserId(userId);
    }

}
