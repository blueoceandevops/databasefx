package com.openjfx.database.app.controller;

import com.openjfx.database.app.config.DbPreference;
import com.openjfx.database.app.controls.SQLEditor;
import com.openjfx.database.app.utils.TableColumnUtils;
import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.config.Constants;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.RobotUtils;
import com.openjfx.database.base.AbstractDataBaseClient;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.common.utils.StringUtils;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.DatabaseFX.I18N;

/**
 * sql editor controller
 *
 * @author yangkui
 * @since 1.0
 */
public class SQLEditController extends AbstractController<JsonObject> {
    @FXML
    private SQLEditor sqlEditor;
    @FXML
    private TableView<ObservableList<StringProperty>> tableView;

    private AbstractDataBaseClient client;

    @Override
    public void init() {
        var uid = intent.getString(Constants.UUID);

        var optional = DbPreference.getConnectionParam(uid);

        var uuid = UUID.randomUUID().toString();

        var scheme = intent.getString(Constants.SCHEME);

        if (optional.isEmpty()) {
            initFailed();
            return;
        }

        var param = optional.get();
        var future = DATABASE_SOURCE.createClient(param, uuid, scheme, 1);
        future.onComplete(ar -> {
            if (ar.failed()) {
                initFailed();
                return;
            }
            this.client = ar.result();
            Platform.runLater(() -> initView(scheme, uuid));
        });
    }

    public void initView(String scheme, String uuid) {
        //load scheme
        var param = client.getConnectionParam();
        final String title;
        if (StringUtils.nonEmpty(scheme)) {
            title = param.getName() + "<" + param.getHost() + "/" + scheme + ">";
        } else {
            title = param.getName() + "<" + param.getHost() + ">";
        }
        stage.setTitle(title);

        var future = client.getPool().getConnection();
        future.onComplete(ar -> {
            if (ar.failed()) {
                DialogUtils.showErrorDialog(ar.cause(), resourceBundle.getString("controller.sql.editor.disable"));
                return;
            }
            var con = ar.result();
            //place into database source pool
            con.close();
        });
        stage.getScene().setOnDragEntered(event -> {
            var files = event.getDragboard().getFiles();
            if (files.size() > 1) {
                DialogUtils.showAlertInfo(I18N.getString("app.stage.sql.terminal.drag.number"));
                return;
            }
            var file = files.get(0);
            loadSqlFile(file);
        });
        stage.setOnCloseRequest(event -> DATABASE_SOURCE.close(uuid));
    }


    @FXML
    public void executeSql() {
        var sql = sqlEditor.getText();
        if (StringUtils.isEmpty(sql)) {
            DialogUtils.showNotification(resourceBundle.getString("controller.sql.editor.sql.empty"), Pos.TOP_CENTER, NotificationType.WARNING);
            return;
        }
        var fut = client.execute(sql);
        fut.onSuccess(rs -> {
            var columnNames = rs.columnsNames();
            var convert = client.getDataConvert();
            final List<String[]> values;
            if (columnNames == null || columnNames.isEmpty()) {
                var columnName = "affected rows";
                columnNames = Collections.singletonList(columnName);
                values = Collections.singletonList(new String[]{String.valueOf(rs.rowCount())});
            } else {
                values = convert.toConvert(rs);
            }
            createData(columnNames, values);
            DialogUtils.showNotification("execute sql success!", Pos.TOP_CENTER, NotificationType.INFORMATION);
        });
        fut.onFailure(t -> DialogUtils.showErrorDialog(t, resourceBundle.getString("controller.sql.editor.sql.executor.fail")));
    }

    @FXML
    public void openFileChooser() {
        var fileChooser = new FileChooser();
        var filter = new FileChooser.ExtensionFilter("SQL Script file", "*.sql");
        fileChooser.setSelectedExtensionFilter(filter);
        var file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        loadSqlFile(file);
    }

    @FXML
    public void copySql(ActionEvent event) {
        var sql = sqlEditor.getText();
        if (StringUtils.isEmpty(sql)) {
            DialogUtils.showNotification(resourceBundle.getString("controller.sql.editor.sql.empty"), Pos.TOP_CENTER, NotificationType.INFORMATION);
            return;
        }
        RobotUtils.addStrClipboard(sql);
    }

    @FXML
    public void clearSql(ActionEvent event) {
        sqlEditor.deleteText(0, sqlEditor.getText().length());
    }

    private void loadSqlFile(File file) {
        var path = file.getAbsolutePath();
        var pattern = "(.)*\\.(sql|SQL)";
        if (!path.matches(pattern)) {
            DialogUtils.showAlertInfo(I18N.getString("app.stage.sql.terminal.drag.format"));
            return;
        }
        var fut = VertexUtils.getFileSystem().readFile(path);
        fut.onComplete(ar -> {
            if (ar.failed()) {
                return;
            }
            var str = ar.result().toString("utf8");
            Platform.runLater(() -> sqlEditor.setText(str));
        });
    }

    private void initFailed() {
        DialogUtils.showAlertInfo(resourceBundle.getString("controller.sql.editor.disable"));
        Platform.runLater(() -> stage.close());
    }

    private void createData(List<String> fields, List<String[]> data) {
        var columns = TableColumnUtils.createTableDataColumnWithField(fields);
        var list = FXCollections.<ObservableList<StringProperty>>observableArrayList();
        for (var row : data) {
            var item = FXCollections.<StringProperty>observableArrayList();
            for (var val : row) {
                item.add(new SimpleStringProperty(val));
            }
            list.add(item);
        }
        Platform.runLater(() -> {
            tableView.getColumns().clear();
            tableView.getColumns().addAll(columns);
            tableView.getItems().clear();
            tableView.getItems().addAll(list);
        });
    }
}
