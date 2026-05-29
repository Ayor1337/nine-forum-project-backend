package com.ayor.config;

import com.ayor.entity.vo.AuthorizeVO;
import com.ayor.entity.pojo.Account;
import com.ayor.filter.JWTAuthorizeFilter;
import com.ayor.filter.MuteActionFilter;
import com.ayor.mapper.AccountMapper;
import com.ayor.result.Result;
import com.ayor.result.ResultCodeEnum;
import com.ayor.service.UserLoginSessionService;
import com.ayor.util.AuthorizeResponseFactory;
import com.ayor.util.JWTUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

    private static final String LOGIN_PATH = "/api/auth/login";

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/auth/register-verifications",
            "/api/auth/registrations",
            LOGIN_PATH,
            "/api/passkeys/authentication/options",
            "/api/passkeys/authentications"
    };

    private static final String[] AUTHENTICATED_USER_ENDPOINTS = {
            "/api/users/me",
            "/api/users/me/**"
    };
    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/users/{user_id}",
            "/api/users/{user_id}/stats",
            "/api/users/{user_id}/followers",
            "/api/users/{user_id}/followings",
            "/api/themes",
            "/api/themes/topics",
            "/api/themes/{theme_id}/topics",
            "/api/topics/{topic_id}/tags",
            "/api/topics/{topic_id}/threads",
            "/api/topics/{topic_id}/thread-rankings",
            "/api/thread-rankings",
            "/api/users/{user_id}/threads",
            "/api/threads/{thread_id}",
            "/api/topics/{topic_id}/announcements",
            "/api/threads/{thread_id}/posts",
            "/api/threads/{thread_id}/likes/count",
            "/api/users/{user_id}/liked-threads",
            "/api/threads/{thread_id}/collections/count",
            "/api/users/{user_id}/collected-threads",
            "/api/search/users",
            "/api/search/hot-keywords",
            "/api/topics/{topic_id}/chat-messages",
            "/api/topics/{topic_id}/breadcrumb",
            "/api/threads/{thread_id}/breadcrumb",
            "/api/page-broadcasts/active"
    };

    private static final String[] PUBLIC_PAGE_ENDPOINTS = {
            "/chat",
            "/chatboard",
            "/system"
    };

    @Resource
    private JWTUtils jwtUtil;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private JWTAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    private MuteActionFilter muteActionFilter;

    @Resource
    private AuthorizeResponseFactory authorizeResponseFactory;

    @Resource
    private UserLoginSessionService loginSessionService;

    /**
     * 构造 Spring Security 过滤链。
     *
     * @param http HTTP 安全构建器
     * @return 安全过滤链
     * @throws Exception 配置异常
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll();
                    auth.requestMatchers(AUTHENTICATED_USER_ENDPOINTS).authenticated();
                    auth.requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll();
                    auth.requestMatchers(PUBLIC_PAGE_ENDPOINTS).permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(auth -> {
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
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(muteActionFilter, JWTAuthorizeFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .build();
    }

    /**
     * 登录成功后的响应处理。
     *
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @param auth 认证信息
     * @throws IOException IO 异常
     */
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        Authentication  auth) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        User user = (User) auth.getPrincipal();
        Account account = accountMapper.getAccountByUsername(user.getUsername());
        AuthorizeVO authorizeVO = authorizeResponseFactory.create(account, req);
        resp.getWriter().write(Result.ok(authorizeVO).toJSONString());
    }

    /**
     * 登录失败后的响应处理。
     *
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @param exception 认证异常
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    public void onAuthenticationFailure(HttpServletRequest req,
                                        HttpServletResponse resp,
                                        AuthenticationException exception) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(Result.fail(401, "用户名或密码错误").toJSONString());
    }

    /**
     * 退出登录成功后的响应处理。
     *
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @param auth 认证信息
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    public void onLogoutSuccess(HttpServletRequest req,
                                HttpServletResponse resp,
                                Authentication auth) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        String authorization = req.getHeader("Authorization");
        DecodedJWT jwt = jwtUtil.resolveJwt(authorization);
        String sessionId = jwt == null ? null : jwt.getClaim("sid").asString();
        PrintWriter writer = resp.getWriter();
        // 校验是否登录，如果没有登录就不可能退出登录
        if (jwtUtil.invalidateJWT(authorization)) {
            loginSessionService.revokeCurrentSession(sessionId);
            writer.write(Result.build(null, ResultCodeEnum.LOGOUT_SUCCESS).toJSONString());
        } else {
            writer.write(Result.build(null, ResultCodeEnum.LOGOUT_FAILURE).toJSONString());
        }
    }

    /**
     * 访问被拒绝时的响应处理。
     *
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @param e 权限异常
     * @throws IOException IO 异常
     * @throws ServletException Servlet 异常
     */
    public void onAccessDeny(HttpServletRequest req,
                             HttpServletResponse resp,
                             AccessDeniedException e) throws IOException, ServletException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(Result.fail(403, "权限不足, 请联系管理员").toJSONString());
    }

    /**
     * 未认证时的响应处理。
     *
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @param e 认证异常
     * @throws IOException IO 异常
     */
    public void onUnauthorized(HttpServletRequest req,
                               HttpServletResponse resp,
                               AuthenticationException e) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");
        resp.setStatus(200);
        resp.getWriter().write(Result.fail(401, e.getMessage()).toJSONString());
    }

}
