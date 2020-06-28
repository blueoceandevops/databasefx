package com.openjfx.database.mysql;

import com.openjfx.database.base.AbstractDataBaseClient;
import com.openjfx.database.base.AbstractDatabaseSource;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.enums.DatabaseType;
import com.openjfx.database.model.ConnectionParam;

import com.openjfx.database.mysql.impl.MySqlUserPrivilege;
import com.openjfx.database.mysql.impl.MysqlCharset;
import com.openjfx.database.mysql.impl.MysqlDataType;
import com.openjfx.database.mysql.impl.MysqlClient;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


/**
 * Mysql database connection pool management
 *
 * @author yangkui
 * @since 1.0
 */
public class MySql extends AbstractDatabaseSource {
    /**
     * Heartbeat ID
     */
    private final Long timerId;

    public MySql() {
        charset = new MysqlCharset();
        dataType = new MysqlDataType();
        generator = new MysqlSQLGenerator();
        userPrivilege = new MySqlUserPrivilege();
        timerId = VertexUtils.getVertex().setPeriodic(20000, timer -> heartBeat());
    }

    @Override
    public Future<AbstractDataBaseClient> createClient(ConnectionParam param) {
        var pool = MysqlHelper.createPool(param);
        var client = MysqlClient.create(pool);
        return validateClient(client, param);
    }

    @Override
    public Future<AbstractDataBaseClient> createClient(ConnectionParam param, String uuid, String scheme, int initPoolSize) {
        var temp = JsonObject.mapFrom(param).mapTo(ConnectionParam.class);
        temp.setUuid(uuid);
        var pool = MysqlHelper.createPool(param, initPoolSize, scheme);
        var client = MysqlClient.create(pool);
        return validateClient(client, temp);
    }

    public void heartBeat() {
        //Send SQL query statement to MySQL server every 20s
        clients.forEach((a, b) -> {
            var future = b.getDql().heartBeatQuery();
            var serverName = b.getConnectionParam().getName();
            var host = b.getConnectionParam().getHost();
            var target = serverName + "<" + host + ">";
            future.onFailure(t -> {
                var str = "failed heart beat to " + target;
                logger.error(str, t);
            });
        });
    }

    @Override
    public void closeAll() {
        super.closeAll();
        //clear clear timer
        if (timerId != null) {
            VertexUtils.getVertex().cancelTimer(timerId);
        }
    }

    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
}
