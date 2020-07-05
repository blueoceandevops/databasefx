package com.openjfx.database.app.controller;

import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.app.controls.SQLEditor;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.EventBusUtils;
import com.openjfx.database.base.AbstractDatabaseSource;
import com.openjfx.database.common.utils.StringUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;

/**
 * New database stage controller
 *
 * @author yangkui
 * @since 1.0
 */
public class CreateSchemeController extends AbstractController<String> {
    @FXML
    private Button cancel;
    @FXML
    private Button create;
    @FXML
    private TabPane tabPane;
    @FXML
    private SQLEditor sqlEditor;
    @FXML
    private EditChoiceBox<String> charsetBox;
    @FXML
    private EditChoiceBox<String> collationBox;
    @FXML
    private EditChoiceBox<String> schemeNameBox;

    @Override
    public void init() {
        var databaseSource = DatabaseFX.DATABASE_SOURCE;
        var databaseCharset = databaseSource.getCharset();
        var charsetList = databaseCharset.getCharset();
        charsetBox.setItems(FXCollections.observableList(charsetList));
        //dynamic select collation
        charsetBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isEmpty(newValue)) {
                return;
            }
            var collations = databaseCharset.getCharsetCollations(newValue);
            collationBox.setItems(FXCollections.observableList(collations));
        });
        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            var index = newValue.intValue();
            if (index == 0) {
                return;
            }
            var sql = getSql(databaseSource);
            sqlEditor.setText(sql);
        });

        cancel.setOnAction(e -> stage.close());

        create.setOnAction(e -> {
            var sql = getSql(databaseSource);
            if (StringUtils.isEmpty(sql)) {
                stage.close();
                return;
            }
            var client = databaseSource.getClient(intent);
            var future = client.getDql().executeSql(sql);
            future.onSuccess(rs -> {
                EventBusUtils.flushScheme(intent);
                //close current stage
                Platform.runLater(stage::close);
            });
            future.onFailure(t -> DialogUtils.showErrorDialog(t, resourceBundle.getString("controller.create.scheme.fail")));
        });
    }

    private String getSql(AbstractDatabaseSource databaseSource) {
        var generator = databaseSource.getGenerator();
        var name = schemeNameBox.getText();
        var charset = charsetBox.getText();
        var collation = collationBox.getText();
        return generator.createScheme(name, charset, collation);
    }

}
