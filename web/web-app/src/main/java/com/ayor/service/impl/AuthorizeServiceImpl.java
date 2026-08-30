package com.ayor.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.message.EmailVerifyMessage;
import com.ayor.entity.stomp.VerifyMessage;
import com.ayor.service.AuthorizeService;
import com.ayor.service.RegistrationVerificationGate;
import com.ayor.service.RegistrationVerificationRateLimitException;
import com.ayor.type.EmailVerifyType;
import com.ayor.util.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 授权服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthorizeServiceImpl implements AuthorizeService {

    private final JWTUtils jwtUtils;

    private final RabbitTemplate rabbitTemplate;

    private final SimpMessagingTemplate messagingTemplate;

    private final RegistrationVerificationGate registrationVerificationGate;
    /**
     * 生成用于注册验证的邮箱 token。
     */

    @Override
    public String createAuthorizeToken(String email, String remoteAddress) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String candidateJwtId = jwtUtils.newEmailJwtId();
        RegistrationVerificationGate.Acquisition acquisition = registrationVerificationGate.acquire(
                normalizedEmail,
                remoteAddress,
                candidateJwtId
        );
        if (acquisition.status() == RegistrationVerificationGate.Status.REUSED) {
            return acquisition.jwtId();
        }
        if (acquisition.status() == RegistrationVerificationGate.Status.LIMITED) {
            throw new RegistrationVerificationRateLimitException(acquisition.retryAfterSeconds());
        }

        JWTUtils.EmailJwt emailJwt = jwtUtils.createEmailJwt(normalizedEmail, acquisition.jwtId());
        rabbitTemplate.convertAndSend(
                "mail.direct",
                "mail",
                new EmailVerifyMessage(normalizedEmail, emailJwt.token(), EmailVerifyType.REGISTER)
        );
        return acquisition.jwtId();
    }
    /**
     * 校验注册邮箱的验证 token。
     */

    @Override
    public boolean validateAuthorizeToken(String token, String email) {
        DecodedJWT decodedJWT = jwtUtils.resolveEmailJwt(token);
        if (decodedJWT == null) {
            return false;
        }
        String decodedEmail = decodedJWT.getClaim("email").asString();
        if (!decodedEmail.equals(email.trim().toLowerCase(Locale.ROOT)))
            return false;
        messagingTemplate.convertAndSend("/verify/" + decodedJWT.getId(), new VerifyMessage(true, token));
        return true;
    }


}
