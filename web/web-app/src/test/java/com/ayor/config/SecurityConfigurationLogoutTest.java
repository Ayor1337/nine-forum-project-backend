package com.ayor.config;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.service.UserLoginSessionService;
import com.ayor.util.JWTUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityConfigurationLogoutTest {

    @Test
    void shouldRevokeCurrentLoginSessionWhenLogoutSucceeds() throws Exception {
        SecurityConfiguration configuration = new SecurityConfiguration();
        JWTUtils jwtUtils = mock(JWTUtils.class);
        UserLoginSessionService loginSessionService = mock(UserLoginSessionService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        StringWriter body = new StringWriter();
        ReflectionTestUtils.setField(configuration, "jwtUtil", jwtUtils);
        ReflectionTestUtils.setField(configuration, "loginSessionService", loginSessionService);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(jwtUtils.resolveJwt("Bearer token")).thenReturn(decodedJWT);
        when(decodedJWT.getClaim("sid")).thenReturn(claim);
        when(claim.asString()).thenReturn("session-1");
        when(jwtUtils.invalidateJWT("Bearer token")).thenReturn(true);

        configuration.onLogoutSuccess(request, response, null);

        verify(loginSessionService).revokeCurrentSession("session-1");
    }
}
