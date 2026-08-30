package com.ayor.util;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.mapper.RoleMapper;
import com.ayor.service.UserLoginSessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthorizeResponseFactory {

    private final JWTUtils jwtUtils;

    private final RoleMapper roleMapper;

    private final UserLoginSessionService loginSessionService;

    public AuthorizeVO create(Account account) {
        return create(account, null);
    }

    public AuthorizeVO create(Account account, HttpServletRequest request) {
        String roleName = roleMapper.getRoleNameById(account.getRoleId());
        UserDetails user = User.withUsername(account.getUsername())
                .password("N/A")
                .roles(roleName)
                .build();
        String sessionId = request == null ? null : UUID.randomUUID().toString();
        JWTUtils.LoginJwt loginJwt = jwtUtils.createLoginJwt(user, account.getAccountId(), account.getUsername(), sessionId);
        if (request != null) {
            loginSessionService.createSession(account.getAccountId(), sessionId, loginJwt.jwtId(), loginJwt.expireTime(), request);
        }
        AuthorizeVO authorizeVO = new AuthorizeVO();
        authorizeVO.setUsername(account.getUsername());
        authorizeVO.setRole(roleName);
        authorizeVO.setToken(loginJwt.token());
        authorizeVO.setExpire(loginJwt.expireTime());
        return authorizeVO;
    }
}
