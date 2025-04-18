package com.doan.backend.services;

import com.doan.backend.dto.request.ShippingAddressRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.entity.ShippingAddress;
import com.doan.backend.mapper.ShippingAddressMapper;
import com.doan.backend.repositories.ShippingAddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingAddressServiceUpdateShippingAddressTest {

    @Mock
    private ShippingAddressRepository shippingAddressRepository;

    @Mock
    private ShippingAddressMapper shippingAddressMapper;

    @InjectMocks
    private ShippingAddressService shippingAddressService;

    @Test
    void updateShippingAddress_ShouldUpdateAddress_WhenNoDefaultConflict() {
        // Arrange
        String id = "1";
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId("user1");
        request.setIsDefault(false);
        request.setRecipientName("John Doe");
        request.setPhoneNumber("1234567890");
        request.setAddressDetail("123 Street");
        request.setCity("City");
        request.setDistrict("District");
        request.setWard("Ward");
        request.setCountry("Country");
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(id);
        ShippingAddressResponse response = new ShippingAddressResponse();

        when(shippingAddressRepository.findById(id)).thenReturn(Optional.of(shippingAddress));
        when(shippingAddressRepository.save(shippingAddress)).thenReturn(shippingAddress);
        when(shippingAddressMapper.toShippingAddressResponse(shippingAddress)).thenReturn(response);

        // Act
        ApiResponse<ShippingAddressResponse> result = shippingAddressService.updateShippingAddress(id, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("Update shipping address successfully", result.getMessage());
        assertEquals(response, result.getResult());
        assertEquals("John Doe", shippingAddress.getRecipientName());
        assertEquals("1234567890", shippingAddress.getPhoneNumber());
        assertEquals("123 Street", shippingAddress.getAddressDetail());
        assertEquals("City", shippingAddress.getCity());
        assertEquals("District", shippingAddress.getDistrict());
        assertEquals("Ward", shippingAddress.getWard());
        assertEquals("Country", shippingAddress.getCountry());
        assertFalse(shippingAddress.getIsDefault());
        verify(shippingAddressRepository, times(1)).findById(id);
        verify(shippingAddressRepository, times(1)).save(shippingAddress);
        verify(shippingAddressMapper, times(1)).toShippingAddressResponse(shippingAddress);
    }

    @Test
    void updateShippingAddress_ShouldThrowException_WhenDefaultConflictExists() {
        // Arrange
        String id = "1";
        ShippingAddressRequest request = new ShippingAddressRequest();
        request.setUserId("user1");
        request.setIsDefault(true);
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setId(id);
        ShippingAddress defaultAddress = new ShippingAddress();
        defaultAddress.setId("2");

        when(shippingAddressRepository.findById(id)).thenReturn(Optional.of(shippingAddress));
        when(shippingAddressRepository.findByUserIdAndIsDefault(request.getUserId(), true)).thenReturn(Optional.of(defaultAddress));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shippingAddressService.updateShippingAddress(id, request);
        });
        assertEquals("Default shipping address already exists", exception.getMessage());
        verify(shippingAddressRepository, times(1)).findById(id);
        verify(shippingAddressRepository, times(1)).findByUserIdAndIsDefault(request.getUserId(), true);
        verifyNoMoreInteractions(shippingAddressRepository, shippingAddressMapper);
    }

    @Test
    void updateShippingAddress_ShouldThrowException_WhenAddressNotFound() {
        // Arrange
        String id = "1";
        ShippingAddressRequest request = new ShippingAddressRequest();

        when(shippingAddressRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            shippingAddressService.updateShippingAddress(id, request);
        });
        assertEquals("Shipping address not found", exception.getMessage());
        verify(shippingAddressRepository, times(1)).findById(id);
        verifyNoMoreInteractions(shippingAddressRepository, shippingAddressMapper);
    }
}