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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;


@Service
@AllArgsConstructor
@Slf4j
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
     * Registers a new user and generates a JWT token for account activation.
     *
     * @param registerRequest the request containing user's information
     * @return the authentication response containing the JWT token
     * @throws PasswordDontMatchException  if the password and password confirm do not match
     * @throws EmailAlreadyExistsException if an account with the given email already exists
     */
    public String registerUser(RegisterRequest registerRequest) {
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

        // Creates a new user based on the information in the request and saves it in the database
        User user = registerRequest.toUser();

        var savedUser = userService.saveUser(user);

        var jwtToken = jwtService.generateTokenForEnableAccount(user.getEmail());

        // create the link for the account activation
        String activationLink = "http://localhost:9090/api/v1/auth/enable-user/" + jwtToken;

        // Send activation link.
        try {
            log.info("Sending activation link to user {}", registerRequest.email());
            //  emailService.sendActivationLink(registerRequest.email(), registerRequest.firstName(), activationLink);
        } catch (Exception e) {
            log.error("Error while sending activation link to user {}", registerRequest.email());
            // throw new MailSendException(registerRequest.email());
        }

        tokenService.saveUserToken(savedUser, jwtToken);
        log.info("User successfully registered with request {}", registerRequest);

        // Returns an authentication response containing the JWT token
        return jwtToken;
    }


    /**
     * Generates a JWT token with an expiration date and sends a reset password email to the user
     * containing a link to reset their password.
     *
     * @param email Email address of the user.
     * @return JWT token.
     */
    public String sendResetPasswordRequestToUser(String email) {
        // If an account with the given email already exists, throws an exception
        var user = userService.findUserByEmail(email);

        var jwtToken = jwtService.generateTokenForResetPassword(user.getEmail());

        // create the link for the account activation
        String resetPasswordLink = "http://localhost:9090/api/v1/auth/reset-password/" + jwtToken;

        // Send activation link.
        try {
            log.info("Sending reset password link to user with email {}", email);
            emailService.sendResetPasswordRequestToUser(email, user.getFirstName(), resetPasswordLink);
        } catch (Exception e) {
            log.error("Error while sending reset password link to user with email {}", email);
            // throw new MailSendException("Error while sending reset password link to user with email :" + email);
        }

        log.info("Reset password link sent to user with email {}", email);
        // Returns an authentication response containing the JWT token
        return jwtToken;
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


    private boolean isPasswordAndPasswordConfirmMatches(RegisterRequest registerRequest) {
        // checks if the password and password confirm are the same
        return registerRequest.password().equals(registerRequest.confirmPassword());
    }

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
