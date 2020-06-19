package com.openjfx.database;


import com.openjfx.database.model.RowChangeModel;
import com.openjfx.database.model.TableColumnMeta;

import java.util.List;

/**
 * SQL statement generation interface
 *
 * @author yangkui
 * @since 1.0
 */
public interface SQLGenerator {

    /**
     * current database create scheme
     *
     * @param name      scheme name
     * @param charset   scheme charset
     * @param collation scheme charset collation
     * @return create scheme sql
     */
    String createScheme(String name, String charset, String collation);

    /**
     * Create SQL statement to modify table field information
     *
     * @param table        target table
     * @param scheme       target scheme
     * @param changeModels change model
     * @param metas        table column meta
     * @return sql statement
     */
    String updateTable(String scheme, String table, List<RowChangeModel> changeModels, List<TableColumnMeta> metas);

    /**
     * create table
     *
     * @param table        table name
     * @param scheme       current scheme
     * @param changeModels row list
     * @return create table sql
     */
    String createTable(String scheme, String table, List<RowChangeModel> changeModels);

    /**
     * generate select sql
     *
     * @param metas  table column meta
     * @param scheme current scheme
     * @param table  table
     * @return sql statement
     */
    String select(List<TableColumnMeta> metas, String scheme, String table);

    /**
     * generate insert sql
     *
     * @param columns table column list
     * @param scheme  table scheme
     * @param table   table name
     * @param values  value
     * @return insert statement
     */
    String insert(String[] columns, String scheme, String table, List<String> values);
}
