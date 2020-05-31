package com.openjfx.database.app.controller;

import com.openjfx.database.app.BaseController;
import com.openjfx.database.app.component.BaseTab;
import com.openjfx.database.app.component.SearchPopup;
import com.openjfx.database.app.config.Constants;
import com.openjfx.database.app.component.MainTabPane;
import com.openjfx.database.app.component.impl.TableTab;
import com.openjfx.database.app.controls.BaseTreeNode;
import com.openjfx.database.app.config.DbPreference;
import com.openjfx.database.app.controls.impl.DBTreeNode;
import com.openjfx.database.app.controls.impl.SchemeTreeNode;
import com.openjfx.database.app.controls.impl.TableTreeNode;
import com.openjfx.database.app.enums.MenuItemOrder;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.enums.TabType;
import com.openjfx.database.app.model.BaseTabMode;
import com.openjfx.database.app.model.impl.TableTabModel;
import com.openjfx.database.app.stage.AboutStage;
import com.openjfx.database.app.stage.CreateConnectionStage;
import com.openjfx.database.app.stage.SQLEditStage;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.app.utils.TreeDataUtils;
import com.openjfx.database.common.VertexUtils;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import static com.openjfx.database.app.config.Constants.ACTION;
import static com.openjfx.database.app.config.Constants.SCHEME;

/**
 * app主界面控制器
 *
 * @author yangkui
 * @since 1.0
 */
public class DatabaseFxController extends BaseController<Void> {

    /**
     * 顶部菜单栏
     */
    @FXML
    private MenuBar menuBar;

    @FXML
    private VBox lBox;

    @FXML
    private TreeView<String> treeView;

    @FXML
    private TreeItem<String> treeItemRoot;

    @FXML
    private MainTabPane tabPane;

    @FXML
    private SplitPane splitPane;
    /**
     * search popup
     */
    private final SearchPopup searchPopup = SearchPopup.simplePopup();
    /**
     * search result list
     */
    private List<Integer> searchList = new ArrayList<>();
    /**
     * search select index
     */
    private int selectIndex = 0;

    /**
     * EVENT-BUS address
     */
    public static final String EVENT_ADDRESS = "controller:databaseFX";


    @Override
    public void init() {
        initDbList();
        //注册MenuItem上的点击事件
        ObservableList<Menu> menus = menuBar.getMenus();
        for (Menu menu : menus) {
            for (MenuItem item : menu.getItems()) {
                item.addEventHandler(ActionEvent.ACTION, event -> {
                    Object obj = event.getSource();
                    if (obj instanceof MenuItem) {
                        MenuItem temp = (MenuItem) obj;

                        Object userData = temp.getUserData();

                        if (Objects.nonNull(userData)) {
                            doExecMenuOrder(userData.toString());
                        }
                    }
                });
            }
        }

        var menu = new ContextMenu();
        treeView.setContextMenu(menu);

        VBox.setVgrow(treeView, Priority.ALWAYS);

        treeView.setOnContextMenuRequested(e -> {
            menu.getItems().clear();
            var item = treeView.getSelectionModel().getSelectedItem();
            if (item instanceof BaseTreeNode) {
                menu.getItems().addAll(((BaseTreeNode<String>) item).getMenus());
            }
        });

        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 2) {
                var selectedItem = treeView.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                if (!(selectedItem instanceof TableTreeNode)) {
                    ((BaseTreeNode) selectedItem).init();
                } else {
                    //加载表数据
                    var tableTreeNode = ((TableTreeNode) selectedItem);
                    var model = new TableTabModel(tableTreeNode.getServerName(), tableTreeNode.getUuid(), tableTreeNode.getDatabase(), tableTreeNode.getValue());
                    addTab(model.getFlag(), model, TabType.TABLE);
                }
            }
        });

        treeView.setOnKeyPressed(event -> {
            //search data in current tree view
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                lBox.getChildren().add(searchPopup);
            }
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

        tabPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            var tabs = tabPane.getTabs();
            var n = tabs.stream().map(it -> (BaseTab) it)
                    .map(it -> it.getModel().getUuid()).distinct().count();
            var b = n > 1;
            for (Tab tab : tabs) {
                if (tab instanceof TableTab) {
                    ((TableTab) tab).updateValue(b);
                }
            }
        });
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            var t = 800;
            var position = 0.2;
            if (newValue.doubleValue() > t) {
                position = 0.15;
            }
            splitPane.setDividerPosition(0, position);
        });
        //window close->close all connection
        stage.setOnCloseRequest(e -> Platform.exit());

        VertexUtils.eventBus().consumer(EVENT_ADDRESS, this::eventBusHandler);
    }

    /**
     * execute menu order
     *
     * @param value order value
     */
    private void doExecMenuOrder(String value) {
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
            var result = DialogUtils.showAlertConfirm("你确定要刷新,刷新将断开所有连接!");
            if (result) {
                tabPane.getTabs().clear();
                DATABASE_SOURCE.closeAll();
                initDbList();
                var message = new JsonObject();
                message.put(ACTION, MainTabPane.EventBusAction.CLEAR);
                VertexUtils.send(MainTabPane.EVENT_BUS_ADDRESS, message);
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

    private void addTab(String flag, BaseTabMode mode, TabType tabType) {
        var tabs = tabPane.getTabs();
        var optional = tabs.stream().map(it -> (BaseTab) it)
                .filter(t -> t.getModel().getFlag().equals(flag)).findAny();

        if (optional.isPresent()) {
            //change tab
            int index = tabs.indexOf(optional.get());
            tabPane.getSelectionModel().select(index);
            return;
        }
        final Tab tab;
        if (tabType == TabType.TABLE) {
            //create tab
            tab = new TableTab((TableTabModel) mode);
            tabPane.getTabs().add(tab);
            ((TableTab) tab).init();
        } else {
            tab = new Tab();
        }
        tabPane.getSelectionModel().select(tab);
    }

    /**
     * eventBus Unified processing of external input information
     *
     * @param message message body
     */
    private void eventBusHandler(Message<JsonObject> message) {
        var body = message.body();

        var action = EventBusAction.valueOf(body.getString(ACTION));

        var uuid = body.getString(Constants.UUID);
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
    }

    @FXML
    public void createQueryTerminal(ActionEvent event) {
        var item = treeView.getSelectionModel().getSelectedItem();
        if (item == null) {
            DialogUtils.showNotification("请至少选中一个数据库", Pos.TOP_CENTER, NotificationType.INFORMATION);
            return;
        }
        var param = new JsonObject();
        if (item instanceof BaseTreeNode) {
            param.put(Constants.UUID, ((BaseTreeNode<String>) item).getUuid());
        }
        String scheme = "";
        if (item instanceof SchemeTreeNode) {
            scheme = item.getValue();
        }
        if (item instanceof TableTreeNode) {
            scheme = ((TableTreeNode) item).getDatabase();
        }
        param.put(SCHEME, scheme);
        new SQLEditStage(param);
    }

    @FXML
    public void createConnection() {
        new CreateConnectionStage();
    }

    @FXML
    public void showDatabaseUser() {
        DialogUtils.showAlertInfo("功能开发中,尽请期待");
    }

    @FXML
    public void showDatabaseModelView() {
        DialogUtils.showAlertInfo("功能开发中,尽请期待");
    }

    enum EventBusAction {
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
    }
}
