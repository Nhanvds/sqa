package com.doan.backend.dto.request;

import com.doan.backend.enums.LoyaltyTierEnum;
import com.doan.backend.enums.RoleEnum;
import com.doan.backend.enums.StatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class UserRequest {
    @Email(message = "Email is invalid")
    String email;

    @NotBlank(message = "Name is required")
    @Size(min = 5, max = 50)
    String name;

    @NotNull(message = "Status is not null")
    StatusEnum status;
    LoyaltyTierEnum loyaltyTier;
    Set<RoleEnum> roles;
}
