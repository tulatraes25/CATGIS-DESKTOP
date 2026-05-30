/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.wms;

import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.WMService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Capabilities {
    private MapLayer topLayer;
    private String title;
    private String description;
    private List<String> mapFormats;
    private List<String> infoFormats;
    private List<String> exceptionFormats;
    private WMService service;
    private String getMapUrl;
    private String getFeatureInfoUrl;
    private String capsString;

    public Capabilities(WMService service, String title, MapLayer topLayer, Collection<String> mapFormats, String description) {
        this(service, title, topLayer, mapFormats, description, service.getServerUrl(), service.getServerUrl(), new ArrayList<String>(), new ArrayList<String>(), "");
    }

    public Capabilities(WMService service, String title, MapLayer topLayer, Collection<String> mapFormats, String description, String getMapUrl, String getFeatureInfoUrl, Collection<String> availableInformationFormats, Collection<String> availableExceptionFormats, String capabilitiesString) {
        this.service = service;
        this.title = title;
        this.topLayer = topLayer;
        this.mapFormats = new ArrayList<String>(mapFormats);
        this.description = description;
        this.getMapUrl = getMapUrl;
        this.getFeatureInfoUrl = getFeatureInfoUrl;
        this.infoFormats = new ArrayList<String>(availableInformationFormats);
        this.exceptionFormats = new ArrayList<String>(availableExceptionFormats);
        this.capsString = capabilitiesString;
    }

    public WMService getService() {
        return this.service;
    }

    public MapLayer getTopLayer() {
        return this.topLayer;
    }

    public String getTitle() {
        return this.title;
    }

    public String[] getMapFormats() {
        String[] formats = new String[this.mapFormats.size()];
        Iterator<String> it = this.mapFormats.iterator();
        int i = 0;
        while (it.hasNext()) {
            formats[i++] = it.next();
        }
        return formats;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getInfoFormats() {
        return this.infoFormats;
    }

    public List<String> getExceptionFormats() {
        return this.exceptionFormats;
    }

    public String getMapUrl() {
        return this.getMapUrl;
    }

    public String getFeatureInfoUrl() {
        return this.getFeatureInfoUrl;
    }

    public String getCapabilitiesAsString() {
        return this.capsString;
    }
}

