package com.esun.financialsystem.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PostUserRequest(
        @NotBlank(message = "userId must not be blank")
        String userId,
        @NotBlank(message = "userName must not be blank")
        String userName,
        @Email(message = "email must be a valid email address")
        @NotBlank(message = "email must not be blank")
        String email,
        @NotBlank(message = "account must not be blank")
        String account) {
}
