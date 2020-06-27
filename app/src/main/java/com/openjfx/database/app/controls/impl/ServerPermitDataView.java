package com.openjfx.database.app.controls.impl;

import com.openjfx.database.app.controls.DataView;
import com.openjfx.database.common.utils.StringUtils;
import javafx.beans.property.SimpleBooleanProperty;
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
        SELECTION
    }

    public ServerPermitDataView() {
        setShowLineNumber(false);
        createColumn(CellType.NAME);
        createColumn(CellType.SELECTION);
    }

    private void createColumn(CellType cellType) {
        var columnName = cellType == CellType.NAME ? "权限" : "授予";
        var tableColumn = new TableColumn<ServerPermitModel, String>(columnName);
        tableColumn.setCellFactory(cal -> new PermitCell(cellType));
        tableColumn.setCellValueFactory(cal -> {
            var model = cal.getValue();
            return switch (cellType) {
                case NAME -> model.nameProperty();
                case SELECTION -> model.hasProperty();
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

        public ServerPermitModel(String name, String hasPermit) {
            this.name = new SimpleStringProperty(name);
            var t = new SimpleBooleanProperty(StringUtils.nonEmpty(hasPermit) && "Y".equals(hasPermit)).toString();
            this.has = new SimpleStringProperty(Boolean.valueOf(t).toString());
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
            if (cellType == CellType.NAME) {
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
