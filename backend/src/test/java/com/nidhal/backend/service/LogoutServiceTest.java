package com.nidhal.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogoutServiceTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LogoutService underTestLogoutService;


    @Test
    public void testLogout_WithValidAuthorizationHeader_CallsRevokeToken() {
        String validAuthHeader = "Bearer validToken";
        when(request.getHeader("Authorization")).thenReturn(validAuthHeader);

        underTestLogoutService.logout(request, response, authentication);

        verify(tokenService).revokeToken("validToken");
        verifyNoMoreInteractions(tokenService);
        verifyNoInteractions(response);
    }

    @Test
    public void testLogoutWithInvalidAuthorizationHeader() {
        String invalidAuthHeader = "InvalidHeader";
        when(request.getHeader("Authorization")).thenReturn(invalidAuthHeader);

        underTestLogoutService.logout(request, response, authentication);

        verify(tokenService, never()).revokeToken(anyString());
        verifyNoInteractions(response);
    }


    @Test
    public void testLogoutWithNullAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        underTestLogoutService.logout(request, response, authentication);

        verify(tokenService, never()).revokeToken(anyString());
        verifyNoInteractions(response);
    }


}
