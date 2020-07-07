package com.openjfx.database.app.controller;

import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.config.DbPreference;
import com.openjfx.database.app.model.TableTransferModel;
import com.openjfx.database.app.utils.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.stream.Collectors;

/**
 * Database table move / copy stage controller
 *
 * @author yangkui
 * @since 1.1
 */
public class TableTransferController extends AbstractController<TableTransferModel> {
    @FXML
    private Label curCon;
    @FXML
    private Label curTable;
    @FXML
    private Label curScheme;
    @FXML
    private Pagination pagination;

    private final HBox selectorBox = new HBox();
    private final TextArea textArea = new TextArea();
    private final ListView<SelectItem> conList = new ListView<>();
    private final ListView<SelectItem> schemeList = new ListView<>();

    @Override
    public void init() {
        curTable.setText(intent.getCurTable());
        curScheme.setText(intent.getCurScheme());
        var client = DatabaseFX.DATABASE_SOURCE.getClient(intent.getCurUUID());
        var address = client.getConnectionParam().getHost();
        var name = client.getConnectionParam().getName();
        var conName = name + "<" + address + ">";
        curCon.setText(conName);
        pagination.setPageFactory(index -> {
            if (index == 0) {
                return selectorBox;
            } else {
                return textArea;
            }
        });
        selectorBox.getStyleClass().add("selector-box");
        selectorBox.getChildren().addAll(conList, schemeList);
        initData();
    }

    private void initData() {
        var cons = DbPreference.getParams();
        cons.forEach(client -> {
            var cc = DatabaseFX.DATABASE_SOURCE.getClient(client.getUuid());
            if (cc == null) {
                return;
            }
            var uuid = client.getUuid();
            var name = cc.getConnectionParam().getName();
            var item = new SelectItem(name, uuid, false);
            conList.getItems().add(item);
        });
    }

    private void loadSchemeList(String uuid) {
        var client = DatabaseFX.DATABASE_SOURCE.getClient(uuid);
        var dql = client.getDql();
        var future = dql.showDatabase();
        future.onComplete(ar -> {
            if (ar.failed()) {
                DialogUtils.showErrorDialog(ar.cause(), "加载Scheme列表失败!");
                return;
            }
            var list = ar.result().stream().map(s -> new SelectItem(s, uuid, true))
                    .collect(Collectors.toList());
            Platform.runLater(() -> {
                if (schemeList.getItems().size() > 0) {
                    schemeList.getItems().clear();
                }
                schemeList.getItems().addAll(list);
            });
        });
    }

    public class SelectItem extends HBox {

        private final String name;
        private final String uuid;

        public SelectItem(String name, String uuid, boolean selectModel) {
            this.name = name;
            this.uuid = uuid;
            var label = new Label(name);
            var checkBox = new CheckBox();
            getStyleClass().add("select-item");
            if (selectModel) {
                getChildren().add(checkBox);
            } else {
                addEventFilter(MouseEvent.MOUSE_CLICKED, event -> loadSchemeList(uuid));
            }
            getChildren().add(label);
        }

        public String getName() {
            return name;
        }

        public String getUuid() {
            return uuid;
        }
    }
}
