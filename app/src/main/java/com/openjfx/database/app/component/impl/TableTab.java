package com.openjfx.database.app.component.impl;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.openjfx.database.DML;
import com.openjfx.database.TableColumnMetaHelper;
import com.openjfx.database.app.TableDataHelper;
import com.openjfx.database.app.component.BaseTab;
import com.openjfx.database.app.component.SearchPopup;
import com.openjfx.database.app.controls.TableDataCell;
import com.openjfx.database.app.controls.TableDataView;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.model.TableSearchResultModel;
import com.openjfx.database.app.model.impl.TableTabModel;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.TableColumnUtils;
import com.openjfx.database.app.utils.TableDataUtils;
import com.openjfx.database.base.AbstractDataBasePool;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.model.TableColumnMeta;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.*;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.utils.AssetUtils.getLocalImage;
import static com.openjfx.database.common.config.StringConstants.NULL;


/**
 * table tab
 *
 * @author yangkui
 * @since 1.0
 */
public class TableTab extends BaseTab<TableTabModel> {
    private static final double ICON_WIDTH = 0x14;
    private static final double ICON_HEIGHT = 0x14;

    private static final Image ADD_DATA_ICON = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "add_data.png");
    private static final Image FLUSH_ICON = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "flush_icon.png");
    private static final Image NEXT_ICON = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "next_icon.png");
    private static final Image LAST_ICON = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "last_icon.png");
    private static final Image SUBMIT_ICON = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "save_icon.png");
    private static final Image DELETE_ICON = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "delete_icon.png");
    private static final Image FLAG_IMAGE = getLocalImage(ICON_WIDTH, ICON_HEIGHT, "point.png");

    private static final String STYLE_SHEETS = "css/table_tab.css";

    private final AbstractDataBasePool pool;
    private final Label flag = new Label();

    private final BorderPane borderPane = new BorderPane();
    private final TableDataView tableView = new TableDataView();

    private final HBox bottomBox = new HBox();

    private int pageIndex = 1;
    private int pageSize = 100;

    private final JFXButton addData = new JFXButton();
    private final JFXButton flush = new JFXButton();
    private final JFXButton next = new JFXButton();
    private final JFXButton last = new JFXButton();
    private final JFXButton submit = new JFXButton();
    private final JFXButton delete = new JFXButton();
    private final JFXTextField numberTextField = new JFXTextField(String.valueOf(pageSize));

    private final List<TableColumnMeta> metas = new ArrayList<>();
    private final Label totalLabel = new Label("共0行");
    private final Label indexCounter = new Label();
    private final Label pageCounter = new Label();

    private final SearchPopup searchPopup = SearchPopup.complexPopup();

    private final List<TableSearchResultModel> searchList = new ArrayList<>();


    private TableColumnMeta keyMeta = null;

    public TableTab(TableTabModel model) {
        super(model);
        pool = DATABASE_SOURCE.getDataBaseSource(model.getUuid());
    }


    public void init() {
        flag.setGraphic(new ImageView(FLAG_IMAGE));
        addData.setGraphic(new ImageView(ADD_DATA_ICON));
        flush.setGraphic(new ImageView(FLUSH_ICON));
        next.setGraphic(new ImageView(NEXT_ICON));
        last.setGraphic(new ImageView(LAST_ICON));
        submit.setGraphic(new ImageView(SUBMIT_ICON));
        delete.setGraphic(new ImageView(DELETE_ICON));

        addData.setTooltip(new Tooltip("新增数据"));
        flush.setTooltip(new Tooltip("刷新"));
        submit.setTooltip(new Tooltip("保存更改"));
        delete.setTooltip(new Tooltip("删除"));


        var lBox = new HBox();
        var rBox = new HBox();
        var rrBox = new HBox();

        lBox.getChildren().addAll(addData, delete, submit);
        rrBox.getChildren().addAll(indexCounter, totalLabel, pageCounter);
        rBox.getChildren().addAll(rrBox, last, next, numberTextField, flush);

        HBox.setHgrow(rBox, Priority.ALWAYS);


        bottomBox.getChildren().addAll(lBox, rBox);
        rrBox.getStyleClass().add("rrbox");
        bottomBox.getStyleClass().add("bottom-box");

        flush.setOnAction(e -> checkChange(true));

        borderPane.getStylesheets().add(STYLE_SHEETS);

        submit.setOnAction(e -> checkChange(false));


        tableView.changeStatusProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.nonNull(newValue) && newValue) {
                setGraphic(flag);
            } else {
                setGraphic(null);
            }
        });

        numberTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                var text = numberTextField.getText();
                if (StringUtils.isEmpty(text)) {
                    numberTextField.setText(String.valueOf(pageSize));
                } else {
                    pageSize = Integer.parseInt(text);
                }
            }
        });

        next.setOnAction(e -> {
            pageIndex++;
            checkChange(true);
        });

        last.setOnAction(e -> {
            if (pageIndex > 1) {
                pageIndex--;
                checkChange(true);
            }
        });

        addData.setOnAction(e -> {
            if (updated()) {
                return;
            }
            var newData = FXCollections.<StringProperty>observableArrayList();
            metas.forEach(meta -> newData.add(new SimpleStringProperty(NULL)));
            tableView.addNewRow(newData);
            tableView.scrollTo(newData);
            tableView.getSelectionModel().select(newData);
        });

        delete.setOnAction(e -> {
            if (updated()) {
                return;
            }
            var selectIndex = tableView.getSelectionModel().getSelectedIndex();
            if (selectIndex == -1) {
                return;
            }
            var item = tableView.getSelectionModel().getSelectedItem();
            tableView.addDeleteItem(item);
        });

        //register shortcuts
        getTabPane().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            var tabPane = getTabPane();
            if (Objects.isNull(tabPane)) {
                return;
            }

            //If the changed tab is not the currently selected tab, it will not be changed
            var selectItem = tabPane.getSelectionModel();
            if (Objects.isNull(selectItem) || selectItem.getSelectedItem() != this) {
                return;
            }

            //fire sve event
            if (event.isControlDown() && event.getCode() == KeyCode.S) {
                event.consume();
                checkChange(false);
            }
            //search data in current table
            if (event.isControlDown() && event.getCode() == KeyCode.F && !tableView.getItems().isEmpty()) {
                borderPane.setTop(searchPopup);
            }
        });

        tableView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            if (index == -1) {
                indexCounter.setText("");
            } else {
                indexCounter.setText("第" + (index + 1) + "条记录");
            }
        });

        searchPopup.textChange(keyword -> {
            var items = tableView.getItems();
            var temp = TableDataUtils.findWithStr(items, keyword);
            searchList.clear();
            searchList.addAll(temp);
            //select first a cell
            if (searchList.size() > 0) {
                searchPopup.setIndexProperty(0);
            }
            return searchList.size();
        });

        searchPopup.indexPropertyProperty().addListener((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            if (index == -1) {
                return;
            }
            var model = searchList.get(index);
            var columns = tableView.getColumns();
            var column = columns.get(model.getColumnIndex());
            //scroll target row
            tableView.scrollTo(model.getRowIndex());
            //scroll target column
            tableView.scrollToColumnIndex(model.getColumnIndex());
            //select target column
            tableView.getSelectionModel().select(model.getRowIndex(), column);
        });

        borderPane.setCenter(tableView);
        borderPane.setBottom(bottomBox);

        setContent(borderPane);
        initTable();
    }

    private void initTable() {
        var future = loadTableMeta().compose(v -> loadData()).compose(v -> countDataNumber());
        future.onSuccess(v -> {
            Platform.runLater(() -> {
                tableView.refresh();
                tableView.resetChange();
            });
        });
        future.onFailure(t -> DialogUtils.showErrorDialog(t, "加载数据失败"));
    }


    private Future<Void> loadTableMeta() {
        var promise = Promise.<Void>promise();
        var future = pool.getDql().showColumns(model.getTable());
        future.onSuccess(metas ->
        {
            if (this.metas.size() > 0) {
                this.metas.clear();
            }
            this.metas.addAll(metas);

            //create column
            var columns = TableColumnUtils.createTableDataColumn(metas);
            Platform.runLater(() -> {
                tableView.getColumns().clear();
                tableView.getColumns().addAll(columns);
            });
            //get key
            var optional = TableColumnMetaHelper.getTableKey(metas);
            if (optional.isPresent()) {
                tableView.setEditable(true);
                keyMeta = optional.get();
            }
            promise.complete();
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    /**
     * Paging load data
     */
    private Future<Void> loadData() {
        Platform.runLater(() -> tableView.getItems().clear());
        var promise = Promise.<Void>promise();
        var future = pool.getDql().query(model.getTable(), pageIndex, pageSize);
        future.onSuccess(rs -> {
            var list = FXCollections.<ObservableList<StringProperty>>observableArrayList();
            for (var values : rs) {
                var item = FXCollections.<StringProperty>observableArrayList();
                for (var val : values) {
                    item.add(new SimpleStringProperty(val));
                }
                list.add(item);
            }

            Platform.runLater(() -> {
                tableView.getItems().addAll(list);
                pageCounter.setText("于第" + pageIndex + "页");
            });
            promise.complete();
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    /**
     * Save changes
     *
     * @param isLoading Whether to refresh the current data list true false do not refresh
     */
    private void checkChange(boolean isLoading) {
        if (!tableView.isChangeStatus()) {
            initTable();
            return;
        }
        var result = DialogUtils.showAlertConfirm("检测到数据已经更新是否同步到数据库?");
        // Synchronous data change to database
        if (result) {
            var dml = DATABASE_SOURCE.getDataBaseSource(model.getUuid()).getDml();
            var future = newData(dml).compose(rs -> updateData(dml)).compose(rs -> deleteData(dml));
            future.onSuccess(rs -> {
                Platform.runLater(() -> {
                    countDataNumber();
                    tableView.resetChange();
                    tableView.refresh();
                    if (isLoading) {
                        initTable();
                    }
                });
            });
            future.onFailure(t -> DialogUtils.showErrorDialog(t, "更新失败"));
        } else {
            tableView.resetChange();
            initTable();
        }
    }

    /**
     * New data
     *
     * @param dml dml
     * @return Return new results
     */
    private Future<Integer> newData(DML dml) {
        var newRows = tableView.getNewRows();

        List<Future> futures = new ArrayList<>();

        for (ObservableList<StringProperty> newRow : newRows) {
            var columns = TableDataHelper.fxPropertyToObject(newRow);
            var future = dml.insert(metas, columns, model.getTable());
            var optional = dml.getAutoIncreaseField(metas);
            if (optional.isPresent()) {
                int i = metas.indexOf(optional.get());
                //Auto increment ID of callback processing after success
                future.setHandler(ar -> {
                    Platform.runLater(() -> {
                        int index = tableView.getItems().indexOf(newRow);
                        if (index != -1) {
                            var item = tableView.getItems().get(index);
                            item.set(i, new SimpleStringProperty(String.valueOf(ar.result())));
                        }
                    });
                });
            }
            futures.add(future);
        }
        var promise = Promise.<Integer>promise();
        var fut = CompositeFuture.all(futures);
        fut.setHandler(ar -> {
            if (ar.succeeded()) {
                promise.complete(futures.size());
                return;
            }
            promise.fail(ar.cause());
        });
        return promise.future();
    }

    /**
     * More detailed data
     *
     * @param dml dml
     * @return Return update results
     */
    private Future<Integer> updateData(DML dml) {
        var change = tableView.getChangeModes();
        //Update data
        if (change.size() > 0) {
            int keyIndex = metas.indexOf(keyMeta);

            //Due to asynchrony, you may only need to update in batch, but not in single update
            var values = TableDataHelper.getChangeValue(change, metas, keyIndex, tableView.getItems());
            //Update data asynchronously
            return dml.batchUpdate(values, model.getTable(), metas);
        }
        return Future.succeededFuture();
    }

    private Future<Integer> deleteData(DML dml) {
        var list = tableView.getDeletes();
        if (!list.isEmpty()) {
            var index = metas.indexOf(keyMeta);
            var keys = list.stream()
                    .map(it -> it.get(index))
                    .map(TableDataHelper::singleFxPropertyToObject)
                    .toArray();
            return dml.batchDelete(keyMeta, keys, model.getTable());
        }
        return Future.succeededFuture();
    }


    private Future<Void> countDataNumber() {
        var promise = Promise.<Void>promise();
        var future = pool.getDql().count(model.getTable());
        future.onSuccess(number -> {
            Platform.runLater(() -> totalLabel.setText("(共" + number + "行)"));
            promise.complete();
        });
        future.onFailure(promise::fail);
        return promise.future();
    }

    /**
     * Determine whether the current table can be updated
     *
     * @return True can update false can not update
     */
    private boolean updated() {
        if (Objects.isNull(keyMeta)) {
            DialogUtils.showNotification("当前设计表无Key,无法进行更新操作", Pos.TOP_CENTER, NotificationType.WARNING);
            return true;
        }
        return false;
    }

    /**
     * Dynamically update tab value
     *
     * @param t Show full table name or not serverName+'/'+tableName
     */
    public void updateValue(boolean t) {
        if (Objects.nonNull(getText())) {
            var a = getText().contains("/");
            var b = !t && !a || t && a;
            if (b) {
                return;
            }
        }
        var name = model.getTableName();
        if (t) {
            name = model.getServerName() + "/" + name;
        }
        setText(name);
        setTooltip(new Tooltip(name));
    }
}
