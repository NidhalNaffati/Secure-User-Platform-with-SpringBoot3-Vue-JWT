package com.nidhal.backend.service;


import com.nidhal.backend.entity.User;
import com.nidhal.backend.exception.EmailAlreadyExistsException;
import com.nidhal.backend.exception.PasswordDontMatchException;
import com.nidhal.backend.requests.AuthenticationRequest;
import com.nidhal.backend.requests.AuthenticationResponse;
import com.nidhal.backend.requests.RegisterRequest;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * The AuthenticationService class is responsible for user authentication and registration processes.
 * <p>
 * It provides methods to authenticate users, register new users, send reset password requests, update passwords, enable user accounts, and refresh authentication tokens.
 */

@Slf4j
@Service
@AllArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final TokenService tokenService;


    /**
     * Authenticates the user and generates a JWT token.
     *
     * @param request the authentication request containing email and password
     * @return the authentication response containing the JWT token
     * @throws BadCredentialsException if the credentials are invalid
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            // Attempts to authenticate the user with the provided email and password
            authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
              )
            );
        } catch (InternalAuthenticationServiceException e) {
            // If the authentication fails, throws an exception with a message indicating invalid credentials
            log.error("error while authenticating user with request {}", request);
            throw new BadCredentialsException("Invalid credentials");
        }

        // If the authentication is successful, retrieves the user from the database and generates a JWT token
        User user = userService.validateCredentials(request.email(), request.password());
        log.info("User {} successfully authenticated with role {}", user.getFirstName(), user.getRole());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, accessToken);

        // Returns an authentication response containing the JWT token
        return new AuthenticationResponse(accessToken, refreshToken);
    }


    /**
     * Registers a new user with the provided registration details.
     * <p>
     * This method performs the following steps:
     * 1. Validates the password and password confirmation to ensure they match.
     * 2. Checks if an account with the given email already exists.
     * 3. Creates a new user based on the information in the registration request.
     * 4. Saves the new user in the database.
     * 5. Generates a JWT token for enabling the user account.
     * 6. Creates an activation link using the generated JWT token.
     * 7. Sends the activation link to the user's email address.
     *
     * @param registerRequest The registration details of the user.
     *                        It contains information such as email, password, first name, etc.
     * @throws PasswordDontMatchException  If the password and password confirmation do not match.
     * @throws EmailAlreadyExistsException If an account with the provided email already exists.
     * @throws MailSendException           If there is an error while sending the activation link to the user.
     */
    public void registerUser(RegisterRequest registerRequest) {
        // If the password and password confirm do not match, throws an exception
        if (!isPasswordAndPasswordConfirmMatches(registerRequest)) {
            log.error("Password and password confirm doesn't match");
            throw new PasswordDontMatchException();
        }

        // If an account with the given email already exists, throws an exception
        if (userService.emailExists(registerRequest.email())) {
            log.error("Email already exists");
            throw new EmailAlreadyExistsException();
        }

        var jwtToken = jwtService.generateTokenForEnableAccount(registerRequest.email());

        // create the link for the account activation
        String activationLink = "http://localhost:9090/api/v1/auth/enable-user/" + jwtToken;

        // Send activation link.
        try {
            log.info("Sending activation link to user {}", registerRequest.email());
            emailService.sendActivationLink(registerRequest.email(), registerRequest.firstName(), activationLink);

            // Creates a new user based on the information in the request and saves it in the database
            User user = registerRequest.toUser();

            var savedUser = userService.saveUser(user);

            tokenService.saveUserToken(savedUser, jwtToken);
            log.info("User successfully registered with request {}", registerRequest);

        } catch (Exception e) {
            log.error("Cannot create user with request {}", registerRequest);
            log.error("Error: {}", e.getMessage());
        }
    }


    /**
     * Generates a JWT token with an expiration date and sends a reset password email to the user
     * containing a link to reset their password.
     *
     * @param email Email address of the user.
     */
    public void sendResetPasswordRequestToUser(String email) {
        // If an account with the given email already exists, throws an exception
        var user = userService.findUserByEmail(email);

        var jwtToken = jwtService.generateTokenForResetPassword(user.getEmail());

        // create the link for the account activation & set the token as a param
        String resetPasswordLink = "http://localhost:5173/reset-password?token=" + jwtToken;

        // Send activation link.
        try {
            log.info("Sending reset password link to user with email {}", email);
            emailService.sendResetPasswordRequestToUser(email, user.getFirstName(), resetPasswordLink);
        } catch (Exception e) {
            log.warn("Error while sending reset password link to user with email {}", email);
            log.info("If u didn't receive the email, due to the fact that we are in dev mode, we can pretend that the following link is sent : {}", resetPasswordLink);
            throw new MailSendException("Error while sending reset password link to user with email :" + email);
        }
        log.info("Reset password link sent to user with email {}", email);
    }


    /**
     * Updates the password of a user given a JWT token and a new password, and confirms that the new password matches
     * the password confirmation.
     *
     * @param token           JWT token containing the user email.
     * @param password        New password.
     * @param passwordConfirm Confirmation of the new password.
     */
    public void upDatePassword(String token, String password, String passwordConfirm) {
        // retrieve the email from the token
        String email = jwtService.extractUsername(token);
        // update the password
        userService.updatePassword(email, password, passwordConfirm);
    }


    /**
     * Enables a user given a JWT token.
     *
     * @param token JWT token containing the user email.
     */
    public void enableUser(String token) {
        // retrieve the email from the token
        String email = jwtService.extractUsername(token);
        // enable the user
        userService.enableUser(email);
    }


    /**
     * Checks if the password and password confirmation match.
     *
     * @param registerRequest The registration details of the user.
     * @return True if the password and password confirmation match, false otherwise.
     */
    public boolean isPasswordAndPasswordConfirmMatches(RegisterRequest registerRequest) {
        // checks if the password and password confirm are the same
        return registerRequest.password().equals(registerRequest.confirmPassword());
    }


    /**
     * Refreshes the JWT token.
     *
     * @param request  HTTP request.
     * @param response HTTP response.
     * @return The new JWT token.
     * @throws IOException If an error occurs while writing the response.
     */
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // initialize the result
        AuthenticationResponse result = null;

        // extract the token from the request header
        final String authHeader = request.getHeader("Authorization");

        // if the token is null or does not start with "Bearer ", return an error
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header.");
            log.error("Missing or invalid Authorization header.");
        } else { // else, try to refresh the token
            try {
                // extract the refresh token
                log.info("Refreshing token for request {}", request.getHeader("Authorization"));
                final String refreshToken = authHeader.substring(7);

                // extract the user email from the refresh token
                var username = jwtService.extractUsername(refreshToken);
                log.info("User email is {}", username);

                // if the user email is not null, find the user in the database
                if (username != null) {
                    // find the user in the database
                    var userDetails = userService.loadUserByUsername(username);
                    log.info("User is {}", userDetails);

                    // if the user is not null and the refresh token is valid, generate a new access token
                    if (jwtService.isTokenValid(refreshToken, userDetails)) {
                        var accessToken = jwtService.generateAccessToken(userDetails.user()); // generate a new access token
                        log.info("Access token is {}", accessToken);
                        tokenService.revokeAllUserTokens(userDetails.user()); // revoke all user tokens
                        tokenService.saveUserToken(userDetails.user(), accessToken); // save the new access token

                        // set the result
                        result = new AuthenticationResponse(accessToken, refreshToken);
                    }
                }
            } catch (ExpiredJwtException ex) { // if the refresh token is expired, return an error
                log.warn("refresh token expired: {}", ex.getMessage());
                response.sendError(SC_UNAUTHORIZED, "refresh token expired");
            } catch (MalformedJwtException e) { // if the refresh token is invalid, return an error
                log.warn("refresh token expired: {}", e.getMessage());
                response.sendError(SC_UNAUTHORIZED, "invalid refresh token.");
            }
        }

        // return the result
        return result;
    }
}
