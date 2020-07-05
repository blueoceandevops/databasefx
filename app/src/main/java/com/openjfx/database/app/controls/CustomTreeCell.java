package com.openjfx.database.app.controls;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;

/**
 * Customer TreeCell
 *
 * @author yangkui
 * @since 1.0
 */
public class CustomTreeCell extends TreeCell<String> {

    private static final String NON_EMPTY_CELL = "non_empty_cell";

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
            getStyleClass().removeAll(NON_EMPTY_CELL);
            return;
        }
        setText(item);
        updateGraphic();
        getStyleClass().add(NON_EMPTY_CELL);
    }

    private void updateGraphic() {
        var item = getTreeItem();
        if (!(item instanceof BaseTreeNode)) {
            return;
        }
        var b = ((BaseTreeNode<String>) item).getImage() != null;
        if (b) {
            var graphic = new ImageView(((BaseTreeNode<String>) item).getImage());
            setGraphic(graphic);
        }
        var cM = new ContextMenu();
        cM.getItems().addAll(((BaseTreeNode<String>) item).getMenus());
        setContextMenu(cM);
    }
}
