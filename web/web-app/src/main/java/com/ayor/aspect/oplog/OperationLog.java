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
}
