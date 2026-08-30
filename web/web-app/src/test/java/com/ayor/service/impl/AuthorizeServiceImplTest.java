package com.ayor.service.impl;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.message.EmailVerifyMessage;
import com.ayor.entity.stomp.VerifyMessage;
import com.ayor.service.RegistrationVerificationGate;
import com.ayor.service.RegistrationVerificationRateLimitException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AuthorizeServiceImplTest {

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RegistrationVerificationGate registrationVerificationGate;

    // 测试创建验证Token时发送注册邮件消息并返回JWT ID
    @Test
    void createAuthorizeTokenSendsRegisterEmailMessageAndReturnsJwtId() {
        AuthorizeServiceImpl service = createService();
        when(jwtUtils.newEmailJwtId()).thenReturn("candidate-id");
        when(registrationVerificationGate.acquire("user@example.com", "127.0.0.1", "candidate-id"))
                .thenReturn(RegistrationVerificationGate.Acquisition.granted("candidate-id"));
        when(jwtUtils.createEmailJwt("user@example.com", "candidate-id"))
                .thenReturn(new JWTUtils.EmailJwt("token", "candidate-id", new java.util.Date()));

        String result = service.createAuthorizeToken(" User@Example.COM ", "127.0.0.1");

        ArgumentCaptor<EmailVerifyMessage> captor = ArgumentCaptor.forClass(EmailVerifyMessage.class);
        verify(rabbitTemplate).convertAndSend(eq("mail.direct"), eq("mail"), captor.capture());
        assertThat(result).isEqualTo("candidate-id");
        assertThat(captor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(captor.getValue().getToken()).isEqualTo("token");
        assertThat(captor.getValue().getType()).isEqualTo(EmailVerifyType.REGISTER);
    }

    @Test
    void createAuthorizeTokenReusesExistingJwtWithoutCreatingTokenOrSendingEmail() {
        AuthorizeServiceImpl service = createService();
        when(jwtUtils.newEmailJwtId()).thenReturn("candidate-id");
        when(registrationVerificationGate.acquire("user@example.com", "127.0.0.1", "candidate-id"))
                .thenReturn(RegistrationVerificationGate.Acquisition.reused("existing-id"));

        assertThat(service.createAuthorizeToken("user@example.com", "127.0.0.1")).isEqualTo("existing-id");

        verify(jwtUtils, never()).createEmailJwt(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
        verify(rabbitTemplate, never()).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(Object.class)
        );
    }

    @Test
    void createAuthorizeTokenRejectsLimitedRequestWithoutJwtOrRabbitSideEffects() {
        AuthorizeServiceImpl service = createService();
        when(jwtUtils.newEmailJwtId()).thenReturn("candidate-id");
        when(registrationVerificationGate.acquire("user@example.com", "127.0.0.1", "candidate-id"))
                .thenReturn(RegistrationVerificationGate.Acquisition.limited(42));

        RegistrationVerificationRateLimitException exception = assertThrows(
                RegistrationVerificationRateLimitException.class,
                () -> service.createAuthorizeToken("user@example.com", "127.0.0.1")
        );

        assertThat(exception.getRetryAfterSeconds()).isEqualTo(42);
        verify(jwtUtils, never()).createEmailJwt(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()
        );
        verify(rabbitTemplate, never()).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(Object.class)
        );
    }

    // 测试校验授权 Token 拒绝解析失败的 Token
    @Test
    void validateAuthorizeTokenRejectsUnresolvedToken() {
        AuthorizeServiceImpl service = createService();
        when(jwtUtils.resolveEmailJwt("bad")).thenReturn(null);

        assertThat(service.validateAuthorizeToken("bad", "user@example.com")).isFalse();
        verify(messagingTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(VerifyMessage.class));
    }

    // 测试校验授权 Token 拒绝邮箱不匹配
    @Test
    void validateAuthorizeTokenRejectsEmailMismatch() {
        AuthorizeServiceImpl service = createService();
        DecodedJWT decodedJWT = decodedEmailJwt(null, "other@example.com");
        when(jwtUtils.resolveEmailJwt("token")).thenReturn(decodedJWT);

        assertThat(service.validateAuthorizeToken("token", "user@example.com")).isFalse();
        verify(messagingTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(VerifyMessage.class));
    }

    // 测试校验授权 Token 推送验证消息在成功
    @Test
    void validateAuthorizeTokenPushesVerifyMessageOnSuccess() {
        AuthorizeServiceImpl service = createService();
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

    private AuthorizeServiceImpl createService() {
        return new AuthorizeServiceImpl(jwtUtils, rabbitTemplate, messagingTemplate, registrationVerificationGate);
    }
}
