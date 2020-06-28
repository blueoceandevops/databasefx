package com.openjfx.database;

import com.openjfx.database.model.PrivilegeModel;

import java.util.List;

/***
 *
 *Database user authority management interface
 *
 * @author yangkui
 * @since 1.0
 */
public interface UserPrivilege {
    /**
     * Get user static permissions list
     *
     * @return static permissions
     */
    List<PrivilegeModel> getStaticPrivilege();

    /**
     * Get user dynamic permissions
     *
     * @return dynamic permissions
     */
    List<PrivilegeModel> getDynamicPrivilege();
}
