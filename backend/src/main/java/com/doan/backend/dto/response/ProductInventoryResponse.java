package com.doan.backend.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductInventoryResponse {
    String id;

    String idProduct;

    SizeResponse size;

    Integer quantity;
}
