package com.openjfx.database.app.model;

import com.openjfx.database.app.component.paginations.EXFormatPage;
import com.openjfx.database.app.component.paginations.EXColumnPage;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * export wizard model
 *
 * @author yangkui
 * @since 1.0
 */
public class EXModel {
    /**
     * save path
     */
    private String path;
    /**
     * uuid
     */
    private final String uuid;
    /**
     * table
     */
    private final String table;
    /**
     * scheme
     */
    private final String scheme;
    /**
     * How to deal null?
     */
    private String nullStr = "null";
    /**
     * Auto open file？
     */
    private boolean autoOpen = true;
    /**
     * Auto close current stage？
     */
    private boolean autoClose = true;
    /**
     * timestamp
     */
    private String timePattern = "yyyyMMddHHmm";
    /**
     * user select table column
     */
    private List<EXColumnPage.FieldTableModel> selectTableColumn = new ArrayList<>();
    /**
     * export data format default txt
     */
    private EXFormatPage.ExportDataType exportDataType = EXFormatPage.ExportDataType.TXT;


    public EXModel(String uuid, String scheme, String table) {
        this.uuid = uuid;
        this.scheme = scheme;
        this.table = table;
    }

    public String getUuid() {
        return uuid;
    }

    public String getScheme() {
        return scheme;
    }

    public String getTable() {
        return table;
    }

    public EXFormatPage.ExportDataType getExportDataType() {
        return exportDataType;
    }

    public void setExportDataType(EXFormatPage.ExportDataType exportDataType) {
        this.exportDataType = exportDataType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<EXColumnPage.FieldTableModel> getSelectTableColumn() {
        return selectTableColumn;
    }

    public void setSelectTableColumn(List<EXColumnPage.FieldTableModel> selectTableColumn) {
        this.selectTableColumn = selectTableColumn;
    }

    public void setTimePattern(String timePattern) {
        this.timePattern = timePattern;
    }

    public String getTimePattern() {
        return timePattern;
    }

    public String getNullStr() {
        return nullStr;
    }

    public void setNullStr(String nullStr) {
        this.nullStr = nullStr;
    }

    public boolean isAutoOpen() {
        return autoOpen;
    }

    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    @Override
    public String toString() {
        return "EXModel{" +
                "uuid='" + uuid + '\'' +
                ", scheme='" + scheme + '\'' +
                ", table='" + table + '\'' +
                ", path='" + path + '\'' +
                ", selectTableColumn=" + selectTableColumn +
                ", exportDataType=" + exportDataType +
                '}';
    }
}
