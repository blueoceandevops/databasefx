package com.openjfx.database.model;

/**
 * database user privilege model
 *
 * @author yangkui
 * @since 1.0
 */
public class PrivilegeModel {
    /***
     * privilege type current only static and dynamic
     *
     * @author yangkui
     * @since 1.0
     */
    public static enum PrivilegeType {
        /**
         * static privilege
         */
        STATIC,
        /**
         * dynamic privilege
         */
        DYNAMIC
    }

    /**
     * privilege name
     */
    private String name;
    /**
     * privilege describe
     */
    private String describe;
    /**
     * privilege type
     */
    private PrivilegeType type;
    /**
     * privilege grant level
     */
    private String level;

    public String getDescribe() {
        return describe;
    }

    public String getName() {
        return name;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrivilegeType getType() {
        return type;
    }

    public void setType(PrivilegeType type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
