package com.openjfx.database.mysql.impl;

import com.openjfx.database.UserPrivilege;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.model.PrivilegeModel;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/***
 *
 *
 * @author yangkui
 * @since 1.0
 */
public class MySqlUserPrivilege implements UserPrivilege {

    private final static List<PrivilegeModel> MYSQL_USER_PRI = new ArrayList<>();

    //static load Mysql user privilege list
    static {
        var fs = VertexUtils.getFileSystem();
        var buffer = fs.readFileBlocking("database/mysql_user_privilege.json");
        var array = buffer.toJsonArray();
        for (Object o : array) {
            var item = (JsonObject) o;
            var model = item.mapTo(PrivilegeModel.class);
            MYSQL_USER_PRI.add(model);
        }
    }

    /**
     * Get the static permission of MySQL user
     *
     * @return {@inheritDoc}
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/grant.html">Mysql official document</a>
     */
    @Override
    public List<PrivilegeModel> getStaticPrivilege() {
        return MYSQL_USER_PRI.stream()
                .filter(model -> model.getType() == PrivilegeModel.PrivilegeType.STATIC)
                .collect(Collectors.toList());
    }

    /**
     * Get the dynamic permission of MySQL user
     *
     * @return {@inheritDoc}
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/grant.html">Mysql official document</a>
     */
    @Override
    public List<PrivilegeModel> getDynamicPrivilege() {
        return MYSQL_USER_PRI.stream()
                .filter(model -> model.getType() == PrivilegeModel.PrivilegeType.DYNAMIC)
                .collect(Collectors.toList());
    }
}
