package com.openjfx.database.app.controls.impl;

import com.openjfx.database.app.controls.BaseTreeNode;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.EventBusUtils;
import com.openjfx.database.model.ConnectionParam;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

import java.util.stream.Collectors;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.DatabaseFX.I18N;
import static com.openjfx.database.app.config.Constants.ACTION;
import static com.openjfx.database.app.utils.AssetUtils.getLocalImage;

/**
 * table folder node
 *
 * @author yangkui
 * @since 1.0
 */
public class TableFolderNode extends BaseTreeNode<String> {

    private static final Image ICON_IMAGE = getLocalImage(20, 20, "folder_icon.png");
    private final String scheme;

    /**
     * event bus address
     */
    public final String eventBusAddress;

    public TableFolderNode(ConnectionParam param, String scheme) {
        super(param, ICON_IMAGE);
        this.scheme = scheme;
        this.eventBusAddress = getUuid() + "_" + scheme;
        setValue(I18N.getString("database.table"));
        var createTable = new MenuItem(I18N.getString("menu.databasefx.tree.create.table"));
        var flush = new MenuItem(I18N.getString("menu.databasefx.tree.flush"));
        //flush table list
        flush.setOnAction((event) -> flush());

        //register event bus
        EventBusUtils.<JsonObject>registerEventBus(eventBusAddress, msg -> {
            var body = msg.body();
            var action = body.getString(ACTION);
            if (EventBusAction.FLUSH_TABLE == EventBusAction.valueOf(action)) {
                flush();
            }
        });

        createTable.setOnAction(e -> EventBusUtils.openDesignTab(getUuid(), param.getName(), scheme, null, DesignTabModel.DesignTableType.CREATE));

        addMenuItem(flush, createTable);

    }

    @Override
    public void init() {
        if (getChildren().size() > 0 || isLoading()) {
            return;
        }
        setLoading(true);
        var dcl = DATABASE_SOURCE.getClient(getUuid()).getDql();
        var future = dcl.showTables(scheme);
        future.onComplete(ar ->
        {
            if (ar.failed()) {
                DialogUtils.showErrorDialog(ar.cause(), I18N.getString("menu.databasefx.tree.database.init.fail"));
            } else {
                var tas = ar.result().stream().map(s -> new TableNode(scheme, s, param.get())).collect(Collectors.toList());
                Platform.runLater(() -> {
                    getChildren().addAll(tas);
                    setExpanded(true);
                });
            }
            setLoading(false);
        });

    }

    @Override
    public TreeItemType getTreeItemType() {
        return TreeItemType.TABLE_FOLDER;
    }

    /**
     * Event bus address
     *
     * @author yangkui
     * @since 1.0
     */
    public enum EventBusAction {
        /**
         * flush table of current scheme
         */
        FLUSH_TABLE
    }
}
