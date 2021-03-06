package com.openjfx.database;

import com.openjfx.database.model.TableColumnMeta;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;
import java.util.Map;

/**
 * 数据库查询语言接口
 *
 * @author yangkui
 * @since 1.0
 */
public interface DQL {
    /**
     * 查看已有数据库信息
     *
     * @return 返回scheme列表
     */
    Future<List<String>> showDatabase();

    /**
     * 查询某个scheme下的table
     *
     * @param scheme scheme名称
     * @return 返回表信息
     */
    Future<List<String>> showTables(String scheme);

    /**
     * Query the view under a scheme
     *
     * @return view list
     */
    Future<List<String>> showViews(String scheme);

    /**
     * 获取某个表的column情况
     *
     * @param scheme current scheme
     * @param table  table
     * @return {@link TableColumnMeta}
     */
    Future<List<TableColumnMeta>> showColumns(String scheme, String table);

    /**
     * 分页查询某张表的数据
     *
     * @param pageIndex 分页查询起始页面
     * @param pageSize  分页查询尺寸
     * @param table     表名
     * @param scheme    current scheme
     * @return 返回查询结果
     */
    Future<List<String[]>> query(String scheme, String table, int pageIndex, int pageSize);

    /**
     * 统计目标表的数量
     *
     * @param tableName 表名
     * @param scheme    current scheme
     * @return 返回数量
     */
    Future<Long> count(String scheme, String tableName);

    /**
     * 心跳查询语句
     *
     * @return 返回空查询结果
     */
    Future<Void> heartBeatQuery();

    /**
     * 执行sql查询语句
     *
     * @param sql sql语句
     * @return 返回结果
     */
    Future<Map<List<String>, List<String[]>>> executeSql(String sql);


    /**
     * obtain create table comment
     *
     * @param scheme scheme
     * @param table  table
     * @return table comment
     */
    Future<String> getCreateTableComment(String scheme, String table);

    /**
     * query current database all user
     *
     * @return user list
     */
    Future<List<String>> getCurrentDatabaseUserList();

}
