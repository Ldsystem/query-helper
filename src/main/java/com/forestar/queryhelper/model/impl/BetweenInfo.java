package com.forestar.queryhelper.model.impl;

import com.mapzone.platform.business.utils.query.annotation.Between;
import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.model.QueryExpr;
import com.mapzone.platform.business.utils.query.model.api.AbsConditionInfo;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public class BetweenInfo<E> extends AbsConditionInfo<E> {

    protected final AbsConditionInfo<E> offset;
    protected final AbsConditionInfo<E> limit;

    protected BetweenInfo(Class<E> entityClass, @NonNull AbsConditionInfo<E> ele1, @Nullable AbsConditionInfo<E> ele2) {
        super(ele1.getAnnotation(Condition.class), ele1.getAnnotation(ConditionGroup.class), entityClass);
        Between ele1Anno = ele1.getAnnotation(Between.class);
        Assert.isTrue(null == ele2 || ele1Anno.isOffset() != ele2.getAnnotation(Between.class).isOffset(),
                String.format("Column [%s] has two [%s] side fields", this.getColumn(), ele1Anno.isOffset()? "offset": "limit"));
        if (ele1Anno.isOffset()) {
            this.offset = ele1;
            this.limit = ele2;
        } else {
            this.offset = ele2;
            this.limit = ele1;
        }
    }

    @Override
    public Optional<QueryExpr> getConditionExpr(E entity) {
        Object offsetVal = Optional.ofNullable(offset).map(info -> info.get(entity)).orElse(null);
        Object limitVal = Optional.ofNullable(limit).map(info -> info.get(entity)).orElse(null);
        String expr;
        List<Object> params = new ArrayList<>();
        if (null == offsetVal && null == limitVal) {
            return Optional.empty();
        } else if (null == offsetVal) {
            expr = this.getColumn() + " < ?";
            params.add(limitVal);
        } else if (null == limitVal) {
            expr = this.getColumn() + " >= ?";
            params.add(offsetVal);
        } else {
            expr = this.getColumn() + " BETWEEN ? AND ?";
            params.addAll(Arrays.asList(offsetVal, limitVal));
        }
        QueryExpr queryExpr = QueryExpr.builder()
                .expr(expr)
                .args(params)
                .build();
        return Optional.of(queryExpr);
    }

    @Override
    public Class<?> getType() {
        return (null == this.offset? limit: offset).getType();
    }

    @Override
    public Object get(E entity) {
        throw new NotImplementedException();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        throw new NotImplementedException();
    }

}
