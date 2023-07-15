package com.nidhal.backend.service;

import com.nidhal.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * This service provides utility methods for creating, validating, and parsing JWT tokens. It uses the JJWT library to
 * generate tokens and extract claims.
 */
@Service
public class JwtService {

    // current time
    private final Date ISSUED_AT = new Date(System.currentTimeMillis());

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpirationTimeInMs;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenExpirationTimeInMs;


    @Value("${jwt.expiration.reset-password}")
    private long resetPasswordExpirationTimeInMs;

    @Value("${jwt.expiration.enable-account}")
    private long enableAccountExpirationTimeInMs;


    @Value("${jwt.secret-key}")
    private String SECRET_KEY;


    /**
     * Extracts a claim from a JWT token using a given function.
     *
     * @param token          the JWT token to extract the claim from
     * @param claimsResolver the function used to extract the claim
     * @param <T>            the type of the claim
     * @return the extracted claim, or null if the token is invalid
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token to extract the username from
     * @return the username, or null if the token is invalid
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from a JWT token.
     *
     * @param token the JWT token to extract the expiration date from
     * @return the expiration date, or null if the token is invalid
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Creates a map of custom claims for a given user.
     *
     * @param user the user to generate the claims for
     * @return the map of custom claims
     */
    public Map<String, Object> getUserClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("enabled", user.isEnabled());
        return claims;
    }

    /**
     * Generates a JWT token for a given user.
     * <p>
     * The token contains the user's username as the subject, the current time as the issued at date
     */

    // this used for the account verification, reset password, etc.
    public String generateToken(String username, long expirationTimeInMs) {
        final Date expirationDate = new Date(System.currentTimeMillis() + expirationTimeInMs);
        return Jwts
                .builder()
                .setSubject(username)
                .setIssuedAt(ISSUED_AT)
                .setExpiration(expirationDate) // set expiration date
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates refresh token for a given user.
     * <p>
     *
     * @param username the username to generate the token for
     * @return the generated refresh token
     */

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpirationTimeInMs);
    }

    /**
     * Generates token for the account activation.
     * <p>
     *
     * @param username the username to generate the token for
     * @return the generated token
     */
    public String generateTokenForEnableAccount(String username) {
        return generateToken(username, enableAccountExpirationTimeInMs);
    }

    /**
     * Generates token for the password reset.
     * <p>
     *
     * @param username the username to generate the token for
     * @return the generated token
     */
    public String generateTokenForResetPassword(String username) {
        return generateToken(username, resetPasswordExpirationTimeInMs);
    }

    /**
     * Generate Access Token for a given user.
     * <p>
     * The token contains the user's username as the subject, the current time as the issued at date
     * and the expiration date.
     *
     * @param user the user to generate the token for
     * @return the generated access token
     */
    public String generateAccessToken(User user) {
        final Date accessTokenExpirationDate = new Date(System.currentTimeMillis() + accessTokenExpirationTimeInMs);

        return Jwts
                .builder()
                .setClaims(getUserClaims(user)) // add user claims
                .setSubject(user.getEmail())
                .setIssuedAt(ISSUED_AT)
                .setExpiration(accessTokenExpirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    /**
     * Checks if the given JWT token is valid for the provided user.
     *
     * @param token       The JWT token to validate.
     * @param userDetails user to validate the token for.
     * @return true if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Determines if a given JWT token is expired.
     *
     * @param token the JWT token to check
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    /**
     * Extracts all the claims from a given JWT token.
     *
     * @param token the JWT token to extract the claims from
     * @return a {@link Claims} object representing the extracted claims
     */
    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Retrieves the key to be used to sign and validate JWT tokens based on the configured secret key.
     *
     * @return a {@link Key} object representing the signing key
     */
    public Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
