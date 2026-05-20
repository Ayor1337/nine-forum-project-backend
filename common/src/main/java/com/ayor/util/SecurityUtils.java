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
        Integer userId = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof UserDetails) {
                        return Integer.parseInt(((UserDetails) principal).getUsername());
                    } else
                        return 0;
                })
                .orElse(0);
        if (userId == null || userId <= 0) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        return userId;
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

}
