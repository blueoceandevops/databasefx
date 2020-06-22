package com.openjfx.database.utils;

import com.openjfx.database.enums.DatabaseType;

public class SQLFormatUtils {
    /**
     * Return different formatted SQL according to different databases
     *
     * @param sql  SQL Statement
     * @param type database type
     * @return format sql statement
     */
    public static String format(String sql, DatabaseType type) {
//        return switch (type) {
//            case H2 -> SQLUtils.formatHive(sql);
//            case MYSQL -> SQLUtils.formatMySql(sql);
//            case ORACLE -> SQLUtils.formatOracle(sql);
//            case SQL_SERVER -> SQLUtils.formatSQLServer(sql);
//        };
        return sql;
    }
}
