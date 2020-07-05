package com.openjfx.database.app.controller;

import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.component.BaseTab;
import com.openjfx.database.app.component.SearchPopup;
import com.openjfx.database.app.component.tabs.DesignTab;
import com.openjfx.database.app.component.tabs.UserTab;
import com.openjfx.database.app.config.Constants;
import com.openjfx.database.app.component.MainTabPane;
import com.openjfx.database.app.component.tabs.TableTab;
import com.openjfx.database.app.controls.BaseTreeNode;
import com.openjfx.database.app.config.DbPreference;
import com.openjfx.database.app.controls.CustomTreeCell;
import com.openjfx.database.app.controls.impl.*;
import com.openjfx.database.app.enums.MenuItemOrder;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.model.tab.BaseTabMode;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.app.model.tab.meta.TableTabModel;
import com.openjfx.database.app.model.tab.meta.UserTabModel;
import com.openjfx.database.app.stage.AboutStage;
import com.openjfx.database.app.stage.CreateConnectionStage;
import com.openjfx.database.app.stage.SQLEditStage;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.EventBusUtils;
import com.openjfx.database.app.utils.TreeDataUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.stream.Collectors;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;
import static com.openjfx.database.app.config.Constants.*;

/**
 * App main interface controller
 *
 * @author yangkui
 * @since 1.0
 */
public class DatabaseFxController extends AbstractController<Void> {
    @FXML
    private MainTabPane tabPane;
    @FXML
    private SplitPane splitPane;
    @FXML
    private TreeView<String> treeView;
    @FXML
    private TreeItem<String> treeItemRoot;

    private int selectIndex = 0;
    private List<Integer> searchList = new ArrayList<>();
    private final SearchPopup searchPopup = SearchPopup.simplePopup();
    public static final String EVENT_ADDRESS = "controller:databaseFX";


    @Override
    public void init() {
        initDbList();
        VBox.setVgrow(treeView, Priority.ALWAYS);
        treeView.setCellFactory(k -> new CustomTreeCell());
        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 2) {
                var selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (!(selectedItem instanceof BaseTreeNode)) {
                    return;
                }
                var itemType = ((BaseTreeNode<String>) selectedItem).getTreeItemType();
                var b = itemType == BaseTreeNode.TreeItemType.TABLE || itemType == BaseTreeNode.TreeItemType.VIEW;
                if (b) {
                    //Load table data
                    var model = TableTabModel.build(selectedItem);
                    addTab(model, BaseTab.TabType.BASE_TABLE_TAB);
                } else if (itemType == BaseTreeNode.TreeItemType.USER) {
                    var model = UserTabModel.build((UserTreeNode) selectedItem);
                    addTab(model, BaseTab.TabType.USER_TAB);
                } else {
                    ((BaseTreeNode<String>) selectedItem).init();
                }
            }
        });
        treeView.setOnKeyPressed(event -> {

        });
        searchPopup.textChange(keyword -> {
            var cc = treeView.getRoot().getChildren();
            selectIndex = 0;
            searchList = TreeDataUtils.searchWithStr(cc, keyword);
            return searchList.size();
        });
        searchPopup.setSearchOnKeyPressed(event -> {
            //Skip to next search result
            if (event.getCode() == KeyCode.ENTER && !searchList.isEmpty()) {
                treeView.getSelectionModel().select(selectIndex);
                selectIndex++;
            }
        });
        searchPopup.setCloseHandler(event -> {
            searchList.clear();
            selectIndex = 0;
        });
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            var t = 800;
            var position = 0.2;
            if (newValue.doubleValue() > t) {
                position = 0.15;
            }
            splitPane.setDividerPosition(0, position);
        });
        EventBusUtils.registerEventBus(EVENT_ADDRESS, this::eventBusHandler);
    }

    @Override
    public void close() {
        Platform.exit();
    }

    /**
     * execute menu order
     *
     * @param event event source
     */
    @FXML
    public void doExecMenuOrder(ActionEvent event) {
        var item = event.getSource();
        if (!(item instanceof MenuItem)) {
            return;
        }
        var value = ((MenuItem) item).getUserData().toString();
        var order = MenuItemOrder.valueOf(value.toUpperCase());
        if (order == MenuItemOrder.CONNECTION) {
            createConnection();
        }
        if (order == MenuItemOrder.ABOUT) {
            new AboutStage();
        }
        if (order == MenuItemOrder.EXIT) {
            Platform.exit();
        }
        if (order == MenuItemOrder.FLUSH) {
            var result = DialogUtils.showAlertConfirm(resourceBundle.getString("controller.databasefx.flush.tips"));
            if (result) {
                tabPane.getTabs().clear();
                DATABASE_SOURCE.closeAll();
                initDbList();
                EventBusUtils.clearMainTab();
            }
        }
    }

    /**
     * render connection list
     */
    private void initDbList() {
        var nodes = DbPreference.getParams().stream().map(DBTreeNode::new).collect(Collectors.toList());
        var observableList = treeItemRoot.getChildren();
        if (!observableList.isEmpty()) {
            observableList.clear();
        }
        treeItemRoot.getChildren().addAll(nodes);
    }

    private void addTab(BaseTabMode mode, BaseTab.TabType tabType) {
        var tabs = tabPane.getTabs();
        var optional = tabs.stream().map(it -> (BaseTab) it)
                .filter(t -> t.getModel().getFlag().equals(mode.getFlag())).findAny();

        if (optional.isPresent()) {
            //change tab
            int index = tabs.indexOf(optional.get());
            tabPane.getSelectionModel().select(index);
            return;
        }
        final BaseTab tab;
        if (tabType == BaseTab.TabType.BASE_TABLE_TAB) {
            //create tab
            tab = new TableTab((TableTabModel) mode);
        } else if (tabType == BaseTab.TabType.USER_TAB) {
            tab = new UserTab((UserTabModel) mode);
        } else {
            tab = new DesignTab((DesignTabModel) mode);
        }

        Platform.runLater(() -> {
            tabPane.getTabs().add(tab);
            tab.init();
            tabPane.getSelectionModel().select(tab);
        });
    }

    /**
     * eventBus Unified processing of external input information
     *
     * @param message message body
     */
    private void eventBusHandler(Message<JsonObject> message) {
        var body = message.body();

        var action = EventBusAction.valueOf(body.getString(ACTION));

        var uuid = body.getString(Constants.UUID, "");
        //create connection
        if (action == EventBusAction.ADD_CONNECTION) {
            DbPreference.getConnectionParam(uuid).ifPresent(db -> {
                var node = new DBTreeNode(db);
                Platform.runLater(() -> treeItemRoot.getChildren().add(node));
            });
        }
        final var nodes = treeItemRoot.getChildren();
        //update connection
        if (action == EventBusAction.UPDATE_CONNECTION) {
            var optional = nodes.stream()
                    .map(db -> ((BaseTreeNode<String>) db)).filter(db -> db.getUuid().equals(uuid)).findAny();
            if (optional.isPresent()) {
                var node = optional.get();
                var optional1 = DbPreference.getConnectionParam(uuid);
                optional1.ifPresent(node::setParam);
            }
        }
        //flush scheme
        if (action == EventBusAction.FLUSH_SCHEME) {
            var optional = nodes.stream().map(db -> (BaseTreeNode<String>) db)
                    .filter(db -> db.getUuid().equals(uuid)).findAny();
            if (optional.isPresent()) {
                var item = optional.get();
                item.flush();
            }
        }

        //open design table
        if (action == EventBusAction.OPEN_DESIGN_TAB) {
            var model = DesignTabModel.build(body);
            addTab(model, BaseTab.TabType.DESIGN_TABLE_TAB);
        }
    }

    @FXML
    public void createQueryTerminal(ActionEvent event) {
        var item = treeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            DialogUtils.showNotification(resourceBundle.getString("controller.databasefx.select.tips"), Pos.TOP_CENTER, NotificationType.INFORMATION);
            return;
        }
        var param = new JsonObject();
        if (item instanceof BaseTreeNode) {
            param.put(Constants.UUID, ((BaseTreeNode<String>) item).getUuid());
        }
        var scheme = "";
        if (item instanceof SchemeTreeNode) {
            scheme = item.getValue();
        }
        if (item instanceof TableTreeNode) {
            scheme = ((TableTreeNode) item).getScheme();
        }
        param.put(SCHEME, scheme);
        new SQLEditStage(param);
    }

    @FXML
    public void createConnection() {
        new CreateConnectionStage();
    }

    public enum EventBusAction {
        /**
         * add connection
         */
        ADD_CONNECTION,
        /**
         * update connection param
         */
        UPDATE_CONNECTION,
        /**
         * flush scheme
         */
        FLUSH_SCHEME,
        /**
         * open tab
         */
        OPEN_DESIGN_TAB
    }
}
