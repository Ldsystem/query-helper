package com.forestar.queryhelper.model.impl;

import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.SubQuery;
import com.mapzone.platform.business.utils.query.model.api.AbsConditionInfo;
import com.mapzone.platform.business.utils.query.model.api.IConditionInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public class ConditionHelper {


    public static <E> AbsConditionInfo<E> ofField(Class<E> eClass, Field fieldRef) {
        Condition condition = fieldRef.getAnnotation(Condition.class);
        SubQuery subQuery = fieldRef.getAnnotation(SubQuery.class);
        if (null == condition)
            return null;
        if (null != subQuery) {
            return new FieldBaseSubQuery<>(eClass, fieldRef);
        } else
            return new FieldBaseCondition<>(eClass, fieldRef);
    }

    public static <E> AbsConditionInfo<E> ofMethod(Class<E> eClass, Method methodRef) {
        Condition condition = methodRef.getAnnotation(Condition.class);
        SubQuery subQueryInfo = methodRef.getAnnotation(SubQuery.class);
        if (null == condition)
            return null;
        if (null != subQueryInfo)
            return new MethodBaseSubQuery<>(eClass, methodRef);
        else
            return new MethodBaseCondition<>(eClass, methodRef);
    }

    public static <E> AbsConditionInfo<E> between(Class<E> eClass, IConditionInfo<E> e1, IConditionInfo<E> e2) {
        return new BetweenInfo<>(eClass, (AbsConditionInfo<E>)e1, (AbsConditionInfo<E>) e2);
    }

}
