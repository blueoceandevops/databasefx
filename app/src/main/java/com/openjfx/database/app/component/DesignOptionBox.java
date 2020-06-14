package com.openjfx.database.app.component;

import com.openjfx.database.DataCharset;
import com.openjfx.database.app.component.tabs.DesignTableTab;
import com.openjfx.database.app.controls.DesignTableView;
import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.model.DesignTableModel;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.enums.DesignTableOperationType;
import com.openjfx.database.model.TableColumnMeta;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.ResourceBundle;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.DatabaseFX.I18N;

/**
 * Design table option
 *
 * @author yangkui
 * @since 1.0
 */
public class DesignOptionBox extends VBox {

    private final EditChoiceBox<String> defaultBox = new EditChoiceBox<>();
    private final EditChoiceBox<String> charsetBox = new EditChoiceBox<>();
    private final EditChoiceBox<String> collationBox = new EditChoiceBox<>();
    private final CheckBox incrementCheck = new CheckBox();
    private final CheckBox unSignedCheck = new CheckBox();

    /**
     * database source
     */
    private final DataCharset dataCharset = DATABASE_SOURCE.getCharset();

    public DesignOptionBox() {

        defaultBox.setHideSelector(true);
        var grid = new GridPane();
        var unSigned = new Label(I18N.getString("view.design.table.option.unsigned"));
        var defaultLabel = new Label(I18N.getString("view.design.table.option.default"));
        var charsetLabel = new Label(I18N.getString("view.design.table.option.charset"));
        var collationLabel = new Label(I18N.getString("view.design.table.option.collation"));
        var autoIncrement = new Label(I18N.getString("view.design.table.option.auto"));
        grid.addRow(0, autoIncrement, incrementCheck);
        grid.addRow(1, unSigned, unSignedCheck);
        grid.addRow(2, defaultLabel, defaultBox);
        grid.addRow(3, charsetLabel, charsetBox);
        grid.addRow(4, collationLabel, collationBox);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.getRowConstraints().add(new RowConstraints());
        grid.getColumnConstraints().add(new ColumnConstraints());


        charsetBox.getItems().addAll(dataCharset.getCharset());

        charsetBox.textProperty().addListener((observable, oldValue, newValue) -> {
            var text = charsetBox.getText();
            var items = charsetBox.getItems();
            for (String charset : items) {
                if (charset.equals(text)) {
                    charsetBox.getSelectionModel().select(charset);
                    var collations = dataCharset.getCharsetCollations(text);
                    var ob = FXCollections.observableList(collations);
                    collationBox.setItems(ob);
                    if (ob.size() > 0) {
                        collationBox.getSelectionModel().select(0);
                    }
                    break;
                }
            }
        });

        this.getChildren().add(grid);

        getStyleClass().add("design-table-option");
    }

    private DesignTableModel model;

    public void updateValue(DesignTableModel model, int rowIndex, DesignTableView tableView) {
        this.model = model;

        var defaultValue = model.getDefaultValue() == null ? "" : model.getDefaultValue();

        //update value
        this.charsetBox.setText(model.getCharset());
        this.collationBox.setText(model.getCollation());
        this.incrementCheck.setSelected(Boolean.parseBoolean(model.getAutoIncrement()));
        this.defaultBox.setText(defaultValue);
        this.unSignedCheck.setSelected(Boolean.parseBoolean(model.getUnSigned()));

        //update all listener
        defaultBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.model.setDefaultValue(newValue);
            tableView.fieldChange(this.model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.DEFAULT, newValue);
        });
        charsetBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.model.setCharset(newValue);
            tableView.fieldChange(this.model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.CHARSET, newValue);
        });
        collationBox.textProperty().addListener((observable, oldValue, newValue) -> {
            this.model.setCollation(newValue);
            tableView.fieldChange(this.model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.COLLATION, newValue);
        });
        incrementCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.model.setAutoIncrement(newValue.toString());
            tableView.fieldChange(this.model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.AUTO_INCREMENT, newValue.toString());
        });
        unSignedCheck.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            this.model.setUnSigned(newValue.toString());
            tableView.fieldChange(this.model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.UN_SIGNED, newValue.toString());
        }));
    }
}
