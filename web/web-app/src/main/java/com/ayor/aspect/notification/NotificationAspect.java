package com.ayor.aspect.notification;

import com.ayor.entity.pojo.Account;
import com.ayor.entity.stomp.ChatUnread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ChatUnreadService;
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
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationAspect {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private final SimpMessagingTemplate messagingTemplate;

    private final SimpUserRegistry userRegistry;

    private final AccountMapper accountMapper;

    private final ConversationMapper conversationMapper;

    private final ChatUnreadService chatUnreadService;

    @Around("@annotation(notification)")
    public Object around(ProceedingJoinPoint joinPoint, Notification notification) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        String username = resolve(notification.user(), context, String.class);
        Integer conversationId = resolve(notification.conversationId(), context, Integer.class);

        switch (notification.type()){
            case RECEIVED_MSG -> chatMessageNotif(username, conversationId);
        }

        return joinPoint.proceed();
    }

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
    private void chatMessageNotif(String username, Integer conversationId) {
        Account account = accountMapper.getAccountByUsername(username);
        Integer toUserId = conversationMapper.getChatPartnerId(account.getAccountId(), conversationId);
        String toUser = accountMapper.getUsernameById(toUserId);


        if (!isUserSubscribed(toUser, "/transfer/conversation/"+conversationId)) {
            long unreadCount = chatUnreadService.addUnread(conversationId, toUser);

            ChatUnread chatUnread = ChatUnread
                    .builder()
                    .unread(unreadCount)
                    .fromUserId(account.getAccountId())
                    .conversationId(conversationId)
                    .build();

            messagingTemplate.convertAndSendToUser(toUser, "/notif", chatUnread);
        } else {
            chatUnreadService.clearUnread(conversationId, username);
        }

    }

    public boolean isUserSubscribed(String username, String destinationPrefix) {
        SimpUser user = userRegistry.getUser(username);
        if (user == null) return false;

        for (SimpSession session : user.getSessions()) {
            for (SimpSubscription sub : session.getSubscriptions()) {
                String dest = sub.getDestination();
                if (dest.contains(destinationPrefix)) {
                    return true;
                }
            }
        }
        return false;
    }


}
