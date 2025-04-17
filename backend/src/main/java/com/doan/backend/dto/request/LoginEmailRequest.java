package com.doan.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;

@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Data
@Builder
@AllArgsConstructor // Explicitly generate a public all-args constructor
@NoArgsConstructor
public class LoginEmailRequest {
    @Email
    @NotBlank(message = "Email is required")
    String email;

    @NotBlank(message = "Password is required")
    String password;

}
