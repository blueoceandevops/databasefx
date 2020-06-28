package com.openjfx.database.app.utils;

import com.openjfx.database.app.component.MainTabPane;
import com.openjfx.database.app.config.Constants;
import com.openjfx.database.app.controller.DatabaseFxController;
import com.openjfx.database.app.controls.impl.TableFolderNode;
import com.openjfx.database.app.model.tab.meta.DesignTabModel;
import com.openjfx.database.common.VertexUtils;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.openjfx.database.app.config.Constants.*;

/**
 * app event bus utils
 *
 * @author yangkui
 * @since 1.0
 */
public class EventBusUtils {
    /**
     * register event bus
     *
     * @param eventBusAddress event bus address
     * @param message         message handler
     * @param <T>             message data type
     */
    public static <T> void registerEventBus(String eventBusAddress, Handler<Message<T>> message) {
        VertexUtils.eventBus().consumer(eventBusAddress, message);
    }

    /**
     * notify {@link DatabaseFxController} open design tab
     *
     * @param conName   连接名
     * @param uuid      uuid
     * @param scheme    scheme
     * @param tableName table name
     * @param type      type
     */
    public static void openDesignTab(String uuid, String conName, String scheme, String tableName, DesignTabModel.DesignTableType type) {
        var params = new JsonObject();
        params.put(Constants.UUID, uuid);
        params.put(Constants.CON_NAME, conName);
        params.put(Constants.SCHEME, scheme);
        params.put(Constants.TYPE, type);
        params.put(Constants.ACTION, DatabaseFxController.EventBusAction.OPEN_DESIGN_TAB);
        params.put(Constants.TABLE_NAME, tableName);
        VertexUtils.send(DatabaseFxController.EVENT_ADDRESS, params);
    }

    /**
     * Notify {@link MainTabPane} to clear all {@link com.openjfx.database.app.component.BaseTab}
     */
    public static void clearMainTab() {
        var message = new JsonObject();
        message.put(ACTION, MainTabPane.EventBusAction.CLEAR);
        VertexUtils.send(MainTabPane.EVENT_BUS_ADDRESS, message);
    }

    /**
     * Notify {@link com.openjfx.database.app.component.MainTabPane} close table tab
     *
     * @param tableName table name
     * @param scheme    database scheme
     * @param uuid      uuid
     */
    public static void closeTableTab(String uuid, String scheme, String tableName) {
        var message = new JsonObject();
        var flag = uuid + "_" + scheme + "_" + tableName;
        message.put(ACTION, MainTabPane.EventBusAction.REMOVE);
        message.put(FLAG, flag);
        VertexUtils.eventBus().send(MainTabPane.EVENT_BUS_ADDRESS, message);
    }

    /**
     * Notify {@link MainTabPane} to close some connection relation tab
     *
     * @param uuid connection uuid
     */
    public static void closeConnectionRelationTab(String uuid) {
        var message = new JsonObject();
        message.put(ACTION, MainTabPane.EventBusAction.REMOVE_MANY);
        message.put(FLAG, uuid);
        //Move out the tabs related to the current database
        VertexUtils.send(MainTabPane.EVENT_BUS_ADDRESS, message);
    }

    /**
     * Notify {@link MainTabPane} close current scheme relative tab
     */
    public static void closeSchemeRelationTab(String uuid, String scheme) {
        var message = new JsonObject();
        message.put(ACTION, MainTabPane.EventBusAction.REMOVE_MANY);
        message.put(FLAG, uuid + "_" + scheme);
        VertexUtils.send(MainTabPane.EVENT_BUS_ADDRESS, message);
    }

    /**
     * Notify {@link TableFolderNode} to flush table list
     *
     * @param uuid   uuid
     * @param scheme scheme
     */
    public static void tableFolderFlushList(String uuid, String scheme) {
        var msg = new JsonObject();
        var address = uuid + "_" + scheme;
        msg.put(ACTION, TableFolderNode.EventBusAction.FLUSH_TABLE);
        VertexUtils.send(address, msg);
    }

    /**
     * event bus notify flush scheme list
     *
     * @param uuid database flag
     */
    public static void flushScheme(String uuid) {
        var msg = new JsonObject();
        msg.put(ACTION, DatabaseFxController.EventBusAction.FLUSH_SCHEME);
        msg.put(UUID, uuid);
        VertexUtils.send(DatabaseFxController.EVENT_ADDRESS, msg);
    }
}
