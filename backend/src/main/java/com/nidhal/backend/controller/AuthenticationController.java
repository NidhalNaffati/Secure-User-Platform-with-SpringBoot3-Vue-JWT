package com.nidhal.backend.controller;


import com.nidhal.backend.exception.PasswordDontMatchException;
import com.nidhal.backend.exception.UserNotFoundException;
import com.nidhal.backend.model.EmailRequest;
import com.nidhal.backend.requests.AuthenticationRequest;
import com.nidhal.backend.requests.AuthenticationResponse;
import com.nidhal.backend.requests.RegisterRequest;
import com.nidhal.backend.service.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.registerUser(request));
    }

    @PostMapping("authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse responseToken = authenticationService.authenticate(request);
        return ResponseEntity.ok(responseToken);
    }

    @PostMapping("enable-user/{token}")
    public ResponseEntity<String> enableUser(
            @PathVariable String token
    ) {
        try {
            authenticationService.enableUser(token);
            // If the enableUser method succeeds, perform the redirect
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/login")
                    .body(null);
        } catch (ExpiredJwtException e) { // ExpiredJwtException is a custom exception
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Link has expired. Please request a new one.");
        } catch (UserNotFoundException e) { // UserNotFoundException is a custom exception
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User not found !!");
        } catch (Exception e) { // For any other unhandled exceptions, return a generic error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to enable user.");
        }
    }

    @PostMapping("refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        AuthenticationResponse responseToken = authenticationService.refreshToken(request, response);
        return ResponseEntity.ok(responseToken);
    }

    @PostMapping("reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody EmailRequest request
    ) {
        return ResponseEntity.ok(authenticationService.sendResetPasswordRequestToUser(request.email()));
    }

    @PostMapping("reset-password/{token}")
    public ResponseEntity<String> resetPassword(
            @PathVariable String token,
            @RequestParam String password,
            @RequestParam String passwordConfirm
    ) {
        try {
            authenticationService.upDatePassword(token, password, passwordConfirm);
            // If the upDatePassword method succeeds, perform the redirect
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/login")
                    .body(null);
        } catch (PasswordDontMatchException e) { // PasswordDontMatchException is a custom exception
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Password don't match !!");
        } catch (ExpiredJwtException e) { // ExpiredJwtException is a custom exception
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Link has expired. Please request a new one.");
        } catch (UserNotFoundException e) { // UserNotFoundException is a custom exception
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User not found !!");
        } catch (Exception e) {  // For any other unhandled exceptions, return a generic error message
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to reset password try again !!");
        }
    }

}
