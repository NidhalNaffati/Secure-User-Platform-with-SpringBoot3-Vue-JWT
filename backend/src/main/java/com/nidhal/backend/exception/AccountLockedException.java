package com.nidhal.backend.exception;

import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.LOCKED;

public class AccountLockedException extends ResponseStatusException {
    public AccountLockedException() {
        super(LOCKED, "Account locked due to multiple failed login attempts. Please contact the administrator.");
    }
}