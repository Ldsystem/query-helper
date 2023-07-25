package com.forestar.queryhelper.model.impl;

import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.annotation.SubQuery;
import com.mapzone.platform.business.utils.query.model.api.AbsSubQueryInfo;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public class FieldBaseSubQuery<E> extends AbsSubQueryInfo<E, Object> {

    private Field fieldRef;

    @SuppressWarnings("unchecked")
    public FieldBaseSubQuery(Class<E> eneityClass, Field fieldRef) {
        super(eneityClass, fieldRef.getAnnotation(Condition.class), fieldRef.getAnnotation(ConditionGroup.class),fieldRef.getAnnotation(SubQuery.class), (Class<Object>) fieldRef.getType());
        this.fieldRef = fieldRef;
    }

    @Override
    @SneakyThrows
    public Object get(E entity) {
        if (!entityClass.isInstance(entity))
            throw new RuntimeException(
                    String.format("[%s] is not a valid instance of [%s]", entity, entityClass));
        // 修改访问修饰符
        fieldRef.setAccessible(true);
        return fieldRef.get(entity);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.fieldRef.getAnnotation(annotationClass);
    }
}
