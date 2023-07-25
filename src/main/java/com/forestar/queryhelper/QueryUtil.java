package com.forestar.queryhelper;

import com.forestar.queryhelper.model.QueryExpr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * @author liushenglong_8597@outlook.com
 * @Date 2023/5/24
 * @Description
 */
@Slf4j
public class QueryUtil {

    public static <V> List<Map<String, Object>> query(DatabaseRepository databaseRepository, List<String> fields, QueryHelper<V> helper, V vo) {
        QueryExpr queryExpr = helper.selectExpr(fields, vo);
        log.debug("Execute Select Query: {}", queryExpr);
        JdbcTemplate template = databaseRepository.getJdbcTemplate(helper.getTableName());
        return template.queryForList(queryExpr.getExpr(), queryExpr.getArgsArr());
    }

    public static <V> List<Map<String, Object>> query(DatabaseRepository databaseRepository, QueryHelper<V> helper, V vo) {
        QueryExpr selectExpr = helper.selectExpr(vo);
        log.debug("Execute Select Query: {}", selectExpr);
        JdbcTemplate template = databaseRepository.getJdbcTemplate(helper.getTableName());
        return template.queryForList(selectExpr.getExpr(), selectExpr.getArgsArr());
    }

    public static <V> int delete(DatabaseRepository databaseRepository, QueryHelper<V> helper, V vo) {
        QueryExpr deleteExpr = helper.deleteExpr(vo).orElseThrow(() -> new RuntimeException("缺少过滤条件:" + vo));
        log.debug("Execute Delete Query: {}", deleteExpr);
        JdbcTemplate template = databaseRepository.getJdbcTemplate(helper.getTableName());
        return template.update(deleteExpr.getExpr(), deleteExpr.getArgsArr());
    }

    public static <V> void deleteWithCallback(DatabaseRepository databaseRepository, QueryHelper<V> helper, V vo, Runnable onSuccess) {
        int delete = delete(databaseRepository, helper, vo);
        if (null != onSuccess && delete > 0)
            onSuccess.run();
    }

    public static <V> int update(DatabaseRepository databaseRepository, QueryHelper<V> helper, V vo) {
        QueryExpr updateExpr = helper.updateExpr(vo).orElseThrow(() -> new RuntimeException("缺少更新字段或过滤条件: " + vo));
        log.debug("Execute Update Query: {}", updateExpr);
        JdbcTemplate template = databaseRepository.getJdbcTemplate(helper.getTableName());
        return template.update(updateExpr.getExpr(), updateExpr.getArgsArr());
    }

    public static <V> void updateWithCallback(DatabaseRepository databaseRepository, QueryHelper<V> helper, V vo, Runnable onSuccess) {
        int update = update(databaseRepository, helper, vo);
        if (null != onSuccess && update > 0)
            onSuccess.run();
    }

}
