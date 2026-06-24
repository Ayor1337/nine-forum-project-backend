package com.ayor.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.message.EmailVerifyMessage;
import com.ayor.entity.stomp.VerifyMessage;
import com.ayor.type.EmailVerifyType;
import com.ayor.util.JWTUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizeServiceImplTest {

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    // 测试创建验证Token时发送注册邮件消息并返回JWT ID
    @Test
    void createAuthorizeTokenSendsRegisterEmailMessageAndReturnsJwtId() {
        AuthorizeServiceImpl service = new AuthorizeServiceImpl(jwtUtils, rabbitTemplate, messagingTemplate);
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(jwtUtils.createJwt("user@example.com")).thenReturn("token");
        when(jwtUtils.resolveEmailJwt("token")).thenReturn(decodedJWT);
        when(decodedJWT.getId()).thenReturn("jwt-id");

        String result = service.createAuthorizeToken("user@example.com");

        ArgumentCaptor<EmailVerifyMessage> captor = ArgumentCaptor.forClass(EmailVerifyMessage.class);
        verify(rabbitTemplate).convertAndSend(eq("mail.direct"), eq("mail"), captor.capture());
        assertThat(result).isEqualTo("jwt-id");
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getToken()).isEqualTo("token");
        assertThat(captor.getValue().getType()).isEqualTo(EmailVerifyType.REGISTER);
    }

    // 测试校验授权 Token 拒绝解析失败的 Token
    @Test
    void validateAuthorizeTokenRejectsUnresolvedToken() {
        AuthorizeServiceImpl service = new AuthorizeServiceImpl(jwtUtils, rabbitTemplate, messagingTemplate);
        when(jwtUtils.resolveEmailJwt("bad")).thenReturn(null);

        assertThat(service.validateAuthorizeToken("bad", "user@example.com")).isFalse();
        verify(messagingTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(VerifyMessage.class));
    }

    // 测试校验授权 Token 拒绝邮箱不匹配
    @Test
    void validateAuthorizeTokenRejectsEmailMismatch() {
        AuthorizeServiceImpl service = new AuthorizeServiceImpl(jwtUtils, rabbitTemplate, messagingTemplate);
        DecodedJWT decodedJWT = decodedEmailJwt(null, "other@example.com");
        when(jwtUtils.resolveEmailJwt("token")).thenReturn(decodedJWT);

        assertThat(service.validateAuthorizeToken("token", "user@example.com")).isFalse();
        verify(messagingTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(VerifyMessage.class));
    }

    // 测试校验授权 Token 推送验证消息在成功
    @Test
    void validateAuthorizeTokenPushesVerifyMessageOnSuccess() {
        AuthorizeServiceImpl service = new AuthorizeServiceImpl(jwtUtils, rabbitTemplate, messagingTemplate);
        DecodedJWT decodedJWT = decodedEmailJwt("jwt-id", "user@example.com");
        when(jwtUtils.resolveEmailJwt("token")).thenReturn(decodedJWT);

        assertThat(service.validateAuthorizeToken("token", "user@example.com")).isTrue();

        ArgumentCaptor<VerifyMessage> captor = ArgumentCaptor.forClass(VerifyMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/verify/jwt-id"), captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("token");
        assertThat(captor.getValue().getIsVerified()).isTrue();
    }

    private DecodedJWT decodedEmailJwt(String jwtId, String email) {
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);
        if (jwtId != null) {
            when(decodedJWT.getId()).thenReturn(jwtId);
        }
        when(decodedJWT.getClaim("email")).thenReturn(claim);
        when(claim.asString()).thenReturn(email);
        return decodedJWT;
    }
}
