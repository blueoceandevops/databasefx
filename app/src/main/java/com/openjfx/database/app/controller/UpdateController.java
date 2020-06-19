package com.openjfx.database.app.controller;

import com.openjfx.database.app.API;
import com.openjfx.database.app.BaseController;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.stage.DatabaseFxStage;
import com.openjfx.database.app.utils.DialogUtils;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static com.openjfx.database.app.config.Constants.DATA;

/**
 * update controller
 *
 * @author yangkui
 * @since 1.0
 */
public class UpdateController extends BaseController<Void> {

    @FXML
    private ListView<UpdateLogItem> listView;

    @FXML
    private Label version;

    private JsonObject info;

    @Override
    public void init() {
        var future = CompletableFuture.supplyAsync(() -> {
            try {
                return API.checkUpdate();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
        future.whenComplete((json, t) -> {
            if (t != null) {
                DialogUtils.showErrorDialog(t, resourceBundle.getString("view.update.check.fail.tips"));
                return;
            }
            this.info = json.getJsonObject(DATA);
            var latestVersion = info.getString("latestVersion");
            var logs = info.getJsonArray("logs");
            var items = new ArrayList<UpdateLogItem>();
            var index = 0;
            for (Object log : logs) {
                items.add(new UpdateLogItem(log.toString(), index + 1));
                index++;
            }
            Platform.runLater(() -> {
                version.setText("V" + latestVersion);
                listView.getItems().addAll(items);
            });
        });
    }

    private static class UpdateLogItem extends HBox {
        public UpdateLogItem(final String log, int index) {
            var flag = new Label(index + ".");
            var content = new Label(log);
            HBox.setHgrow(content, Priority.ALWAYS);
            getChildren().addAll(flag, content);
        }
    }

    @FXML
    public void close() {
        stage.close();
        new DatabaseFxStage();
    }

    @FXML
    public void immediateUpdate() {
        if (info == null) {
            return;
        }
        var url = info.getString("url");
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            logger.error("Failed to call up browser", e);
            DialogUtils.showNotification(resourceBundle.getString("app.call.browser.failed"), Pos.TOP_CENTER, NotificationType.INFORMATION);
        }
    }
}
