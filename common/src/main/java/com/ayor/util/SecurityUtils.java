package com.ayor.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    /**
     * 获取当前认证用户的 ID。
     *
     * @return 用户 ID
     * @throws AuthenticationCredentialsNotFoundException 未找到登录验证凭证
     */
    public Integer getSecurityUserId() {
        Integer userId = getOptionalSecurityUserId();
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        return userId;
    }

    /**
     * 获取当前认证用户的 ID，未登录或无法解析时返回 null。
     *
     * @return 用户 ID，未认证时返回 null
     */
    public Integer getOptionalSecurityUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(this::parsePrincipalUserId)
                .filter(userId -> userId > 0)
                .orElse(null);
    }

    /**
     * 获取当前认证对象。
     *
     * @return 认证对象，未认证时返回 null
     */
    public Authentication getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .orElse(null);
    }

    private Integer parsePrincipalUserId(Object principal) {
        String username = null;
        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String principalValue) {
            username = principalValue;
        }
        if (username == null) {
            return null;
        }
        try {
            return Integer.parseInt(username);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
