package com.openjfx.database.app.controls.impl;


import com.openjfx.database.app.controls.DataView;
import com.openjfx.database.app.model.TableDataChangeMode;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Custom table view
 *
 * @author yangkui
 * @since 1.0
 */
public class TableDataView extends DataView<ObservableList<StringProperty>> {

    /**
     * Cache Deleted Row Data
     */
    private final List<ObservableList<StringProperty>> deletes = FXCollections.observableArrayList();

    /**
     * Store the changed column information in the collection
     */
    private final List<TableDataChangeMode> changeModes = new ArrayList<>();

    /**
     * New row data
     */
    private final List<ObservableList<StringProperty>> newRows = new ArrayList<>();


    /**
     * Add the number of deleted rows to the cache collection
     *
     * @param item target data
     */
    public void addDeleteItem(ObservableList<StringProperty> item) {
        if (deletes.contains(item)) {
            return;
        }
        int index = getItems().indexOf(item);
        boolean a = getItems().remove(item);
        if (a) {

            //Check whether the deleted data is in the change list
            var optional = changeModes.stream().filter(i -> i.getRowIndex() == index).findAny();

            optional.ifPresent(changeModes::remove);

            //Check whether the data is in the new column
            var b = newRows.contains(item);
            if (b) {
                //Remove data
                newRows.remove(item);
            } else {
                deletes.add(item);
            }
            sortChange(index);
        }
    }

    public void addChangeMode(TableDataChangeMode mode) {
        changeModes.add(mode);
    }

    public Optional<TableDataChangeMode> getChangeModel(int rowIndex, int columnIndex) {
        var key = rowIndex + "_" + columnIndex;
        Optional<TableDataChangeMode> optional = Optional.empty();
        for (TableDataChangeMode changeMode : changeModes) {
            if (changeMode.getId().equals(key)) {
                optional = Optional.of(changeMode);
                break;
            }
        }
        return optional;
    }

    public void removeChange(TableDataChangeMode model) {
        this.changeModes.remove(model);
    }

    public void addNewRow(ObservableList<StringProperty> newRow) {
        if (newRows.contains(newRow)) {
            return;
        }
        newRows.add(newRow);
        getItems().add(newRow);
    }

    public void removeRow(ObservableList<StringProperty> row) {
        this.newRows.remove(row);
    }

    /**
     * Reset cache information
     */
    public void resetChange() {
        this.deletes.clear();
        this.changeModes.clear();
        this.newRows.clear();
    }

    /**
     * Reorder
     *
     * @param index Deleted Row factor
     */
    private void sortChange(int index) {
        changeModes.stream()
                .filter(item -> item.getRowIndex() > index)
                .forEach(item -> item.setRowIndex(item.getRowIndex() - 1));
    }

    public boolean isChange() {
        return changeModes.size() != 0 || deletes.size() != 0 || newRows.size() != 0;
    }

    public List<ObservableList<StringProperty>> getDeletes() {
        return deletes;
    }

    public List<TableDataChangeMode> getChangeModes() {
        return changeModes;
    }

    public List<ObservableList<StringProperty>> getNewRows() {
        return newRows;
    }
}
