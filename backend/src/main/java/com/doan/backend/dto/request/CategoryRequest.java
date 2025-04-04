package com.doan.backend.dto.request;

import com.doan.backend.enums.StatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    String name;
    String description;
    @NotNull(message = "Status is required")
    StatusEnum status;
}
