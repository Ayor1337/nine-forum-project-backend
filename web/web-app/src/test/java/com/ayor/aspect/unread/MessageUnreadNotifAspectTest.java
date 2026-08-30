package com.ayor.aspect.unread;

import com.ayor.entity.stomp.MessageUnread;
import com.ayor.entity.vo.UnreadOverviewVO;
import com.ayor.service.MessageUnreadService;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import com.ayor.util.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageUnreadNotifAspectTest {

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private STOMPUtils stompUtils;

    @Mock
    private MessageUnreadService messageUnreadService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    // 测试自己的写入动作不会通知自己
    @Test
    void ownWriteActionDoesNotNotifySelf() throws Throwable {
        MessageUnreadNotifAspect aspect = createAspect();
        MessageUnreadNotif annotation = annotation("7", "/notif/system", false);
        prepareJoinPoint("target", new Object[0]);
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, annotation);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
        verifyNoInteractions(stompUtils, messageUnreadService, messagingTemplate);
    }

    // 测试目标用户未订阅来源目的地时新增未读
    @Test
    void unreadIsAddedWhenTargetUserIsNotSubscribedToSourceDestination() throws Throwable {
        MessageUnreadNotifAspect aspect = createAspect();
        MessageUnread unread = new MessageUnread(3L);
        UnreadOverviewVO overview = new UnreadOverviewVO();
        MessageUnreadNotif annotation = annotation("7", "/notif/system", false);
        prepareJoinPoint("target", new Object[0]);
        when(securityUtils.getSecurityUserId()).thenReturn(99);
        when(stompUtils.isUserSubscribed("7", "/notif/system")).thenReturn(false);
        when(stompUtils.isUserSubscribed("7", "/notif/unread")).thenReturn(true);
        when(stompUtils.isUserSubscribed("7", "/notif/unread/" + UnreadMessageType.SYSTEM_MESSAGE)).thenReturn(false);
        when(stompUtils.isUserSubscribed("7", "/notif/unread-overview")).thenReturn(true);
        when(messageUnreadService.getUnreadVO(7)).thenReturn(unread);
        when(messageUnreadService.getUnreadVO(7, UnreadMessageType.SYSTEM_MESSAGE)).thenReturn(unread);
        when(messageUnreadService.getUnreadOverviewVO(7)).thenReturn(overview);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.around(joinPoint, annotation);

        assertThat(result).isEqualTo("ok");
        verify(messageUnreadService).addUnread(7, UnreadMessageType.SYSTEM_MESSAGE, 1L);
        verify(messagingTemplate).convertAndSendToUser("7", "/notif/unread", unread);
        verify(messagingTemplate).convertAndSendToUser("7", "/notif/unread/system", unread);
        verify(messagingTemplate).convertAndSendToUser("7", "/notif/unread-overview", overview);
    }

    // 测试用户已订阅来源目的地时不新增未读
    @Test
    void unreadIsNotAddedWhenUserIsSubscribedToSourceDestination() throws Throwable {
        MessageUnreadNotifAspect aspect = createAspect();
        MessageUnreadNotif annotation = annotation("7", "/notif/system", false);
        prepareJoinPoint("target", new Object[0]);
        when(securityUtils.getSecurityUserId()).thenReturn(99);
        when(stompUtils.isUserSubscribed("7", "/notif/system")).thenReturn(true);
        when(stompUtils.isUserSubscribed("7", "/notif/unread")).thenReturn(false);
        when(stompUtils.isUserSubscribed("7", "/notif/unread/" + UnreadMessageType.SYSTEM_MESSAGE)).thenReturn(true);
        when(stompUtils.isUserSubscribed("7", "/notif/unread-overview")).thenReturn(false);
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.around(joinPoint, annotation);

        verify(messageUnreadService, never()).addUnread(7, UnreadMessageType.SYSTEM_MESSAGE, 1L);
        verify(messagingTemplate, never()).convertAndSendToUser(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    // 测试已读动作会在继续执行前清理未读
    @Test
    void readActionClearsUnreadBeforeProceeding() throws Throwable {
        MessageUnreadNotifAspect aspect = createAspect();
        MessageUnreadNotif annotation = annotation("#p0", "/notif/system", true);
        prepareJoinPoint("read", new Object[]{7});
        when(securityUtils.getSecurityUserId()).thenReturn(7);
        when(stompUtils.isUserSubscribed("7", "/notif/unread")).thenReturn(false);
        when(stompUtils.isUserSubscribed("7", "/notif/unread/" + UnreadMessageType.SYSTEM_MESSAGE)).thenReturn(true);
        when(stompUtils.isUserSubscribed("7", "/notif/unread-overview")).thenReturn(false);
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.around(joinPoint, annotation);

        verify(messageUnreadService).clearUnread(7, UnreadMessageType.SYSTEM_MESSAGE);
        verify(messageUnreadService, never()).addUnread(7, UnreadMessageType.SYSTEM_MESSAGE, 1L);
    }

    private MessageUnreadNotifAspect createAspect() {
        return new MessageUnreadNotifAspect(beanFactory, messagingTemplate, stompUtils, messageUnreadService, securityUtils);
    }

    private void prepareJoinPoint(String methodName, Object[] args) throws Throwable {
        Method method;
        if ("read".equals(methodName)) {
            method = Target.class.getDeclaredMethod(methodName, Integer.class);
        } else {
            method = Target.class.getDeclaredMethod(methodName);
        }
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new Target());
        when(joinPoint.getArgs()).thenReturn(args);
    }

    private MessageUnreadNotif annotation(String accountId, String subscribeDest, boolean doRead) {
        MessageUnreadNotif annotation = org.mockito.Mockito.mock(MessageUnreadNotif.class);
        when(annotation.accountId()).thenReturn(accountId);
        when(annotation.subscribeDest()).thenReturn(subscribeDest);
        when(annotation.doRead()).thenReturn(doRead);
        org.mockito.Mockito.lenient().when(annotation.type()).thenReturn(UnreadMessageType.SYSTEM_MESSAGE);
        return annotation;
    }

    private static class Target {
        @SuppressWarnings("unused")
        void target() {
        }

        @SuppressWarnings("unused")
        void read(Integer accountId) {
        }
    }
}
