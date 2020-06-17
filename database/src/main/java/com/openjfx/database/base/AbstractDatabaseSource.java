package com.openjfx.database.base;

import com.openjfx.database.DataCharset;
import com.openjfx.database.DataType;
import com.openjfx.database.SQLGenerator;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.enums.DatabaseType;
import com.openjfx.database.model.ConnectionParam;
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
    protected ConcurrentHashMap<String, AbstractDataBasePool> pools = new ConcurrentHashMap<>();
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
    public AbstractDataBasePool getDataBaseSource(String uuid) {
        Objects.requireNonNull(uuid);
        return pools.get(uuid);
    }

    /**
     * New database connection pool
     *
     * @param params Connection parameters
     * @return Return to pool
     */
    public abstract AbstractDataBasePool createPool(ConnectionParam params);

    /**
     * Create database connection pool
     *
     * @param param        Create database connection pool connection parameters
     * @param uuid         Connection identification
     * @param initPoolSize Initialize dimensions
     * @return Back to connection pool
     */
    public abstract AbstractDataBasePool createPool(ConnectionParam param, String uuid, String database, int initPoolSize);

    /**
     * Create database connection pool
     *
     * @param param        connection param
     * @param uuid         Connection identification
     * @param initPoolSize Initialize dimensions
     * @return Back to connection pool
     */
    public abstract AbstractDataBasePool createPool(ConnectionParam param, String uuid, int initPoolSize);

    /**
     * Close a connection pool
     *
     * @param uuid uuid
     */
    public void close(String uuid) {
        var pool = pools.get(uuid);
        if (pool == null) {
            var debug = uuid + " corresponding database connection pool non-existent," +
                    "so cancel the close action!";
            logger.debug(debug);
            return;
        }
        pool.close();
        //Move out of database connection cache
        pools.remove(uuid);
        var debug = uuid + " connection closed successfully!";
        logger.debug(debug);
    }

    /**
     * close resource
     */
    public void closeAll() {
        pools.forEach((uuid, pool) -> close(uuid));
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
    protected void createPool(AbstractDataBasePool pool, ConnectionParam param) {
        pool.setConnectionParam(param);
        //Make sure the link is available before joining the cache
        var fut = pool.getDql().heartBeatQuery();
        var uuid = param.getUuid();
        fut.onFailure(t -> {
            pool.close();
            logger.error("create database pool failed", t);
        });
        fut.onSuccess(rs -> {
            pools.put(uuid, pool);
            logger.debug("create database pool success uuid:{}!", uuid);
        });
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
