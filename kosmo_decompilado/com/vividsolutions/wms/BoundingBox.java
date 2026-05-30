/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.wms;

public class BoundingBox {
    private double minx;
    private double miny;
    private double maxx;
    private double maxy;
    private String epsg;

    public BoundingBox(String epsg, double minx, double miny, double maxx, double maxy) {
        this.epsg = epsg;
        this.minx = minx;
        this.miny = miny;
        this.maxx = maxx;
        this.maxy = maxy;
    }

    public String getSRS() {
        return this.epsg;
    }

    public double getMinX() {
        return this.minx;
    }

    public double getMinY() {
        return this.miny;
    }

    public double getMaxX() {
        return this.maxx;
    }

    public double getMaxY() {
        return this.maxy;
    }
}

