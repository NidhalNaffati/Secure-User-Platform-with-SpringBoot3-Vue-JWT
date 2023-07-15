package com.nidhal.backend.config;


import com.nidhal.backend.entity.User;
import com.nidhal.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.nidhal.backend.model.Role.ROLE_ADMIN;

/**
 * This class is responsible for creating the admin user if it does not exist.
 * It implements the ApplicationRunner interface provided by Spring Boot.
 * The run method will be executed after the application context is loaded.
 */

@Component
@Slf4j
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final String adminUsername;
    private final String adminPassword;
    private final PasswordEncoder passwordEncoder;


    public AdminInitializer(UserRepository userRepository,
                            @Value("${admin.username}") String adminUsername,
                            @Value("${admin.password}") String adminPassword, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        User admin = User.builder()
                .firstName("admin")
                .lastName("admin")
                .email(adminUsername)
                .password(adminPassword)
                .confirmPassword(adminPassword)
                .role(ROLE_ADMIN)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        if (!userRepository.existsByEmail(adminUsername)) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("Admin user created successfully");
        } else {
            log.info("Admin user already exists");
        }
    }
}

