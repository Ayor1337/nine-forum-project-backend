package com.ayor.aspect.oplog;

import java.lang.annotation.*;

/**
 * 标记需要记录操作日志的方法。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述.
     */
    String value() default "";

    /**
     * 是否记录方法入参，默认记录。
     */
    boolean logParams() default true;

    /**
     * 是否记录方法返回值，默认不记录。
     */
    boolean logResult() default false;

    /**
     * 是否将成功操作保存到数据库。
     */
    boolean save() default false;

    /**
     * 持久化日志中的操作类型。
     */
    String action() default "";

    /**
     * 持久化日志中的目标类型。
     */
    String targetType() default "";

    /**
     * 从方法参数中提取目标 ID 的参数名；为空表示无目标 ID。
     */
    String targetIdParam() default "";
}
