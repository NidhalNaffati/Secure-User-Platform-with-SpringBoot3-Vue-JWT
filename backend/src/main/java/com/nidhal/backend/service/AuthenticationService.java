package com.nidhal.backend.service;


import com.nidhal.backend.entity.Token;
import com.nidhal.backend.entity.User;
import com.nidhal.backend.exception.EmailAlreadyExistsException;
import com.nidhal.backend.exception.PasswordDontMatchException;
import com.nidhal.backend.repository.TokenRepository;
import com.nidhal.backend.requests.AuthenticationRequest;
import com.nidhal.backend.requests.AuthenticationResponse;
import com.nidhal.backend.requests.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;



@Service
@Slf4j
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;


    public AuthenticationService(AuthenticationManager authenticationManager,
                                 EmailService emailService,
                                 UserService userService,
                                 JwtService jwtService,
                                 TokenRepository tokenRepository) {
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.userService = userService;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
    }

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
        log.info("User {} successfully authenticated with authority {}", user.getFirstName(), user.getAuthorities());

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        log.info("User successfully authenticated with request {}", request);

        // Returns an authentication response containing the JWT token
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }


    private void revokeAllUserTokens(User user) {
        // get all valid tokens for the user
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());

        // if there is no valid token, return
        if (validUserTokens.isEmpty())
            return;

        // revoke all valid tokens
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        // save all revoked tokens
        tokenRepository.saveAll(validUserTokens);
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

        var jwtToken = jwtService.generateTokenForEnableAccount(user.getUsername());

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

        saveUserToken(savedUser, jwtToken);
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

        var jwtToken = jwtService.generateTokenForResetPassword(user.getUsername());

        // create the link for the account activation
        String resetPasswordLink = "http://localhost:9090/api/v1/auth/reset-password/" + jwtToken;

        // Send activation link.
        try {
            log.info("Sending reset password link to user with email {}", email);
            //   emailService.sendResetPasswordRequestToUser(email, user.getFirstName(), resetPasswordLink);
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
}
