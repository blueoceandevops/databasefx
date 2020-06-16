package com.openjfx.database.app.model;

import com.openjfx.database.app.component.tabs.DesignTableTab;
import com.openjfx.database.app.controls.EditChoiceBox;
import com.openjfx.database.common.MultipleHandler;
import com.openjfx.database.model.TableColumnMeta;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.stream.Collectors;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;

/**
 * design table model
 *
 * @author yangkui
 * @since 1.0
 */
public class DesignTableModel {
    private final StringProperty field;
    private final StringProperty type;
    private final StringProperty length;
    private final StringProperty decimalPoint;
    private final StringProperty nullable;
    private final StringProperty primaryKey;
    private final StringProperty comment;
    private final StringProperty defaultValue;
    private final StringProperty charset;
    private final StringProperty collation;
    private final StringProperty unSigned;
    private final StringProperty autoIncrement;
    private final TableColumnMeta meta;

    public DesignTableModel(TableColumnMeta meta) {
        this.meta = meta;

        field = new SimpleStringProperty(meta.getField());
        type = new SimpleStringProperty(meta.getType());
        length = new SimpleStringProperty(meta.getLength());
        decimalPoint = new SimpleStringProperty(meta.getDecimalPoint());
        nullable = new SimpleStringProperty(meta.getNotNull().toString());
        primaryKey = new SimpleStringProperty(meta.getPrimaryKey().toString());
        comment = new SimpleStringProperty(meta.getComment());
        defaultValue = new SimpleStringProperty(meta.getDefault());
        charset = new SimpleStringProperty(meta.getCharset());
        collation = new SimpleStringProperty(meta.getCollation());
        unSigned = new SimpleStringProperty(meta.getUnsigned().toString());
        autoIncrement = new SimpleStringProperty(meta.getAutoIncrement().toString());
    }

    public String getField() {
        return field.get();
    }

    public StringProperty fieldProperty() {
        return field;
    }

    public void setField(String field) {
        this.field.set(field);
    }

    public String getType() {
        return type.get();
    }

    public StringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getLength() {
        return length.get();
    }

    public StringProperty lengthProperty() {
        return length;
    }

    public void setLength(String length) {
        this.length.set(length);
    }

    public String getDecimalPoint() {
        return decimalPoint.get();
    }

    public StringProperty decimalPointProperty() {
        return decimalPoint;
    }

    public void setDecimalPoint(String decimalPoint) {
        this.decimalPoint.set(decimalPoint);
    }

    public StringProperty nullableProperty() {
        return nullable;
    }

    public StringProperty primaryKeyProperty() {
        return primaryKey;
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment.set(comment);
    }

    public String getDefaultValue() {
        return defaultValue.get();
    }

    public StringProperty defaultValueProperty() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    public String getCharset() {
        return charset.get();
    }

    public StringProperty charsetProperty() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset.set(charset);
    }

    public String getCollation() {
        return collation.get();
    }

    public StringProperty collationProperty() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation.set(collation);
    }

    public String getNullable() {
        return nullable.get();
    }

    public void setNullable(String nullable) {
        this.nullable.set(nullable);
    }

    public String getPrimaryKey() {
        return primaryKey.get();
    }


    public void setPrimaryKey(String primaryKey) {
        this.primaryKey.set(primaryKey);
    }

    public String getUnSigned() {
        return unSigned.get();
    }

    public StringProperty unSignedProperty() {
        return unSigned;
    }

    public void setUnSigned(String unSigned) {
        this.unSigned.set(unSigned);
    }

    public String getAutoIncrement() {
        return autoIncrement.get();
    }

    public StringProperty autoIncrementProperty() {
        return autoIncrement;
    }

    public void setAutoIncrement(String autoIncrement) {
        this.autoIncrement.set(autoIncrement);
    }

    public TableColumnMeta getMeta() {
        return meta;
    }

    public void setValue(TableColumnMeta.TableColumnEnum columnEnum, String value) {
        switch (columnEnum) {
            case TYPE -> setType(value);
            case NULL -> setNullable(value);
            case FIELD -> setField(value);
            case KEY -> setPrimaryKey(value);
            case AUTO_INCREMENT -> setAutoIncrement(value);
            case DECIMAL_POINT -> setDecimalPoint(value);
            case UN_SIGNED -> setUnSigned(value);
            case CHARSET -> setCharset(value);
            case COLLATION -> setCollation(value);
            case COMMENT -> setComment(value);
            case LENGTH -> setLength(value);
            default -> setDefaultValue(value);
        }
    }
}
