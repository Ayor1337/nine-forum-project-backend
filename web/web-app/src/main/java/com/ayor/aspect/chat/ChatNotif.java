package com.ayor.aspect.chat;

import com.ayor.type.NotificationType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;


/**
 * 自定义注解
 * 用于通知
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ChatNotif {

    String userId() default "";

    String conversationId() default "";

    @NotNull
    NotificationType type();

}
