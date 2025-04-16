package com.doan.backend.test;

import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.repositories.ShippingAddressRepository;
import com.doan.backend.services.ShippingAddressService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingAddressServiceDeleteShippingAddressTest {

    @Mock
    private ShippingAddressRepository shippingAddressRepository;

    @InjectMocks
    private ShippingAddressService shippingAddressService;

    @Test
    void deleteShippingAddress_ShouldDeleteAddress_WhenIdIsValid() {
        // Arrange
        String id = "1";

        doNothing().when(shippingAddressRepository).deleteById(id);

        // Act
        ApiResponse<Void> result = shippingAddressService.deleteShippingAddress(id);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Delete shipping address successfully", result.getMessage());
        verify(shippingAddressRepository, times(1)).deleteById(id);
    }
}