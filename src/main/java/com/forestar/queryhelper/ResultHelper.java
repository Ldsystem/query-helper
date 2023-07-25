package com.forestar.queryhelper;

import com.forestar.queryhelper.annotation.Select;
import com.forestar.queryhelper.annotation.TableName;
import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/22
 * @Description
 */
@Slf4j
public class ResultHelper<V> {

    private final Class<V> entityClass;
    private final List<String> selectFields;
    private String tableName;

    public ResultHelper(@NotNull Class<V> clz) {
        this.entityClass = clz;
        this.selectFields = new ArrayList<>();
        this.init();
    }

    public List<String> selectFields() {
        return this.selectFields;
    }

    /**
     * 初始化实体类字段信息
     */
    private void init() {
        TableName tableName;
        if ((tableName = this.entityClass.getAnnotation(TableName.class))!= null)
            this.tableName = tableName.value();

        // 初始化字段信息
        List<Field> fields = new ArrayList<>();
        Class<?> clz = this.entityClass;
        do {
            Field[] declaredFields = clz.getDeclaredFields();
            fields.addAll(Arrays.asList(declaredFields));
            clz = clz.getSuperclass();
        } while (clz != null);

        this.process(fields);
    }

    private void process(List<Field> objects) {
        // 解析字段
        for (Field object : objects) {
            Select select = object.getAnnotation(Select.class);
            if (null != select)
                selectFields.add(selectField(object.getName(), select));
        }
    }

    private String selectField(String fieldName, Select select) {
        String tableAlias = select.tableAlias();
        String column = StringUtils.hasText(select.column())? select.column(): fieldName;
        String alias = select.alias();
        String fullName = StringUtils.hasText(tableAlias) ? tableName + "." + column : column;
        return StringUtils.hasText(alias) ? String.format("%s AS %s", fullName, alias) : fullName;
    }

}
