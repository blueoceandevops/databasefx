package com.openjfx.database.app.controls;

import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.model.DesignTableModel;
import com.openjfx.database.app.skin.TableDataViewSkin;
import com.openjfx.database.app.utils.TableCellUtils;
import com.openjfx.database.model.TableColumnMeta;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;

/**
 * Design table view
 *
 * @author yangkui
 * @since 1.0
 */
public class DesignTableView extends TableView<TableColumnMeta> {


    private final ObservableList<TableColumnMeta> metas = FXCollections.observableArrayList();

    private final TableColumn<TableColumnMeta, String> fieldColumn;
    private final TableColumn<TableColumnMeta, String> fileTypeColumn;
    private final TableColumn<TableColumnMeta, String> lengthColumn;
    private final TableColumn<TableColumnMeta, String> decimalColumn;
    private final TableColumn<TableColumnMeta, String> nullColumn;
    private final TableColumn<TableColumnMeta, String> keyColumn;
    private final TableColumn<TableColumnMeta, String> commentColumn;

    public DesignTableView() {
        setSortPolicy(e -> null);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        getSelectionModel().setCellSelectionEnabled(true);

        fieldColumn = createColumn("view.design.table.field.name", TableColumnMeta.TableColumnEnum.FIELD);
        fileTypeColumn = createColumn("view.design.table.field.type", TableColumnMeta.TableColumnEnum.TYPE);
        lengthColumn = createColumn("view.design.table.field.length", TableColumnMeta.TableColumnEnum.LENGTH);
        decimalColumn = createColumn("view.design.table.field.decimal", TableColumnMeta.TableColumnEnum.DECIMAL_POINT);
        nullColumn = createColumn("view.design.table.field.null", TableColumnMeta.TableColumnEnum.NULL);
        keyColumn = createColumn("view.design.table.field.key", TableColumnMeta.TableColumnEnum.PRIMARY_KEY);
        commentColumn = createColumn("view.design.table.field.comment", TableColumnMeta.TableColumnEnum.COMMENT);

        setEditable(true);
    }

    private TableColumn<TableColumnMeta, String> createColumn(String text, TableColumnMeta.TableColumnEnum columnEnum) {
        var column = new TableColumn<TableColumnMeta, String>(DatabaseFX.I18N.getString(text));
        column.setUserData(columnEnum);
        column.setCellFactory(param -> new CustomTableCell(columnEnum));
        column.setCellValueFactory(param -> {
            var tableColumn = param.getTableColumn();
            var meta = param.getValue();
            var tableColumnEnum = (TableColumnMeta.TableColumnEnum) tableColumn.getUserData();
            var str = switch (tableColumnEnum) {
                case PRIMARY_KEY -> meta.getPrimaryKey().toString();
                case NULL -> meta.getNull().toString();
                case TYPE -> meta.getType();
                case LENGTH -> meta.getLength();
                case DECIMAL_POINT -> meta.getDecimalPoint();
                case FIELD -> meta.getField();
                default -> meta.getComment();
            };
            return new SimpleStringProperty(str);
        });
        getColumns().add(column);
        return column;
    }

    public ObservableList<TableColumnMeta> getMetas() {
        return metas;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableDataViewSkin(this);
    }

    private static class CustomTableCell extends TableCell<TableColumnMeta, String> {


        private final HBox graphic = new HBox();

        public CustomTableCell(TableColumnMeta.TableColumnEnum columnEnum) {
            var list = DATABASE_SOURCE.getDataType().getDataTypeList();
            var items = FXCollections.observableArrayList(list);
            var textField = new TextField();
            var checkBox = new CheckBox();
            var typeBox = new EditChoiceBox<>();
            graphic.setAlignment(Pos.CENTER);
            HBox.setHgrow(typeBox, Priority.ALWAYS);
            typeBox.getItems().addAll(items);
            switch (columnEnum) {
                case PRIMARY_KEY, NULL -> graphic.getChildren().add(checkBox);
                case TYPE -> graphic.getChildren().add(typeBox);
                default -> graphic.getChildren().add(textField);
            }
            //listener mouse enter change table select row
            graphic.getChildren().get(0).addEventFilter(MouseEvent.MOUSE_ENTERED, e -> {
                var index = getTableRow().getIndex();
                var tableView = getTableView();
                var curIndex = tableView.getSelectionModel().getSelectedIndex();
                if (index != curIndex) {
                    tableView.getSelectionModel().select(index);
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }
            setGraphicValue(item);
        }

        public void setGraphicValue(String item) {
            var node = graphic.getChildren().get(0);
            if (node instanceof CheckBox) {
                var select = Boolean.parseBoolean(item);
                ((CheckBox) node).setSelected(select);
            } else if (node instanceof EditChoiceBox) {
                ((EditChoiceBox) node).setText(item);
            } else {
                ((TextField) node).setText(item);
            }
            if (getGraphic() == null) {
                setText(null);
                setGraphic(graphic);
            }
        }
    }
}
