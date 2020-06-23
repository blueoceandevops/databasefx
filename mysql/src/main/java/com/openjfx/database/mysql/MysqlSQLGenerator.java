package com.openjfx.database.mysql;

import com.openjfx.database.SQLGenerator;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.model.RowChangeModel;
import com.openjfx.database.model.TableColumnMeta;
import com.openjfx.database.mysql.impl.DesignTableSQLGenerator;

import java.util.List;
import java.util.Map;

/**
 * Mysql Generator impl
 *
 * @author yangkui
 * @since 1.0
 */
public class MysqlSQLGenerator implements SQLGenerator {
    @Override
    public String createScheme(String name, String charset, String collation) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        var sql = "CREATE DATABASE `" + name + "`";
        if (StringUtils.nonEmpty(charset)) {
            sql += " CHARACTER SET '" + charset + "'";
        }
        if (StringUtils.nonEmpty(collation)) {
            sql += " COLLATE '" + collation + "'";
        }
        return sql;
    }

    @Override
    public String updateTable(String scheme, String table, List<RowChangeModel> changeModels, List<TableColumnMeta> metas) {
        return DesignTableSQLGenerator.updateTable(SQLHelper.fullTableName(scheme, table), changeModels, metas);
    }

    @Override
    public String createTable(String scheme, String table, List<RowChangeModel> changeModels) {
        return DesignTableSQLGenerator.createTable(changeModels, SQLHelper.fullTableName(scheme, table));
    }

    @Override
    public String select(List<TableColumnMeta> metas, String scheme, String table) {
        var tableName = SQLHelper.fullTableName(scheme, table);
        var sb = new StringBuilder();
        sb.append("SELECT ");
        var i = 0;
        for (TableColumnMeta meta : metas) {
            sb.append(meta.getField());
            if (i == metas.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
            i++;
        }
        sb.append(" FROM ");
        sb.append(tableName);
        return sb.toString();
    }

    @Override
    public String select(Map<String, String> columns, String scheme, String table) {
        var tableName = SQLHelper.fullTableName(scheme, table);
        var sb = new StringBuilder();
        sb.append("SELECT ");
        var i = 0;
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            var field = entry.getKey();
            var alias = entry.getValue();
            sb.append(field);
            if (StringUtils.nonEmpty(alias)) {
                sb.append(" ");
                sb.append(alias);
            }
            if (i == columns.size() - 1) {
                sb.append(" ");
            } else {
                sb.append(",");
            }
            i++;
        }
        sb.append(" FROM ");
        sb.append(tableName);
        return sb.toString();
    }

    @Override
    public String insert(String[] columns, String scheme, String table, List<String> values) {
        var tableName = SQLHelper.fullTableName(scheme, table);
        var sb = new StringBuilder("INSERT INTO " + tableName + "(");
        for (int i = 0; i < columns.length; i++) {
            var column = columns[i];
            sb.append(column);
            if (i < columns.length - 1) {
                sb.append(",");
            }
        }
        sb.append(") VALUES ");
        var rowSize = values.size() / columns.length;
        for (int i = 0; i < rowSize; i++) {
            var k = 0;
            sb.append("(");
            while (k < columns.length) {
                var val = values.get(i + k * rowSize);
                sb.append(SQLHelper.escapeFieldValue(val));
                if (k < columns.length - 1) {
                    sb.append(",");
                }
                k++;
            }
            sb.append(")");
            if (i < rowSize - 1) {
                sb.append(",");
            } else {
                sb.append(";");
            }
        }
        return sb.toString();
    }
}
