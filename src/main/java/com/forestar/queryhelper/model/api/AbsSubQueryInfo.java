package com.forestar.queryhelper.model.api;

import com.mapzone.platform.business.utils.query.QueryHelper;
import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.annotation.SubQuery;
import com.mapzone.platform.business.utils.query.model.QueryExpr;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public abstract class AbsSubQueryInfo<E, F> extends AbsConditionInfo<E> {

    private String from;
    private String field;
    private final QueryHelper<F> helper;
    private final Class<F> fieldType;
    public AbsSubQueryInfo(Class<E> entityClass, Condition condition, ConditionGroup group, SubQuery subQuery, @NotNull Class<F> fieldType) {
        super(condition, group, entityClass);
        this.from = subQuery.from();
        this.field = subQuery.select();
        this.fieldType = fieldType;
        helper = new QueryHelper<>(fieldType);
    }

    @Override
    protected Optional<QueryExpr> getConditionExpr(E entity) {
        F fieldValue = this.getFieldValue(entity);
        if (null == fieldValue)
            return Optional.empty();

        QueryExpr expr = this.helper.whereExpr(fieldValue);
        String querySql = String.format("SELECT %s FROM %s WHERE %s",
                this.field, this.from, expr.getExpr());
        QueryExpr ret = QueryExpr.builder()
                .expr(String.format("%s IN (%s)", this.getColumn(), querySql))
                .args(expr.getArgs())
                .build();
        return Optional.of(ret);
    }

    @Override
    public Class<?> getType() {
        return fieldType;
    }

    private F getFieldValue(E entity) {
        Object value = this.get(entity);
        return fieldType.cast(value);
    }
}
