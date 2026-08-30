package com.ayor.controller;

import com.ayor.entity.stomp.ConversationTypingMessage;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Date;

@Controller
@RequiredArgsConstructor
public class ConversationTypingController {

    private final AuthorizationService authorizationService;

    private final ConversationMapper conversationMapper;

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/conversations/{conversationId}/typing")
    public void typing(@DestinationVariable Integer conversationId,
                       @Payload ConversationTypingMessage message,
                       Principal principal) {
        Integer fromUserId = resolveUserId(principal);
        authorizationService.assertCanAccessConversation(fromUserId, conversationId);
        Integer partnerId = conversationMapper.getChatPartnerId(fromUserId, conversationId);
        if (partnerId == null) {
            return;
        }
        ConversationTypingMessage typingMessage = ConversationTypingMessage.builder()
                .conversationId(conversationId)
                .fromUserId(fromUserId)
                .typing(message != null && Boolean.TRUE.equals(message.getTyping()))
                .time(new Date())
                .build();
        messagingTemplate.convertAndSendToUser(
                partnerId.toString(),
                "/transfer/conversation/" + conversationId + "/typing",
                typingMessage
        );
    }

    private Integer resolveUserId(Principal principal) {
        if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)) {
            return null;
        }
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof UserDetails userDetails) {
            return Integer.parseInt(userDetails.getUsername());
        }
        return null;
    }
}
