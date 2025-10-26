package com.ayor.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JWTAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    private JWTUtils jwtUtil;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private RoleMapper roleMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        if(authorization == null) {
            authorization = Optional.ofNullable(request.getCookies())
                    .map(cookies -> {
                        for (Cookie cookie : cookies) {
                            if(cookie.getName().equals("Authorization")) {

                                return "Bearer " + cookie.getValue();
                            }
                        }
                        return null;
                    }).orElse( null);
        }
        DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
        if(jwt != null) {
            UserDetails user = jwtUtil.toUser(jwt);
            // 获取用户权限
            String roleNameByUsername = roleMapper.getRoleNameByUsername(user.getUsername());

            // 创建新的授权信息
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleNameByUsername));

            List<String> permissions = permissionMapper.getPermissionsByUsername(user.getUsername());
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority("PERM_" +  permission));
            }

            Integer topicId = roleMapper.getTopicIdByUsername(user.getUsername());

            if (topicId != null) {
                authorities.add(new SimpleGrantedAuthority("TOPIC_" + topicId));
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
