package com.openjfx.database.app.model;

import com.openjfx.database.app.component.paginations.EXFormatPage;
import com.openjfx.database.app.component.paginations.EXColumnPage;

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
     * uuid
     */
    private final String uuid;
    /**
     * scheme
     */
    private final String scheme;
    /**
     * table
     */
    private final String table;
    /**
     * save path
     */
    private String path;
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

    @Override
    public String toString() {
        return "ExportWizardModel{" +
                "uuid='" + uuid + '\'' +
                ", scheme='" + scheme + '\'' +
                ", table='" + table + '\'' +
                ", selectTableColumn=" + selectTableColumn +
                ", exportDataType=" + exportDataType +
                '}';
    }
}
