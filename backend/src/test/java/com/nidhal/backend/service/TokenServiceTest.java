package com.nidhal.backend.service;


import com.nidhal.backend.entity.Token;
import com.nidhal.backend.entity.User;
import com.nidhal.backend.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService underTestTokenService;

    private final static String VALID_TOKEN_VALUE = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkb2N0b3IyNUBnbWFpbC5jb20iLCJpYXQiOjE2ODQzMjc3NjYsImV4cCI6MTY4NDMyODc0NH0.S6fdOeLYpi_xiZbyvEIzPzhMm7bjlKs2k0kNYEJFCIQ";

    private static final Token STORED_TOKEN_ENTITY = Token.builder()
            .id(6464L)
            .token(VALID_TOKEN_VALUE)
            .revoked(false)
            .expired(false)
            .build();

    @Test
    void testRevokeToken() {
        // given
        when(tokenRepository.findByToken(VALID_TOKEN_VALUE)).thenReturn(Optional.of(STORED_TOKEN_ENTITY));

        // when
        underTestTokenService.revokeToken(VALID_TOKEN_VALUE);

        // then
        verify(tokenRepository).save(argThat(token -> token.isExpired() && token.isRevoked()));
    }


    @Test
    void revokeToken_shouldSetTokenAsExpiredAndRevoked() {
        // given
        when(tokenRepository.findByToken(VALID_TOKEN_VALUE)).thenReturn(Optional.of(STORED_TOKEN_ENTITY));

        // when
        underTestTokenService.revokeToken(VALID_TOKEN_VALUE);

        // then
        assertTrue(STORED_TOKEN_ENTITY.isExpired());
        assertTrue(STORED_TOKEN_ENTITY.isRevoked());
        verify(tokenRepository, times(1)).save(STORED_TOKEN_ENTITY);
    }

    @Test
    void revokeToken_shouldNotSaveTokenIfTokenNotFound() {
        // given
        when(tokenRepository.findByToken(VALID_TOKEN_VALUE)).thenReturn(Optional.empty());

        // when
        underTestTokenService.revokeToken(VALID_TOKEN_VALUE);

        // then
        verify(tokenRepository, never()).save(any());
    }


    @Test
    void isTokenValid_shouldReturnFalseIfTokenIsExpired() {
        // given
        String token = "expired-token";
        Token storedToken = new Token();
        storedToken.setExpired(true);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(storedToken));

        // when
        boolean isValid = underTestTokenService.isTokenValid(token);

        // then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseIfTokenIsRevoked() {
        // given
        String token = "revoked-token";
        Token storedToken = new Token();
        storedToken.setRevoked(true);
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(storedToken));

        // when
        boolean isValid = underTestTokenService.isTokenValid(token);

        // then
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseIfTokenNotFound() {
        // given
        String token = "non-existent-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // when
        boolean isValid = underTestTokenService.isTokenValid(token);

        // then
        assertFalse(isValid);
    }

    @Test
    void saveUserToken_ShouldSaveToken() {
        // given
        User user = new User(); // Create a user object for testing
        String jwtToken = "sampleJwtToken";

        // when
        underTestTokenService.saveUserToken(user, jwtToken);

        // then
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    void revokeAllUserTokens_ShouldRevokeValidTokens() {
        // given
        User user = new User();
        user.setId(123_456_789L);// Create a user object for testing
        Token token1 = new Token(123L, "eyJhbGciOiJIUzI1NiJ9", false, false, user);
        Token token2 = new Token(456L, "eyJhbGciOiJIUzI1NiJ9", false, false, user);
        List<Token> validTokens = List.of(token1, token2);

        when(tokenRepository.findAllValidTokenByUser(user.getId())).thenReturn(validTokens);

        // when
        underTestTokenService.revokeAllUserTokens(user);

        // then
        verify(tokenRepository, times(1)).saveAll(any());
        validTokens.forEach(token -> {
            assertTrue(token.isExpired());
            assertTrue(token.isRevoked());
        });
    }
}