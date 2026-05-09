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
@RequiredArgsConstructor
@Slf4j
public class MessageUnreadNotifAspect {

    private final BeanFactory beanFactory;

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private final SimpMessagingTemplate messagingTemplate;

    private final STOMPUtils stompUtils;


    private final MessageUnreadService messageUnreadService;

    /**
     * 在消息相关方法执行前后更新未读消息计数并推送通知。
     *
     * @param joinPoint 切点
     * @param messageUnreadNotif 未读消息通知注解
     * @return 目标方法返回值
     * @throws Throwable 目标方法异常
     */
    @Around("@annotation(messageUnreadNotif)")
    public Object around(ProceedingJoinPoint joinPoint, MessageUnreadNotif messageUnreadNotif) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        String[] paramNames = nameDiscoverer.getParameterNames(method);

        MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(joinPoint.getTarget(),
                        method, args, nameDiscoverer);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));

        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }

        Integer accountId = resolve(messageUnreadNotif.accountId(), context, Integer.class);
        String subscribeDest = messageUnreadNotif.subscribeDest();
        if (accountId != 0) {
            sendUnreadNotificationToUser(accountId, subscribeDest, messageUnreadNotif.type(), messageUnreadNotif.doRead());
            return joinPoint.proceed();
        }

        return joinPoint.proceed();
    }

    /**
     * 解析 SpEL 表达式。
     *
     * @param expressionString 表达式字符串
     * @param context 计算上下文
     * @param returnType 返回类型
     * @param <T> 返回值类型
     * @return 解析结果
     */
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

    /**
     * 向指定用户发送未读消息通知。
     *
     * @param accountId 通知的对象
     * @param subscribeDest 订阅目的地
     * @param type 消息类型
     * @param doRead 是否是读取操作
     */
    private void sendUnreadNotificationToUser(Integer accountId, String subscribeDest, UnreadMessageType type, boolean doRead) {
        // 增加 / 清除未读消息数量
        if (doRead) {
            messageUnreadService.clearUnread(accountId, type);
        } else {
            if (!stompUtils.isUserSubscribed(accountId.toString(), subscribeDest)) {
                messageUnreadService.addUnread(accountId, type, 1L);
            }
        }

        // 当用户正在当前网页（订阅到了未读），则发送未读的消息
        if (stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread")) {
            messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/unread", messageUnreadService.getUnreadVO(accountId));
        }
        // 当用户订阅了某一种未读消息通知，则意味用户本身就在某一个网站里面，则不会发送新消息
        if (!stompUtils.isUserSubscribed(accountId.toString(), "/notif/unread/" + type)) {
            messagingTemplate.convertAndSendToUser(accountId.toString(), "/notif/unread/"+ type.getType(),
                    messageUnreadService.getUnreadVO(accountId, type));
        }
    }

}
