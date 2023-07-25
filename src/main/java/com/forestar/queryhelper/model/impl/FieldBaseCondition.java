package com.forestar.queryhelper.model.impl;

import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.model.api.AbsConditionInfo;
import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author      liushenglong_8597@outlook.com
 * @Date        2023/5/22
 * @Description 实体类的字段信息(基类)
 */
public class FieldBaseCondition<E> extends AbsConditionInfo<E> {
    protected final Field fieldRef;

    protected FieldBaseCondition(Class<E> entityClass, Field fieldRef) {
        super(fieldRef.getAnnotation(Condition.class), fieldRef.getAnnotation(ConditionGroup.class), entityClass);
        this.fieldRef = fieldRef;
    }

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
    public Class<?> getType() {
        return this.fieldRef.getType();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return this.fieldRef.getAnnotation(annotationClass);
    }

}
