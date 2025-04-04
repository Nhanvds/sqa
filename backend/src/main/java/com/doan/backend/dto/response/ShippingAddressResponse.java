package com.doan.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingAddressResponse {
    String id;

    String recipientName;

    String phoneNumber;

    String addressDetail;

    String country;

    String city;

    String district;

    String ward;

    Boolean isDefault;
}
