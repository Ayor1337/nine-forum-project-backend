package com.ayor.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtils {

    public String getSecurityUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof UserDetails) {
                        return ((UserDetails) principal).getUsername();
                    } else {
                        return principal.toString();
                    }
                })
                .orElse(null);
    }

    public String getSecurityUserId() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof UserDetails) {
                        return ((UserDetails) principal).getUsername(); // 这里应该返回用户ID，根据你的实现来定
                    } else {
                        return principal.toString();
                    }
                })
                .orElse(null);
    }
    
    public Authentication getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .orElse(null);
    }

}