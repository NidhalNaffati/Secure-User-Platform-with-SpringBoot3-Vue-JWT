package com.nidhal.backend.requests;

public record AuthenticationRequest(
        String email,

        String password
) {

}
