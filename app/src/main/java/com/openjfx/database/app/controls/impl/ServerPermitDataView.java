package com.openjfx.database.app.controls.impl;

import com.openjfx.database.app.controls.DataView;
import com.openjfx.database.model.PrivilegeModel;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;


/**
 * Server permit data view
 *
 * @author yangkui
 * @since 1.0
 */
public class ServerPermitDataView extends DataView<ServerPermitDataView.ServerPermitModel> {

    private enum CellType {
        /**
         * permit name
         */
        NAME,
        /**
         * permit selection
         */
        SELECTION,
        /**
         * describe
         */
        DESCRIBE,
        /**
         * Scope
         */
        SCOPE
    }

    private final CheckBox selectOrCancelAll;

    public ServerPermitDataView() {
        setShowLineNumber(false);
        this.selectOrCancelAll = new CheckBox();
        createColumn(CellType.SELECTION);
        createColumn(CellType.NAME);
        createColumn(CellType.DESCRIBE);
        createColumn(CellType.SCOPE);
        selectOrCancelAll.selectedProperty().addListener((observable, oldValue, newValue) -> {
            var val = newValue.toString();
            getItems().forEach(m -> m.setHas(val));
        });
    }

    private void createColumn(CellType cellType) {
        var columnName = switch (cellType) {
            case SELECTION -> "";
            case NAME -> "权限";
            case DESCRIBE -> "描述";
            case SCOPE -> "范围";
        };
        var tableColumn = new TableColumn<ServerPermitModel, String>(columnName);
        if (cellType == CellType.SELECTION) {
            tableColumn.setGraphic(selectOrCancelAll);
        }
        tableColumn.setCellFactory(cal -> new PermitCell(cellType));
        tableColumn.setCellValueFactory(cal -> {
            var model = cal.getValue();
            return switch (cellType) {
                case NAME -> model.nameProperty();
                case SELECTION -> model.hasProperty();
                case DESCRIBE -> model.describeProperty();
                case SCOPE -> model.levelProperty();
            };
        });
        getColumns().add(tableColumn);
    }

    /**
     * user permit for database
     *
     * @author yangkui
     * @since 1.0
     */
    public static class ServerPermitModel {

        private final StringProperty name;
        private final StringProperty has;
        private final StringProperty describe;
        private final StringProperty level;

        public ServerPermitModel(PrivilegeModel model) {
            this.name = new SimpleStringProperty(model.getName());
            this.has = new SimpleStringProperty("false");
            this.describe = new SimpleStringProperty(model.getDescribe());
            this.level = new SimpleStringProperty(model.getLevel());
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getHas() {
            return has.get();
        }

        public StringProperty hasProperty() {
            return has;
        }

        public void setHas(String has) {
            this.has.set(has);
        }

        public String getDescribe() {
            return describe.get();
        }

        public StringProperty describeProperty() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe.set(describe);
        }

        public String getLevel() {
            return level.get();
        }

        public StringProperty levelProperty() {
            return level;
        }

        public void setLevel(String level) {
            this.level.set(level);
        }
    }

    private static class PermitCell extends TableCell<ServerPermitModel, String> {

        private final CellType cellType;

        private final HBox graphic = new HBox();

        private final CheckBox selection = new CheckBox();

        public PermitCell(CellType cellType) {
            this.cellType = cellType;
            graphic.setAlignment(Pos.CENTER);
            graphic.getChildren().add(selection);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            if (empty) {
                setText(null);
                setGraphic(null);
                return;
            }
            updateValue(item);
        }

        private void updateValue(String item) {
            if (cellType != CellType.SELECTION) {
                setText(item);
                return;
            }
            if (getGraphic() == null) {
                setText(null);
                setGraphic(graphic);
            }
            var select = Boolean.parseBoolean(item);
            selection.setSelected(select);
        }
    }

}
