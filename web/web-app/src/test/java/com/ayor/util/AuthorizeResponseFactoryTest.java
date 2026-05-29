package com.ayor.util;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.UserLoginSessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizeResponseFactoryTest {

    private final JWTUtils jwtUtils = mock(JWTUtils.class);
    private final RoleMapper roleMapper = mock(RoleMapper.class);
    private final UserLoginSessionService loginSessionService = mock(UserLoginSessionService.class);
    private final AuthorizeResponseFactory factory = new AuthorizeResponseFactory(jwtUtils, roleMapper, loginSessionService);

    @Test
    void shouldCreateSessionBackedAuthorizeResponse() {
        Account account = new Account();
        account.setAccountId(7);
        account.setUsername("tester");
        account.setRoleId(2);
        Date expireTime = new Date(System.currentTimeMillis() + 60_000);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(roleMapper.getRoleNameById(2)).thenReturn("USER");
        when(jwtUtils.createLoginJwt(any(UserDetails.class), eq(7), eq("tester"), anyString()))
                .thenReturn(new JWTUtils.LoginJwt("jwt-token", "jwt-1", "session-1", expireTime));

        AuthorizeVO result = factory.create(account, request);

        assertEquals("tester", result.getUsername());
        assertEquals("USER", result.getRole());
        assertEquals("jwt-token", result.getToken());
        assertEquals(expireTime, result.getExpire());
        ArgumentCaptor<String> sessionCaptor = ArgumentCaptor.forClass(String.class);
        verify(jwtUtils).createLoginJwt(any(UserDetails.class), eq(7), eq("tester"), sessionCaptor.capture());
        verify(loginSessionService).createSession(7, sessionCaptor.getValue(), "jwt-1", expireTime, request);
    }
}
