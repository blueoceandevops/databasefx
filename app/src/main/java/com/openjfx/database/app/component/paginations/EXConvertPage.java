package com.openjfx.database.app.component.paginations;

import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.model.EXModel;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * Export data convert page
 *
 * @author yangkui
 * @since 1.0
 */
public class EXConvertPage extends GridPane {

    private final EXModel exModel;

    public EXConvertPage(EXModel exModel) {
        this.exModel = exModel;

        var lLabel = new Label("时间格式:");

        var charsetBox = new EditChoiceBox<String>(false);
        var tPattern = new TextField(exModel.getTimePattern());

        addRow(0, lLabel, tPattern);

        var keySet = Charset.availableCharsets().keySet();
        for (String s : keySet) {
            charsetBox.getItems().add(s);
        }

        tPattern.textProperty().addListener((observable, oldValue, newValue) -> exModel.setTimePattern(newValue));

        getStyleClass().add("ex-convert-page");
    }
}
