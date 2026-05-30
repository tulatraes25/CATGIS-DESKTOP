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

public class TrData
implements Comparable {
    private String authority = null;
    private int code = 0;
    private String name = null;
    private String crsSource = null;
    private String crsTarget = null;
    private String details = null;
    private Date date = null;
    private Properties properies = new Properties();

    public TrData(String authority, int code, String name, String crsSource, String crsTarget, String detalis) {
        this.authority = authority;
        this.code = code;
        this.name = name;
        this.crsSource = crsSource;
        this.crsTarget = crsTarget;
        this.details = detalis;
        this.date = DateTime.getCurrentDate();
    }

    public TrData(String authority, int code, String name, String crsSource, String crsTarget, String detalis, Date date) {
        this.authority = authority;
        this.code = code;
        this.name = name;
        this.crsSource = crsSource;
        this.crsTarget = crsTarget;
        this.details = detalis;
        this.date = date;
    }

    public TrData() {
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
        TrData trData = (TrData)o;
        return this.date.compareTo(trData.getDate());
    }

    public String getCrsSource() {
        return this.crsSource;
    }

    public void setCrsSource(String crsSource) {
        this.crsSource = crsSource;
    }

    public String getCrsTarget() {
        return this.crsTarget;
    }

    public void setCrsTarget(String crsTarget) {
        this.crsTarget = crsTarget;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}

