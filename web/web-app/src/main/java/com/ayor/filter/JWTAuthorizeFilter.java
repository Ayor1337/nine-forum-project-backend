package com.ayor.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.result.Result;
import com.ayor.type.AccountStatus;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
import java.util.List;

@Component
public class JWTAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    private JWTUtils jwtUtil;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private RoleMapper roleMapper;

    /**
     * 解析请求中的 JWT，并把用户权限写入 Spring Security 上下文。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
//        if(authorization == null) {
//            authorization = Optional.ofNullable(request.getCookies())
//                    .map(cookies -> {
//                        for (Cookie cookie : cookies) {
//                            if(cookie.getName().equals("Authorization")) {
//
//                                return "Bearer " + cookie.getValue();
//                            }
//                        }
//                        return null;
//                    }).orElse( null);
//        }
        DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
        if(jwt != null) {
            UserDetails user = jwtUtil.toUser(jwt);
            Integer userId = Integer.parseInt(user.getUsername());
            Account account = accountMapper.getAccountById(userId);
            if (account != null && AccountStatus.fromCode(account.getStatus()) == AccountStatus.BANNED) {
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(Result.fail(401, "账号已被封禁").toJSONString());
                return;
            }
            // 获取用户权限
            String roleNameByUsername = roleMapper.getRoleNameByUserId(userId);

            // 创建新的授权信息
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleNameByUsername));

            List<String> permissions = permissionMapper.getPermissionsByAccountId(userId);
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority("PERM_" +  permission));
            }

            Integer topicId = roleMapper.getTopicIdByUserId(userId);

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
