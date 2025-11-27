package com.ayor.config;

import com.ayor.entity.app.vo.AuthorizeVO;
import com.ayor.entity.pojo.Account;
import com.ayor.filter.JWTAuthorizeFilter;
import com.ayor.mapper.AccountMapper;
import com.ayor.result.Result;
import com.ayor.result.ResultCodeEnum;
import com.ayor.util.JWTUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Resource
    private JWTUtils jwtUtil;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private JWTAuthorizeFilter jwtAuthorizeFilter;


    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.requestMatchers("/api/*/info/**").permitAll();
                    auth.requestMatchers("/chat", "/chatboard", "/system").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(auth -> {
                    auth.loginProcessingUrl("/api/auth/login");
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
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .build();
    }

    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        Authentication  auth) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        User user = (User) auth.getPrincipal();
        Account account = accountMapper.getAccountByUsername(user.getUsername());
        String token = jwtUtil.createJwt(user, account.getAccountId(), user.getUsername());
        AuthorizeVO authorizeVO = new AuthorizeVO();
        BeanUtils.copyProperties(account, authorizeVO);
        authorizeVO.setToken(token);
        authorizeVO.setExpire(jwtUtil.expiredTime());
        resp.getWriter().write(Result.ok(authorizeVO).toJSONString());
    }

    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        AuthenticationException exception) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(Result.fail(401, exception.getMessage()).toJSONString());
    }

    public void onLogoutSuccess(HttpServletRequest req,
                                HttpServletResponse resp,
                                Authentication auth) throws IOException, ServletException {
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

    public void onAccessDeny(HttpServletRequest req,
                             HttpServletResponse resp,
                             AccessDeniedException e) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(Result.fail(403, "权限不足, 请联系管理员").toJSONString());
    }

    public void onUnauthorized(HttpServletRequest req,
                               HttpServletResponse resp,
                               AuthenticationException e) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        resp.setStatus(200);
        resp.getWriter().write(Result.fail(401, e.getMessage()).toJSONString());
    }

}
