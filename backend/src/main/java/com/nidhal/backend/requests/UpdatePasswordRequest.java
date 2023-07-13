package com.nidhal.backend.requests;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UpdatePasswordRequest(
        @NotNull
        String token,

        @NotNull(message = "Password shouldn't be null ")
        @Length(min = 8, max = 16, message = "password length should be more than 8 and less than 16")
        String password,

        @NotNull(message = "Password Confirm shouldn't be null ")
        String passwordConfirm
) {
}
