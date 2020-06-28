package com.openjfx.database.app.model.tab.meta;

import com.openjfx.database.app.component.BaseTab;
import com.openjfx.database.app.controls.BaseTreeNode;
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
     * Table type
     */
    private final BaseTab.TabType tableType;

    public TableTabModel(String uuid,
                         String scheme,
                         String table,
                         String conName,
                         BaseTab.TabType tableType) {

        super(uuid, conName);
        this.scheme = scheme;
        this.table = table;
        this.tableType = tableType;
    }

    @Override
    public String getFlag() {
        return uuid + "_" + scheme + "_" + table;
    }

    public static TableTabModel build(TreeItem<String> treeNode) {
        final String scheme;
        final String tableName;
        final BaseTab.TabType tableType;
        final String uuid;
        final String conName;
        if (treeNode instanceof TableTreeNode) {
            var tableNode = (TableTreeNode) treeNode;
            scheme = tableNode.getScheme();
            tableName = tableNode.getValue();
            tableType = BaseTab.TabType.BASE_TABLE_TAB;
            uuid = tableNode.getUuid();
        } else {
            var viewNode = (TableViewTreeNode) treeNode;
            scheme = viewNode.getScheme();
            tableName = viewNode.getValue();
            tableType = BaseTab.TabType.VIEW_TAB;
            uuid = viewNode.getUuid();
        }
        conName = ((BaseTreeNode<String>) treeNode).getConName();
        return new TableTabModel(uuid, scheme, tableName, conName, tableType);
    }

    public String getScheme() {
        return scheme;
    }

    public String getTable() {
        return table;
    }

    public BaseTab.TabType getTableType() {
        return tableType;
    }
}
