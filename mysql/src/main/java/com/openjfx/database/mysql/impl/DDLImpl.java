package com.openjfx.database.mysql.impl;

import com.openjfx.database.DDL;
import com.openjfx.database.mysql.SQLHelper;
import io.vertx.core.Future;
import io.vertx.core.Promise;

public class DDLImpl implements DDL {
    private final MysqlClient client;

    public DDLImpl(MysqlClient client) {
        this.client = client;
    }

    @Override
    public Future<Void> dropDatabase(String database) {
        var sql = "DROP DATABASE " + database;
        var promise = Promise.<Void>promise();
        var future = client.query(sql);
        future.onSuccess(rows -> promise.complete());
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<Void> dropTable(String table, String scheme) {
        var tableName = SQLHelper.fullTableName(scheme, table);
        var sql = "DROP TABLE " + tableName;
        var promise = Promise.<Void>promise();
        var future = client.query(sql);
        future.onSuccess(rows -> promise.complete());
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<Integer> dropView(String scheme, String view) {
        var viewName = SQLHelper.fullTableName(scheme, view);
        var sql = "DROP VIEW IF EXISTS " + viewName;
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
