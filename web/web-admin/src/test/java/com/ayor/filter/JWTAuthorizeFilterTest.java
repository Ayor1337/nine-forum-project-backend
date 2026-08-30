package com.ayor.filter;

import com.alibaba.fastjson2.JSONObject;
import com.ayor.result.ResultCodeEnum;
import com.ayor.util.JWTUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JWTAuthorizeFilterTest {

    // 测试Authorization请求头无法解析时返回Token过期
    @Test
    void shouldReturnTokenExpiredWhenAuthorizationHeaderCannotBeResolved() throws Exception {
        JWTAuthorizeFilter filter = new JWTAuthorizeFilter();
        JWTUtils jwtUtils = mock(JWTUtils.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtils);
        when(jwtUtils.resolveJwt("Bearer invalid-token")).thenReturn(null);

        filter.doFilter(request, response, new MockFilterChain());

        JSONObject result = JSONObject.parseObject(response.getContentAsString());
        assertThat(result.getInteger("code")).isEqualTo(ResultCodeEnum.TOKEN_EXPIRED.getCode());
        assertThat(result.getString("message")).isEqualTo(ResultCodeEnum.TOKEN_EXPIRED.getMessage());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    }

    // 测试继续过滤链当 Authorization 请求头为缺失
    @Test
    void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing() throws Exception {
        JWTAuthorizeFilter filter = new JWTAuthorizeFilter();
        JWTUtils jwtUtils = mock(JWTUtils.class);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtils);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getContentAsString()).isEmpty();
        assertThat(filterChain.getRequest()).isSameAs(request);
    }
}
