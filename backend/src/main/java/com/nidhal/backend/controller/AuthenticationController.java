package com.nidhal.backend.controller;


import com.nidhal.backend.requests.*;
import com.nidhal.backend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.registerUser(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletResponse servletResponse
    ) {

        AuthenticationResponse responseToken = authenticationService.authenticate(request);

        servletResponse.setHeader("Authorization", "Bearer " + responseToken.accessToken());
        return ResponseEntity.ok(responseToken);
    }

    @PostMapping("/enable-user/{token}")
    public ResponseEntity<String> enableUser(@PathVariable String token) {
        authenticationService.enableUser(token);
        return ResponseEntity.ok("Account enabled successfully");
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(request, response);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(authenticationService.sendResetPasswordRequestToUser(request.email()));
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<String> updatePassword(
            @PathVariable String token,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        authenticationService.upDatePassword(token, request.password(), request.passwordConfirm());
        return ResponseEntity.ok("Password reset successfully");
    }


}
