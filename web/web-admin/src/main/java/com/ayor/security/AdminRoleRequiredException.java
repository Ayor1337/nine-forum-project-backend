package com.ayor.security;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 表示账号存在但不具备管理端要求的 OWNER 角色。
 */
public class AdminRoleRequiredException extends UsernameNotFoundException {

    public AdminRoleRequiredException() {
        super("用户权限不足");
    }
}
