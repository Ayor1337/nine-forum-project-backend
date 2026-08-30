package com.ayor.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityUtilsTest {

    private final SecurityUtils securityUtils = new SecurityUtils();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // 测试获取可选安全上下文用户 ID 返回已认证用户 ID
    @Test
    void getOptionalSecurityUserIdShouldReturnAuthenticatedUserId() {
        UserDetails principal = User.withUsername("42")
                .password("unused")
                .authorities(List.of())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertEquals(42, securityUtils.getOptionalSecurityUserId());
    }

    // 测试未认证时可选安全上下文用户ID返回空
    @Test
    void getOptionalSecurityUserIdShouldReturnNullWhenUnauthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("42", null)
        );

        assertNull(securityUtils.getOptionalSecurityUserId());
    }

    // 测试认证主体无效时可选安全上下文用户ID返回空
    @Test
    void getOptionalSecurityUserIdShouldReturnNullWhenPrincipalIsInvalid() {
        UserDetails principal = User.withUsername("not-a-number")
                .password("unused")
                .authorities(List.of())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        assertNull(securityUtils.getOptionalSecurityUserId());
    }

    // 测试未认证时强制获取安全上下文用户ID仍抛异常
    @Test
    void getSecurityUserIdShouldStillThrowWhenUnauthenticated() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, securityUtils::getSecurityUserId);
    }
}
