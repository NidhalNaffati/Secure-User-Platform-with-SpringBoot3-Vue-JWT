package com.nidhal.backend.controller;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> sayHelloToUSER() {
        return ResponseEntity.ok("Hello my user");
    }

    @GetMapping("/doctor")
    public ResponseEntity<String> sayHelloToROLE_DOCTOR() {
        return ResponseEntity.ok("Hello my doctor");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> sayHelloToROLE_ADMIN() {
        return ResponseEntity.ok("Hello my admin");
    }

}
