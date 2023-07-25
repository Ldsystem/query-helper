package com.forestar.queryhelper.model.impl;

import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.model.api.AbsConditionInfo;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public class MethodBaseCondition<E> extends AbsConditionInfo<E> {

    private final Class<E> entityClass;
    private final Method methodRef;

    protected MethodBaseCondition(Class<E> entityClass, Method method) {
        super(method.getAnnotation(Condition.class), method.getAnnotation(ConditionGroup.class), entityClass);
        this.entityClass = entityClass;
        this.methodRef = method;
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
    public Class<?> getType() {
        return methodRef.getReturnType();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.methodRef.getAnnotation(annotationClass);
    }
}
