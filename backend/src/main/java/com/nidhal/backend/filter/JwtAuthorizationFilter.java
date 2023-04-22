package com.nidhal.backend.filter;


import com.nidhal.backend.entity.User;
import com.nidhal.backend.service.JwtService;
import com.nidhal.backend.service.TokenService;
import com.nidhal.backend.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

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


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (request.getServletPath().contains("/api/v1/auth/register") ||
            request.getServletPath().contains("/api/v1/auth/refresh-token") ||
            request.getServletPath().contains("/api/v1/auth/reset-password")
        ) {
            log.info("skipping the filter for the following request url {} : ", request.getServletPath());
            filterChain.doFilter(request, response);
        } else {

            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            // Check if Authorization header is missing or does not contain a valid JWT
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    // Extract the JWT from the Authorization header
                    jwt = authHeader.substring(7);

                    // Extract the user email from the JWT and check if it is valid
                    userEmail = jwtService.extractUsername(jwt);


                    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // Retrieve the userDetails from the database
                        User user = userService.findUserByEmail(userEmail);

                        var isTokenValid = tokenService.isTokenValid(jwt);
                        // Check if the JWT is valid for the retrieved userDetails
                        log.info("isTokenValid: {}", isTokenValid);

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
                } catch (ExpiredJwtException ex) {
                    log.warn("JWT has expired: {}", ex.getMessage());
                    response.setStatus(SC_UNAUTHORIZED);
                    response.getWriter().write("JWT has expired from the filter");
                } catch (MalformedJwtException ex) {
                    log.warn("JWT is malformed: {}", ex.getMessage());
                    response.setStatus(SC_UNAUTHORIZED);
                    response.getWriter().write("JWT is malformed");
                } catch (SignatureException ex) {
                    log.warn("JWT signature is invalid: {}", ex.getMessage());
                    response.setStatus(SC_UNAUTHORIZED);
                    response.getWriter().write("JWT signature is invalid");
                } catch (UnsupportedJwtException exception) {
                    log.warn("JWT is unsupported: {}", exception.getMessage());
                    response.setStatus(SC_UNAUTHORIZED);
                    response.getWriter().write("JWT is unsupported");
                } catch (Exception ex) {
                    log.error("Failed to extract username from JWT: {}", ex.getMessage());
                    response.sendError(SC_INTERNAL_SERVER_ERROR, "Failed to extract username from JWT");
                }
            } else {
                log.error("Authorization header is missing or does not contain a valid JWT");
                filterChain.doFilter(request, response);
            }
        }
    }
}
