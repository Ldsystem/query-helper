package com.forestar.queryhelper.model;

import com.mapzone.platform.business.utils.query.annotation.Value;
import lombok.SneakyThrows;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/22
 * @Description
 */
public class ValueInfo<E> {

    private final Class<E> eClass;
    private final Field fieldRef;

    private String tableAlias;
    private String column;
    private boolean ignoreIfMiss;

    public ValueInfo(Class<E> entityClass, Field fieldRef) {
        this.eClass = entityClass;
        this.fieldRef = fieldRef;
        this.init();
    }

    private void init() {
        Value annotation = this.fieldRef.getAnnotation(Value.class);
        if (null == annotation)
            throw new  IllegalStateException(
                    String.format("Value field should be annotated with [%s]", Value.class));
        this.tableAlias = annotation.tableAlias();
        this.column = annotation.column();
        this.ignoreIfMiss = annotation.ignoreIfMiss();
    }

    public Object getValue(E entity) {
        return this.get(entity);
    }

    public Optional<QueryExpr> getUpdateSetExpr(E entity) {
        Object value = this.get(entity);
        if (null == value && this.ignoreIfMiss)
            return Optional.empty();
        QueryExpr expr = QueryExpr.builder()
                .expr(this.fullName() + " = ?")
                .args(Collections.singletonList(value)).build();
        return Optional.of(expr);
    }

    public Optional<QueryExpr> getInsertField(E entity) {
        Object value = this.get(entity);
        if (null == value && this.ignoreIfMiss)
            return Optional.empty();
        QueryExpr expr = QueryExpr.builder()
                .expr(this.column)
                .args(Collections.singletonList(value)).build();
        return Optional.of(expr);
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

    protected String fullName() {
        return StringUtils.hasText(this.tableAlias) ?
                String.join(".", new String[]{this.tableAlias, this.column}) : this.column;
    }
}
