package com.forestar.queryhelper.annotation;

import com.mapzone.platform.business.utils.query.constant.Fuzzy;
import com.mapzone.platform.business.utils.query.constant.ListOpr;
import com.mapzone.platform.business.utils.query.constant.LogicOpr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/22
 * @Description
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {

    /** 排序 */
    int order() default 0;

    /** 数据表别名 */
    String tableAlias() default "";

    /** 字段名 */
    String column();

    /** 模糊查询 */
    Fuzzy fuzzy() default Fuzzy.None;

    /** list字段值的操作 */
    ListOpr listOpr() default ListOpr.NotList;

    /** 前置的逻辑运算符 */
    LogicOpr logicOpr() default LogicOpr.AND;

}
