package com.openjfx.database.mysql.impl;

import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.enums.DesignTableOperationType;
import com.openjfx.database.model.RowChangeModel;
import com.openjfx.database.model.TableColumnMeta;
import com.openjfx.database.mysql.SQLHelper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.openjfx.database.enums.DesignTableOperationSource.TABLE_COMMENT;
import static com.openjfx.database.enums.DesignTableOperationSource.TABLE_FIELD;

/**
 * Design table sql generator
 *
 * @author yangkui
 * @since 1.0
 */
public class DesignTableSQLGenerator {
    /**
     * update design table
     *
     * @return
     */
    public static String updateTable(String table, List<RowChangeModel> rowChangeModels, List<TableColumnMeta> metas) {
        var sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(table);
        var updateFieldList = rowChangeModels
                .stream()
                .filter(rowChangeModel -> rowChangeModel.getSource() == TABLE_FIELD)
                .collect(Collectors.toList());
        var i = 0;
        for (RowChangeModel row : updateFieldList) {
            var operateType = row.getOperationType();
            var meta = row.getTableColumnMeta();
            if (operateType == DesignTableOperationType.CREATE || operateType == DesignTableOperationType.UPDATE) {
                var optional = row.getColumn(TableColumnMeta.TableColumnEnum.FIELD);
                if (operateType == DesignTableOperationType.UPDATE) {
                    if (optional.isPresent()) {
                        var column = optional.get();
                        sb.append(" CHANGE COLUMN ");
                        sb.append(column.getOriginValue());
                        sb.append(" ");
                        sb.append(column.getNewValue());
                    } else {
                        sb.append(" MODIFY COLUMN ");
                        sb.append(meta.getField());
                    }
                } else {
                    sb.append(" ADD COLUMN ");
                    if (optional.isPresent()) {
                        var k = SQLHelper.escapeSingleField(optional.get().getNewValue());
                        sb.append(k);
                    } else {
                        sb.append(meta.getField());
                    }
                }
                sb.append(" ");
                sb.append(updateTableField(row, meta));
            } else {
                sb.append("DROP COLUMN ");
                sb.append(SQLHelper.escapeSingleField(meta.getField()));
            }
            if (i != updateFieldList.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        var keys = getPrimaryKey(rowChangeModels, metas);
        var comment = createTableComment(rowChangeModels);
        if (updateFieldList.isEmpty()) {
            sb.append(" ");
            if (keys.isEmpty()) {
                comment = comment.substring(1);
            } else {
                keys = keys.substring(1);
            }
        }
        sb.append(keys);
        sb.append(comment);
        return sb.toString();
    }

    /**
     * create table
     *
     * @return
     */
    public static String createTable(List<RowChangeModel> changeModels, String table) {
        var sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(table);
        sb.append("(");
        var updateFieldList = changeModels.stream()
                .filter(rowChangeModel -> rowChangeModel.getSource() == TABLE_FIELD)
                .collect(Collectors.toList());
        var i = 0;
        for (RowChangeModel row : updateFieldList) {
            var meta = row.getTableColumnMeta();
            var optional = row.getColumn(TableColumnMeta.TableColumnEnum.FIELD);
            if (optional.isPresent()) {
                sb.append(optional.get().getNewValue());
            } else {
                sb.append(meta.getField());
            }
            sb.append(" ");
            sb.append(updateTableField(row, meta));
            var option = row.getColumn(TableColumnMeta.TableColumnEnum.PRIMARY_KEY);
            option.ifPresent(column -> sb.append("PRIMARY KEY"));
            if (i != updateFieldList.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append(") ");
        var comment = createTableComment(changeModels);
        if (comment.contains(",")) {
            comment = comment.substring(1);
        }
        sb.append(comment);
        return sb.toString();
    }

    private static String updateTableField(RowChangeModel rowChangeModel, TableColumnMeta meta) {
        var sb = new StringBuilder();
        var a = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.TYPE);
        var b = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.LENGTH);
        var c = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.DECIMAL_POINT);
        if (a.isPresent() || b.isPresent() || c.isEmpty()) {
            final String type;
            if (a.isPresent()) {
                type = a.get().getNewValue();
            } else {
                type = meta.getType();
            }
            sb.append(type);
            sb.append("(");
            b.ifPresentOrElse(columnChangeModel -> sb.append(columnChangeModel.getNewValue()), () -> sb.append(meta.getLength()));

            var isDecimalPoint = new MysqlDataType().hasDecimalPoint(type);
            if (isDecimalPoint) {
                sb.append(",");
                sb.append(c.isPresent() ? c.get().getNewValue() : meta.getDecimalPoint());
            }
            sb.append(")");
        } else {
            sb.append(meta.getOriginalType());
        }
        sb.append(" ");
        var d = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.UN_SIGNED);
        var dd = d.map(column -> Boolean.parseBoolean(column.getNewValue())).orElse(meta.getUnsigned());
        if (dd) {
            sb.append("UNSIGNED ");
        }
        var e = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.CHARSET);
        var f = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.COLLATION);
        if (StringUtils.nonEmpty(meta.getCharset()) || e.isPresent() || f.isPresent()) {
            sb.append("CHARACTER SET ");
            sb.append(e.isPresent() ? e.get().getNewValue() : meta.getCharset());
            sb.append(" COLLATE ");
            sb.append(f.isPresent() ? f.get().getNewValue() : meta.getCollation());
        }
        sb.append(" ");
        var g = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.NULL);
        var nullable = g
                .map(columnChangeModel -> Boolean.parseBoolean(columnChangeModel.getNewValue()))
                .orElseGet(meta::getNotNull);
        sb.append(nullable ? "NOT NULL " : "NULL ");

        var h = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.AUTO_INCREMENT);
        var hh = h.map(column -> Boolean.parseBoolean(column.getNewValue())).orElse(meta.getAutoIncrement());
        if (hh) {
            sb.append("AUTO_INCREMENT ");
        }
        var i = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.DEFAULT);
        if (i.isPresent() || meta.getDefault() != null) {
            sb.append("DEFAULT '");
            sb.append(i.isPresent() ? i.get().getNewValue() : meta.getDefault());
            sb.append("' ");
        }
        var j = rowChangeModel.getColumn(TableColumnMeta.TableColumnEnum.COMMENT);
        if (j.isPresent() || meta.getComment() != null) {
            sb.append("COMMENT '");
            sb.append(j.isPresent() ? j.get().getNewValue() : meta.getComment());
            sb.append("' ");
        }
        return sb.toString();
    }

    private static String getPrimaryKey(List<RowChangeModel> rows, List<TableColumnMeta> metas) {
        //old key
        var originKey = new ArrayList<Integer>();
        for (int i = 0; i < metas.size(); i++) {
            var meta = metas.get(i);
            if (meta.getPrimaryKey()) {
                originKey.add(i);
            }
        }

        var oldKeyLength = originKey.size();
        var rowChangeModels = rows.stream()
                .filter(rowChangeModel -> rowChangeModel.containField(TableColumnMeta.TableColumnEnum.PRIMARY_KEY))
                .collect(Collectors.toList());
        for (RowChangeModel rowChangeModel : rowChangeModels) {
            var index = rowChangeModel.getRowIndex();
            if (originKey.contains(index)) {
                originKey.remove(index);
            } else {
                originKey.add(index);
            }
        }
        var sb = new StringBuilder();

        var kk = new ArrayList<String>();

        for (Integer key : originKey) {
            var optional = rows.stream().filter(r -> r.getRowIndex() == key).findAny();
            if (optional.isPresent()) {
                var row = optional.get();
                var col = row.getColumn(TableColumnMeta.TableColumnEnum.FIELD);
                kk.add(col.isEmpty() ? row.getTableColumnMeta().getField() : col.get().getNewValue());
            } else {
                kk.add(metas.get(key).getField());
            }
        }

        if (oldKeyLength > 0) {
            sb.append(",DROP PRIMARY KEY");
        }
        for (int i = 0; i < kk.size(); i++) {
            if (i == 0) {
                sb.append(",ADD PRIMARY KEY(");
            }
            var key = kk.get(i);
            sb.append(key);
            if (i < kk.size() - 1) {
                sb.append(",");
            } else {
                sb.append(")");
            }
        }

        return sb.toString();
    }

    private static String createTableComment(List<RowChangeModel> rowChangeModels) {
        var optional = rowChangeModels.stream().filter(row -> row.getSource() == TABLE_COMMENT).findAny();
        String str = "";
        if (optional.isPresent()) {
            var ap = optional.get().getColumn(TableColumnMeta.TableColumnEnum.COMMENT);
            if (ap.isPresent()) {
                str = ",COMMENT '" + ap.get().getNewValue() + "'";
            }
        }
        str += ";";
        return str;
    }
}
