package com.ayor.filter;

import com.alibaba.fastjson2.JSONObject;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.type.AccountStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MuteActionFilterTest {

    @Mock
    private AccountMapper accountMapper;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // 测试非禁言操作路由放行不带账号查询
    @Test
    void nonMutedActionRoutePassesWithoutAccountLookup() throws Exception {
        MuteActionFilter filter = new MuteActionFilter(accountMapper);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/threads");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        verify(accountMapper, never()).getAccountById(org.mockito.ArgumentMatchers.anyInt());
    }

    // 测试禁言操作路由放行当未认证
    @Test
    void mutedActionRoutePassesWhenUnauthenticated() throws Exception {
        MuteActionFilter filter = new MuteActionFilter(accountMapper);
        MockHttpServletRequest request = mutedRequest("/api/threads");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        verify(accountMapper, never()).getAccountById(org.mockito.ArgumentMatchers.anyInt());
    }

    // 测试禁言操作路由放行用于正常账号
    @Test
    void mutedActionRoutePassesForNormalAccount() throws Exception {
        MuteActionFilter filter = new MuteActionFilter(accountMapper);
        authenticateAs("7");
        when(accountMapper.getAccountById(7)).thenReturn(account(AccountStatus.ACTIVE));
        MockHttpServletRequest request = mutedRequest("/api/conversations/3/messages");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    // 测试禁言操作路由拦截禁言账号带有 JSON 失败响应
    @Test
    void mutedActionRouteBlocksMutedAccountWithJsonFailure() throws Exception {
        MuteActionFilter filter = new MuteActionFilter(accountMapper);
        authenticateAs("7");
        when(accountMapper.getAccountById(7)).thenReturn(account(AccountStatus.MUTED));
        MockHttpServletRequest request = mutedRequest("/api/topics/9/chat-messages");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        JSONObject body = JSONObject.parseObject(response.getContentAsString());
        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(body.getInteger("code")).isEqualTo(403);
        assertThat(body.getString("message")).isEqualTo("账号已被禁言");
    }

    private void authenticateAs(String username) {
        User principal = new User(username, "password", List.of());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private MockHttpServletRequest mutedRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        return request;
    }

    private Account account(AccountStatus status) {
        Account account = new Account();
        account.setStatus(status.getCode());
        return account;
    }
}
