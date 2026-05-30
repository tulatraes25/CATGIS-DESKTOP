/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf.utils;

public class DxfCodePage {
    public static final String CODE_PAGE_ANSI_1252 = "ANSI_1252";
    public static final String DEFAULT_CHARSET_ENCODING = "windows-1252";
    public static final String CODE_PAGE_ANSI_1250 = "ANSI_1250";
    public static final String CODE_PAGE_8859_1 = "ISO8859-1";
    public static final String CODE_PAGE_8859_2 = "ISO8859-2";

    public static String toDXFCodePage(String charsetName) {
        String codePage = CODE_PAGE_ANSI_1252;
        if ("windows-1250".equals(charsetName)) {
            codePage = CODE_PAGE_ANSI_1250;
        } else if ("ISO-8859-1".equals(charsetName)) {
            codePage = CODE_PAGE_8859_1;
        } else if ("ISO-8859-2".equals(charsetName)) {
            codePage = CODE_PAGE_8859_2;
        }
        return codePage;
    }
}

