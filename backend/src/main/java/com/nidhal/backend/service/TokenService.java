package com.nidhal.backend.service;


import com.nidhal.backend.entity.Token;
import com.nidhal.backend.entity.User;
import com.nidhal.backend.repository.TokenRepository;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void revokeToken(String token) {
        var storedToken = tokenRepository
                .findByToken(token) // Retrieve the token from the database
                .orElse(null); // If the token is not found in the database, return null

        // If the token is found, set it as expired and revoked and save it in the database
        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
        }
    }

    public boolean isTokenValid(String jwt) {
        return tokenRepository
                .findByToken(jwt) // Retrieve the token from the database
                .map(t -> !t.isExpired() && !t.isRevoked()) // Check if the token is not expired and not revoked
                .orElse(false); // If the token is not found in the database, return false
    }

    public void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public void revokeAllUserTokens(User user) {
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
}

