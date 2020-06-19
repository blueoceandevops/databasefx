package com.openjfx.database.mysql.impl;

import com.alibaba.druid.sql.SQLUtils;
import com.openjfx.database.base.AbstractDataBasePool;
import com.openjfx.database.enums.DatabaseType;
import com.openjfx.database.utils.SQLFormatUtils;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Encapsulate mysql pool class
 *
 * @author yangkui
 * @since 1.0
 */
public class MysqlPoolImpl extends AbstractDataBasePool {

    private static final Logger logger = LoggerFactory.getLogger(MysqlPoolImpl.class);

    private MysqlPoolImpl(MySQLPool pool) {
        this.pool = pool;
        dql = new DQLImpl(this);
        ddl = new DDLImpl(this);
        dml = new DMLImpl(this);
        dataConvert = new SimpleMysqlDataConvert();
    }

    public static AbstractDataBasePool create(MySQLPool pool) {
        return new MysqlPoolImpl(pool);
    }

    @Override
    public Future<RowSet<Row>> execute(String sql) {
        return query(sql);
    }

    public Future<RowSet<Row>> query(String sql) {
        logSqlStatement(sql, List.of());
        return pool.query(sql);
    }

    public Future<RowSet<Row>> preparedQuery(String sql, Tuple tuple) {
        logSqlStatement(sql, tuple);
        return pool.preparedQuery(sql, tuple);
    }

    public Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> tuples) {
        logSqlStatement(sql, tuples);
        return pool.preparedBatch(sql, tuples);
    }

    public void logSqlStatement(String sql, Tuple tuple) {
        logSqlStatement(sql, List.of(tuple));
    }

    public void logSqlStatement(String sql, List<Tuple> tuples) {
        var sb = new StringBuilder();
        sb.append("SQL:");
        sb.append(sql);
        sb.append(" ");
        if (tuples.size() > 0) {
            sb.append("Params:");
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
        logger.debug(sb.toString());
    }
}
