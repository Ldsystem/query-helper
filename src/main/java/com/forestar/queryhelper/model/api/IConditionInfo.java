package com.forestar.queryhelper.model.api;

import com.mapzone.platform.business.utils.query.constant.LogicOpr;
import com.mapzone.platform.business.utils.query.model.QueryExpr;

import java.util.Optional;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
public interface IConditionInfo<E> {
    String getGroupCode();
    int getOrder();
    int getGroupOrder();
    LogicOpr getOpr();
    LogicOpr getGroupOpr();
    Optional<QueryExpr> conditionExpr(E entity);
}
