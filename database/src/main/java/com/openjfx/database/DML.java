package com.openjfx.database;

import com.openjfx.database.model.TableColumnMeta;
import io.vertx.core.Future;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 数据库操作语言接口
 *
 * @author yangkui
 * @since 1.0
 */
public interface DML {
    /**
     * 批量更新
     *
     * @param table  表名
     * @param items  待更新数据
     * @param metas  列信息
     * @param scheme current scheme
     * @return 返回更新结果
     */
    Future<Integer> batchUpdate(List<Map<String, Object[]>> items, String scheme, String table, List<TableColumnMeta> metas);

    /**
     * 新增数据
     *
     * @param metas   table meta
     * @param columns 列值
     * @param table   表名
     * @param scheme  current scheme
     * @return 返回新增结果
     */
    Future<Long> insert(List<TableColumnMeta> metas, Object[] columns, String scheme, String table);

    /**
     * 批量删除
     *
     * @param keyMeta   key字段
     * @param keyValues key值列表
     * @param tableName 表名
     * @param scheme    current scheme
     * @return 返回受影响行数
     */
    Future<Integer> batchDelete(TableColumnMeta keyMeta, Object[] keyValues, String scheme, String tableName);

    /**
     * Rename table
     *
     * @param table  current table name
     * @param target target table name
     * @param scheme current scheme
     * @return result
     */
    Future<Integer> renameTable(String table, String target, String scheme);
}
