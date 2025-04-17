package com.doan.backend.controllers;


import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.CartResponse;
import com.doan.backend.services.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(CartController.class)
public class TestCartController {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    @Test
    @DisplayName("TC_CART_GET_01 - Kiểm tra lấy cart thành công")
    void testGetCart_Success() throws Exception {
        // Arrange
        String userId = "user123";
        CartResponse cartResponse = new CartResponse();
        ApiResponse<CartResponse> expectedResponse = new ApiResponse<>();
        expectedResponse.setCode(200);
        expectedResponse.setMessage("Success");
        expectedResponse.setResult(cartResponse);

        when(cartService.getCartByUserId(userId)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/cart") // đúng endpoint mapping
                        .param("userId", userId))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("TC_CART_GET_02 - Kiểm tra validate thiếu param (userId null/rỗng)")
    void testGetCart_MissingUserId_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/cart")) // Replace đúng URL nếu @RequestMapping có prefix
                .andExpect(status().isBadRequest());
    }

}
