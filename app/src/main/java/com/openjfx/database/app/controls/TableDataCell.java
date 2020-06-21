package com.openjfx.database.app.controls;

import com.openjfx.database.app.controls.impl.TableDataView;
import com.openjfx.database.app.model.TableDataChangeMode;
import com.openjfx.database.app.utils.TableCellUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.openjfx.database.common.config.StringConstants.NULL;


/**
 * table cell
 *
 * @author yangkui
 * @since 1.0
 */
public class TableDataCell extends TableCell<ObservableList<StringProperty>, String> {

    private TableTextField textField;

    /**
     * Null style
     */
    private final String NULL_STYLE = "null-style";
    /**
     * Value change style
     */
    private final String CHANGE_STYLE = "change-style";

    {
        //Do not wrap to prevent text from being too long
        setWrapText(false);

        addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            var tableView = getTableView();
            if (tableView == null) {
                return;
            }
            var tableRow = getTableRow();
            var rowIndex = tableRow.getIndex();
            tableView.getSelectionModel().setCellSelectionEnabled(true);
            tableView.getSelectionModel().select(rowIndex, getTableColumn());
        });
    }


    private final ObjectProperty<StringConverter<String>> converter = new SimpleObjectProperty<>();

    public TableDataCell(StringConverter<String> converter) {
        this.setConverter(converter);
    }

    public StringConverter<String> getConverter() {
        return converter.get();
    }

    public ObjectProperty<StringConverter<String>> converterProperty() {
        return converter;
    }

    public void setConverter(StringConverter<String> converter) {
        this.converter.set(converter);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || Objects.isNull(item) || Objects.isNull(getTableView()) || Objects.isNull(getTableRow())) {
            setText(null);
            setGraphic(null);
            getStyleClass().removeAll(NULL_STYLE, CHANGE_STYLE);
            return;
        }
        TableCellUtils.updateItem(this, textField);

        if (item.equals(NULL)) {
            if (!getStyleClass().contains(NULL_STYLE)) {
                getStyleClass().add(NULL_STYLE);
            }
        } else {
            getStyleClass().remove(NULL_STYLE);
        }

        var dataView = (TableDataView) getTableView();

        int rowIndex = getTableRow().getIndex();
        int colIndex = getTableView().getColumns().indexOf(getTableColumn()) - dataView.getColumnOffset();
        var optional = dataView.getChangeModel(rowIndex, colIndex);
        optional.ifPresentOrElse(t -> {
            if (!getStyleClass().contains(CHANGE_STYLE)) {
                getStyleClass().add(CHANGE_STYLE);
            }
        }, () -> getStyleClass().remove(CHANGE_STYLE));
        setText(item);
    }

    @Override
    public void startEdit() {

        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }

        super.startEdit();

        if (isEditing()) {
            var column = (TableDataColumn) getTableColumn();
            if (textField == null) {
                textField = TableCellUtils.createTextField(this, column.getMeta());
            }
            TableCellUtils.startEdit(this, textField);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        TableCellUtils.cancelEdit(this, null);
    }

    @Override
    public void commitEdit(String newValue) {

        var oldValue = getItem();
        var rowIndex = getTableRow().getIndex();
        var dataView = (TableDataView) getTableView();
        var colIndex = dataView.getEditingCell().getColumn() - dataView.getColumnOffset();

        //Value change
        if (!oldValue.equals(newValue)) {

            var optional = dataView.getChangeModel(rowIndex, colIndex);

            if (optional.isEmpty()) {
                var model = new TableDataChangeMode();
                model.setRowIndex(rowIndex);
                model.setColumnIndex(colIndex);
                model.setOriginalData(oldValue);
                model.setChangeData(newValue);
                dataView.addChangeMode(model);
            } else {
                var model = optional.get();
                if (model.getOriginalData().equals(newValue)) {
                    dataView.removeChange(model);
                } else {
                    //Value update
                    model.setChangeData(newValue);
                }
            }
        }
        super.commitEdit(newValue);
    }

    public static Callback<TableColumn<ObservableList<StringProperty>, String>, TableCell<ObservableList<StringProperty>, String>> forTableColumn() {
        return list -> new TableDataCell(new DefaultStringConverter());
    }
}
