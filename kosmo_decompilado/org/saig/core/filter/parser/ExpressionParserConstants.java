/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter.parser;

public interface ExpressionParserConstants {
    public static final int EOF = 0;
    public static final int SQ_STRING = 8;
    public static final int DQ_STRING = 9;
    public static final int AND = 12;
    public static final int OR = 13;
    public static final int NOT = 14;
    public static final int EQ = 15;
    public static final int NEQ = 16;
    public static final int GT = 17;
    public static final int LT = 18;
    public static final int GTE = 19;
    public static final int LTE = 20;
    public static final int TRUE = 21;
    public static final int FALSE = 22;
    public static final int POINT = 23;
    public static final int LINESTRING = 24;
    public static final int POLYGON = 25;
    public static final int MULTIPOINT = 26;
    public static final int MULTILINESTRING = 27;
    public static final int MULTIPOLYGON = 28;
    public static final int GEOMETRYCOLLECTION = 29;
    public static final int LP = 30;
    public static final int RP = 31;
    public static final int LSP = 32;
    public static final int RSP = 33;
    public static final int IDENTIFIER = 34;
    public static final int INTEGER_LITERAL = 35;
    public static final int FLOATING_LITERAL = 36;
    public static final int LETTER = 37;
    public static final int DIGIT = 38;
    public static final int EXPONENT = 39;
    public static final int DEFAULT = 0;
    public static final int IN_SQ = 1;
    public static final int IN_DQ = 2;
    public static final String[] tokenImage = new String[]{"<EOF>", "\" \"", "\"\\t\"", "\"\\n\"", "\"\\r\"", "\"\\f\"", "\"\\'\"", "\"\\\"\"", "\"\\'\"", "\"\\\"\"", "<token of kind 10>", "<token of kind 11>", "<AND>", "<OR>", "<NOT>", "<EQ>", "<NEQ>", "<GT>", "<LT>", "<GTE>", "<LTE>", "\"true\"", "\"false\"", "\"point\"", "\"linestring\"", "\"polygon\"", "\"multipoint\"", "\"multilinestring\"", "\"multipolygon\"", "\"geometrycollection\"", "\"(\"", "\")\"", "\"[\"", "\"]\"", "<IDENTIFIER>", "<INTEGER_LITERAL>", "<FLOATING_LITERAL>", "<LETTER>", "<DIGIT>", "<EXPONENT>", "\"+\"", "\"-\"", "\"*\"", "\"/\"", "\",\""};
}

