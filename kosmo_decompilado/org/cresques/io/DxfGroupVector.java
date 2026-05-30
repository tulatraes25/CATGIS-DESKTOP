/*
 * Decompiled with CFR 0.152.
 */
package org.cresques.io;

import java.util.Vector;
import org.cresques.io.DxfGroup;

public class DxfGroupVector
extends Vector<DxfGroup> {
    private static final long serialVersionUID = -3370601314380922368L;

    public boolean hasCode(int code) {
        DxfGroup grp = null;
        int i = 0;
        while (i < this.size()) {
            grp = (DxfGroup)this.get(i);
            if (grp.code == code) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public Object getData(int code) {
        DxfGroup grp = null;
        int i = 0;
        while (i < this.size()) {
            grp = (DxfGroup)this.get(i);
            if (grp.code == code) {
                return grp.data;
            }
            ++i;
        }
        return null;
    }

    public String getDataAsString(int code) {
        return (String)this.getData(code);
    }

    public double getDataAsDouble(int code) {
        Number f = (Number)this.getData(code);
        if (f == null) {
            return 0.0;
        }
        return f.doubleValue();
    }

    public int getDataAsInt(int code) {
        Number i = (Number)this.getData(code);
        if (i == null) {
            return 0;
        }
        return i.intValue();
    }

    @Override
    public String toString() {
        String str = "DxfGroupVector[";
        DxfGroup grp = null;
        int i = 0;
        while (i < this.size()) {
            grp = (DxfGroup)this.get(i);
            str = String.valueOf(str) + "(" + grp.code + ":" + grp.data + "),";
            ++i;
        }
        return str;
    }
}

