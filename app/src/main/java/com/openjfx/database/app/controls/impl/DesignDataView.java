package com.openjfx.database.app.controls.impl;

import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.controls.DataView;
import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.model.DesignTableModel;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.enums.DesignTableOperationSource;
import com.openjfx.database.enums.DesignTableOperationType;
import com.openjfx.database.model.ColumnChangeModel;
import com.openjfx.database.model.RowChangeModel;
import com.openjfx.database.model.TableColumnMeta;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;

/**
 * Design table view
 *
 * @author yangkui
 * @since 1.0
 */
public class DesignDataView extends DataView<DesignTableModel> {
    /**
     * cached all field change
     */
    private final List<RowChangeModel> changeModels = new ArrayList<>();
    /**
     * table column meta list
     */
    private final List<TableColumnMeta> metas = new ArrayList<>();

    public DesignDataView() {
        setAutoColumnWidth(false);
        setShowLineNumber(false);
        createColumn("view.design.table.field.name", TableColumnMeta.TableColumnEnum.FIELD);
        createColumn("view.design.table.field.type", TableColumnMeta.TableColumnEnum.TYPE);
        createColumn("view.design.table.field.length", TableColumnMeta.TableColumnEnum.LENGTH);
        createColumn("view.design.table.field.decimal", TableColumnMeta.TableColumnEnum.DECIMAL_POINT);
        createColumn("view.design.table.field.null", TableColumnMeta.TableColumnEnum.NULL);
        createColumn("view.design.table.field.key", TableColumnMeta.TableColumnEnum.PRIMARY_KEY);
        createColumn("view.design.table.field.comment", TableColumnMeta.TableColumnEnum.COMMENT);
    }

    private void createColumn(String text, TableColumnMeta.TableColumnEnum columnEnum) {
        var column = new TableColumn<DesignTableModel, String>(DatabaseFX.I18N.getString(text));
        column.setUserData(columnEnum);
        column.setCellFactory(param -> new CustomTableCell(columnEnum));
        column.setCellValueFactory(param -> {
            var tableColumn = param.getTableColumn();
            var model = param.getValue();
            var tableColumnEnum = (TableColumnMeta.TableColumnEnum) tableColumn.getUserData();
            return switch (tableColumnEnum) {
                case PRIMARY_KEY -> model.primaryKeyProperty();
                case NULL -> model.nullableProperty();
                case TYPE -> model.typeProperty();
                case LENGTH -> model.lengthProperty();
                case DECIMAL_POINT -> model.decimalPointProperty();
                case FIELD -> model.fieldProperty();
                default -> model.commentProperty();
            };
        });
        final double prop;
        if (columnEnum != TableColumnMeta.TableColumnEnum.COMMENT) {
            prop = 0.13;
        } else {
            prop = 0.2;
        }
        column.prefWidthProperty().bind(widthProperty().multiply(prop));
        getColumns().add(column);
    }

    /**
     * table field change
     *
     * @param meta            table column meta
     * @param type            operation type
     * @param rowIndex        row index
     * @param tableColumnEnum column property
     * @param newValue        change value
     */
    public void fieldChange(TableColumnMeta meta, DesignTableOperationType type, int rowIndex, TableColumnMeta.TableColumnEnum tableColumnEnum, String newValue) {
        var realRowIndex = getRealRowIndex(rowIndex, DesignTableOperationSource.TABLE_FIELD);
        var optional = changeModels.stream()
                .filter(rowChangeModel -> rowChangeModel.getSource() == DesignTableOperationSource.TABLE_FIELD)
                .filter(rowChangeModel -> rowChangeModel.getRowIndex() == realRowIndex)
                .findAny();
        final RowChangeModel row;
        //row already exist
        if (optional.isPresent()) {
            row = optional.get();
            var optional1 = row.getColumn(tableColumnEnum);
            if (optional1.isPresent()) {
                var add = checkFieldValue(tableColumnEnum, meta, newValue);
                var col = optional1.get();
                if (add) {
                    col.setNewValue(newValue);
                } else {
                    row.getColumnChangeModels().remove(col);
                }
            } else {
                createColumnChange(row, tableColumnEnum, meta, newValue);
            }
        } else {
            var columns = new ArrayList<ColumnChangeModel>();
            row = new RowChangeModel(realRowIndex, type, DesignTableOperationSource.TABLE_FIELD, columns, meta);
            if (tableColumnEnum != null) {
                boolean add = checkFieldValue(tableColumnEnum, meta, newValue);
                if (add) {
                    createColumnChange(row, tableColumnEnum, meta, newValue);
                    changeModels.add(row);
                }
            } else {
                changeModels.add(row);
            }
        }
        if (row.getOperationType() == DesignTableOperationType.UPDATE && row.getColumnChangeModels().isEmpty()) {
            changeModels.remove(row);
        }
    }

    /**
     * create column change
     *
     * @param row             row
     * @param tableColumnEnum tableColumnEnum
     * @param meta            meta
     * @param newValue        newValue
     */
    private void createColumnChange(RowChangeModel row, TableColumnMeta.TableColumnEnum tableColumnEnum, TableColumnMeta meta, String newValue) {
        var col = new ColumnChangeModel();
        col.setFieldName(tableColumnEnum);
        col.setOriginValue(getMetaValue(meta, tableColumnEnum));
        col.setNewValue(newValue);
        row.getColumnChangeModels().add(col);
    }

    /**
     * check column change
     *
     * @param tableColumnEnum change field
     * @param meta            table row meta data
     * @param newValue        new value
     * @return return create column result
     */
    private boolean checkFieldValue(TableColumnMeta.TableColumnEnum tableColumnEnum, TableColumnMeta meta, String newValue) {
        var a = tableColumnEnum == TableColumnMeta.TableColumnEnum.DEFAULT || tableColumnEnum == TableColumnMeta.TableColumnEnum.COMMENT;
        var add = true;
        var fieldVal = getMetaValue(meta, tableColumnEnum);
        if (a) {
            if (fieldVal == null) {
                add = !(newValue == null || "".equals(newValue));
            } else {
                add = !fieldVal.equals(newValue);
            }
        } else if (fieldVal != null && fieldVal.equals(newValue)) {
            add = false;
        }
        return add;
    }

    private String getMetaValue(TableColumnMeta meta, TableColumnMeta.TableColumnEnum tableColumnEnum) {
        var obj = meta.getFieldValue(tableColumnEnum);
        return obj == null ? null : obj.toString();
    }

    /**
     * delete change row
     *
     * @param source   operation source
     * @param meta     table column meta
     * @param rowIndex row index
     */
    public void deleteChange(TableColumnMeta meta, DesignTableOperationSource source, int rowIndex) {
        var realRowIndex = getRealRowIndex(rowIndex, source);
        var optional = changeModels.stream()
                .filter(rowChangeModel -> rowChangeModel.getSource() == source)
                .filter(rowChangeModel -> rowChangeModel.getRowIndex() == realRowIndex)
                .findAny();
        if (optional.isEmpty()) {
            var rowChange = new RowChangeModel(realRowIndex, DesignTableOperationType.DELETE, source, List.of(), meta);
            changeModels.add(rowChange);
        } else {
            var row = optional.get();
            //if the row is new create execute delete the row
            if (row.getOperationType() == DesignTableOperationType.CREATE) {
                changeModels.remove(row);
            }
        }
    }

    /**
     * refresh table column
     *
     * @param metas table column meta
     */
    public void flushTableColumnMeta(List<TableColumnMeta> metas) {
        var list = metas.stream().map(DesignTableModel::new).collect(Collectors.toList());
        this.metas.clear();
        this.metas.addAll(metas);
        Platform.runLater(() -> {
            getItems().clear();
            getItems().addAll(list);
            if (getItems().size() > 0) {
                getSelectionModel().select(0);
            }
        });
    }

    public void tableCommentChange(final String original, final String comment) {
        var optional = changeModels.stream()
                .filter(rowChangeModel -> rowChangeModel.getSource() == DesignTableOperationSource.TABLE_COMMENT)
                .findAny();
        if (optional.isEmpty()) {
            var column = new ColumnChangeModel(TableColumnMeta.TableColumnEnum.COMMENT);
            column.setOriginValue(original);
            column.setNewValue(comment);
            var row = new RowChangeModel(9999,
                    DesignTableOperationType.UPDATE, DesignTableOperationSource.TABLE_COMMENT, List.of(column), null);
            changeModels.add(row);
        } else {
            var row = optional.get();
            var optional1 = row.getColumn(TableColumnMeta.TableColumnEnum.COMMENT);
            if (optional1.isEmpty()) {
                return;
            }
            var column = optional1.get();
            if (comment.equals(column.getOriginValue())) {
                changeModels.remove(row);
            } else {
                column.setNewValue(comment);
            }
        }
    }

    public String getSQLStatement(DesignTabModel.DesignTableType type, String scheme, String table) {
        if (changeModels.isEmpty()) {
            return "";
        }
        var generator = DATABASE_SOURCE.getGenerator();
        final String sql;
        if (type == DesignTabModel.DesignTableType.CREATE) {
            sql = generator.createTable(scheme, table, changeModels);
        } else {
            sql = generator.updateTable(scheme, table, changeModels, metas);
        }
        return sql;
    }

    /**
     * Exclude the deleted fields and get the real index location
     *
     * @param rowIndex table row index
     * @return real row index
     */
    public int getRealRowIndex(final int rowIndex, DesignTableOperationSource source) {
        var list = changeModels.stream()
                .filter(rowChangeModel -> rowChangeModel.getSource() == source)
                .filter(row -> row.getRowIndex() < rowIndex && row.getOperationType() == DesignTableOperationType.DELETE)
                .collect(Collectors.toList());
        return rowIndex + list.size();
    }

    /**
     * clear cached change
     */
    public void clearChange() {
        this.changeModels.clear();
    }

    private class CustomTableCell extends TableCell<DesignTableModel, String> {

        private final HBox graphic = new HBox();
        private final TableColumnMeta.TableColumnEnum columnEnum;

        public CustomTableCell(TableColumnMeta.TableColumnEnum columnEnum) {
            this.columnEnum = columnEnum;
            var list = DATABASE_SOURCE.getDataType().getDataTypeList();
            var items = FXCollections.observableArrayList(list);
            var textField = new TextField();
            var checkBox = new CheckBox();
            var typeBox = new EditChoiceBox<>();
            graphic.setAlignment(Pos.CENTER);
            HBox.setHgrow(textField, Priority.ALWAYS);
            typeBox.getItems().addAll(items);
            switch (columnEnum) {
                case PRIMARY_KEY, NULL -> graphic.getChildren().add(checkBox);
                case TYPE -> {
                    graphic.getChildren().add(typeBox);
                    typeBox.prefWidthProperty().bind(graphic.widthProperty());
                }
                default -> graphic.getChildren().add(textField);
            }
            //listener mouse enter change table select row
            graphic.getChildren().get(0).addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
                var index = getTableRow().getIndex();
                var tableView = getTableView();
                var curIndex = tableView.getSelectionModel().getSelectedIndex();
                if (index != curIndex) {
                    tableView.getSelectionModel().select(index);
                }
            });
            textField.textProperty().addListener(valueChange());
            typeBox.textProperty().addListener(valueChange());
            checkBox.selectedProperty().addListener(valueChange());
        }

        private <T> ChangeListener<T> valueChange() {
            return (observable, oldValue, newValue) -> {
                var index = getTableRow().getIndex();
                if (index == -1) {
                    return;
                }
                var column = getTableColumn();
                var item = getTableView().getItems().get(index);
                var columnMeta = (TableColumnMeta.TableColumnEnum) column.getUserData();
                var fileValue = (newValue == null ? null : newValue.toString());
                DesignDataView.this.fieldChange(item.getMeta(), DesignTableOperationType.UPDATE, index, columnMeta, fileValue);
                item.setValue(columnEnum, fileValue);
            };
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
            if (getGraphic() == null) {
                setText(null);
                setGraphic(graphic);
            }
            var node = graphic.getChildren().get(0);
            if (node instanceof CheckBox) {
                var select = Boolean.parseBoolean(item);
                ((CheckBox) node).setSelected((select));
            } else if (node instanceof EditChoiceBox) {
                ((EditChoiceBox) node).setText(item);
            } else {
                var field = (TextField) node;
                var carePos = field.getCaretPosition();
                field.setText(item);
                field.positionCaret(carePos);
            }
        }
    }
}
