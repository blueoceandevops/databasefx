package com.openjfx.database.app.model.tab;

/**
 * base tab model
 *
 * @author yangkui
 * @since 1.0
 */
public abstract class BaseTabMode {
    /**
     * 数据库标识
     */
    protected final String uuid;
    /**
     * con name
     */
    private final String conName;

    public BaseTabMode(String uuid, String conName) {
        this.uuid = uuid;
        this.conName = conName;
    }


    public String getUuid() {
        return uuid;
    }

    public String getConName() {
        return conName;
    }

    /**
     * <p>
     * This method is used to generate an independent identifier,
     * which can distinguish the function / function of each tab
     * </p>
     *
     * @return tab independent identifier
     */
    public abstract String getFlag();
}
