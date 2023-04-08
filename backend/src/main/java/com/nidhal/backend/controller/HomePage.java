package com.nidhal.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/home")
public class HomePage {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hello from secured endpoint");
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> sayHelloToUSER() {
        return ResponseEntity.ok("Hello my user");
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyAuthority('ROLE_DOCTOR')")
    public ResponseEntity<String> sayHelloToROLE_DOCTOR() {
        return ResponseEntity.ok("Hello my doctor");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> sayHelloToROLE_ADMIN() {
        return ResponseEntity.ok("Hello my admin");
    }

}
