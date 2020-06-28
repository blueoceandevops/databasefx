package com.openjfx.database.app.component.tabs;

import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.component.BaseTab;
import com.openjfx.database.app.controls.impl.ServerPermitDataView;
import com.openjfx.database.app.model.tab.meta.UserTabModel;
import com.openjfx.database.app.utils.AssetUtils;
import com.openjfx.database.model.PrivilegeModel;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;

import java.security.PrivilegedAction;
import java.util.stream.Collectors;

/**
 * database user tab
 *
 * @author yangkui
 * @since 1.0
 */
public class UserTab extends BaseTab<UserTabModel> {
    @FXML
    private TabPane tabPane;

    @FXML
    private ChoiceBox<String> plugins;

    @FXML
    private ChoiceBox<String> policies;

    @FXML
    private ServerPermitDataView permitDataView;

    /**
     * user icon
     */
    private final static Image USER_ICON = AssetUtils.getLocalImage(20, 20, "user_icon.png");

    public UserTab(UserTabModel model) {
        super(model);
        setTabIcon(USER_ICON);
        setText(model.getUser() + "@" + model.getHost() + "(" + model.getConName() + ")");
        setTooltip(new Tooltip(getText()));
        loadView("user_tab_view.fxml");
    }

    @Override
    public void init() {
        plugins.getItems().addAll("mysql_native_password", "sha256_password");
        policies.getItems().addAll("DEFAULT", "IMMEDIATE", "INTERVAL", "NEVER");
        var privileges = DatabaseFX.DATABASE_SOURCE.getUserPrivilege();
        var items = privileges.getStaticPrivilege().stream()
                .map(ServerPermitDataView.ServerPermitModel::new).collect(Collectors.toList());
        this.permitDataView.getItems().addAll(items);
    }

    @Override
    public TabType getTabType() {
        return TabType.USER_TAB;
    }
}
