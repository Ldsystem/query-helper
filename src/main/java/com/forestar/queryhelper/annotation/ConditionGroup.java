package com.forestar.queryhelper.annotation;

import com.mapzone.platform.business.utils.query.constant.LogicOpr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/24
 * @Description
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionGroup {

    int order() default 0;

    String code();
    LogicOpr opr() default LogicOpr.AND;

}
