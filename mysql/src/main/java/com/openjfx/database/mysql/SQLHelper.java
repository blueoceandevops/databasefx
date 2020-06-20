package com.openjfx.database.mysql;

import com.openjfx.database.model.TableColumnMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SQL statement processing auxiliary class
 *
 * @author yangkui
 * @since 1.0
 */
public class SQLHelper {

    public static String escapeSingleField(String field) {
        return "`" + field + "`";
    }

    public static String fullTableName(String scheme, String table) {
        Objects.requireNonNull(scheme);
        Objects.requireNonNull(table);
        return "`" + scheme + "`" + ".`" + table + "`";
    }

    public static String escapeFieldValue(String val) {
        return val == null ? null : "'" + val + "'";
    }
}
