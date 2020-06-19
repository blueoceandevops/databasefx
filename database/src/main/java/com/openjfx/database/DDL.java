package com.openjfx.database;


import io.vertx.core.Future;

/**
 * 数据定义语言接口
 *
 * @author yangkui
 * @since 1.0
 */
public interface DDL {
    /**
     * 删除数据库
     *
     * @param database 数据库名
     * @return 返回删除结果
     */
    Future<Void> dropDatabase(String database);


    /**
     * 删除table
     *
     * @param table 表名
     * @param schem scheme
     * @return 返回删除结果
     */
    Future<Void> dropTable(String table, String schem);

    /**
     * delete view
     *
     * @param view   view name
     * @param scheme target scheme
     * @return return drop result
     */
    Future<Integer> dropView(String scheme, String view);
}
