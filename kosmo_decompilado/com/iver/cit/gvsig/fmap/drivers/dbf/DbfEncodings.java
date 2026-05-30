/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.iver.cit.gvsig.fmap.drivers.dbf;

import java.nio.charset.Charset;
import org.apache.log4j.Logger;

public class DbfEncodings {
    private static final Logger LOGGER = Logger.getLogger(DbfEncodings.class);
    private static DbfEncodings theInstance = null;
    private final int[] dbfIds;

    private DbfEncodings() {
        int[] nArray = new int[27];
        nArray[1] = 2;
        nArray[2] = 3;
        nArray[3] = 4;
        nArray[4] = 100;
        nArray[5] = 101;
        nArray[6] = 102;
        nArray[7] = 103;
        nArray[8] = 106;
        nArray[9] = 107;
        nArray[10] = 120;
        nArray[11] = 121;
        nArray[12] = 122;
        nArray[13] = 123;
        nArray[14] = 124;
        nArray[15] = 125;
        nArray[16] = 126;
        nArray[17] = 150;
        nArray[18] = 151;
        nArray[19] = 152;
        nArray[20] = 200;
        nArray[21] = 201;
        nArray[22] = 202;
        nArray[23] = 203;
        nArray[24] = 247;
        nArray[25] = 248;
        nArray[26] = 153;
        this.dbfIds = nArray;
    }

    public static DbfEncodings getInstance() {
        if (theInstance == null) {
            theInstance = new DbfEncodings();
        }
        return theInstance;
    }

    public String getCharsetForDbfId(int i) {
        String cCP = null;
        switch (i) {
            case 0: {
                cCP = "UNKNOWN";
                break;
            }
            case 1: {
                cCP = "US-ASCII";
                break;
            }
            case 2: {
                cCP = "Cp850";
                break;
            }
            case 3: {
                cCP = "Cp1252";
                break;
            }
            case 4: {
                cCP = "MacRoman";
                break;
            }
            case 100: {
                cCP = "Cp852";
                break;
            }
            case 101: {
                cCP = "Cp866";
                break;
            }
            case 102: {
                cCP = "Cp865";
                break;
            }
            case 103: {
                cCP = "Cp861";
                break;
            }
            case 104: {
                cCP = "Cp895";
                break;
            }
            case 105: {
                cCP = "Cp620";
                break;
            }
            case 106: {
                cCP = "Cp737";
                break;
            }
            case 107: {
                cCP = "Cp857";
                break;
            }
            case 120: {
                cCP = "Big5";
                break;
            }
            case 121: {
                cCP = "Cp949";
                break;
            }
            case 122: {
                cCP = "GB2312";
                break;
            }
            case 123: {
                cCP = "EUC-JP";
                break;
            }
            case 124: {
                cCP = "Cp838";
                break;
            }
            case 125: {
                cCP = "windows-1255";
                break;
            }
            case 126: {
                cCP = "Cp1256";
                break;
            }
            case 150: {
                cCP = "cyrillic";
                break;
            }
            case 151: {
                cCP = "macintosh";
                break;
            }
            case 152: {
                cCP = "MacGreek";
                break;
            }
            case 200: {
                cCP = "Cp1250";
                break;
            }
            case 201: {
                cCP = "Cp1251";
                break;
            }
            case 202: {
                cCP = "Cp1254";
                break;
            }
            case 203: {
                cCP = "ISO-8859-7";
                break;
            }
            case 247: {
                cCP = "ISO-8859-1";
                break;
            }
            case 248: {
                cCP = "ISO-8859-15";
                break;
            }
            case 153: {
                cCP = "UTF-8";
            }
        }
        return cCP;
    }

    public short getDbfIdForCharset(Charset charset) {
        int dbfId = 0;
        String s = charset.name();
        if (s.equalsIgnoreCase("US-ASCII")) {
            dbfId = 1;
        } else if (s.equalsIgnoreCase("Cp850")) {
            dbfId = 2;
        } else if (s.equalsIgnoreCase("Cp1252")) {
            dbfId = 3;
        } else if (s.equalsIgnoreCase("MacRoman")) {
            dbfId = 4;
        } else if (s.equalsIgnoreCase("Cp852")) {
            dbfId = 100;
        } else if (s.equalsIgnoreCase("Cp865")) {
            dbfId = 101;
        } else if (s.equalsIgnoreCase("Cp866")) {
            dbfId = 102;
        } else if (s.equalsIgnoreCase("Cp861")) {
            dbfId = 103;
        } else if (s.equalsIgnoreCase("Cp895")) {
            dbfId = 104;
        } else if (s.equalsIgnoreCase("Cp620")) {
            dbfId = 105;
        } else if (s.equalsIgnoreCase("Cp737")) {
            dbfId = 106;
        } else if (s.equalsIgnoreCase("Cp857")) {
            dbfId = 107;
        } else if (s.equalsIgnoreCase("Big5")) {
            dbfId = 120;
        } else if (s.equalsIgnoreCase("Cp949")) {
            dbfId = 121;
        } else if (s.equalsIgnoreCase("GB2312")) {
            dbfId = 122;
        } else if (s.equalsIgnoreCase("EUC-JP")) {
            dbfId = 123;
        } else if (s.equalsIgnoreCase("Cp838")) {
            dbfId = 124;
        } else if (s.equalsIgnoreCase("windows-1255")) {
            dbfId = 125;
        } else if (s.equalsIgnoreCase("Cp1256")) {
            dbfId = 126;
        } else if (s.equalsIgnoreCase("windows-1256")) {
            dbfId = 126;
        } else if (s.equalsIgnoreCase("cyrillic")) {
            dbfId = 150;
        } else if (s.equalsIgnoreCase("macintosh")) {
            dbfId = 151;
        } else if (s.equalsIgnoreCase("MacGreek")) {
            dbfId = 152;
        } else if (s.equalsIgnoreCase("Cp1250")) {
            dbfId = 200;
        } else if (s.equalsIgnoreCase("windows-1250")) {
            dbfId = 200;
        } else if (s.equalsIgnoreCase("Cp1251")) {
            dbfId = 201;
        } else if (s.equalsIgnoreCase("windows-1251")) {
            dbfId = 201;
        } else if (s.equalsIgnoreCase("Cp1254")) {
            dbfId = 202;
        } else if (s.equalsIgnoreCase("windows-1254")) {
            dbfId = 202;
        } else if (s.equalsIgnoreCase("ISO-8859-7")) {
            dbfId = 203;
        } else if (s.equalsIgnoreCase("ISO-8859-1")) {
            dbfId = 247;
        } else if (s.equalsIgnoreCase("ISO-8859-15")) {
            dbfId = 248;
        } else if (s.equalsIgnoreCase("UTF-8")) {
            dbfId = 153;
        }
        LOGGER.debug((Object)("getDbfIdForCharset " + s + " dbfId = " + dbfId));
        return (short)dbfId;
    }

    public int[] getSupportedDbfLanguageIDs() {
        return this.dbfIds;
    }
}

