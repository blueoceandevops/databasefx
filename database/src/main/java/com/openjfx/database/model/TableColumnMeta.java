package com.openjfx.database.model;

/**
 * TableColumn meta
 *
 * @author yangkui
 * @since 1.0
 */
public class TableColumnMeta {

    public enum TableColumnEnum {
        /**
         * field
         */
        FIELD,
        /**
         * type
         */
        TYPE,
        /**
         * collation
         */
        COLLATION,
        /**
         * null
         */
        NULL,
        /**
         * key
         */
        KEY,
        /**
         * autoIncrement
         */
        AUTO_INCREMENT,
        /**
         * default
         */
        DEFAULT,
        /**
         * extra
         */
        EXTRA,
        /**
         * comment
         */
        COMMENT,
        /**
         * length
         */
        LENGTH,
        /**
         * decimal point
         */
        DECIMAL_POINT,
        /**
         * charset
         */
        CHARSET,
        /**
         * primary key
         */
        PRIMARY_KEY,
        /**
         * unSigned
         */
        UN_SIGNED
    }

    /**
     * field name
     */
    private String Field;
    /**
     * original type contain length and point
     */
    private String OriginalType;
    /**
     * data type
     */
    private String Type;
    /**
     * charset collation
     */
    private String Collation = "";
    /**
     * not null?
     */
    private Boolean NotNull = Boolean.FALSE;
    /**
     * is Key
     */
    private String Key;
    /**
     * field is auto_increment
     */
    private Boolean AutoIncrement = Boolean.FALSE;
    /**
     * field default
     */
    private String Default;
    /**
     * field extra
     */
    private String Extra;
    /**
     * field privileges
     */
    private String Privileges;
    /**
     * field comment
     */
    private String Comment;
    /**
     * field length
     */
    private String Length;

    /**
     * field decimal point
     */
    private String DecimalPoint;
    /**
     * field charset
     */
    private String Charset = "";
    /**
     * field is primary Key
     */
    private Boolean PrimaryKey = Boolean.FALSE;
    /**
     * un signed
     */
    private Boolean unsigned = Boolean.FALSE;

    public TableColumnMeta() {
    }

    public TableColumnMeta(final String field) {
        this.setField(field);
    }

    public String getField() {
        return Field;
    }

    public void setField(String field) {
        Field = field;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getCollation() {
        return Collation;
    }

    public void setCollation(String collation) {
        Collation = collation;
    }

    public Boolean getNotNull() {
        return NotNull;
    }

    public void setNotNull(Boolean notNull) {
        NotNull = notNull;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }

    public String getDefault() {
        return Default;
    }

    public void setDefault(String aDefault) {
        Default = aDefault;
    }

    public String getExtra() {
        return Extra;
    }

    public void setExtra(String extra) {
        Extra = extra;
    }

    public String getPrivileges() {
        return Privileges;
    }

    public void setPrivileges(String privileges) {
        Privileges = privileges;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getLength() {
        return Length;
    }

    public void setLength(String length) {
        Length = length;
    }

    public String getDecimalPoint() {
        return DecimalPoint;
    }

    public void setDecimalPoint(String decimalPoint) {
        DecimalPoint = decimalPoint;
    }

    public Boolean getAutoIncrement() {
        return AutoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        AutoIncrement = autoIncrement;
    }

    public String getCharset() {
        return Charset;
    }

    public void setCharset(String charset) {
        Charset = charset;
    }

    public Boolean getPrimaryKey() {
        return PrimaryKey;
    }

    public String getOriginalType() {
        return OriginalType;
    }

    public void setOriginalType(String originalType) {
        OriginalType = originalType;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        PrimaryKey = primaryKey;
    }

    public Boolean getUnsigned() {
        return unsigned;
    }

    public void setUnsigned(Boolean unsigned) {
        this.unsigned = unsigned;
    }

    public <T> T getFieldValue(TableColumnEnum tableColumnEnum) {
        final T t;
        if (tableColumnEnum == TableColumnEnum.FIELD) {
            t = (T) this.getField();
        } else if (tableColumnEnum == TableColumnEnum.TYPE) {
            t = (T) this.getType();
        } else if (tableColumnEnum == TableColumnEnum.AUTO_INCREMENT) {
            t = (T) this.getAutoIncrement();
        } else if (tableColumnEnum == TableColumnEnum.CHARSET) {
            t = (T) this.getCharset();
        } else if (tableColumnEnum == TableColumnEnum.COLLATION) {
            t = (T) this.getCollation();
        } else if (tableColumnEnum == TableColumnEnum.COMMENT) {
            t = (T) this.getComment();
        } else if (tableColumnEnum == TableColumnEnum.DEFAULT) {
            t = (T) this.getDefault();
        } else if (tableColumnEnum == TableColumnEnum.EXTRA) {
            t = (T) this.getExtra();
        } else if (tableColumnEnum == TableColumnEnum.KEY) {
            t = (T) this.getKey();
        } else if (tableColumnEnum == TableColumnEnum.LENGTH) {
            t = (T) this.getLength();
        } else if (tableColumnEnum == TableColumnEnum.PRIMARY_KEY) {
            t = (T) this.getPrimaryKey();
        } else if (tableColumnEnum == TableColumnEnum.NULL) {
            t = (T) this.getNotNull();
        } else if (tableColumnEnum == TableColumnEnum.DECIMAL_POINT) {
            t = (T) this.getDecimalPoint();
        } else {
            t = (T) this.getUnsigned();
        }
        return t;
    }

    public static TableColumnMeta defaultMeta() {
        var meta = new TableColumnMeta();
        meta.setUnsigned(false);
        meta.setType("varchar");
        meta.setField("Unnamed");
        meta.setPrimaryKey(false);
        meta.setLength("255");
        meta.setDecimalPoint("0");
        meta.setOriginalType("varchar(255)");
        return meta;
    }
}
