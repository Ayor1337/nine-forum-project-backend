package com.ayor.util;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Component
@RequiredArgsConstructor
public class AuthorizeResponseFactory {

    private final JWTUtils jwtUtils;

    private final RoleMapper roleMapper;

    public AuthorizeVO create(Account account) {
        String roleName = roleMapper.getRoleNameById(account.getRoleId());
        UserDetails user = User.withUsername(account.getUsername())
                .password("N/A")
                .roles(roleName)
                .build();
        String token = jwtUtils.createJwt(user, account.getAccountId(), account.getUsername());
        AuthorizeVO authorizeVO = new AuthorizeVO();
        authorizeVO.setUsername(account.getUsername());
        authorizeVO.setRole(roleName);
        authorizeVO.setToken(token);
        authorizeVO.setExpire(jwtUtils.expiredTime());
        return authorizeVO;
    }
}
