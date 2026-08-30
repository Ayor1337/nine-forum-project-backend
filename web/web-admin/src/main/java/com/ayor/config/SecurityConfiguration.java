package com.ayor.config;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.filter.JWTAuthorizeFilter;
import com.ayor.mapper.AccountMapper;
import com.ayor.result.Result;
import com.ayor.result.ResultCodeEnum;
import com.ayor.security.AdminRoleRequiredException;
import com.ayor.service.AccountService;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REQUIRED_ROLE = "ROLE_OWNER";

    @Resource
    private JWTUtils jwtUtil;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private JWTAuthorizeFilter jwtAuthorizeFilter;

    /**
     * 管理端专用认证提供者，保留非 OWNER 角色异常以便映射为 403。
     */
    @Bean
    DaoAuthenticationProvider adminAuthenticationProvider(AccountService accountService,
                                                           PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(accountService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        authenticationProvider.setHideUserNotFoundExceptions(false);
        return authenticationProvider;
    }

    /**
     * 构建管理端的安全过滤链，挂载登录、登出、异常处理和 JWT 授权过滤器。
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    DaoAuthenticationProvider authenticationProvider) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.POST, LOGIN_PATH).permitAll();
                    auth.anyRequest().hasAuthority(REQUIRED_ROLE);
                })
                .formLogin(auth -> {
                    // There is no anonymous HTML login page in the management app.
                    auth.loginPage(LOGIN_PATH);
                    auth.loginProcessingUrl(LOGIN_PATH);
                    auth.successHandler(this::onAuthenticationSuccess);
                    auth.failureHandler(this::onAuthenticationFailure);
                })
                .logout(auth -> {
                    auth.logoutUrl("/api/auth/logout");
                    auth.logoutSuccessHandler(this::onLogoutSuccess);
                })
                .sessionManagement(auth -> {
                    auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .exceptionHandling(conf -> {
                    conf.accessDeniedHandler(this::onAccessDeny);
                    conf.authenticationEntryPoint(this::onUnauthorized);
                })
                .authenticationProvider(authenticationProvider)
                // LogoutFilter precedes UsernamePasswordAuthenticationFilter; JWT must
                // run first so logout can enforce the same OWNER gate.
                .addFilterBefore(jwtAuthorizeFilter, LogoutFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .build();
    }

    /**
     * 登录成功后签发 JWT，并把账号基础信息一起返回给前端。
     */
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        Authentication auth) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        User user = (User) auth.getPrincipal();
        Account account = accountMapper.getAccountByName(user.getUsername());
        String token = jwtUtil.createJwt(user, account.getAccountId(), user.getUsername());
        AuthorizeVO authorizeVO = new AuthorizeVO();
        BeanUtils.copyProperties(account, authorizeVO);
        authorizeVO.setToken(token);
        authorizeVO.setExpire(jwtUtil.expiredTime());
        resp.getWriter().write(Result.ok(authorizeVO).toJSONString());
    }

    /**
     * 登录失败时返回统一的认证错误响应。
     */
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        AuthenticationException exception) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        if (exception instanceof AdminRoleRequiredException) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(Result.fail(403, "权限不足").toJSONString());
            return;
        }
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write(Result.fail(401, "用户名或密码错误").toJSONString());
    }

    /**
     * 退出登录时失效当前 JWT，并返回退出结果。
     */
    public void onLogoutSuccess(HttpServletRequest req,
                                HttpServletResponse resp,
                                Authentication auth) throws IOException, ServletException {
        // LogoutFilter runs before URL authorization, so guard this endpoint here too.
        if (auth == null) {
            onUnauthorized(req, resp, null);
            return;
        }
        if (!auth.getAuthorities().stream()
                .anyMatch(authority -> REQUIRED_ROLE.equals(authority.getAuthority()))) {
            onAccessDeny(req, resp, new AccessDeniedException("权限不足"));
            return;
        }
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        String authorization = req.getHeader("Authorization");
        PrintWriter writer = resp.getWriter();
        // 校验是否登录，如果没有登录就不可能退出登录
        if (jwtUtil.invalidateJWT(authorization)) {
            writer.write(Result.build(null, ResultCodeEnum.LOGOUT_SUCCESS).toJSONString());
        } else {
            writer.write(Result.build(null, ResultCodeEnum.LOGOUT_FAILURE).toJSONString());
        }
    }

    /**
     * 权限不足时返回统一错误响应。
     */
    public void onAccessDeny(HttpServletRequest req,
                             HttpServletResponse resp,
                             AccessDeniedException e) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.getWriter().write(Result.fail(403, "权限不足, 请联系管理员").toJSONString());
    }

    /**
     * 未认证时返回统一错误响应。
     */
    public void onUnauthorized(HttpServletRequest req,
                               HttpServletResponse resp,
                               AuthenticationException e) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write(Result.fail(401, "未认证").toJSONString());
    }

}
