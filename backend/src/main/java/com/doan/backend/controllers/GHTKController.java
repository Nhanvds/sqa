package com.doan.backend.controllers;

import com.doan.backend.dto.request.AddressRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.GHTKCostResponse;
import com.doan.backend.services.GHTKService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ghtk")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GHTKController {
    GHTKService ghtkService;

    @PostMapping("/shipping-costs")
    public ApiResponse<GHTKCostResponse> shippingCosts(@RequestBody AddressRequest addressRequest) {
        return ghtkService.shippingCosts(addressRequest);
    }
}
