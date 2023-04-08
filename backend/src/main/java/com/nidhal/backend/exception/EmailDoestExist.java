package com.nidhal.backend.exception;

import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class EmailDoestExist extends ResponseStatusException {
    public EmailDoestExist() {
        super(NOT_FOUND, "Email doesn't exists");
    }
}
