package com.forestar.queryhelper.model.api;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/24
 * @Description
 */
public abstract class AbsFieldInfo<E> {
    protected final Class<E> eClass;
    protected final Field fieldRef;

    public AbsFieldInfo(Class<E> entityClass, Field fieldRef) {
        this.eClass = entityClass;
        this.fieldRef = fieldRef;
    }

    @SneakyThrows
    public Object get(E entity) {
        if (!eClass.isInstance(entity))
            throw new RuntimeException(
                    String.format("[%s] is not a valid instance of [%s]", entity, eClass));
        // 修改访问修饰符
        fieldRef.setAccessible(true);
        return fieldRef.get(entity);
    }
}
