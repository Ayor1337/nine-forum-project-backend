package com.ayor.aspect.chat;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.stomp.ChatUnread;
import com.ayor.entity.stomp.MessageUnread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.MessageUnreadService;
import com.ayor.type.UnreadMessageType;
import com.ayor.util.STOMPUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatNotifAspect {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;

    private final AccountMapper accountMapper;

    private final ConversationMapper conversationMapper;

    private final ChatUnreadService chatUnreadService;

    private final MessageUnreadService messageUnreadService;

    @Around("@annotation(chatNotif)")
    public Object around(ProceedingJoinPoint joinPoint, ChatNotif chatNotif) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        String username = resolve(chatNotif.user(), context, String.class);
        Integer conversationId = resolve(chatNotif.conversationId(), context, Integer.class);

        switch (chatNotif.type()){
            case SEND_MSG -> chatMessageNotif(username, conversationId);
            case RECEIVED_MSG -> readChatMessage(conversationId, username);
        }

        return joinPoint.proceed();
    }

    // 解析 SpEL 表达式
    private <T> T resolve(String expressionString, EvaluationContext context, Class<T> returnType) {
        if (expressionString == null || expressionString.isEmpty()) {
            return null;
        }
        try {
            Expression expression = parser.parseExpression(expressionString);
            return expression.getValue(context, returnType);
        } catch (Exception e) {
            log.error("解析表达式错误: {}", e.getMessage());
            return null;
        }
    }

    // 聊天消息通知
    private void chatMessageNotif(String username, Integer conversationId) {
        Account account = accountMapper.getAccountByUsername(username);
        Integer toUserId = conversationMapper.getChatPartnerId(account.getAccountId(), conversationId);
        String toUser = accountMapper.getUsernameById(toUserId);

        // 如果用户不在订阅中, 则不会发送未读消息的通知
        if (stompUtils.isUserSubscribed(toUser, "/transfer/conversation/"+conversationId)) {
            chatUnreadService.clearUnread(conversationId, username);
        } else {
            long unreadConversationCount = chatUnreadService.addUnread(conversationId, toUser);
            messageUnreadService.addUnread(toUser, UnreadMessageType.USER_MESSAGE, unreadConversationCount);
            ChatUnread chatUnread = ChatUnread
                    .builder()
                    .unread(unreadConversationCount)
                    .fromUserId(account.getAccountId())
                    .conversationId(conversationId)
                    .build();

            messagingTemplate.convertAndSendToUser(toUser, "/notif/unread/whisper", chatUnread);
            messagingTemplate.convertAndSendToUser(toUser, "/notif/unread/user", messageUnreadService.getUnreadVO(toUser, UnreadMessageType.USER_MESSAGE));
            messagingTemplate.convertAndSendToUser(toUser, "/notif/unread", messageUnreadService.getUnreadVO(toUser));
        }

    }

    private void readChatMessage(Integer conversationId, String username) {
        ChatUnread emptyUnread = ChatUnread.emptyUnread(conversationId, accountMapper.getAccountIdByUsername( username));
        long cost = chatUnreadService.clearUnread(conversationId, username);
        messageUnreadService.clearUnread(username, UnreadMessageType.USER_MESSAGE, cost);
        MessageUnread messageUnread = messageUnreadService.getUnreadVO(username);

        messagingTemplate
                .convertAndSendToUser(username,
                        "/notif/unread/whisper",
                        emptyUnread);
        messagingTemplate
                .convertAndSendToUser(username,
                        "/notif/unread",
                        messageUnread);
        messagingTemplate
                .convertAndSendToUser(username,
                        "/notif/unread/user",
                        messageUnread);
    }
}
