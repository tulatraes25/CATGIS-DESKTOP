/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf;

public class LineTypeItem {
    public static final int DASH = 0;
    public static final int DOT = 1;
    public static final int EMPTY = 2;
    int type = 0;
    double length = 0.0;

    public LineTypeItem(int type, double length) {
        this.type = type;
        this.length = length;
    }

    public LineTypeItem(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getLength() {
        switch (this.type) {
            case 0: {
                return this.length;
            }
            case 2: {
                return -this.length;
            }
        }
        return 0.0;
    }

    public void setLength(double length) {
        this.length = length;
    }
}

