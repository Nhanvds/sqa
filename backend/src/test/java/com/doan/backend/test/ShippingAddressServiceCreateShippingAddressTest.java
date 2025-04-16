package com.doan.backend.test;

import com.doan.backend.dto.request.ShippingAddressRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.entity.ShippingAddress;
import com.doan.backend.mapper.ShippingAddressMapper;
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
class ShippingAddressServiceCreateShippingAddressTest {

    @Mock
    private ShippingAddressRepository shippingAddressRepository;

    @Mock
    private ShippingAddressMapper shippingAddressMapper;

    @InjectMocks
    private ShippingAddressService shippingAddressService;

    @Test
    void createShippingAddress_ShouldCreateAddress_WhenNoDefaultExists() {
        // Arrange
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId("user1");
        request.setIsDefault(false);
        ShippingAddress shippingAddress = new ShippingAddress();
        ShippingAddressResponse response = new ShippingAddressResponse();

        when(shippingAddressRepository.existsByUserIdAndIsDefault(request.getUserId(), true)).thenReturn(false);
        when(shippingAddressMapper.toShippingAddress(request)).thenReturn(shippingAddress);
        when(shippingAddressRepository.save(shippingAddress)).thenReturn(shippingAddress);
        when(shippingAddressMapper.toShippingAddressResponse(shippingAddress)).thenReturn(response);

        // Act
        ApiResponse<ShippingAddressResponse> result = shippingAddressService.createShippingAddress(request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Create shipping address successfully", result.getMessage());
        assertEquals(response, result.getResult());
        verify(shippingAddressRepository, times(1)).existsByUserIdAndIsDefault(request.getUserId(), true);
        verify(shippingAddressMapper, times(1)).toShippingAddress(request);
        verify(shippingAddressRepository, times(1)).save(shippingAddress);
        verify(shippingAddressMapper, times(1)).toShippingAddressResponse(shippingAddress);
    }

    @Test
    void createShippingAddress_ShouldThrowException_WhenDefaultExists() {
        // Arrange
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId("user1");
        request.setIsDefault(true);

        when(shippingAddressRepository.existsByUserIdAndIsDefault(request.getUserId(), true)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shippingAddressService.createShippingAddress(request);
        });
        assertEquals("Default shipping address already exists", exception.getMessage());
        verify(shippingAddressRepository, times(1)).existsByUserIdAndIsDefault(request.getUserId(), true);
        verifyNoMoreInteractions(shippingAddressRepository, shippingAddressMapper);
    }
}