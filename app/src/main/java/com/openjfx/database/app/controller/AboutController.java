package com.openjfx.database.app.controller;

import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.utils.AssetUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * about application stage
 *
 * @author yangkui
 * @since 1.0
 */
public class AboutController extends AbstractController<Void> {
    @FXML
    private Label name;
    @FXML
    private Label author;
    @FXML
    private Label version;
    @FXML
    private Label website;

    @Override
    public void init() {
        var future = CompletableFuture.supplyAsync(() -> {
            Map<String, String> map;
            try {
                map = AssetUtils.loadManifest();
            } catch (IOException e) {
                logger.error("load manifest attr failed", e);
                throw new RuntimeException(e);
            }
            return map;
        });
        future.whenComplete((attr, t) -> {
            if (t != null) {
                return;
            }
            Platform.runLater(() -> {
                name.setText(attr.get("App-Name"));
                author.setText(attr.get("App-Author"));
                version.setText(attr.get("Manifest-Version"));
                website.setText(attr.get("App-Website"));
            });
        });
    }
}
