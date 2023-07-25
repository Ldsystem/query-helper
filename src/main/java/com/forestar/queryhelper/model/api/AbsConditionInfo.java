package com.forestar.queryhelper.model.api;

import com.mapzone.platform.business.utils.query.annotation.Condition;
import com.mapzone.platform.business.utils.query.annotation.ConditionGroup;
import com.mapzone.platform.business.utils.query.constant.Fuzzy;
import com.mapzone.platform.business.utils.query.constant.ListOpr;
import com.mapzone.platform.business.utils.query.constant.LogicOpr;
import com.mapzone.platform.business.utils.query.model.QueryExpr;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mapzone.platform.business.utils.query.constant.ListOpr.InList;
import static com.mapzone.platform.business.utils.query.constant.ListOpr.NotList;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/23
 * @Description
 */
@Getter
public abstract class AbsConditionInfo<E> implements IConditionInfo<E> {

    public static final String DEFAULT_GROUP_NAME = "DEFAULT_GROUP";

    protected final Class<E> entityClass;
    private int order;
    private String tableAlias;
    private String column;
    private Fuzzy fuzzy;
    private ListOpr listOpr;
    private LogicOpr opr;
    private String groupCode = DEFAULT_GROUP_NAME;
    private int groupOrder = -1;
    private LogicOpr groupOpr = LogicOpr.AND;

    protected AbsConditionInfo(Condition condition, ConditionGroup group, Class<E> entityClass) {
        this.entityClass = entityClass;
        this.init(condition, group);
    }

    protected void init(Condition annotation, ConditionGroup group) {
        if (annotation == null)
            throw new IllegalStateException(
                    String.format("Condition field should be annotated with [%s]", Condition.class));
        this.order = annotation.order();
        this.tableAlias = annotation.tableAlias();
        this.column = annotation.column();
        this.fuzzy = annotation.fuzzy();
        this.listOpr = annotation.listOpr();
        this.opr = annotation.logicOpr();
        if (null != group) {
            this.groupCode = group.code();
            this.groupOrder = group.order();
            this.groupOpr = group.opr();
        }
    }

    public Optional<QueryExpr> conditionExpr(E entity) {
        if (!entityClass.isInstance(entity))
            throw new RuntimeException(
                    String.format("[%s] is not a valid instance of [%s]", entity, entityClass));
        Optional<QueryExpr> conditionExpr = this.getConditionExpr(entity);
        conditionExpr.ifPresent(expr -> {
            expr.setOrder(this.order);
            expr.setGroupOrder(this.groupOrder);
            expr.setOpr(this.opr);
            expr.setGroupOpr(this.groupOpr);
        });
        return conditionExpr;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Optional<QueryExpr> getConditionExpr(E entity) {
        Object value = this.get(entity);
        if (ObjectUtils.isEmpty(value))
            return Optional.empty();
        String condition;
        List<Object> params = new ArrayList<>();
        if (NotList != this.listOpr) {
            if (InList == this.listOpr) {
                List list = (List) value;
                if (list.isEmpty())
                    return Optional.empty();
                condition = String.format("%s IN (%s)", this.fullName(),
                        list.stream().map(ignore -> "?").collect(Collectors.joining(",")));
                params.addAll(list);
            } else {
                if (!StringUtils.hasText(String.valueOf(value)))
                    return Optional.empty();
                String[] items = String.valueOf(value).split(",");
                List<String> args = Arrays.asList(items);
                condition = String.format("%s IN (%s)", this.fullName(),
                        args.stream().map(ignore -> "?").collect(Collectors.joining(",")));
                params.addAll(args);
            }
        } else if (this.fuzzy != Fuzzy.None) {
            condition = this.fullName() + " LIKE ?";
            params.add(this.fuzzy.fuzzy(String.valueOf(value)));
        } else {
            condition = this.fullName() + " = ?";
            params.add(value);
        }
        return Optional.of(QueryExpr.builder().expr(condition).args(params).build());
    }

    protected String fullName() {
        return StringUtils.hasText(this.tableAlias) ?
                String.join(".", new String[]{this.tableAlias, this.column}) : this.column;
    }

    public abstract Object get(E entity);

    public abstract Class<?> getType();

    public abstract <T extends Annotation> T getAnnotation(Class<T> annotationClass);
}
