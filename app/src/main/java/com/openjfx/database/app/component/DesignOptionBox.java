package com.openjfx.database.app.component;

import com.openjfx.database.DataCharset;
import com.openjfx.database.app.controls.impl.DesignDataView;
import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.model.DesignTableModel;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.enums.DesignTableOperationType;
import com.openjfx.database.model.TableColumnMeta;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

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
    /**
     * design table model
     */
    private DesignTableModel model;


    private int rowIndex;

    public DesignOptionBox(DesignDataView tableView) {

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
            boolean canUpdate = false;
            if (StringUtils.nonEmpty(model.getCollation())) {
                var charset = dataCharset.getCharset(model.getCollation());
                canUpdate = charset.equals(newValue);
            }
            if (canUpdate) {
                return;
            }
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

        //update all listener
        defaultBox.textProperty().addListener((observable, oldValue, newValue) -> {
            model.setDefaultValue(newValue);
            tableView.fieldChange(model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.DEFAULT, newValue);
        });
        charsetBox.textProperty().addListener((observable, oldValue, newValue) -> {
            model.setCharset(newValue);
            tableView.fieldChange(model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.CHARSET, newValue);
        });
        collationBox.textProperty().addListener((observable, oldValue, newValue) -> {
            model.setCollation(newValue);
            tableView.fieldChange(model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.COLLATION, newValue);
        });
        incrementCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            model.setAutoIncrement(newValue.toString());
            tableView.fieldChange(model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.AUTO_INCREMENT, newValue.toString());
        });
        unSignedCheck.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            model.setUnSigned(newValue.toString());
            tableView.fieldChange(model.getMeta(), DesignTableOperationType.UPDATE, rowIndex, TableColumnMeta.TableColumnEnum.UN_SIGNED, newValue.toString());
        }));

        getStyleClass().add("design-table-option");
    }


    public void updateValue(DesignTableModel model, int rowIndex) {
        this.model = model;
        this.rowIndex = rowIndex;

        final String defaultValue;
        if (model.getDefaultValue() == null) {
            defaultValue = "";
        } else {
            defaultValue = model.getDefaultValue();
        }
        //update value
        this.charsetBox.setText(model.getCharset());
        this.collationBox.setText(model.getCollation());
        this.incrementCheck.setSelected(Boolean.parseBoolean(model.getAutoIncrement()));
        this.defaultBox.setText(defaultValue);
        this.unSignedCheck.setSelected(Boolean.parseBoolean(model.getUnSigned()));
    }
}
