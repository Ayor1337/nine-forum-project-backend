package com.ayor.service.impl;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.ayor.entity.message.EmailVerifyMessage;
import com.ayor.entity.stomp.VerifyMessage;
import com.ayor.service.AuthorizeService;
import com.ayor.type.EmailVerifyType;
import com.ayor.util.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeServiceImpl implements AuthorizeService {

    private final JWTUtils jwtUtils;

    private final RabbitTemplate rabbitTemplate;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public String createAuthorizeToken(String email) {
        String token = jwtUtils.createJwt(email);
        rabbitTemplate.convertAndSend("mail.direct", "mail", new EmailVerifyMessage(email, token, EmailVerifyType.REGISTER));
        DecodedJWT decodedJWT = jwtUtils.resolveEmailJwt(token);
        return decodedJWT.getId();
    }

    @Override
    public boolean validateAuthorizeToken(String token, String email) {
        DecodedJWT decodedJWT = jwtUtils.resolveEmailJwt(token);
        if (decodedJWT == null) {
            return false;
        }
        String decodedEmail = decodedJWT.getClaim("email").asString();
        if (!decodedEmail.equals(email))
            return false;
        messagingTemplate.convertAndSend("/verify/" + decodedJWT.getId(), new VerifyMessage(true, token));
        return true;
    }


}
