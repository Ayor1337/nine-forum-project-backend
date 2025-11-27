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
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatNotifAspect {

    private final BeanFactory beanFactory;

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

        MethodBasedEvaluationContext ctx = new MethodBasedEvaluationContext(joinPoint.getTarget(), method, args, nameDiscoverer);
        ctx.setBeanResolver(new BeanFactoryResolver(beanFactory));
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                ctx.setVariable(paramNames[i], args[i]);
            }
        }
        Integer userId = resolve(chatNotif.userId(), ctx, Integer.class);
        Integer conversationId = resolve(chatNotif.conversationId(), ctx, Integer.class);

        switch (chatNotif.type()){
            case SEND_MSG -> chatMessageNotif(userId, conversationId);
            case RECEIVED_MSG -> readChatMessage(conversationId, userId);
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
    private void chatMessageNotif(Integer accountId, Integer conversationId) {
        Account account = accountMapper.getAccountById(accountId);
        Integer toUserId = conversationMapper.getChatPartnerId(account.getAccountId(), conversationId);

        // 如果用户不在订阅中, 则不会发送未读消息的通知
        if (stompUtils.isUserSubscribed(toUserId.toString(), "/transfer/conversation/"+conversationId)) {
            chatUnreadService.clearUnread(conversationId, accountId);
        } else {
            long unreadConversationCount = chatUnreadService.addUnread(conversationId, toUserId);
            messageUnreadService.addUnread(toUserId, UnreadMessageType.USER_MESSAGE, unreadConversationCount);
            ChatUnread chatUnread = ChatUnread
                    .builder()
                    .unread(unreadConversationCount)
                    .fromUserId(account.getAccountId())
                    .conversationId(conversationId)
                    .build();

            messagingTemplate.convertAndSendToUser(toUserId.toString(), "/notif/unread/whisper", chatUnread);
            messagingTemplate.convertAndSendToUser(toUserId.toString(), "/notif/unread/user", messageUnreadService.getUnreadVO(toUserId, UnreadMessageType.USER_MESSAGE));
            messagingTemplate.convertAndSendToUser(toUserId.toString(), "/notif/unread", messageUnreadService.getUnreadVO(toUserId));
        }

    }

    private void readChatMessage(Integer conversationId, Integer accountId) {
        ChatUnread emptyUnread = ChatUnread.emptyUnread(conversationId, accountId);
        long cost = chatUnreadService.clearUnread(conversationId, accountId);
        messageUnreadService.clearUnread(accountId, UnreadMessageType.USER_MESSAGE, cost);
        MessageUnread messageUnread = messageUnreadService.getUnreadVO(accountId);

        if(stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread/whisper")) {
            messagingTemplate
                    .convertAndSendToUser(accountId.toString(),
                            "/notif/unread/whisper",
                            emptyUnread);
        }
        if(stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread")){
            messagingTemplate
                    .convertAndSendToUser(accountId.toString(),
                            "/notif/unread",
                            messageUnread);
        }
        if(stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread/user")) {
            messagingTemplate
                    .convertAndSendToUser(accountId.toString(),
                            "/notif/unread/user",
                            messageUnread);
        }


    }
}
