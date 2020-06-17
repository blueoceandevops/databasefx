package com.openjfx.database.mysql.impl;

import com.openjfx.database.DML;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.model.TableColumnMeta;
import com.openjfx.database.mysql.SQLHelper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Tuple;

import java.util.*;

import static com.openjfx.database.common.config.StringConstants.*;

/**
 * Mysql DML impl
 *
 * @author yangkui
 * @since 1.0
 */
public class DMLImpl implements DML {

    private final MysqlPoolImpl client;

    public DMLImpl(MysqlPoolImpl client) {
        this.client = client;
    }

    @Override
    public Future<Integer> batchUpdate(List<Map<String, Object[]>> items, String tableName, List<TableColumnMeta> metas) {
        var optional = metas.stream().filter(col -> StringUtils.nonEmpty(col.getKey())).findFirst();
        var promise = Promise.<Integer>promise();
        if (optional.isEmpty()) {
            promise.fail("无法找到key值,故取消更新");
        } else {
            var keyMeta = optional.get();
            var sql = updateSql(metas, keyMeta, tableName);
            var tuples = new ArrayList<Tuple>();
            for (var item : items) {
                var t = item.get(ROW);
                var tuple = Tuple.tuple();
                for (var o : t) {
                    tuple.addValue(o);
                }
                tuple.addValue(item.get(KEY)[0]);
                tuples.add(tuple);
            }
            client.preparedBatch(sql, tuples).onSuccess(r -> promise.complete(1)).onFailure(promise::fail);
        }

        return promise.future();
    }

    @Override
    public Future<Long> insert(List<TableColumnMeta> metas, Object[] columns, String tableName) {

        var sql = insertSql(metas, tableName);

        var promise = Promise.<Long>promise();

        var tuple = Tuple.wrap(Arrays.asList(columns));

        var future = client.preparedQuery(sql, tuple);
        future.onSuccess(rows -> {
            var lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
            promise.complete(lastInsertId);
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    private String insertSql(List<TableColumnMeta> metas, String table) {
        var tableName = SQLHelper.escapeMysqlField(table);
        var sb = new StringBuilder("INSERT INTO ");
        var vs = new StringBuilder();
        sb.append(tableName);
        sb.append("(");
        for (int i = 0; i < metas.size(); i++) {
            var meta = metas.get(i);
            var field = SQLHelper.escapeMysqlField(meta.getField());
            sb.append(field);
            vs.append("?");
            if (i < metas.size() - 1) {
                sb.append(",");
                vs.append(",");
            }
        }
        sb.append(") ");
        sb.append(" VALUES(");
        sb.append(vs.toString());
        sb.append(")");
        return sb.toString();
    }

    public String updateSql(List<TableColumnMeta> metas, TableColumnMeta keyMeta, String table) {
        var tableName = SQLHelper.escapeMysqlField(table);
        var sb = new StringBuilder("UPDATE ");
        sb.append(tableName);
        sb.append(" SET ");
        for (int i = 0; i < metas.size(); i++) {
            var meta = metas.get(i);
            var field = SQLHelper.escapeMysqlField(meta.getField());
            sb.append(field);
            sb.append("=?");
            if (i != metas.size() - 1) {
                sb.append(",");
            }
        }
        var keyField = SQLHelper.escapeMysqlField(keyMeta.getField());
        sb.append(" WHERE ");
        sb.append(keyField);
        sb.append("=?");
        return sb.toString();
    }

    @Override
    public Future<Integer> batchDelete(TableColumnMeta keyMeta, Object[] keyValues, String table) {
        var sb = new StringBuilder();
        var tableName = SQLHelper.escapeMysqlField(table);
        sb.append("DELETE FROM ");
        sb.append(tableName);
        sb.append(" WHERE ");
        sb.append(SQLHelper.escapeMysqlField(keyMeta.getField()));
        sb.append(" =?");
        var sql = sb.toString();
        var tuples = new ArrayList<Tuple>();
        for (var keyValue : keyValues) {
            tuples.add(Tuple.of(keyValue));
        }
        var promise = Promise.<Integer>promise();
        var future = client.preparedBatch(sql, tuples);
        future.onSuccess(rows -> promise.complete(keyValues.length));
        future.onFailure(promise::fail);
        return promise.future();
    }


    @Override
    public Future<Integer> renameTable(String table, String target, String scheme) {
        var t = scheme + "." + table;
        var tt = scheme + "." + target;
        var sql = "RENAME TABLE " + SQLHelper.escapeMysqlField(t) + " TO " + SQLHelper.escapeMysqlField(tt);
        var promise = Promise.<Integer>promise();
        var future = client.query(sql);
        future.onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(ar.result().rowCount());
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }
}
