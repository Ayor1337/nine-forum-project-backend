package com.ayor.util;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class STOMPUtilsTest {

    // 测试精确订阅判断不会把 typing 子目的地当作会话正文订阅
    @Test
    void shouldNotTreatTypingSubscriptionAsConversationMessageSubscription() {
        SimpUserRegistry registry = mock(SimpUserRegistry.class);
        SimpUser user = mock(SimpUser.class);
        SimpSession session = mock(SimpSession.class);
        SimpSubscription typingSubscription = mock(SimpSubscription.class);
        when(registry.getUser("22")).thenReturn(user);
        when(user.getSessions()).thenReturn(Set.of(session));
        when(session.getSubscriptions()).thenReturn(Set.of(typingSubscription));
        when(typingSubscription.getDestination()).thenReturn("/transfer/conversation/7/typing");
        STOMPUtils utils = new STOMPUtils(registry);

        assertFalse(utils.isUserSubscribedExactly("22", "/transfer/conversation/7"));
        assertTrue(utils.isUserSubscribedExactly("22", "/transfer/conversation/7/typing"));
    }

    // 测试精确订阅判断兼容显式用户 ID 的 user 目的地
    @Test
    void shouldMatchExplicitUserDestinationExactly() {
        SimpUserRegistry registry = mock(SimpUserRegistry.class);
        SimpUser user = mock(SimpUser.class);
        SimpSession session = mock(SimpSession.class);
        SimpSubscription subscription = mock(SimpSubscription.class);
        when(registry.getUser("22")).thenReturn(user);
        when(user.getSessions()).thenReturn(Set.of(session));
        when(session.getSubscriptions()).thenReturn(Set.of(subscription));
        when(subscription.getDestination()).thenReturn("/user/22/transfer/conversation/7");
        STOMPUtils utils = new STOMPUtils(registry);

        assertTrue(utils.isUserSubscribedExactly("22", "/transfer/conversation/7"));
    }
}
