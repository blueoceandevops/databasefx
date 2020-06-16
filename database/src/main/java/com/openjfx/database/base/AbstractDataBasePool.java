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

    private static final Logger logger = LoggerFactory.getLogger(AbstractDataBasePool.class);

    public Future<RowSet<Row>> query(String sql) {
        printSqlStatement(sql, List.of());
        return pool.query(sql);
    }

    public Future<RowSet<Row>> preparedQuery(String sql, Tuple tuple) {
        printSqlStatement(sql, tuple);
        return pool.preparedQuery(sql, tuple);
    }

    public Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> tuples) {
        printSqlStatement(sql, tuples);
        return pool.preparedBatch(sql, tuples);
    }

    public void printSqlStatement(String sql, Tuple tuple) {
        printSqlStatement(sql, List.of(tuple));
    }

    public void printSqlStatement(String sql, List<Tuple> tuples) {
        var sb = new StringBuilder();
        var line = "\r\n";
        sb.append(line);
        sb.append("SQL:");
        sb.append(line);
        sb.append(sql);
        sb.append(line);
        if (tuples.size() > 0) {
            sb.append("Params:");
            sb.append(line);
            sb.append("[");
        }
        var j = 0;
        for (Tuple tuple : tuples) {
            var param = new StringBuilder();
            param.append("[");
            for (int i = 0; i < tuple.size(); i++) {
                var val = tuple.getValue(i);
                param.append(val == null ? null : val.toString());
                if (i != tuple.size() - 1) {
                    param.append(",");
                } else {
                    param.append("]");
                }
            }
            if (j < tuples.size() - 1) {
                sb.append(",");
            }
            ++j;
            sb.append(param);
        }
        sb.append(tuples.size() > 0 ? "]" : "");
        var info = sb.toString();
        if (info.endsWith(line)) {
            info = info.substring(0, info.length() - 2);
        }
        logger.debug(info);
    }

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
