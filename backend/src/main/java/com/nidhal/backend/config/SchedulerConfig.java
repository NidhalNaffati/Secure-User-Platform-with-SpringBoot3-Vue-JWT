package com.nidhal.backend.config;


import com.nidhal.backend.service.TokenService;
import com.nidhal.backend.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuration class for scheduling tasks.
 * This class is responsible for setting up a scheduled task that runs every 24 hours to delete unactivated user accounts.
 */
@Configuration
@EnableScheduling
@AllArgsConstructor
public class SchedulerConfig {

    private final UserService userService;

    private final TokenService tokenService;


    /**
     * Scheduled task that runs every 24 hours to delete unactivated user accounts.
     * The task is executed with a fixed delay of 24 hours between runs.
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void deleteUnactivatedUsers() {
        userService.deleteUnactivatedUsers();
    }

    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void deleteRevokedAndExpiredTokens() {
        tokenService.deleteRevokedAndExpiredTokens();
    }

}
