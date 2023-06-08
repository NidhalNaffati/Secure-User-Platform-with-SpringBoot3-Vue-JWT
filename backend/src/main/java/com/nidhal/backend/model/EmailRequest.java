package com.nidhal.backend.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record EmailRequest(
        @Email
        @NotNull
        String email
) {
}
