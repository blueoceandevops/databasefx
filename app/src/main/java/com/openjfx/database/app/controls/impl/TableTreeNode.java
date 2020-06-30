package com.openjfx.database.app.controls.impl;

import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.controls.BaseTreeNode;
import com.openjfx.database.app.model.EXModel;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.app.stage.EXStage;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.EventBusUtils;
import com.openjfx.database.model.ConnectionParam;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;


import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.DatabaseFX.I18N;
import static com.openjfx.database.app.utils.AssetUtils.getLocalImage;

/**
 * Database table node
 *
 * @author yangkui
 * @since 1.0
 */
public class TableTreeNode extends BaseTreeNode<String> {

    private static final Image ICON_IMAGE = getLocalImage(20, 20, "table_icon.png");

    /**
     * database
     */
    private final String scheme;

    public TableTreeNode(String scheme, String tableName, ConnectionParam param) {
        super(param, ICON_IMAGE);

        this.scheme = scheme;

        setValue(tableName);

        var design = new MenuItem(I18N.getString("menu.databasefx.tree.design.table"));
        var delete = new MenuItem(I18N.getString("menu.databasefx.tree.delete.table"));
        var exportData = new MenuItem(I18N.getString("menu.databasefx.tree.export.data"));
        var rename = new MenuItem(I18N.getString("menu.databasefx.tree.rename"));

        design.setOnAction(e -> EventBusUtils.openDesignTab(
                getUuid(),
                param.getName(),
                getScheme(),
                getValue(),
                DesignTabModel.DesignTableType.UPDATE));
        delete.setOnAction(e -> {
            var tips = I18N.getString("menu.databasefx.tree.delete.table.tips") + " " + getValue() + "?";
            var result = DialogUtils.showAlertConfirm(tips);
            if (!result) {
                return;
            }
            var client = DatabaseFX.DATABASE_SOURCE.getClient(getUuid());
            var future = client.getDdl().dropTable(getValue(), scheme);

            future.onSuccess(ar -> {
                EventBusUtils.closeTableTab(getUuid(), scheme, getValue());
                Platform.runLater(() -> getParent().getChildren().remove(this));
            });

            future.onFailure(t -> DialogUtils.showErrorDialog(t, I18N.getString("menu.databasefx.tree.delete.table.fail")));
        });
        exportData.setOnAction(event -> {
            var model = new EXModel(getUuid(), scheme, getValue());
            new EXStage(model);
        });
        //rename table
        rename.setOnAction(e -> {
            var target = DialogUtils.showInputDialog(I18N.getString("menu.databasefx.tree.rename.table.tips"), getValue()).trim();
            if (target.isEmpty() || target.trim().equals(getValue())) {
                return;
            }
            var client = DATABASE_SOURCE.getClient(getUuid());
            var dml = client.getDml();
            var future = dml.renameTable(getValue(), target, scheme);
            future.onSuccess(r -> {
                EventBusUtils.closeTableTab(getUuid(), scheme, getValue());
                Platform.runLater(() -> setValue(target));
            });
            future.onFailure(t -> DialogUtils.showErrorDialog(t, I18N.getString("menu.databasefx.tree.rename.failed")));
        });
        addMenuItem(design, exportData, rename, delete);
    }

    public String getScheme() {
        return scheme;
    }

    @Override
    public void init() {
    }
}
