package com.openjfx.database.app;

import com.openjfx.database.app.annotation.Layout;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.common.utils.StringUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static com.openjfx.database.app.utils.AssetUtils.getLocalImage;


/**
 * base stage
 *
 * @param <T> Pass parameter type
 * @author yangkui
 * @since 1.0
 */
public class AbstractStage<T> extends Stage {
    private Layout layout;
    protected Scene scene = null;
    protected AbstractController<T> controller;

    /**
     * No parameters need to be passed
     */
    public AbstractStage() {
        initController();
        controller.init();
        initStage();
    }

    /**
     * Parameters need to be passed
     *
     * @param intent param
     */

    public AbstractStage(T intent) {
        initController();
        controller.setIntent(intent);
        controller.init();
        initStage();
    }

    /**
     * Get layout annotation
     *
     * @return Return annotation information
     */
    private Layout getLayout() {
        var layout = this.getClass().getAnnotation(Layout.class);
        if (Objects.isNull(layout)) {
            throw new RuntimeException(DatabaseFX.I18N.getString("base.stage.layout.null"));
        }
        return layout;
    }

    /**
     * init controller
     */
    private void initController() {
        this.layout = this.getClass().getAnnotation(Layout.class);
        if (this.layout == null) {
            throw new RuntimeException(DatabaseFX.I18N.getString("base.stage.layout.null"));
        }
        var path = "fxml/" + layout.layout();
        var url = ClassLoader.getSystemResource(path);
        var loader = new FXMLLoader(url);
        loader.setResources(DatabaseFX.I18N);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            DialogUtils.showErrorDialog(e, DatabaseFX.I18N.getString("base.stage.layout.load.fail"));
            throw new RuntimeException(e);
        }
        scene = new Scene(root);
        setScene(scene);
        controller = loader.getController();
        controller.setStage(this);
        //add global style
        scene.getStylesheets().add("css/base.css");
    }

    /**
     * init stage
     */
    private void initStage() {
        this.setWidth(this.layout.width());
        this.setHeight(this.layout.height());
        this.initStyle(this.layout.stageStyle());
        this.initModality(this.layout.modality());
        this.setMaximized(this.layout.maximized());
        this.setResizable(this.layout.resizable());
        this.setIconified(this.layout.iconified());
        this.setAlwaysOnTop(this.layout.alwaysOnTop());
        this.setOnCloseRequest(e -> this.controller.close());
        this.getIcons().add(getLocalImage(200, 200, this.layout.icon()));
        this.setTitle(StringUtils.isEmpty(this.layout.title()) ? "" : DatabaseFX.I18N.getString(this.layout.title()));
        if (layout.show()) {
            this.show();
        }
    }
}
