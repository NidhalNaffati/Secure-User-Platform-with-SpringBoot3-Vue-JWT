package com.nidhal.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class AdminController {

    @GetMapping("/admin")
    public ResponseEntity<String> adminGreeting() {
        return ResponseEntity.ok("Hello admin u are reading this message from a protected endpoint. Only admins can access this endpoint.");
    }

}
