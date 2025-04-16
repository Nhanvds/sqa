package com.doan.backend.test;

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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingAddressServiceGetShippingAddressByUserIdTest {

    @Mock
    private ShippingAddressRepository shippingAddressRepository;

    @Mock
    private ShippingAddressMapper shippingAddressMapper;

    @InjectMocks
    private ShippingAddressService shippingAddressService;

    @Test
    void getShippingAddressByUserId_ShouldReturnAddresses_WhenAddressesExist() {
        // Arrange
        String userId = "user1";
        List<ShippingAddress> addresses = Collections.singletonList(new ShippingAddress());
        Iterable<ShippingAddressResponse> responses = Collections.singletonList(new ShippingAddressResponse());

        when(shippingAddressRepository.findByUserId(userId)).thenReturn(addresses);
        when(shippingAddressMapper.toShippingAddressResponseIterable(addresses)).thenReturn(responses);

        // Act
        ApiResponse<Iterable<ShippingAddressResponse>> result = shippingAddressService.getShippingAddressByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Get shipping address by user id successfully", result.getMessage());
        assertEquals(responses, result.getResult());
        verify(shippingAddressRepository, times(1)).findByUserId(userId);
        verify(shippingAddressMapper, times(1)).toShippingAddressResponseIterable(addresses);
    }

    @Test
    void getShippingAddressByUserId_ShouldReturnEmpty_WhenNoAddressesExist() {
        // Arrange
        String userId = "user1";
        List<ShippingAddress> addresses = Collections.emptyList();
        Iterable<ShippingAddressResponse> responses = Collections.emptyList();

        when(shippingAddressRepository.findByUserId(userId)).thenReturn(addresses);
        when(shippingAddressMapper.toShippingAddressResponseIterable(addresses)).thenReturn(responses);

        // Act
        ApiResponse<Iterable<ShippingAddressResponse>> result = shippingAddressService.getShippingAddressByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Get shipping address by user id successfully", result.getMessage());
        assertFalse(result.getResult().iterator().hasNext());
        verify(shippingAddressRepository, times(1)).findByUserId(userId);
        verify(shippingAddressMapper, times(1)).toShippingAddressResponseIterable(addresses);
    }
}