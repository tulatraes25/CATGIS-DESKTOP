/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.iver.utiles.DateTime
 */
package org.gvsig.crs.persistence;

import com.iver.utiles.DateTime;
import java.util.Date;
import java.util.Properties;

public class CrsData
implements Comparable {
    private String authority = null;
    private int code = 0;
    private String name = null;
    private Date date = null;
    private Properties properies = new Properties();

    public CrsData(String authority, int code, String name) {
        this.authority = authority;
        this.code = code;
        this.name = name;
        this.date = DateTime.getCurrentDate();
    }

    public CrsData(String authority, int code, String name, Date date) {
        this.authority = authority;
        this.code = code;
        this.name = name;
        this.date = date;
    }

    public CrsData() {
    }

    public String getAuthority() {
        return this.authority;
    }

    public int getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public Properties getProperies() {
        return this.properies;
    }

    public void setProperies(Properties properies) {
        this.properies = properies;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return this.date;
    }

    public void updateLastAccess() {
        this.date = DateTime.getCurrentDate();
    }

    public int compareTo(Object o) {
        CrsData crsData = (CrsData)o;
        return this.date.compareTo(crsData.getDate());
    }
}

