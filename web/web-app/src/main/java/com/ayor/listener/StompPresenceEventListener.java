package com.ayor.listener;

import com.ayor.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class StompPresenceEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Integer accountId = resolveUserId(accessor);
        if (accountId != null) {
            presenceService.markOnline(accountId, accessor.getSessionId());
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Integer accountId = resolveUserId(accessor);
        if (accountId != null) {
            presenceService.markOffline(accountId, accessor.getSessionId());
        }
    }

    private Integer resolveUserId(StompHeaderAccessor accessor) {
        Integer sessionAccountId = resolveSessionAccountId(accessor);
        if (sessionAccountId != null) {
            return sessionAccountId;
        }
        Principal principal = accessor.getUser();
        if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)) {
            return null;
        }
        Object principalObject = authentication.getPrincipal();
        if (principalObject instanceof UserDetails userDetails) {
            return Integer.parseInt(userDetails.getUsername());
        }
        return null;
    }

    private Integer resolveSessionAccountId(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) {
            return null;
        }
        Object accountId = accessor.getSessionAttributes().get("accountId");
        if (accountId == null) {
            return null;
        }
        return Integer.parseInt(accountId.toString());
    }
}
