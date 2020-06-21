package com.openjfx.database.app.controls;

import com.openjfx.database.DataType;
import com.openjfx.database.common.Handler;
import com.openjfx.database.model.TableColumnMeta;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.function.UnaryOperator;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.DatabaseFX.I18N;
import static com.openjfx.database.app.utils.AssetUtils.*;
import static com.openjfx.database.common.config.StringConstants.NULL;

/**
 * customer table TextField
 *
 * @author yangkui
 * @since 1.0
 */
public class TableTextField extends HBox {

    private static final Image EXTENSION_ICON = getLocalImage(20, 20, "extension-icon.png");

    private final TextField textField = new TextField();

    private final StringProperty text = textField.textProperty();

    private final DataType.DataTypeEnum inputType;

    public TableTextField(final String text, final TableColumnMeta meta) {
        final var extension = new Button();
        setText(text);
        HBox.setHgrow(textField, Priority.ALWAYS);
        getChildren().addAll(textField, extension);

        var dataType = DATABASE_SOURCE.getDataType();
        var type = dataType.getDataType(meta.getType());

        if (dataType.isCategory(type, DataType.DataTypeEnum.DATETIME)) {
            inputType = DataType.DataTypeEnum.DATETIME;
        } else if (dataType.isCategory(type, DataType.DataTypeEnum.NUMBER)) {
            inputType = DataType.DataTypeEnum.NUMBER;
        } else {
            inputType = DataType.DataTypeEnum.STRING;
        }

        extension.setGraphic(new ImageView(EXTENSION_ICON));
        extension.setOnAction(event -> {
            var dialog = new InputDialog(text);
            var optional = dialog.showAndWait();
            optional.ifPresent(textField::setText);
        });
//        registerFormatter(textField);
        //add css class
        getStyleClass().add("table-text-field");
        getStylesheets().add("css/table_text_field.css");
    }

    public void setActionEvent(Handler<Void, String> handler) {
        textField.setOnAction(e -> {
            handler.handler(textField.getText());
            e.consume();
        });
    }

    public void selectAll() {
        textField.requestFocus();
        textField.selectAll();
    }

    public void setOnKeyRelease(EventHandler<KeyEvent> event) {
        textField.setOnKeyPressed(event);
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public String getText() {
        return text.get();
    }

    /**
     * Number formatting handler, which is used to restrict the input of data type number
     */
    private final UnaryOperator<TextFormatter.Change> numberFilter = change -> {
        var text = change.getText();
        var pattern = "([0-9]|.)*";
        if (text.matches(pattern)) {
            return change;
        }
        return null;
    };
    /**
     * Time date type input string verification
     */
    private final UnaryOperator<TextFormatter.Change> dateTimeFilter = change -> {
        var text = change.getText();
        var divStr = ":";
        var divStr1 = "-";
        var pattern = "[0-9]*";
        if (text.matches(pattern) || divStr.equals(text) || text.equals(divStr1)) {
            return change;
        }
        return null;
    };


    /**
     * Register TextFormatter uniformly
     *
     * @param control target control
     */
    private void registerFormatter(final TextInputControl control) {
        final var dateTimeFormat = new TextFormatter<>(dateTimeFilter);
        final var numberFormatter = new TextFormatter<>(numberFilter);
        if (inputType == DataType.DataTypeEnum.NUMBER) {
            control.setTextFormatter(numberFormatter);
        }
        if (inputType == DataType.DataTypeEnum.DATETIME) {
            control.setTextFormatter(dateTimeFormat);
        }
    }

    private static class InputDialog extends TextInputDialog {
        /**
         * @param text target text
         */
        public InputDialog(final String text) {
            var pane = getDialogPane();
            var textArea = new TextArea();
            if (!text.equals(NULL)) {
                textArea.setText(text);
            }
            pane.setHeader(new Label());
            textArea.setWrapText(true);
//            registerFormatter(textArea);
            textArea.setPadding(Insets.EMPTY);
            pane.setContent(textArea);
            pane.setPadding(Insets.EMPTY);
            pane.getStylesheets().add("css/base.css");
            setTitle(I18N.getString("databasefx.table.edit.dialog"));
            setResultConverter(buttonType -> {
                if (buttonType == ButtonType.CANCEL) {
                    return text;
                }
                return textArea.getText();
            });
        }
    }
}
