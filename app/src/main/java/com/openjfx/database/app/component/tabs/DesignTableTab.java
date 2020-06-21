package com.openjfx.database.app.component.tabs;


import com.openjfx.database.app.component.BaseTab;
import com.openjfx.database.app.component.DesignOptionBox;
import com.openjfx.database.app.controls.impl.DesignDataView;
import com.openjfx.database.app.controls.SQLEditor;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.model.DesignTableModel;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.app.utils.AssetUtils;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.EventBusUtils;
import com.openjfx.database.base.AbstractDataBaseClient;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.enums.DesignTableOperationSource;
import com.openjfx.database.enums.DesignTableOperationType;
import com.openjfx.database.model.TableColumnMeta;
import com.openjfx.database.utils.SQLFormatUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;


import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;


/**
 * Design table tab
 *
 * @author yangkui
 * @since 1.0
 */
public class DesignTableTab extends BaseTab<DesignTabModel> {
    @FXML
    private TabPane tabPane;

    @FXML
    private DesignDataView fieldTable;

    @FXML
    private SplitPane splitPane;

    @FXML
    private SQLEditor sqlEditor;

    @FXML
    private TextArea commentTextArea;

    private DesignOptionBox box;

    private AbstractDataBaseClient client;
    /**
     * un-title
     */
    private final String UN_TITLE = "Untitled";

    private final static Image IMAGE_ICON = AssetUtils.getLocalImage(20, 20, "design_table_icon.png");

    public DesignTableTab(DesignTabModel model) {
        super(model);
        loadView("design_tab_view.fxml");
        setTabIcon(IMAGE_ICON);
    }

    @Override
    public void init() {
        initDataTable();

        box = new DesignOptionBox(fieldTable);
        splitPane.getItems().add(box);

        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            var tab = tabPane.getTabs().get(index);
            if (index == 2) {
                var sql = getSql(getTableName(false));
                var formatSql = SQLFormatUtils.format(sql, DATABASE_SOURCE.getDatabaseType());
                sqlEditor.setText(formatSql);
            }
            if (index == 1) {
                var ua = tab.getUserData();
                if (ua == null) {
                    commentTextArea.textProperty().addListener((observable1, oldValue1, newValue1) -> fieldTable.tableCommentChange(oldValue1, newValue1));
                    tab.setUserData("COMMENT");
                }
            }
        });

        fieldTable.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            if (index == -1) {
                return;
            }
            var model = fieldTable.getItems().get(index);
            box.updateValue(model, index);
        }));

        //dynamic show/hide bottom DesignOptionBox
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            if (index == -1) {
                return;
            }
            var items = splitPane.getItems();
            if (index != 0 && items.size() > 1) {
                items.remove(1);
            }
            if (index == 0 && items.size() == 1) {
                items.add(box);
                splitPane.setDividerPositions(0.99);
            }
        });
    }

    private void initDataTable() {
        updateTableName();
        client = DATABASE_SOURCE.getClient(model.getUuid());
        if (model.getDesignTableType() == DesignTabModel.DesignTableType.UPDATE) {
            var future = client.getDql().showColumns(model.getScheme(), model.getTableName());
            //clear design table
            Platform.runLater(() -> fieldTable.getItems().clear());
            //load design table info
            var fut = future.compose(rs -> {
                fieldTable.flushTableColumnMeta(rs);
                return client.getDql().getCreateTableComment(model.getScheme(), model.getTableName());
            });
            fut.onComplete(ar -> {
                if (ar.failed()) {
                    DialogUtils.showErrorDialog(ar.cause(), i18nStr("controller.design.table.init.fail"));
                    return;
                }
                Platform.runLater(() -> commentTextArea.setText(ar.result()));
            });

        }
    }

    @FXML
    public void save() {
        var tableName = getTableName(true);
        if (StringUtils.isEmpty(tableName)) {
            return;
        }
        var sql = getSql(tableName);
        if (StringUtils.isEmpty(sql)) {
            return;
        }
        var future = client.execute(sql);
        future.onSuccess(ar -> {
            if (model.getDesignTableType() == DesignTabModel.DesignTableType.CREATE) {
                model.setDesignTableType(DesignTabModel.DesignTableType.UPDATE);
                model.setTableName(tableName);
                EventBusUtils.tableFolderFlushList(model.getUuid(), model.getScheme());
            }
            fieldTable.clearChange();
            //refresh data table
            initDataTable();
            DialogUtils.showNotification(i18nStr("controller.design.table.update.success"), Pos.TOP_CENTER, NotificationType.INFORMATION);
        });
        future.onFailure(t -> DialogUtils.showErrorDialog(t, i18nStr("controller.design.table.update.fail")));
    }

    @FXML
    public void createNewField() {
        var model = new DesignTableModel(TableColumnMeta.defaultMeta());
        var items = fieldTable.getItems();
        items.add(model);
        var index = items.size() - 1;
        //note this row code must place first row
        fieldTable.fieldChange(model.getMeta(), DesignTableOperationType.CREATE, index, null, "");
        //init property
        fieldTable.getSelectionModel().select(index);
    }

    @FXML
    public void deleteField() {
        var index = fieldTable.getSelectionModel().getSelectedIndex();
        if (index != -1) {
            //remove item from table
            var item = fieldTable.getItems().remove(index);
            fieldTable.deleteChange(item.getMeta(), DesignTableOperationSource.TABLE_FIELD, index);
        }
    }

    @FXML
    public void setPrimaryKey(ActionEvent event) {
        var index = fieldTable.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            return;
        }
        var item = fieldTable.getItems().get(index);
        var ab = !Boolean.parseBoolean(item.getPrimaryKey());
        //select key
        item.primaryKeyProperty().set(Boolean.valueOf(ab).toString());
    }

    private String getSql(String table) {
        return fieldTable.getSQLStatement(model.getDesignTableType(), model.getScheme(), table);
    }

    private String getTableName(boolean input) {
        var tableName = UN_TITLE;
        //Prompt user for table name
        if (StringUtils.isEmpty(model.getTableName()) && input) {
            tableName = DialogUtils.showInputDialog(i18nStr("controller.design.table.input"));
        } else {
            if (StringUtils.nonEmpty(model.getTableName())) {
                tableName = model.getTableName();
            }
        }
        return tableName;
    }

    private void updateTableName() {
        final String title;
        final String table = model.getTableName();
        if (StringUtils.isEmpty(table)) {
            title = UN_TITLE + "@" + model.getScheme();
        } else {
            title = table + "@" + model.getScheme();
        }

        Platform.runLater(() -> setText(title));
    }
}
