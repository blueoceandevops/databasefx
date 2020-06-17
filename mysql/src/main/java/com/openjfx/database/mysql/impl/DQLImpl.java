package com.openjfx.database.mysql.impl;

import com.openjfx.database.DQL;
import com.openjfx.database.DataConvert;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.model.TableColumnMeta;
import com.openjfx.database.mysql.PageHelper;
import com.openjfx.database.mysql.SQLHelper;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.*;


public class DQLImpl implements DQL {

    private final MysqlPoolImpl client;

    private final DataConvert dataConvert = new SimpleMysqlDataConvert();

    public DQLImpl(MysqlPoolImpl client) {
        this.client = client;
    }

    @Override
    public Future<List<String>> showDatabase() {
        var sql = "SHOW DATABASES";
        var promise = Promise.<List<String>>promise();
        var future = client.query(sql);
        future.onSuccess(r -> {
            var schemes = new ArrayList<String>();
            r.forEach(row -> {
                var scheme = row.getValue(0).toString();
                schemes.add(scheme);
            });
            promise.complete(schemes);
        });
        future.onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public Future<List<String>> showTables(String scheme) {
        var promise = Promise.<List<String>>promise();
        var sql = "SELECT TABLE_NAME FROM `information_schema`.`TABLES` WHERE (`table_type` ='BASE TABLE' OR `TABLE_TYPE`='SYSTEM VIEW') AND table_schema =?";
        var future = client.preparedQuery(sql, Tuple.of(scheme));
        future.onSuccess(r -> {
            var schemes = new ArrayList<String>();
            r.forEach(row -> {
                var table = row.getValue(0).toString();
                schemes.add(table);
            });
            promise.complete(schemes);
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<List<String>> showViews(String scheme) {
        var sql = "SELECT TABLE_NAME FROM `information_schema`.`TABLES` WHERE `TABLE_TYPE` ='VIEW' AND table_schema =?";
        var promise = Promise.<List<String>>promise();
        var future = client.preparedQuery(sql, Tuple.of(scheme));
        future.onSuccess(rs -> {
            var views = new ArrayList<String>();
            rs.forEach(row -> {
                var table = StringUtils.getObjectStrElseGet(row.getValue(0), "");
                views.add(table);
            });
            promise.complete(views);
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<List<TableColumnMeta>> showColumns(String table) {
        var tableName = SQLHelper.escapeMysqlField(table);

        var sql = "SHOW FULL COLUMNS FROM " + tableName;

        var promise = Promise.<List<TableColumnMeta>>promise();

        var future = client.query(sql);
        future.onSuccess(rows -> {
            var dataType = new MysqlDataType();
            var charset = new MysqlCharset();
            var metas = new ArrayList<TableColumnMeta>();

            for (var row : rows) {

                var meta = new TableColumnMeta();

                var key = StringUtils.getObjectStrElseGet(row.getValue("Key"), "");
                var field = StringUtils.getObjectStrElseGet(row.getValue("Field"), "");
                var comment = StringUtils.getObjectStrElseGet(row.getValue("Comment"), null);
                var nullable = StringUtils.getObjectStrElseGet(row.getValue("Null"), "No");
                var privileges = StringUtils.getObjectStrElseGet("Privileges", "");
                var type = StringUtils.getObjectStrElseGet(row.getValue("Type"), "");
                var extra = StringUtils.getObjectStrElseGet(row.getValue("Extra"), "");
                var collation = StringUtils.getObjectStrElseGet(row.getValue("Collation"), "");
                var defaultValue = StringUtils.getObjectStrElseGet(row.getValue("Default"), null);

                var isKey = key.contains("PRI");
                var notNull = "NO".equals(nullable);
                var isUnSigned = type.contains("unsigned");
                var autoIncrement = extra.contains("auto_increment");

                meta.setKey(key);
                meta.setField(field);
                meta.setExtra(extra);
                meta.setNotNull(notNull);
                meta.setComment(comment);
                meta.setPrimaryKey(isKey);
                meta.setOriginalType(type);
                meta.setUnsigned(isUnSigned);
                meta.setCollation(collation);
                meta.setDefault(defaultValue);
                meta.setPrivileges(privileges);
                meta.setAutoIncrement(autoIncrement);
                meta.setType(dataType.getDataType(type));
                meta.setCharset(charset.getCharset(collation));
                meta.setLength(dataType.getDataTypeLength(type));
                meta.setDecimalPoint(dataType.getDataFieldDecimalPoint(type));

                metas.add(meta);
            }
            promise.complete(metas);
        });
        future.onFailure(promise::fail);

        return promise.future();
    }

    @Override
    public Future<List<String[]>> query(String table, int pageIndex, int pageSize) {
        var tableName = SQLHelper.escapeMysqlField(table);
        var sql = "SELECT * FROM " + tableName + " LIMIT ?,?";
        var a = PageHelper.getInitPage(pageIndex, pageSize);
        var tuple = Tuple.of(a, pageSize);
        var promise = Promise.<List<String[]>>promise();
        var future = client.preparedQuery(sql, tuple);
        future.onSuccess(rows -> {
            var list = dataConvert.toConvert(rows);
            promise.complete(list);
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<Long> count(String table) {
        var tableName = SQLHelper.escapeMysqlField(table);
        var sql = "SELECT COUNT(*) FROM " + tableName;
        var promise = Promise.<Long>promise();
        var future = client.query(sql);
        future.onSuccess(rows -> {
            var number = 0L;
            for (var row : rows) {
                number = row.getLong(0);
            }
            promise.complete(number);
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<Void> heartBeatQuery() {
        var sql = "SELECT 1";
        var promise = Promise.<Void>promise();
        var future = client.getPool().query(sql);
        future.onSuccess(ar -> promise.complete());
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<Map<List<String>, List<String[]>>> executeSql(String sql) {
        var future = client.query(sql);
        var promise = Promise.<Map<List<String>, List<String[]>>>promise();
        future.onSuccess(rows -> {
            var columns = rows.columnsNames();
            var dd = dataConvert.toConvert(rows);
            var map = new HashMap<List<String>, List<String[]>>();
            map.put(columns, dd);
            promise.complete(map);
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<String> showCreateTable(String table) {
        var tableName = SQLHelper.escapeMysqlField(table);
        var sql = "SHOW CREATE table " + tableName;
        var future = client.query(sql);
        var promise = Promise.<String>promise();
        future.onSuccess(rs -> {
            for (Row row : rs) {
                var str = row.getString(1);
                promise.complete(str);
                return;
            }
            promise.complete("");
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<String> getCreateTableComment(String table) {
        var future = showCreateTable(table);
        var promise = Promise.<String>promise();
        future.onSuccess(createTableSql -> {
            if (StringUtils.isEmpty(createTableSql)) {
                promise.complete("");
                return;
            }
            var index = createTableSql.lastIndexOf(")");
            if (index == -1) {
                promise.complete("");
                return;
            }
            var str = createTableSql.substring(index + 1);
            var tIndex = str.indexOf("COMMENT");
            if (tIndex == -1) {
                promise.complete("");
            } else {
                var comment = str.substring(tIndex + 9, str.length() - 1);
                promise.complete(comment);
            }
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    @Override
    public Future<List<String>> getCurrentDatabaseUserList() {
        var promise = Promise.<List<String>>promise();
        var sql = "SELECT * FROM mysql.user";
        client.query(sql).onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            var list = new ArrayList<String>();
            for (Row row : ar.result()) {
                var user = StringUtils.getObjectStrElseGet(row.getValue("User"), "");
                var host = StringUtils.getObjectStrElseGet(row.getValue("Host"), "");
                list.add(user + '@' + host);
            }
            promise.complete(list);
        });
        return promise.future();
    }
}
