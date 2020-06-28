package com.openjfx.database.app.model.tab.meta;

import com.openjfx.database.app.controls.impl.UserTreeNode;
import com.openjfx.database.app.model.tab.BaseTabMode;

/**
 * user tab model
 *
 * @author yangkui
 * @since 1.0
 */
public class UserTabModel extends BaseTabMode {
    /**
     * user name
     */
    private String user;
    /**
     * host
     */
    private String host;
    /**
     * tab value
     */
    private String value;

    public UserTabModel(String uuid, String conName) {
        super(uuid, conName);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getFlag() {
        return uuid + "_user_" + value;
    }

    public static UserTabModel build(UserTreeNode treeNode) {
        var uuid = treeNode.getUuid();
        var value = treeNode.getValue();
        var conName = treeNode.getConName();
        var tabModel = new UserTabModel(uuid, conName);
        var array = value.split("@");
        tabModel.setHost(array[1]);
        tabModel.setUser(array[0]);
        tabModel.setValue(value);
        return tabModel;
    }
}
