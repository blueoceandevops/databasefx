package com.openjfx.database.mysql;

import com.openjfx.database.base.AbstractDataBasePool;
import com.openjfx.database.base.AbstractDatabaseSource;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.enums.DatabaseType;
import com.openjfx.database.model.ConnectionParam;

import com.openjfx.database.mysql.impl.MysqlCharset;
import com.openjfx.database.mysql.impl.MysqlDataType;
import com.openjfx.database.mysql.impl.MysqlPoolImpl;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        timerId = VertexUtils.getVertex().setPeriodic(20000, timer -> heartBeat());
    }

    @Override
    public AbstractDataBasePool createPool(ConnectionParam param) {
        var mySqlPool = MysqlHelper.createPool(param);
        var pool = MysqlPoolImpl.create(mySqlPool);
        pool.setConnectionParam(param);
        createPool(pool, param);
        return pool;
    }

    @Override
    public AbstractDataBasePool createPool(ConnectionParam param, String uuid, String database, int initPoolSize) {
        var newParam = JsonObject.mapFrom(param).mapTo(ConnectionParam.class);
        newParam.setUuid(uuid);
        var mySqlPool = MysqlHelper.createPool(param, initPoolSize, database);
        var pool = MysqlPoolImpl.create(mySqlPool);
        createPool(pool, newParam);
        return pool;
    }

    @Override
    public AbstractDataBasePool createPool(ConnectionParam param, String uuid, int initPoolSize) {
        var newParam = JsonObject.mapFrom(param).mapTo(ConnectionParam.class);
        newParam.setUuid(uuid);
        var mySqlPool = MysqlHelper.createPool(param, initPoolSize);
        var pool = MysqlPoolImpl.create(mySqlPool);
        createPool(pool, newParam);
        return pool;
    }

    public void heartBeat() {
        //Send SQL query statement to MySQL server every 20s
        pools.forEach((a, b) -> {
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
