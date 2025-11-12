package com.ayor.aspect.unread;

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
@RequiredArgsConstructor
@Slf4j
public class MessageUnreadNotifAspect {

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;


    private final MessageUnreadService messageUnreadService;

    @Around("@annotation(messageUnreadNotif)")
    public Object around(ProceedingJoinPoint joinPoint, MessageUnreadNotif messageUnreadNotif) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        EvaluationContext context = new StandardEvaluationContext();

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        Integer accountId = resolve(messageUnreadNotif.accountId(), context, Integer.class);
        String subscribeDest = messageUnreadNotif.subscribeDest();
        if (accountId != 0) {
            sendNotificationToUser(accountId, subscribeDest, messageUnreadNotif.type(), messageUnreadNotif.doRead());
            return joinPoint.proceed();
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

    private void sendNotificationToUser(Integer accountId, String subscribeDest, UnreadMessageType type, boolean doRead) {
        if (doRead) {
            messageUnreadService.clearUnread(accountId, type);
        } else {
            if (!stompUtils.isUserSubscribed(accountId.toString(), subscribeDest)) {
                messageUnreadService.addUnread(accountId, type, 1L);
            }
        }
        messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/unread", messageUnreadService.getUnreadVO(accountId));
        messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/unread/"+ type.getType(),
                messageUnreadService.getUnreadVO(accountId, type));
    }

}
