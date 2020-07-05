package com.openjfx.database.app.controls;


import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.model.ConnectionParam;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Database node base class
 *
 * @param <T> value type
 * @author yangkui
 * @since 1.0
 */
public abstract class BaseTreeNode<T> extends TreeItem<T> {

    /**
     * Customer TreeCell type
     *
     * @author yangkui
     * @since 1.0
     */
    public enum TreeItemType {
        /**
         * database user
         */
        USER,
        /**
         * database view
         */
        VIEW,
        /**
         * database basic table
         */
        TABLE,
        /**
         * database scheme
         */
        SCHEME,
        /**
         * database connection
         */
        DATABASE_CON,
        /**
         * database view folder
         */
        VIEW_FOLDER,
        /**
         * database user folder
         */
        USER_FOLDER,
        /**
         * database table folder
         */
        TABLE_FOLDER,
        /**
         * database scheme folder
         */
        SCHEME_FOLDER,
    }

    /**
     * Loading state, prevent repeated loading true means false is not in loading
     */
    private final BooleanProperty loading = new SimpleBooleanProperty();
    /**
     * Connection parameters property
     */
    protected ObjectProperty<ConnectionParam> param = new SimpleObjectProperty<>();
    /**
     * Menu list
     */
    protected List<MenuItem> menus = new ArrayList<>();
    /**
     * TreeCell icon
     */
    private final Image image;

    /**
     * Node constructor
     *
     * @param param Link parameters
     */
    public BaseTreeNode(ConnectionParam param, Image image) {
        this.image = image;
        this.param.set(param);
    }

    /**
     * initialization failed
     *
     * @param throwable Exception information
     * @param message   Error message
     */
    protected void initFailed(Throwable throwable, String message) {
        DialogUtils.showErrorDialog(throwable, message);
        setLoading(false);
    }

    public String getUuid() {
        return param.get().getUuid();
    }

    public String getConName() {
        return param.get().getName();
    }

    /**
     * flush
     */
    public void flush() {
        Platform.runLater(() -> {
            setExpanded(false);
            getChildren().clear();
            init();
        });
    }

    public List<MenuItem> getMenus() {
        return menus;
    }

    /**
     * Batch add menu
     *
     * @param menuItems Add on behalf list
     */
    protected void addMenuItem(final MenuItem... menuItems) {
        this.menus.addAll(Arrays.asList(menuItems));
    }

    /**
     * Add menu to specified location
     *
     * @param index position index
     * @param item  Menu to be added
     */
    protected void addMenuItem(final int index, final MenuItem item) {
        if (!menus.contains(item)) {
            menus.add(index, item);
        }
    }

    /**
     * Add a single menu to the list
     *
     * @param item Menu to be added
     */
    protected void addMenuItem(final MenuItem item) {
        if (!menus.contains(item)) {
            menus.add(item);
        }
    }

    /**
     * Called when a child node is initialized
     */
    public void init() {
    }

    public Image getImage() {
        return image;
    }

    protected void removeMenu(MenuItem item) {
        this.menus.remove(item);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public ConnectionParam getParam() {
        return param.get();
    }

    public ObjectProperty<ConnectionParam> paramProperty() {
        return param;
    }

    public void setParam(ConnectionParam param) {
        this.param.set(param);
    }

    public abstract TreeItemType getTreeItemType();
}
