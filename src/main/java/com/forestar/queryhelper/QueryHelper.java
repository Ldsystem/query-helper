package com.forestar.queryhelper;

import com.forestar.queryhelper.annotation.*;
import com.forestar.queryhelper.model.QueryExpr;
import com.forestar.queryhelper.model.ValueInfo;
import com.forestar.queryhelper.model.api.AbsConditionInfo;
import com.forestar.queryhelper.model.api.IConditionInfo;
import com.forestar.queryhelper.model.impl.ConditionHelper;
import com.sun.istack.internal.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/22
 * @Description
 */
@Slf4j
public class QueryHelper<V> {

    public static final String DEFAULT_EXPR = "1 = 1";

    private final Class<V> entityClass;
    private final Map<String, List<IConditionInfo<V>>> conditionInfo;
    private final List<ValueInfo<V>> valueInfo;
    private final List<String> selectFields;
    @Getter @Setter
    private String tableName;

    public QueryHelper(@NotNull Class<V> clz) {
        this.entityClass = clz;
        this.conditionInfo = new LinkedHashMap<>();
        this.valueInfo = new ArrayList<>();
        this.selectFields = new ArrayList<>();
        this.init();
    }

    public QueryExpr selectExpr(List<String> fields, V v) {
        QueryExpr whereExpr = this.whereExpr(v);
        return QueryExpr.builder()
                .expr(String.format("SELECT %s FROM %s WHERE %s",
                        String.join(", ", fields), this.tableName, whereExpr.getExpr()))
                .args(whereExpr.getArgs())
                .build();
    }

    public QueryExpr selectExpr(V v) {
        if (this.selectFields.isEmpty())
            log.info("没有标注查询字段且没有传入字段, 将返回全部字段");
        return selectExpr(selectFields.isEmpty() ? Collections.singletonList("*") : selectFields, v);
    }

    public List<String> selectFields() {
        return this.selectFields;
    }

    public QueryExpr countExpr(V v) {
        QueryExpr whereExpr = this.whereExpr(v);
        return QueryExpr.builder()
                .expr(String.format("SELECT COUNT(1) FROM %s WHERE %s",
                        this.tableName, whereExpr.getExpr()))
                .args(whereExpr.getArgs())
                .build();
    }

    public Optional<QueryExpr> updateExpr(V v) {
        QueryExpr whereExpr = this.whereExpr(v);
        if (!checkWhereExpr(whereExpr))
            return Optional.empty();
        return this.updateSetItemExpr(v).map(updateExpr -> {
            List<Object> args = new ArrayList<>(updateExpr.getArgs());
            args.addAll(whereExpr.getArgs());
            return QueryExpr.builder()
                    .expr(String.format("UPDATE %s SET %s WHERE %s",
                            this.tableName, updateExpr.getExpr(), whereExpr.getExpr()))
                    .args(args)
                    .build();
        });
    }

    public Optional<QueryExpr> deleteExpr(V v) {
        QueryExpr whereExpr = this.whereExpr(v);
        if (!checkWhereExpr(whereExpr))
            return Optional.empty();
        QueryExpr deleteExpr = QueryExpr.builder()
                .expr(String.format("DELETE FROM %s WHERE %s",
                        this.tableName, whereExpr.getExpr()))
                .args(whereExpr.getArgs())
                .build();
        return Optional.of(deleteExpr);
    }

    public Optional<QueryExpr> insertExpr(V v) {
        return this.insertFields(v).map(insertExpr -> QueryExpr.builder().expr(
                        String.format("INSERT INTO %s (%s) VALUES (%s)",
                                QueryHelper.this.tableName, insertExpr.getExpr(), insertExpr.getArgs().stream().map(ignore -> "?").collect(Collectors.joining(","))))
                .args(insertExpr.getArgs()).build());
    }

    public QueryExpr whereExpr(V vo) {
        if (conditionInfo.isEmpty())
            return QueryExpr.builder().expr(DEFAULT_EXPR).args(Collections.emptyList()).build();
        List<List<QueryExpr>> exprs = conditionInfo.values()
                .stream()
                .sorted(Comparator.comparingInt(list -> list.get(0).getGroupOrder()))
                .map(list -> list.stream().map(info -> info.conditionExpr(vo)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.toList());
        StringBuilder expr = new StringBuilder();
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < exprs.size(); i++) {
            if (i != 0) {
                QueryExpr groupExpr = exprs.get(i).get(0);
                expr.append(String.format(" %s ", groupExpr.getGroupOpr().name()));
            }
            expr.append(" (");
            for (int j = 0; j < exprs.get(i).size(); j++) {
                QueryExpr obj = exprs.get(i).get(j);
                if (j != 0)
                    expr.append(String.format(" %s ", obj.getOpr().name()));
                expr.append(obj.getExpr());
                args.addAll(obj.getArgs());
            }
            expr.append(") ");
        }
        return QueryExpr.builder()
                .expr(expr.toString())
                .args(args)
                .build();
    }

    public boolean checkWhereExpr(QueryExpr whereExpr) {
        return !DEFAULT_EXPR.equals(whereExpr.getExpr().trim());
    }

    public QueryExpr queryFilter(V v, String adtFilter, Object ...adtArgs) {
        QueryExpr whereExpr = whereExpr(v);
        if (StringUtils.hasText(adtFilter)) {
            String expr = whereExpr.getExpr();
            List<Object> args = whereExpr.getArgs();
            expr += " AND " + adtFilter;
            args.addAll(Arrays.asList(adtArgs));
            return QueryExpr.builder()
                    .expr(expr)
                    .args(args).build();
        }
        return whereExpr;
    }

    public Optional<QueryExpr> updateSetItemExpr(V v) {
        if (valueInfo.isEmpty())
            return Optional.empty();
        List<QueryExpr> updateSetItems = valueInfo.stream()
                .map(vInfo -> vInfo.getUpdateSetExpr(v))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        QueryExpr updateSetExpr = QueryExpr.builder()
                .expr(updateSetItems.stream().map(QueryExpr::getExpr).collect(Collectors.joining(",")))
                .args(updateSetItems.stream().map(QueryExpr::getArgs).flatMap(List::stream).collect(Collectors.toList()))
                .build();
        return Optional.of(updateSetExpr);
    }

    public Optional<QueryExpr> insertFields(V v) {
        if (valueInfo.isEmpty())
            return Optional.empty();
        List<QueryExpr> insertFields = valueInfo.stream()
                .map(vInfo -> vInfo.getInsertField(v))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        QueryExpr insertExpr = QueryExpr.builder()
                .expr(insertFields.stream().map(QueryExpr::getExpr).collect(Collectors.joining(", ")))
                .args(insertFields.stream().map(QueryExpr::getArgs).flatMap(List::stream).collect(Collectors.toList()))
                .build();
        return Optional.of(insertExpr);
    }

    /**
     * 初始化实体类字段信息
     */
    private void init() {
        TableName tableName = null;
        if ((tableName = this.entityClass.getAnnotation(TableName.class))!= null)
            this.tableName = tableName.value();

        // 初始化字段信息
        List<AccessibleObject> methodAndFields = new ArrayList<>();
        Class<?> clz = this.entityClass;
        do {
            Method[] declaredMethods = clz.getDeclaredMethods();
            methodAndFields.addAll(Arrays.asList(declaredMethods));
            Field[] declaredFields = clz.getDeclaredFields();
            methodAndFields.addAll(Arrays.asList(declaredFields));
            clz = clz.getSuperclass();
        } while (clz != null);

        this.process(methodAndFields);
    }

    private void process(List<? extends AccessibleObject> objects) {
        Map<String, List<IConditionInfo<V>>> betweenMap = new HashMap<>();
        // 解析字段
        for (AccessibleObject object : objects) {
            if (null != object.getAnnotation(Value.class) )
                // no need for type check
                valueInfo.add(new ValueInfo<>(entityClass, (Field) object));
            Select select = object.getAnnotation(Select.class);
            if (null != select)
                selectFields.add(selectField(((Field)object).getName(), select));
            if (null != object.getAnnotation(Condition.class)) {
                AbsConditionInfo<V> ele = object instanceof Field ?
                        ConditionHelper.ofField(this.entityClass, (Field) object) :
                        ConditionHelper.ofMethod(this.entityClass, (Method) object);
                if (null == ele)
                    continue;
                if (null != object.getAnnotation(Between.class)) {
                    betweenMap.compute(ele.getColumn(), (k, list) -> {
                        if (null == list)
                            list = new ArrayList<>();
                        list.add(ele);
                        return list;
                    });
                } else
                    this.addCondition(ele);
            }
        }
        // 处理between字段
        betweenMap.entrySet().stream().map(entry -> {
            List<IConditionInfo<V>> betweenEles = entry.getValue();
            Assert.isTrue(betweenEles.size() <= 2,
                    String.format("more than two between fields found for column [%s]", entry.getKey()));
            return ConditionHelper.between(entityClass, betweenEles.get(0), betweenEles.size() > 1 ? betweenEles.get(1) : null);
        }).forEach(this::addCondition);
        conditionInfo.values().forEach(list -> list.sort(Comparator.comparing(IConditionInfo::getOrder)));
    }

    private void addCondition(IConditionInfo<V> ele) {
        this.conditionInfo.compute(ele.getGroupCode(), (k, list) -> {
            if (null == list)
                list = new ArrayList<>();
            list.add(ele);
            return list;
        });
    }

    private String selectField(String fieldName, Select select) {
        String tableAlias = select.tableAlias();
        String column = StringUtils.hasText(select.column())? select.column(): fieldName;
        String alias = select.alias();
        String fullName = StringUtils.hasText(tableAlias) ? tableName + "." + column : column;
        return StringUtils.hasText(alias) ? String.format("%s AS %s", fullName, alias) : fullName;
    }

}
