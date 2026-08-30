package securitycontract;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.Account;
import com.ayor.config.SecurityConfiguration;
import com.ayor.config.WebConfiguration;
import com.ayor.filter.JWTAuthorizeFilter;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.PermissionMapper;
import com.ayor.mapper.RoleMapper;
import com.ayor.security.AdminRoleRequiredException;
import com.ayor.service.AccountService;
import com.ayor.util.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = com.ayor.controller.AccountController.class)
@ContextConfiguration(classes = AdminSecurityContractTest.TestApplication.class)
class AdminSecurityContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JWTUtils jwtUtils;

    @MockitoBean
    private AccountMapper accountMapper;

    @MockitoBean
    private PermissionMapper permissionMapper;

    @MockitoBean
    private RoleMapper roleMapper;

    @BeforeEach
    void resetServiceStubs() {
        when(accountService.getAccounts(nullable(String.class), any(Integer.class), any(Integer.class), nullable(Integer.class)))
                .thenReturn(new PageEntity<>(0L, List.of()));
    }

    @Test
    void anonymousManagementApiIsRejectedBeforeController() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        verify(accountService, never()).getAccounts(nullable(String.class), any(Integer.class), any(Integer.class), nullable(Integer.class));
    }

    @Test
    void anonymousOpenApiDocumentationIsRejected() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void onlyPostLoginIsAnonymous() throws Exception {
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(get("/login"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void nonOwnerCannotAccessManagementApi() throws Exception {
        mockMvc.perform(get("/api/accounts").with(user("member").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("权限不足")));

        verify(accountService, never()).getAccounts(nullable(String.class), any(Integer.class), any(Integer.class), nullable(Integer.class));
    }

    @Test
    void ownerCanAccessManagementApi() throws Exception {
        mockMvc.perform(get("/api/accounts").with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(accountService).getAccounts(nullable(String.class), any(Integer.class), any(Integer.class), nullable(Integer.class));
    }

    @Test
    void nonOwnerLoginIsRejectedWithPermissionMessage() throws Exception {
        when(accountService.loadUserByUsername("member"))
                .thenThrow(new AdminRoleRequiredException());

        mockMvc.perform(post("/api/auth/login")
                        .param("username", "member")
                        .param("password", "correct-password"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("权限不足"))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("token"))));
    }

    @Test
    void invalidCredentialsAreRejectedAsUnauthorized() throws Exception {
        when(accountService.loadUserByUsername("owner"))
                .thenReturn(User.withUsername("owner")
                        .password(passwordEncoder.encode("correct-password"))
                        .roles("OWNER")
                        .build());

        mockMvc.perform(post("/api/auth/login")
                        .param("username", "owner")
                        .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void ownerLoginReturnsToken() throws Exception {
        String encodedPassword = passwordEncoder.encode("correct-password");
        when(accountService.loadUserByUsername("owner"))
                .thenReturn(User.withUsername("owner").password(encodedPassword).roles("OWNER").build());

        Account account = new Account();
        account.setAccountId(1);
        account.setUsername("owner");
        when(accountMapper.getAccountByName("owner")).thenReturn(account);
        when(jwtUtils.createJwt(any(UserDetails.class), eq(1), eq("owner"))).thenReturn("owner-token");
        when(jwtUtils.expiredTime()).thenReturn(new Date());

        mockMvc.perform(post("/api/auth/login")
                        .param("username", "owner")
                        .param("password", "correct-password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("owner-token"));
    }

    @Test
    void anonymousLogoutIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void nonOwnerLogoutIsRejected() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(user("member").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("权限不足")));
    }

    @Test
    void ownerCanLogout() throws Exception {
        when(jwtUtils.invalidateJWT("Bearer owner-token")).thenReturn(true);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer owner-token")
                        .with(user("owner").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @SpringBootConfiguration
    @Import({SecurityConfiguration.class, WebConfiguration.class, AdminSecurityContractTest.PassThroughFilterConfiguration.class})
    static class TestApplication {

        @Bean
        com.ayor.controller.AccountController accountController(AccountService accountService) {
            return new com.ayor.controller.AccountController(accountService);
        }
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class PassThroughFilterConfiguration {

        @Bean(name = "JWTAuthorizeFilter")
        JWTAuthorizeFilter jwtAuthorizeFilter() {
            return new JWTAuthorizeFilter() {
                @Override
                protected void doFilterInternal(HttpServletRequest request,
                                                HttpServletResponse response,
                                                FilterChain filterChain) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }
}
