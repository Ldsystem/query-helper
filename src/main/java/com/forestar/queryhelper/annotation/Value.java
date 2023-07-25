package com.forestar.queryhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/22
 * @Description
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    String tableAlias() default "";

    String column();

    boolean ignoreIfMiss() default true;
}