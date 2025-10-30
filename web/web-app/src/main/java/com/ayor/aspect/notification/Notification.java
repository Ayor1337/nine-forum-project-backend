package com.ayor.aspect.notification;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Notification {

    String user() default "";

    String conversationId() default "";

    @NotNull
    NotificationType type();

}
