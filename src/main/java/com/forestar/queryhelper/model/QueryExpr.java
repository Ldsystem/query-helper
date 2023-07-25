package com.forestar.queryhelper.model;

import com.mapzone.platform.business.utils.query.constant.LogicOpr;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/22
 * @Description
 */
@Getter
@Builder
public class QueryExpr {
    @Setter
    private int order;
    @Setter
    private int groupOrder;
    @Setter
    private LogicOpr opr;
    @Setter
    private LogicOpr groupOpr;
    private String expr;
    private List<Object> args;

    //
    public Object[] getArgsArr() {
        return args.toArray();
    }

    @Override
    public String toString() {
        return String.format("\nSQL Expr\n: %s \nArgs: %s", expr, args);
    }
}
