package com.doan.backend.services;


import com.doan.backend.dto.request.ShippingAddressRequest;
import com.doan.backend.dto.response.ApiResponse;
import com.doan.backend.dto.response.ShippingAddressResponse;
import com.doan.backend.entity.ShippingAddress;
import com.doan.backend.mapper.ShippingAddressMapper;
import com.doan.backend.repositories.ShippingAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ShippingAddressService {

    ShippingAddressRepository shippingAddressRepository;
    ShippingAddressMapper shippingAddressMapper;

    public ApiResponse<ShippingAddressResponse> createShippingAddress(ShippingAddressRequest shippingAddressRequest) {
        if (shippingAddressRequest.getIsDefault() && shippingAddressRepository.existsByUserIdAndIsDefault(shippingAddressRequest.getUserId(), true)) {
            throw new RuntimeException("Default shipping address already exists");
        }
        ShippingAddress shippingAddress = shippingAddressMapper.toShippingAddress(shippingAddressRequest);
        ShippingAddressResponse shippingAddressResponse = shippingAddressMapper.toShippingAddressResponse(shippingAddressRepository.save(shippingAddress));
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(200)
                .message("Create shipping address successfully")
                .result(shippingAddressResponse)
                .build();
    }

    public ApiResponse<ShippingAddressResponse> updateShippingAddress(String id, ShippingAddressRequest shippingAddressRequest) {
        ShippingAddress shippingAddress = shippingAddressRepository.findById(id).orElseThrow(() -> new RuntimeException("Shipping address not found"));
        if (shippingAddressRequest.getIsDefault()) {
            Optional<ShippingAddress> defaultShippingAddress = shippingAddressRepository.findByUserIdAndIsDefault(shippingAddressRequest.getUserId(), true);
            if (defaultShippingAddress.isPresent() && !defaultShippingAddress.get().getId().equals(id)) {
                throw new RuntimeException("Default shipping address already exists");
            }
        }

        shippingAddress.setRecipientName(shippingAddressRequest.getRecipientName());
        shippingAddress.setPhoneNumber(shippingAddressRequest.getPhoneNumber());
        shippingAddress.setAddressDetail(shippingAddressRequest.getAddressDetail());
        shippingAddress.setCity(shippingAddressRequest.getCity());
        shippingAddress.setDistrict(shippingAddressRequest.getDistrict());
        shippingAddress.setWard(shippingAddressRequest.getWard());
        shippingAddress.setCountry(shippingAddressRequest.getCountry());
        shippingAddress.setIsDefault(shippingAddressRequest.getIsDefault());
        ShippingAddressResponse shippingAddressResponse = shippingAddressMapper.toShippingAddressResponse(shippingAddressRepository.save(shippingAddress));
        return ApiResponse.<ShippingAddressResponse>builder()
                .code(200)
                .message("Update shipping address successfully")
                .result(shippingAddressResponse)
                .build();
    }

    public ApiResponse<Iterable<ShippingAddressResponse>> getShippingAddressByUserId(String userId) {
        Iterable<ShippingAddressResponse> shippingAddresses = shippingAddressMapper.toShippingAddressResponseIterable(shippingAddressRepository.findByUserId(userId));
        return ApiResponse.<Iterable<ShippingAddressResponse>>builder()
                .code(200)
                .message("Get shipping address by user id successfully")
                .result(shippingAddresses)
                .build();
    }

    public ApiResponse<Void> deleteShippingAddress(String id) {
        shippingAddressRepository.deleteById(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Delete shipping address successfully")
                .build();
    }
}
