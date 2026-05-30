/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.wms;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.wms.WMService;
import es.kosmo.core.crs.CrsAxisOrder;
import java.awt.geom.Rectangle2D;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class FeatureInfoRequest {
    private static Logger LOGGER = Logger.getLogger(FeatureInfoRequest.class);
    private static final String DEFAULT_INFO_FORMAT = "text/html";
    private WMService service;
    private List<String> layerList;
    private String format;
    private int x;
    private int y;
    private int imgWidth;
    private int imgHeight;
    private String imgFormat;
    private Envelope bbox;
    private String srs;
    private String version = "1.0.0";

    public FeatureInfoRequest(WMService service) {
        this.service = service;
        this.layerList = new ArrayList<String>();
        this.format = null;
    }

    public WMService getService() {
        return this.service;
    }

    public String getFormat() {
        return this.format;
    }

    public List<String> getLayers() {
        return Collections.unmodifiableList(this.layerList);
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setLayers(List<String> layers) {
        this.layerList = layers;
    }

    public static String listToString(List<String> list) {
        Iterator<String> it = list.iterator();
        StringBuffer buf = new StringBuffer();
        while (it.hasNext()) {
            String layer = it.next();
            buf.append(layer);
            if (!it.hasNext()) continue;
            buf.append(",");
        }
        return buf.toString();
    }

    public URL getURL() throws MalformedURLException {
        StringBuffer urlBuf = new StringBuffer();
        String ver = "REQUEST=GetFeatureInfo&WMTVER=1.0";
        String srsConstant = "SRS";
        if ("1.1.0".equals(this.version)) {
            ver = "REQUEST=GetFeatureInfo&SERVICE=WMS&VERSION=1.1.0";
        } else if ("1.1.1".equals(this.version)) {
            ver = "REQUEST=GetFeatureInfo&SERVICE=WMS&VERSION=1.1.1";
        } else if (this.version.startsWith("1.3")) {
            ver = "REQUEST=GetFeatureInfo&SERVICE=WMS&VERSION=" + this.version;
            srsConstant = "CRS";
        }
        String getFeatureInfoUrl = this.service.getGetFeatureInfoUrl();
        if (!StringUtils.contains((String)getFeatureInfoUrl, (char)'?')) {
            getFeatureInfoUrl = String.valueOf(getFeatureInfoUrl) + "?";
        } else if (!StringUtils.endsWith((String)getFeatureInfoUrl, (String)"&") && !StringUtils.endsWith((String)getFeatureInfoUrl, (String)"?")) {
            getFeatureInfoUrl = String.valueOf(getFeatureInfoUrl) + "&";
        }
        urlBuf.append(String.valueOf(getFeatureInfoUrl) + ver + "&WIDTH=" + this.imgWidth + "&HEIGHT=" + this.imgHeight + "&FORMAT=" + this.imgFormat);
        urlBuf.append("&LAYERS=" + FeatureInfoRequest.listToString(this.layerList));
        urlBuf.append("&QUERY_LAYERS=" + FeatureInfoRequest.listToString(this.layerList));
        urlBuf.append("&INFO_FORMAT=");
        if (StringUtils.isEmpty((String)this.format)) {
            urlBuf.append(DEFAULT_INFO_FORMAT);
        } else {
            urlBuf.append(this.format);
        }
        urlBuf.append("&" + srsConstant + "=" + this.srs);
        Rectangle2D.Double view = this.version.startsWith("1.3") ? (this.service.getAxisOrder().equals((Object)CrsAxisOrder.NORTH_EAST) ? new Rectangle2D.Double(this.bbox.getMinY(), this.bbox.getMinX(), this.bbox.getMaxY() - this.bbox.getMinY(), this.bbox.getMaxX() - this.bbox.getMinX()) : new Rectangle2D.Double(this.bbox.getMinX(), this.bbox.getMinY(), this.bbox.getMaxX() - this.bbox.getMinX(), this.bbox.getMaxY() - this.bbox.getMinY())) : new Rectangle2D.Double(this.bbox.getMinX(), this.bbox.getMinY(), this.bbox.getMaxX() - this.bbox.getMinX(), this.bbox.getMaxY() - this.bbox.getMinY());
        urlBuf.append("&BBOX=" + view.getMinX() + "," + view.getMinY() + "," + view.getMaxX() + "," + view.getMaxY());
        if (this.version.startsWith("1.3")) {
            urlBuf.append("&I=" + this.x + "&J=" + this.y);
        } else {
            urlBuf.append("&X=" + this.x + "&Y=" + this.y);
        }
        if (this.service.getInformationFeatureCount() > 1) {
            urlBuf.append("&FEATURE_COUNT=" + this.service.getInformationFeatureCount());
        }
        LOGGER.info((Object)("GetFeatureInfo request " + urlBuf.toString()));
        return new URL(urlBuf.toString());
    }

    public void setVersion(String ver) {
        this.version = ver;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Envelope getBbox() {
        return this.bbox;
    }

    public void setBbox(Envelope bbox) {
        this.bbox = bbox;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public void setImgFormat(String imageFormat) {
        this.imgFormat = imageFormat;
    }
}

