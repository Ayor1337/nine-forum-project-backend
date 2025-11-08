package com.ayor.aspect.unread;

import com.ayor.type.UnreadMessageType;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageUnreadNotif {

    String username() default "";

    String accountId() default "0";

    String subscribeDest();

    boolean doRead() default false;

    @NotNull
    UnreadMessageType type();

}
