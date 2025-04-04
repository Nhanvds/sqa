package com.doan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class ShippingAddressRequest {
    @NotBlank(message = "User id is required")
    String userId;

    @NotBlank(message = "Recipient name is required")
    String recipientName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Invalid phone number")
    String phoneNumber;

    @NotBlank(message = "Address detail is required")
    String addressDetail;

    @NotBlank(message = "Country is required")
    String country;

    @NotBlank(message = "City is required")
    String city;

    @NotBlank(message = "District is required")
    String district;

    @NotBlank(message = "Ward is required")
    String ward;

    Boolean isDefault;
}