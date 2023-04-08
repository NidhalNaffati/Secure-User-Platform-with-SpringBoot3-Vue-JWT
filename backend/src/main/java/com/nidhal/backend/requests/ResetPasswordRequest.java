package com.nidhal.backend.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record ResetPasswordRequest(
        @Email(message = "Email should be valid")
        @NotNull(message = "Email shouldn't be null")
        @Length(min = 3, message = "email length should be more than 10 ")
        String email
) {
}
