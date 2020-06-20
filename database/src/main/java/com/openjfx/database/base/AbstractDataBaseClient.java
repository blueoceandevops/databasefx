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
public abstract class AbstractDataBaseClient {
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

    /**
     * <p>In this method, the user obtains the connection pool object directly</p>
     * <p>
     * Note:This method directly exposes the connection pool to external calls,
     * which is likely to cause connection leakage in the connection pool.
     * If there are no special requirements such as things,
     * it is not recommended to directly obtain the connection pool for operation
     * </p>
     */
    public Pool getPool() {
        return pool;
    }

    /**
     * Abstract methods for executing external SQL statements
     *
     * @param sql sql statement
     * @return execute result
     */
    public abstract Future<RowSet<Row>> execute(String sql);

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

    public void setConnectionParam(ConnectionParam connectionParam) {
        this.connectionParam = connectionParam;
    }
}
