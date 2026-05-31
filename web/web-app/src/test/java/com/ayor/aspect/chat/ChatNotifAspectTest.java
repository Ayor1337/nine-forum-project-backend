package com.ayor.aspect.chat;

import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.MessageUnreadService;
import com.ayor.type.NotificationType;
import com.ayor.util.STOMPUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatNotifAspectTest {

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ChatUnreadService chatUnreadService;

    @Mock
    private MessageUnreadService messageUnreadService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private ChatNotif chatNotif;

    @Test
    void shouldNotCreateMessageNotificationWhenSendMessageIsRejected() throws Throwable {
        ChatNotifAspect aspect = new ChatNotifAspect(
                beanFactory,
                messagingTemplate,
                stompUtils,
                accountMapper,
                conversationMapper,
                chatUnreadService,
                messageUnreadService
        );
        Method method = ChatTarget.class.getDeclaredMethod("sendMessage", Integer.class, Integer.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new ChatTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{10, 7});
        when(joinPoint.proceed()).thenThrow(new AccessDeniedException("Access denied"));
        when(chatNotif.userId()).thenReturn("#p0");
        when(chatNotif.conversationId()).thenReturn("#p1");
        lenient().when(chatNotif.type()).thenReturn(NotificationType.SEND_MSG);

        assertThrows(AccessDeniedException.class, () -> aspect.around(joinPoint, chatNotif));

        verifyNoInteractions(accountMapper, conversationMapper, stompUtils, chatUnreadService,
                messageUnreadService, messagingTemplate);
    }

    private static class ChatTarget {
        @SuppressWarnings("unused")
        void sendMessage(Integer accountId, Integer conversationId) {
        }
    }
}
