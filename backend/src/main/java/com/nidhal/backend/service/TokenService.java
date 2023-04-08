package com.nidhal.backend.service;


import com.nidhal.backend.entity.Token;
import com.nidhal.backend.entity.User;
import com.nidhal.backend.repository.TokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

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


    @Transactional
    public void deleteRevokedAndExpiredTokens() {
        List<Token> tokensToDelete = tokenRepository.findByRevokedTrueOrExpiredTrue();
        for (Token token : tokensToDelete) {
            User user = token.getUser();
            user.getTokens().remove(token);
        }
        tokenRepository.deleteAll(tokensToDelete);

    }
}
