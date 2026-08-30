package com.ayor.config;

import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityConfigurationUnauthorizedTest {

    // 测试返回未认证状态码当未授权
    @Test
    void shouldReturnUnauthenticatedCodeWhenUnauthorized() throws Exception {
        SecurityConfiguration configuration = new SecurityConfiguration();
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        configuration.onUnauthorized(null, response, new BadCredentialsException("Unauthorized"));

        JSONObject result = JSONObject.parseObject(body.toString());
        assertThat(result.getInteger("code")).isEqualTo(401);
        assertThat(result.getString("message")).isEqualTo("Unauthorized");
        verify(response).setStatus(200);
    }
}
