package com.openjfx.database.base;

import com.openjfx.database.DataCharset;
import com.openjfx.database.DataType;
import com.openjfx.database.SQLGenerator;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.enums.DatabaseType;
import com.openjfx.database.model.ConnectionParam;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database connection pool superclass
 *
 * @author yangkui
 * @since 1.0
 */
public abstract class AbstractDatabaseSource {
    protected static Logger logger;
    /**
     * Database connection pool cache map
     */
    protected ConcurrentHashMap<String, AbstractDataBaseClient> clients = new ConcurrentHashMap<>();
    /**
     * data charset
     */
    protected DataCharset charset;
    /**
     * current database support data type
     */
    protected DataType dataType;

    /**
     * current database local sql generator
     */
    protected SQLGenerator generator;

    public AbstractDatabaseSource() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Get database connection pool according to UUID
     *
     * @param uuid uuid
     * @return Back to database connection pool
     */
    public AbstractDataBaseClient getClient(String uuid) {
        Objects.requireNonNull(uuid);
        return clients.get(uuid);
    }

    /**
     * New database connection pool
     *
     * @param params Connection parameters
     * @return Return to pool
     */
    public abstract Future<AbstractDataBaseClient> createClient(ConnectionParam params);

    /**
     * Create database connection pool
     *
     * @param param        Create database connection pool connection parameters
     * @param uuid         Connection identification
     * @param initPoolSize Initialize dimensions
     * @param scheme       target scheme
     * @return Back to connection pool
     */
    public abstract Future<AbstractDataBaseClient> createClient(ConnectionParam param, String uuid, String scheme, int initPoolSize);

    /**
     * Close a connection pool
     *
     * @param uuid uuid
     */
    public void close(String uuid) {
        var pool = clients.get(uuid);
        if (pool == null) {
            var debug = "{} corresponding database connection pool non-existent,so cancel the close action!";
            logger.debug(debug, uuid);
            return;
        }
        pool.close();
        //Move out of database connection cache
        clients.remove(uuid);
        var debug = "{} connection closed successfully!";
        logger.debug(debug, uuid);
    }

    /**
     * close resource
     */
    public void closeAll() {
        clients.forEach((uuid, pool) -> close(uuid));
    }

    /**
     * get current database type{@link DatabaseType}
     *
     * @return current database type
     */
    public abstract DatabaseType getDatabaseType();

    /**
     * create pool after must check current pool can use,else call {@link Pool#close()}
     * close current database source pool
     *
     * @param pool  wait test database pool
     * @param param connection param
     */
    protected Future<AbstractDataBaseClient> validateClient(AbstractDataBaseClient pool, ConnectionParam param) {
        //Make sure the link is available before joining the cache
        var uuid = param.getUuid();
        pool.setConnectionParam(param);
        var fut = pool.getDql().heartBeatQuery();
        var promise = Promise.<AbstractDataBaseClient>promise();
        fut.onComplete(ar -> {
            if (ar.failed()) {
                pool.close();
                promise.fail(ar.cause());
                logger.error("create database pool failed", ar.cause());
            } else {
                clients.put(uuid, pool);
                promise.complete(pool);
                logger.debug("create database pool success uuid:{}!", uuid);
            }
        });
        return promise.future();
    }

    public DataCharset getCharset() {
        return charset;
    }

    public DataType getDataType() {
        return dataType;
    }

    public SQLGenerator getGenerator() {
        return generator;
    }
}
