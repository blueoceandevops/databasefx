package com.openjfx.database.mysql.impl;

import com.openjfx.database.base.AbstractDataBasePool;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulate mysql pool class
 *
 * @author yangkui
 * @since 1.0
 */
public class MysqlPoolImpl extends AbstractDataBasePool {

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
}
