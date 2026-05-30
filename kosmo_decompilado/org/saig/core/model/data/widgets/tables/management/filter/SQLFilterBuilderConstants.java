/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.model.data.widgets.tables.management.filter;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.saig.jump.lang.I18N;

public class SQLFilterBuilderConstants {
    public static final String CONNECT_OP_NONE = "        ";
    public static final String CONNECT_OP_OR = "OR      ";
    public static final String CONNECT_OP_NOT_OR = "NOT OR  ";
    public static final String CONNECT_OP_AND = "AND     ";
    public static final String CONNECT_OP_NOT_AND = "NOT AND ";
    public static final String INDENT_TEXT = "        ";
    public static final String OP_EQUALS = I18N.getString(SQLFilterBuilderConstants.class, "is-equal-to");
    public static final String OP_DOES_NOT_EQUALS = I18N.getString(SQLFilterBuilderConstants.class, "is-different-than");
    public static final String OP_IS_LESS_THAN = I18N.getString(SQLFilterBuilderConstants.class, "is-lower-than");
    public static final String OP_IS_LESS_THAN_OR_EQUAL_TO = I18N.getString(SQLFilterBuilderConstants.class, "is-lower-or-equal-than");
    public static final String OP_IS_GREATER_THAN = I18N.getString(SQLFilterBuilderConstants.class, "is-greater-than");
    public static final String OP_IS_GREATER_THAN_OR_EQUAL_TO = I18N.getString(SQLFilterBuilderConstants.class, "is-greater-or-equal-than");
    public static final String OP_LIKE = I18N.getString(SQLFilterBuilderConstants.class, "is-like");
    public static final String OP_NOT_LIKE = I18N.getString(SQLFilterBuilderConstants.class, "is-not-like");
    public static final String OP_IS_BLANK = I18N.getString(SQLFilterBuilderConstants.class, "is-null");
    public static final String OP_IS_NOT_BLANK = I18N.getString(SQLFilterBuilderConstants.class, "is-not-null");
    public static final String OP_BETWEEN = I18N.getString(SQLFilterBuilderConstants.class, "is-between");
    public static final String OP_NOT_BETWEEN = I18N.getString(SQLFilterBuilderConstants.class, "is-not-between");
    public static final String OP_IN = I18N.getString(SQLFilterBuilderConstants.class, "is-one-of");
    public static final String OP_NOT_IN = I18N.getString(SQLFilterBuilderConstants.class, "is-not-one-of");
    public static final String OP_IS_YESTERDAY = I18N.getString(SQLFilterBuilderConstants.class, "is-yesterdey");
    public static final String OP_IS_TODAY = I18N.getString(SQLFilterBuilderConstants.class, "is-today");
    public static final String OP_IS_TOMORROW = I18N.getString(SQLFilterBuilderConstants.class, "is-tomorrow");
    public static final String OP_IS_THIS_WEEK = I18N.getString(SQLFilterBuilderConstants.class, "is-this-week");
    public static final String OP_IS_THIS_MONTH = I18N.getString(SQLFilterBuilderConstants.class, "is-this-month");
    public static final String OP_IS_THIS_YEAR = I18N.getString(SQLFilterBuilderConstants.class, "is-this-year");
    public static final String OP_IS_LAST_WEEK = I18N.getString(SQLFilterBuilderConstants.class, "is-last-week");
    public static final String OP_IS_LAST_MONTH = I18N.getString(SQLFilterBuilderConstants.class, "is-last-month");
    public static final String OP_IS_LAST_YEAR = I18N.getString(SQLFilterBuilderConstants.class, "is-last-year");
    public static final String OP_IS_NEXT_WEEK = I18N.getString(SQLFilterBuilderConstants.class, "is-next-week");
    public static final String OP_IS_NEXT_MONTH = I18N.getString(SQLFilterBuilderConstants.class, "is-next-month");
    public static final String OP_IS_NEXT_YEAR = I18N.getString(SQLFilterBuilderConstants.class, "is-next-year");
    public static Map<String, String> sqlOperations = new HashMap<String, String>();

    static {
        sqlOperations.put(OP_EQUALS, "=");
        sqlOperations.put(OP_DOES_NOT_EQUALS, "!=");
        sqlOperations.put(OP_IS_LESS_THAN, "<");
        sqlOperations.put(OP_IS_LESS_THAN_OR_EQUAL_TO, "<=");
        sqlOperations.put(OP_IS_GREATER_THAN, ">");
        sqlOperations.put(OP_IS_GREATER_THAN_OR_EQUAL_TO, ">=");
        sqlOperations.put(OP_LIKE, "LIKE");
        sqlOperations.put(OP_NOT_LIKE, "NOT LIKE");
        sqlOperations.put(OP_IS_BLANK, "IS NULL");
        sqlOperations.put(OP_IS_NOT_BLANK, "IS NOT NULL");
        sqlOperations.put(OP_BETWEEN, "BETWEEN");
        sqlOperations.put(OP_NOT_BETWEEN, "NOT BETWEEN");
        sqlOperations.put(OP_IN, "IN");
        sqlOperations.put(OP_NOT_IN, "NOT IN");
    }

    public static boolean operationHasNoValues(String operation) {
        return operation.equals(OP_IS_BLANK) || operation.equals(OP_IS_NOT_BLANK) || operation.equals(OP_IS_YESTERDAY) || operation.equals(OP_IS_TODAY) || operation.equals(OP_IS_TOMORROW) || operation.equals(OP_IS_THIS_WEEK) || operation.equals(OP_IS_THIS_MONTH) || operation.equals(OP_IS_THIS_YEAR) || operation.equals(OP_IS_LAST_WEEK) || operation.equals(OP_IS_LAST_MONTH) || operation.equals(OP_IS_LAST_YEAR) || operation.equals(OP_IS_NEXT_WEEK) || operation.equals(OP_IS_NEXT_MONTH) || operation.equals(OP_IS_NEXT_YEAR) || operation.equals(OP_IS_BLANK);
    }

    public static boolean operationHasOneValue(String operation) {
        return operation.equals(OP_EQUALS) || operation.equals(OP_DOES_NOT_EQUALS) || operation.equals(OP_IS_LESS_THAN) || operation.equals(OP_IS_LESS_THAN_OR_EQUAL_TO) || operation.equals(OP_IS_GREATER_THAN) || operation.equals(OP_IS_GREATER_THAN_OR_EQUAL_TO) || operation.equals(OP_IS_LAST_MONTH) || operation.equals(OP_LIKE) || operation.equals(OP_NOT_LIKE);
    }

    public static boolean operationHasTwoValues(String operation) {
        return operation.equals(OP_BETWEEN) || operation.equals(OP_NOT_BETWEEN);
    }

    public static boolean operationHasManyValues(String operation) {
        return operation.equals(OP_IN) || operation.equals(OP_NOT_IN);
    }

    public static String valueFormatter(Object value) {
        String text = value == null ? "" : (value instanceof Date ? DateFormat.getDateInstance(3).format(value) : value.toString());
        return text;
    }

    public static String getSQLOperation(String opName) {
        return sqlOperations.get(opName);
    }
}

