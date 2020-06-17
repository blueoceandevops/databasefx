package com.openjfx.database.base;

import com.openjfx.database.*;
import com.openjfx.database.model.ConnectionParam;
import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulate database management connection pool
 *
 * @author yangkui
 * @since 1.0
 */
public class AbstractDataBasePool {
    /**
     * database language
     */
    protected DDL ddl;
    protected DCL dcl;
    protected DML dml;
    protected DQL dql;

    protected Pool pool;

    protected DataConvert dataConvert;

    /**
     * connection param
     */
    private ConnectionParam connectionParam;

    public DDL getDdl() {
        return ddl;
    }

    public DCL getDcl() {
        return dcl;
    }


    public DML getDml() {
        return dml;
    }


    public DQL getDql() {
        return dql;
    }

    public void close() {
        pool.close();
    }


    public DataConvert getDataConvert() {
        return dataConvert;
    }

    public Pool getPool() {
        return pool;
    }

    /**
     * get connection from database pool.
     * <p>This method will not be used in general,
     * but it needs to be called when some scenes only need to get the linked object to complete.
     * Such as database things, etc</p>
     *
     * @return connection
     */
    public Future<SqlConnection> getConnection() {
        return pool.getConnection();
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    public void setConnectionParam(ConnectionParam connectionParam) {
        this.connectionParam = connectionParam;
    }
}
