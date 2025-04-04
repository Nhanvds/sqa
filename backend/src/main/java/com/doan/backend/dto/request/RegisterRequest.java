package com.doan.backend.dto.request;

import com.doan.backend.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class RegisterRequest {

    @Email(message = "Email is invalid")
    String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,16}$", message = "Password must be contain a uppercase, a lowercase, a number, a special character and at least 8 letter")
    String password;

    @NotBlank(message = "Name is required")
    @Size(min = 5, max = 50)
    String name;

    Set<RoleEnum> roles;
}
