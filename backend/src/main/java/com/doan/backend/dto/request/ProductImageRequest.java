package com.doan.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageRequest {
    @NotBlank(message = "Product is required")
    String idProduct;
    @NotNull(message = "ImageUrl is not null")
    List<String> imageUrl;
}
