package com.nidhal.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1")
public class AdminController {

    @GetMapping("/admin")
    public ResponseEntity<String> sayHelloToROLE_ADMIN() {
        return ResponseEntity.ok("Hello admin");
    }

}
