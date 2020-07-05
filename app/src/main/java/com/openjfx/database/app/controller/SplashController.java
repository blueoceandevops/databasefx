package com.openjfx.database.app.controller;

import com.openjfx.database.app.API;
import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.config.DbPreference;
import com.openjfx.database.app.stage.DatabaseFxStage;
import com.openjfx.database.app.stage.UpdateStage;
import com.openjfx.database.app.utils.AssetUtils;
import com.openjfx.database.model.ConnectionParam;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.openjfx.database.app.config.Constants.*;
import static com.openjfx.database.app.config.FileConfig.loadConfig;
import static com.openjfx.database.app.utils.DialogUtils.showErrorDialog;

/**
 * Splash stage controller
 *
 * @author yangkui
 * @since 1.0
 */
public class SplashController extends AbstractController<Void> {
    @FXML
    private Label title;
    /**
     * Is there a new version
     */
    private final AtomicBoolean update = new AtomicBoolean(false);

    @Override
    public void init() {
        var future = CompletableFuture.runAsync(() -> {
            try {
                init0();
                init1();
                init2();
                init3();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        future.whenComplete((r, t) -> {
            //close current stage
            // Platform.runLater(stage::close);
            if (Objects.nonNull(t)) {
                logger.error("App init failed", t);
                showErrorDialog(t, resourceBundle.getString("app.startup.fail"));
            } else {
                updateProgress(resourceBundle.getString("app.startup.success"));
                Platform.runLater(() -> {
                    if (update.get()) {
                        new UpdateStage();
                    } else {
                        new DatabaseFxStage();
                    }
                });
            }
            Platform.runLater(stage::close);
        });
    }


    /**
     * update progress
     *
     * @param title progress describe
     */
    private void updateProgress(String title) {
        Platform.runLater(() -> this.title.setText(title));
    }

    private void init0() throws Exception {
        AssetUtils.loadAllFont();
        updateProgress(resourceBundle.getString("app.startup.init"));
        Thread.sleep(250);
    }

    private void init1() throws InterruptedException {
        updateProgress(resourceBundle.getString("app.startup.load.config"));
        var db = loadConfig(DB_CONFIG_FILE);
        var params = db.getJsonArray(DATABASE).stream()
                .map(it -> ((JsonObject) it).mapTo(ConnectionParam.class))
                .collect(Collectors.toList());
        DbPreference.setParams(params);
        Thread.sleep(250);
    }

    private void init2() throws InterruptedException {
        updateProgress(resourceBundle.getString("app.startup.load.ui.config"));
        loadConfig(UI_CONFIG_FILE);
        Thread.sleep(250);
    }

    private void init3() {
        updateProgress(resourceBundle.getString("app.startup.load.update"));
        try {
            var json = API.checkUpdate();
            if (json != null) {
                var j = json.getJsonObject(DATA);
                update.set(j.getBoolean(UPDATE));
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Check updated failed", e);
        }
    }
}
