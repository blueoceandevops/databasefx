package com.openjfx.database.app.component;

import com.openjfx.database.DataCharset;
import com.openjfx.database.DataType;
import com.openjfx.database.app.controls.impl.DesignDataView;
import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.model.DesignTableModel;
import com.openjfx.database.app.utils.UiUtils;
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
    private int rowIndex;
    private DesignTableModel model;

    private final CheckBox unSignedCheck = new CheckBox();
    private final CheckBox incrementCheck = new CheckBox();
    private final DataType dataType = DATABASE_SOURCE.getDataType();
    private final DataCharset dataCharset = DATABASE_SOURCE.getCharset();

    private final EditChoiceBox<String> defaultBox = new EditChoiceBox<>();
    private final EditChoiceBox<String> charsetBox = new EditChoiceBox<>();
    private final EditChoiceBox<String> collationBox = new EditChoiceBox<>();


    private final HBox h = new HBox();
    private final HBox h1 = new HBox();
    private final HBox h2 = new HBox();
    private final HBox h3 = new HBox();
    private final HBox h4 = new HBox();

    public DesignOptionBox(DesignDataView tableView) {

        defaultBox.setHideSelector(true);

        var unSigned = new Label(I18N.getString("view.design.table.option.unsigned"));
        var defaultLabel = new Label(I18N.getString("view.design.table.option.default"));
        var charsetLabel = new Label(I18N.getString("view.design.table.option.charset"));
        var collationLabel = new Label(I18N.getString("view.design.table.option.collation"));
        var autoIncrement = new Label(I18N.getString("view.design.table.option.auto"));

        h.getChildren().addAll(autoIncrement, incrementCheck);
        h1.getChildren().addAll(unSigned, unSignedCheck);
        h2.getChildren().addAll(defaultLabel, defaultBox);
        h3.getChildren().addAll(charsetLabel, charsetBox);
        h4.getChildren().addAll(collationLabel, collationBox);


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
        var type = dataType.getCategory(model.getType());
        getChildren().clear();
        switch (type) {
            case NUMBER -> getChildren().addAll(h, h1, h2);
            case STRING -> getChildren().addAll(h2, h3, h4);
            case DATETIME -> getChildren().addAll(h2);
        }
        //update value
        this.charsetBox.setText(model.getCharset());
        this.collationBox.setText(model.getCollation());
        this.incrementCheck.setSelected(Boolean.parseBoolean(model.getAutoIncrement()));
        this.defaultBox.setText(defaultValue);
        this.unSignedCheck.setSelected(Boolean.parseBoolean(model.getUnSigned()));
    }
}
