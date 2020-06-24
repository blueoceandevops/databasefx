package com.openjfx.database.app.skin;

import com.openjfx.database.app.controls.EditChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ChoiceBoxSkin;
import javafx.scene.layout.StackPane;

/**
 * simple impl edit choice-box skin
 *
 * @param <T> {@link javafx.scene.control.ChoiceBox} data type
 * @author yangkui
 * @since 1.0
 */
public class EditChoiceBoxSkin<T> extends ChoiceBoxSkin<T> {

    private final TextField textField = new TextField();

    public EditChoiceBoxSkin(EditChoiceBox<T> control) {
        super(control);
        textField.setEditable(control.isEdit());
        var label = (Label) getChildren().get(0);
        var openButton = (StackPane) getChildren().get(1);

        label.setGraphic(textField);
        label.setGraphicTextGap(0);
        //listener edit property
        control.editProperty().addListener((observable, oldValue, newValue) -> textField.setEditable(newValue));
        //forbid text show in label
        label.textProperty().addListener((observable, oldValue, newValue) -> label.setText(""));
        //hide open button
        control.hideSelectorProperty().addListener((observable, oldValue, newValue) -> {
            openButton.setVisible(!newValue);
        });
        //listener ChoiceBox select change
        control.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            if (index == -1) {
                return;
            }
            var item = control.getItems().get(index);
            textField.setText(item.toString());
        });

        //listener TextField change
        textField.textProperty().addListener((observable, oldValue, newValue) -> control.setText(newValue));
        control.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            var text = textField.getText();
            if (!text.equals(newValue)) {
                textField.setText(newValue);
            }
            var items = control.getItems();
            var index = items.indexOf(newValue);
            control.getSelectionModel().select(index);
        });
    }
}
