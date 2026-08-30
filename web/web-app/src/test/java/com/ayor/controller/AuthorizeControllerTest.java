package com.ayor.controller;

import com.ayor.entity.dto.RegDTO;
import com.ayor.mail.EmailHtmlTemplates;
import com.ayor.result.Result;
import com.ayor.service.AccountService;
import com.ayor.service.AuthorizeService;
import com.ayor.service.RegistrationVerificationRateLimitException;
import com.ayor.controller.exception.RegistrationVerificationExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthorizeControllerTest {

    private AuthorizeService authorizeService;
    private AuthorizeController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authorizeService = mock(AuthorizeService.class);
        controller = new AuthorizeController(
                authorizeService,
                mock(AccountService.class),
                mock(EmailHtmlTemplates.class)
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RegistrationVerificationExceptionHandler())
                .build();
    }

    @Test
    void registerVerifyPassesServletResolvedRemoteAddress() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.8");
        when(authorizeService.createAuthorizeToken("user@example.com", "203.0.113.8")).thenReturn("jwt-id");

        Result<String> result = controller.registerVerify(new RegDTO("user@example.com"), request);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("jwt-id");
        verify(authorizeService).createAuthorizeToken("user@example.com", "203.0.113.8");
    }

    @Test
    void registerVerifyReturns429AndRetryAfterWhenQuotaIsLimited() throws Exception {
        when(authorizeService.createAuthorizeToken("user@example.com", "127.0.0.1"))
                .thenThrow(new RegistrationVerificationRateLimitException(37));

        mockMvc.perform(post("/api/auth/register-verifications")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "37"))
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").value("请求过于频繁，请稍后重试"));
    }
}
