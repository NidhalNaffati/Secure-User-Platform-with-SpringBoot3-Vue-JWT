package com.nidhal.backend.controller;

import com.nidhal.backend.entity.User;
import com.nidhal.backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/admin")
@AllArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<String> adminGreeting() {
        return ResponseEntity.ok("Hello admin u are reading this message from a protected endpoint. Only admins can access this endpoint.");
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/locked-users")
    public ResponseEntity<List<User>> getLockedUsers() {
        return ResponseEntity.ok(userService.getLockedUsers());
    }

    @GetMapping("/unlocked-users")
    public ResponseEntity<List<User>> getUnlockedUsers() {
        return ResponseEntity.ok(userService.getUnlockedUsers());
    }

    @DeleteMapping("/users/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/lock-user/{email}")
    public ResponseEntity<String> enableUser(@PathVariable String email) {
        userService.lockUser(email);
        return ResponseEntity.ok("User locked successfully");
    }

    @PostMapping("/unlock-user/{email}")
    public ResponseEntity<String> disableUser(@PathVariable String email) {
        userService.unlockUser(email);
        return ResponseEntity.ok("User unlocked successfully");
    }

}
