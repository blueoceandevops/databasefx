package com.openjfx.database.app.component.paginations;

import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.model.EXModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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


    public EXConvertPage(EXModel exModel) {

        var lLabel = new Label("时间格式:");
        var nLabel = new Label("null值:");
        var oLabel = new Label("打开文件:");
        var closeL = new Label("关闭窗口:");
        var oCheckBox = new CheckBox();
        var cCheckBox = new CheckBox();
        var nChoice = new ChoiceBox<String>();
        var tPattern = new TextField(exModel.getTimePattern());

        oCheckBox.setSelected(true);
        cCheckBox.setSelected(true);
        nChoice.getItems().addAll("", "null");
        nChoice.getSelectionModel().select(1);


        addRow(0, oLabel, oCheckBox);
        addRow(1, closeL, cCheckBox);
        addRow(2, nLabel, nChoice);
        addRow(3, lLabel, tPattern);

        tPattern.textProperty().addListener((observable, oldValue, newValue) -> exModel.setTimePattern(newValue));
        oCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> exModel.setAutoOpen(newValue));
        cCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> exModel.setAutoClose(newValue));
        nChoice.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> exModel.setNullStr(newValue)));

        getStyleClass().add("ex-convert-page");
    }
}
