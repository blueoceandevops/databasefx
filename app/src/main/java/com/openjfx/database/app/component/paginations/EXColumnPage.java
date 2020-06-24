package com.openjfx.database.app.component.paginations;

import com.openjfx.database.app.controls.DataView;
import com.openjfx.database.app.model.EXModel;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.model.TableColumnMeta;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.stream.Collectors;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;

/**
 * export wizard select column page
 *
 * @author yangkui
 * @since 1.0
 */
public class EXColumnPage extends BorderPane {

    private final FieldTableView tableView;


    public EXColumnPage(EXModel model) {
        this.tableView = new FieldTableView(model);
        setCenter(tableView);
        initTableColumn(model);
        getStyleClass().add("export-wizard-select-column-page");
    }

    private void initTableColumn(EXModel model) {
        var client = DATABASE_SOURCE.getClient(model.getUuid());
        var dql = client.getDql();
        var future = dql.showColumns(model.getScheme(), model.getTable());
        future.onSuccess(ar -> {
            var items = ar.stream().map(FieldTableModel::new).collect(Collectors.toList());
            Platform.runLater(() -> {
                tableView.getItems().clear();
                tableView.getItems().addAll(items);
                tableView.selectAll();
            });
        });
        future.onFailure(t -> DialogUtils.showErrorDialog(t, "获取表列数据失败"));
    }


    private static class FieldTableView extends DataView<FieldTableModel> {

        private final CheckBox checkAll;

        private final EXModel model;

        public FieldTableView(EXModel model) {
            setShowLineNumber(false);
            setAutoColumnWidth(false);

            this.model = model;
            this.checkAll = new CheckBox();

            createTableColumn(CTableColumn.SELECT);
            createTableColumn(CTableColumn.FIELD);
            createTableColumn(CTableColumn.ALIAS);

            checkAll.selectedProperty().addListener((observable, oldValue, newValue) -> {
                var items = getItems();
                for (FieldTableModel item : items) {
                    item.setSelected(newValue.toString());
                }
            });
            getStyleClass().add("field-table-view");
        }

        public void selectAll() {
            checkAll.setSelected(true);
        }

        private void createTableColumn(CTableColumn cTableColumn) {
            final TableColumn<FieldTableModel, String> column;
            switch (cTableColumn) {
                case ALIAS -> {
                    column = new TableColumn<>("别名");
                }
                case FIELD -> {
                    column = new TableColumn<>("字段");
                }
                default -> {
                    column = new TableColumn<>();
                    var box = new HBox();
                    box.setAlignment(Pos.CENTER);
                    box.getChildren().add(checkAll);
                    column.setGraphic(box);
                }
            }
            column.setUserData(cTableColumn);
            if (cTableColumn == CTableColumn.SELECT) {
                column.prefWidthProperty().bind(this.widthProperty().multiply(0.1));
            } else {
                column.prefWidthProperty().bind(this.widthProperty().multiply(0.4));
            }
            column.setCellValueFactory(param -> {
                var col = param.getTableColumn();
                var cc = (CTableColumn) col.getUserData();
                var model = param.getValue();
                return switch (cc) {
                    case SELECT -> model.selectedProperty();
                    case FIELD -> model.fieldProperty();
                    default -> model.aliasProperty();
                };
            });
            column.setCellFactory(t -> new CTableCell(cTableColumn));
            getColumns().add(column);
        }
    }

    public static class FieldTableModel {
        /**
         * field name
         */
        private final StringProperty selected;
        private final StringProperty field;
        private final StringProperty alias;
        private final TableColumnMeta meta;

        public FieldTableModel(TableColumnMeta meta) {
            this.meta = meta;
            this.selected = new SimpleStringProperty("false");
            this.field = new SimpleStringProperty(meta.getField());
            this.alias = new SimpleStringProperty("");
        }

        public String getSelected() {
            return selected.get();
        }

        public StringProperty selectedProperty() {
            return selected;
        }

        public void setSelected(String selected) {
            this.selected.set(selected);
        }

        public String getField() {
            return field.get();
        }

        public StringProperty fieldProperty() {
            return field;
        }

        public void setField(String field) {
            this.field.set(field);
        }

        public String getAlias() {
            return alias.get();
        }

        public StringProperty aliasProperty() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias.set(alias);
        }

        public TableColumnMeta getMeta() {
            return meta;
        }
    }

    private static class CTableCell extends TableCell<FieldTableModel, String> {

        private final HBox graphic = new HBox();
        private final CTableColumn cTableColumn;

        public CTableCell(CTableColumn cTableColumn) {
            this.cTableColumn = cTableColumn;

            var label = new Label();
            var checkBox = new CheckBox();
            var textField = new TextField();
            HBox.setHgrow(textField, Priority.ALWAYS);

            switch (cTableColumn) {
                case SELECT -> {
                    graphic.getChildren().add(checkBox);
                    graphic.setAlignment(Pos.CENTER);
                }
                case FIELD -> graphic.getChildren().add(label);
                case ALIAS -> graphic.getChildren().add(textField);
            }
            getStyleClass().add("c-table-cell");
            textField.textProperty().addListener(valueChange());
            checkBox.selectedProperty().addListener(valueChange());
        }

        private <T> ChangeListener<T> valueChange() {
            return ((observable, oldValue, newValue) -> {
                var tableView = (FieldTableView) getTableView();
                var tableRow = getTableRow();
                if (tableView == null || tableRow == null) {
                    return;
                }
                var rowIndex = tableRow.getIndex();
                var item = tableView.getItems().get(rowIndex);
                if (cTableColumn == CTableColumn.SELECT) {
                    var select = (Boolean) newValue;
                    item.setSelected(select.toString());
                    var selectColumns = tableView.model.getSelectTableColumn();
                    if (select) {
                        selectColumns.add(item);
                    } else {
                        selectColumns.remove(item);
                    }
                } else {
                    item.setAlias((String) newValue);
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty) {
                setGraphic(null);
                return;
            }
            updateValue(item);
        }

        private void updateValue(String item) {
            var node = graphic.getChildren().get(0);
            if (cTableColumn == CTableColumn.SELECT) {
                ((CheckBox) node).setSelected(Boolean.parseBoolean(item));
            } else if (cTableColumn == CTableColumn.ALIAS) {
                var textField = (TextField) node;
                var pos = textField.getCaretPosition();
                textField.setText(item);
                textField.positionCaret(pos);
            } else {
                var label = (Label) node;
                label.setText(item);
            }
            if (getGraphic() == null) {
                setGraphic(graphic);
            }
        }
    }

    private enum CTableColumn {
        /**
         * selected?
         */
        SELECT,
        /**
         * field name
         */
        FIELD,
        /**
         * field alias
         */
        ALIAS
    }

}
