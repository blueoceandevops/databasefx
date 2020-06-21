package com.openjfx.database.app.controls;

import com.openjfx.database.app.skin.TableDataViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

/***
 *
 * base table view
 * @since 1.0
 * @author yangkui
 * @param <T> {@inheritDoc}
 */
public class DataView<T> extends TableView<T> {
    /**
     * is auto calculate column width
     */
    private final BooleanProperty autoColumnWidth = new SimpleBooleanProperty(true);
    /**
     * order number column
     */
    private final TableColumn<T, String> selectColumn = new TableColumn<>();
    /**
     * Start offset of S / N, set when querying page by page in main English
     */
    private final IntegerProperty baseNumber = new SimpleIntegerProperty(0);
    /**
     * is show row number
     */
    private final BooleanProperty showLineNumber = new SimpleBooleanProperty(true);
    /**
     * column offset
     */
    private final IntegerProperty columnOffset = new SimpleIntegerProperty(1);

    public DataView() {
        setSortPolicy(cal -> null);
        //Enable cell selection
        getSelectionModel().setCellSelectionEnabled(true);
        getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        selectColumn.setCellFactory(e -> new SelectTableItem<>(this));
        selectColumn.getStyleClass().add("table-column-index");

        getColumns().addListener((ListChangeListener<TableColumn<T, ?>>) c -> {
            if (!getColumns().contains(selectColumn) && isShowLineNumber()) {
                getColumns().add(0, selectColumn);
            }
        });
        showLineNumber.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                columnOffset.set(1);
                getColumns().add(0, selectColumn);
            } else {
                columnOffset.set(0);
                getColumns().remove(selectColumn);
            }
        });
    }

    private static class SelectTableItem<T> extends TableCell<T, String> {

        private static final String DEFAULT_STYLE_CLASS = "table-cell-index";
        private static final String DEFAULT_SELECT_STYLE_CLASS = "table-cell-index-select";
        private final DataView<T> dataView;

        public SelectTableItem(DataView<T> dataView) {
            this.dataView = dataView;
            addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                var index = getCurrentRowIndex();
                dataView.getSelectionModel().setCellSelectionEnabled(false);
                getTableView().getSelectionModel().select(index);
            });
            //listener select row change
            this.dataView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> select(getCurrentRowIndex() != newValue.intValue()));
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty) {
                setText(null);
                getStyleClass().remove(DEFAULT_STYLE_CLASS);
                return;
            }
            if (!getStyleClass().contains(DEFAULT_STYLE_CLASS)) {
                getStyleClass().add(DEFAULT_STYLE_CLASS);
            }
            var curIndex = getCurrentRowIndex();
            var index = this.dataView.baseNumber.get() + curIndex;
            setText(String.valueOf(index + 1));
            select(getSelectRowIndex() != curIndex);
        }

        private void select(boolean a) {
            var obs = getStyleClass();
            if (a) {
                getStyleClass().remove(DEFAULT_SELECT_STYLE_CLASS);
            } else {
                if (!obs.contains(DEFAULT_SELECT_STYLE_CLASS)) {
                    getStyleClass().add(DEFAULT_SELECT_STYLE_CLASS);
                }
            }
        }

        private int getCurrentRowIndex() {
            var tableRow = getTableRow();
            if (tableRow == null) {
                return -2;
            }
            return tableRow.getIndex();
        }

        private int getSelectRowIndex() {
            var tableView = getTableView();
            if (tableView == null) {
                return -1;
            }
            var selectionModel = tableView.getSelectionModel();
            return selectionModel.getSelectedIndex();
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableDataViewSkin<>(this);
    }

    public boolean isAutoColumnWidth() {
        return autoColumnWidth.get();
    }

    public BooleanProperty autoColumnWidthProperty() {
        return autoColumnWidth;
    }

    public void setAutoColumnWidth(boolean autoColumnWidth) {
        this.autoColumnWidth.set(autoColumnWidth);
    }

    public int getBaseNumber() {
        return baseNumber.get();
    }

    public IntegerProperty baseNumberProperty() {
        return baseNumber;
    }

    public void setBaseNumber(int baseNumber) {
        this.baseNumber.set(baseNumber);
    }

    public boolean isShowLineNumber() {
        return showLineNumber.get();
    }

    public BooleanProperty showLineNumberProperty() {
        return showLineNumber;
    }

    public void setShowLineNumber(boolean showLineNumber) {
        this.showLineNumber.set(showLineNumber);
    }

    public int getColumnOffset() {
        return columnOffset.get();
    }

    public IntegerProperty columnOffsetProperty() {
        return columnOffset;
    }

    public void setColumnOffset(int columnOffset) {
        this.columnOffset.set(columnOffset);
    }
}
