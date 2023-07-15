package com.nidhal.backend.service;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

/**
 * Service class for logging out users. Uses TokenService to revoke the token used by the user.
 * <p>
 * This class implements the LogoutHandler interface from Spring Security.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenService tokenService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("No Authorization header found");
            return;
        } else {
            jwt = authHeader.substring(7);
            tokenService.revokeToken(jwt);
        }
    }
}
