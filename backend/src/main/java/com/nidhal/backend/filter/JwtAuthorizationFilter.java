package com.nidhal.backend.filter;


import com.nidhal.backend.entity.User;
import com.nidhal.backend.service.JwtService;
import com.nidhal.backend.service.TokenService;
import com.nidhal.backend.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This class is a Spring component that extends OncePerRequestFilter, which is a convenient base class for filter
 * implementations. It filters incoming requests and checks for a valid JWT in the Authorization header. If a valid JWT
 * is found, it authenticates the user associated with the token.
 * <p>
 * The component has two dependencies injected via its constructor: JwtService and UserService.
 */
@Component
@Slf4j
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final TokenService tokenService;

    /**
     * Filters incoming requests and authenticates the user associated with a valid JWT in the Authorization header.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if there is an error processing the request
     * @throws IOException      if there is an I/O error while processing the request
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Check if Authorization header is missing or does not contain a valid JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the JWT from the Authorization header
        jwt = authHeader.substring(7);

        // Extract the user email from the JWT and check if it is valid
        userEmail = jwtService.extractUsername(jwt);


        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Retrieve the userDetails from the database
            User user = userService.findUserByEmail(userEmail);

            var isTokenValid = tokenService.isTokenValid(jwt);
            // Check if the JWT is valid for the retrieved userDetails

            if (jwtService.isTokenValid(jwt, user) && isTokenValid) {
                // create a new authentication token with the retrieved user
                log.warn("User {} is authenticated with authorities {}", user.getUsername(), user.getAuthorities());
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                );

                // Set the details of the authentication token
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set the authentication token in the SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}
