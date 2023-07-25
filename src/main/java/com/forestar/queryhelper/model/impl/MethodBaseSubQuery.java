package com.forestar.queryhelper.model.impl;

import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.annotation.SubQuery;
import com.mapzone.platform.business.utils.query.model.api.AbsSubQueryInfo;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public class MethodBaseSubQuery<E> extends AbsSubQueryInfo<E, Object> {

    private final Method methodRef;

    @SuppressWarnings("unchecked")
    public MethodBaseSubQuery(Class<E> entityClass, Method methodRef) {
        super(entityClass, methodRef.getAnnotation(Condition.class), methodRef.getAnnotation(ConditionGroup.class), methodRef.getAnnotation(SubQuery.class), (Class<Object>) methodRef.getReturnType());
        this.methodRef = methodRef;
    }

    @Override
    @SneakyThrows
    public Object get(E entity) {
        if (!entityClass.isInstance(entity))
            throw new RuntimeException(
                    String.format("[%s] is not a valid instance of [%s]", entity, entityClass));
        if (!methodRef.isAccessible())
            methodRef.setAccessible(true);
        return methodRef.invoke(entity);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return methodRef.getAnnotation(annotationClass);
    }
}
