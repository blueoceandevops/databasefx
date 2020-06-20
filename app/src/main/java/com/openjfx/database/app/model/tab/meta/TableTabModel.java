package com.openjfx.database.app.model.tab.meta;

import com.openjfx.database.app.controls.impl.TableTreeNode;
import com.openjfx.database.app.controls.impl.TableViewTreeNode;
import com.openjfx.database.app.model.tab.BaseTabMode;
import javafx.scene.control.TreeItem;


/**
 * Table tab metadata
 *
 * @author yangkui
 * @since 1.0
 */
public class TableTabModel extends BaseTabMode {
    /**
     * Database name
     */
    private final String scheme;
    /**
     * Table name
     */
    private final String table;
    /**
     * Server name
     */
    private final String serverName;
    /**
     * Table type
     */
    private TableType tableType;

    public TableTabModel(String uuid, String flag, String scheme, String table, String serverName, TableType tableType) {
        super(uuid, flag);
        this.scheme = scheme;
        this.table = table;
        this.serverName = serverName;
        this.tableType = tableType;
    }

    public static TableTabModel build(TreeItem<String> treeNode) {
        final String scheme;
        final String serverName;
        final String tableName;
        final TableType tableType;
        final String uuid;
        if (treeNode instanceof TableTreeNode) {
            var tableNode = (TableTreeNode) treeNode;
            scheme = tableNode.getScheme();
            serverName = tableNode.getServerName();
            tableName = tableNode.getValue();
            tableType = TableType.BASE_TABLE;
            uuid = tableNode.getUuid();
        } else {
            var viewNode = (TableViewTreeNode) treeNode;
            scheme = viewNode.getScheme();
            serverName = viewNode.getServerName();
            tableName = viewNode.getValue();
            tableType = TableType.VIEW;
            uuid = viewNode.getUuid();
        }
        var flag = uuid + "_" + scheme + "_" + tableName;
        return new TableTabModel(uuid, flag, scheme, tableName, serverName, tableType);
    }

    public String getScheme() {
        return scheme;
    }

    public String getTable() {
        return table;
    }

    public String getServerName() {
        return serverName;
    }

    public TableType getTableType() {
        return tableType;
    }

    public void setTableType(TableType tableType) {
        this.tableType = tableType;
    }

    /**
     * table type
     *
     * @author yangkui
     * @since 1.0
     */
    public enum TableType {
        /**
         * system view
         */
        SYSTEM_VIEW,
        /**
         * base table
         */
        BASE_TABLE,
        /**
         * view
         */
        VIEW
    }
}
