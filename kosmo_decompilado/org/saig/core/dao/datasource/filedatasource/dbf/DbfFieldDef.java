/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.dao.datasource.filedatasource.dbf;

import com.vividsolutions.jump.io.EndianDataInputStream;
import java.io.IOException;
import org.saig.core.dao.datasource.filedatasource.dbf.DbfConsts;
import org.saig.jump.lang.I18N;

public class DbfFieldDef
implements DbfConsts {
    static final boolean DEBUG = false;
    public StringBuffer fieldname = new StringBuffer(11);
    public char fieldtype;
    public int fieldstart;
    public int fieldlen;
    public int fieldnumdec;

    public DbfFieldDef() {
    }

    public DbfFieldDef(String fieldname, char fieldtype, int fieldlen, int fieldnumdec) {
        this.fieldname = new StringBuffer(fieldname);
        this.fieldname.setLength(11);
        this.fieldtype = fieldtype;
        this.fieldlen = fieldlen;
        this.fieldnumdec = fieldnumdec;
    }

    public String toString() {
        return new String(this.fieldname + " " + this.fieldtype + " " + this.fieldlen + "." + this.fieldnumdec);
    }

    public String getFieldName() {
        return this.fieldname.toString().trim();
    }

    public void setup(int pos, EndianDataInputStream dFile) throws IOException {
        byte[] strbuf = new byte[11];
        int j = -1;
        int term = -1;
        int i = 0;
        while (i < 11) {
            byte b = dFile.readByteLE();
            if (b == 0) {
                if (term == -1) {
                    term = j;
                }
            } else {
                strbuf[++j] = b;
            }
            ++i;
        }
        if (term == -1) {
            term = j;
        }
        String name = new String(strbuf, 0, term + 1);
        this.fieldname.append(name.trim());
        this.fieldtype = (char)dFile.readUnsignedByteLE();
        this.fieldstart = pos;
        dFile.skipBytes(4);
        switch (this.fieldtype) {
            case 'C': 
            case 'D': 
            case 'G': 
            case 'L': 
            case 'M': 
            case 'c': {
                this.fieldlen = dFile.readUnsignedByteLE();
                this.fieldnumdec = dFile.readUnsignedByteLE();
                this.fieldnumdec = 0;
                break;
            }
            case 'F': 
            case 'N': 
            case 'f': 
            case 'n': {
                this.fieldlen = dFile.readUnsignedByteLE();
                this.fieldnumdec = dFile.readUnsignedByteLE();
                break;
            }
            default: {
                System.out.println(I18N.getMessage("org.saig.core.dao.datasource.filedatasource.dbf.DbfFieldDef.help-wrong-field-type-{0}", new Object[]{String.valueOf(this.fieldtype)}));
            }
        }
        dFile.skipBytes(14);
    }
}

