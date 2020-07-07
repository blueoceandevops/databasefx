package com.openjfx.database.app.model;

public class TableTransferModel {
    /**
     * current connection
     */
    private String curUUID;
    /**
     * current scheme
     */
    private String curScheme;
    /**
     * current table
     */
    private String curTable;
    /**
     * target connection
     */
    private String tarUUID;
    /**
     * target scheme
     */
    private String tarScheme;

    public String getCurUUID() {
        return curUUID;
    }

    public void setCurUUID(String curUUID) {
        this.curUUID = curUUID;
    }

    public String getCurScheme() {
        return curScheme;
    }

    public void setCurScheme(String curScheme) {
        this.curScheme = curScheme;
    }

    public String getCurTable() {
        return curTable;
    }

    public void setCurTable(String curTable) {
        this.curTable = curTable;
    }

    public String getTarUUID() {
        return tarUUID;
    }

    public void setTarUUID(String tarUUID) {
        this.tarUUID = tarUUID;
    }

    public String getTarScheme() {
        return tarScheme;
    }

    public void setTarScheme(String tarScheme) {
        this.tarScheme = tarScheme;
    }
}
