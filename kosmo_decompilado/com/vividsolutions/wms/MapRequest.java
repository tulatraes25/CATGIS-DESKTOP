/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.apache.xerces.parsers.DOMParser
 */
package com.vividsolutions.wms;

import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.MapStyle;
import com.vividsolutions.wms.WMService;
import com.vividsolutions.wms.util.XMLTools;
import es.kosmo.core.crs.CrsAxisOrder;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.saig.jump.lang.I18N;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.misc.BASE64Encoder;

public class MapRequest {
    private static final Logger LOGGER = Logger.getLogger(MapRequest.class);
    private WMService service;
    private int imgWidth;
    private int imgHeight;
    private List<String> layerList;
    private List<MapStyle> layerStyles;
    private BoundingBox bbox;
    private boolean transparent;
    private String format;
    private String exceptionFormat;
    private String time;
    private String version = "1.0.0";

    public MapRequest(WMService service) {
        this.service = service;
        this.imgWidth = 100;
        this.imgHeight = 100;
        this.layerList = new ArrayList<String>();
        this.bbox = service.getCapabilities().getTopLayer().getBoundingBox();
        this.transparent = false;
        this.format = null;
    }

    public WMService getService() {
        return this.service;
    }

    public String getFormat() {
        return this.format;
    }

    public String getExceptionFormat() {
        return this.exceptionFormat;
    }

    public int getImageWidth() {
        return this.imgWidth;
    }

    public int getImageHeight() {
        return this.imgHeight;
    }

    public List<String> getLayers() {
        return Collections.unmodifiableList(this.layerList);
    }

    public BoundingBox getBoundingBox() {
        return this.bbox;
    }

    public boolean getTransparent() {
        return this.transparent;
    }

    public String getVersion() {
        return this.version;
    }

    public void setFormat(String format) throws IllegalArgumentException {
        this.format = format;
    }

    public void setImageWidth(int imageWidth) {
        this.imgWidth = imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imgHeight = imageHeight;
    }

    public void setImageSize(int imageWidth, int imageHeight) {
        this.imgWidth = imageWidth;
        this.imgHeight = imageHeight;
    }

    public void setLayers(List<String> layers) {
        this.layerList = layers;
    }

    public void setBoundingBox(BoundingBox bbox) {
        this.bbox = bbox;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
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
        String ver = "REQUEST=map&WMTVER=1.0";
        String srsConstant = "SRS";
        if ("1.1.0".equals(this.version)) {
            ver = "REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.0";
        } else if ("1.1.1".equals(this.version)) {
            ver = "REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1";
        } else if (this.version.startsWith("1.3")) {
            ver = "REQUEST=GetMap&SERVICE=WMS&VERSION=" + this.version;
            srsConstant = "CRS";
        }
        String getMapUrl = this.service.getGetMapUrl();
        if (!StringUtils.contains((String)getMapUrl, (char)'?')) {
            getMapUrl = String.valueOf(getMapUrl) + "?";
        } else if (!StringUtils.endsWith((String)getMapUrl, (String)"&") && !StringUtils.endsWith((String)getMapUrl, (String)"?")) {
            getMapUrl = String.valueOf(getMapUrl) + "&";
        }
        urlBuf.append(String.valueOf(getMapUrl) + ver + "&WIDTH=" + this.imgWidth + "&HEIGHT=" + this.imgHeight);
        try {
            urlBuf.append("&LAYERS=" + URLEncoder.encode(MapRequest.listToString(this.layerList), "ISO-8859-1"));
        }
        catch (UnsupportedEncodingException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
        if (this.transparent) {
            urlBuf.append("&TRANSPARENT=TRUE");
        } else {
            urlBuf.append("&TRANSPARENT=FALSE");
        }
        if (this.format != null) {
            urlBuf.append("&FORMAT=" + this.format.replaceAll(" ", "%20"));
        }
        if (this.bbox != null) {
            Rectangle2D.Double view = this.version.startsWith("1.3") ? (this.service.getAxisOrder().equals((Object)CrsAxisOrder.NORTH_EAST) ? new Rectangle2D.Double(this.bbox.getMinY(), this.bbox.getMinX(), this.bbox.getMaxY() - this.bbox.getMinY(), this.bbox.getMaxX() - this.bbox.getMinX()) : new Rectangle2D.Double(this.bbox.getMinX(), this.bbox.getMinY(), this.bbox.getMaxX() - this.bbox.getMinX(), this.bbox.getMaxY() - this.bbox.getMinY())) : new Rectangle2D.Double(this.bbox.getMinX(), this.bbox.getMinY(), this.bbox.getMaxX() - this.bbox.getMinX(), this.bbox.getMaxY() - this.bbox.getMinY());
            urlBuf.append("&BBOX=" + view.getMinX() + "," + view.getMinY() + "," + view.getMaxX() + "," + view.getMaxY());
            if (this.bbox.getSRS() != null && !this.bbox.getSRS().equals("LatLon")) {
                urlBuf.append("&" + srsConstant + "=" + this.bbox.getSRS());
            }
        }
        if (this.time != null) {
            urlBuf.append("&TIME=" + this.time);
        }
        ArrayList<String> styleNames = new ArrayList<String>();
        for (MapStyle element : this.layerStyles) {
            styleNames.add(element.getName());
        }
        try {
            urlBuf.append("&STYLES=" + URLEncoder.encode(MapRequest.listToString(styleNames), "ISO-8859-1"));
        }
        catch (UnsupportedEncodingException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        if (StringUtils.isNotEmpty((String)this.service.getVendorParameters())) {
            urlBuf.append("&" + this.service.getVendorParameters());
        }
        LOGGER.info((Object)urlBuf.toString());
        return new URL(urlBuf.toString());
    }

    public Image getImage() throws MalformedURLException {
        URL url = null;
        try {
            int responseCode;
            url = this.getURL();
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (this.service.getBasicAuthData() != null) {
                String userPassword = String.valueOf(this.service.getBasicAuthData().getUserName()) + ":" + this.service.getBasicAuthData().getPassword();
                String encoding = new BASE64Encoder().encode(userPassword.getBytes());
                conn.setRequestProperty("Authorization", "Basic " + encoding);
            }
            if ((responseCode = conn.getResponseCode()) != 200) {
                throw new MalformedURLException(String.valueOf(I18N.getString("com.vividsolutions.wms.MapRequest.Connection-non-valid-with-the-server")) + " - " + I18N.getMessage("com.vividsolutions.wms.MapRequest.HTTP-error-{0}", new Object[]{responseCode}));
            }
            String type = conn.getContentType();
            if (type != null && type.startsWith("image")) {
                BufferedImage bi = ImageIO.read(conn.getInputStream());
                if (bi == null) {
                    throw new MalformedURLException(I18N.getString("com.vividsolutions.wms.MapRequest.The-WMS-server-sent-an-invalid-image"));
                }
                return bi;
            }
            String response = MapRequest.inputStreamToString(type, conn.getInputStream());
            throw new MalformedURLException(I18N.getMessage("com.vividsolutions.wms.MapRequest.server-failure-{0}", new Object[]{response}));
        }
        catch (IOException e) {
            LOGGER.warn((Object)I18N.getMessage("com.vividsolutions.wms.MapRequest.The-URL-{0}-can-not-be-loaded-the-exception-produced-was-{1}", new Object[]{url.toString(), e.getMessage()}));
            throw new MalformedURLException(String.valueOf(I18N.getString("com.vividsolutions.wms.MapRequest.The-WMS-server-sent-an-invalid-image")) + ": " + e.getMessage());
        }
    }

    private static String inputStreamToString(String type, InputStream inputStream) throws IOException {
        String s = null;
        if (type.equalsIgnoreCase("XML") || type.equalsIgnoreCase("application/vnd.ogc.se_xml")) {
            try {
                DOMParser parser = new DOMParser();
                parser.setFeature("http://xml.org/sax/features/validation", false);
                parser.parse(new InputSource(inputStream));
                Document doc = parser.getDocument();
                Node serviceExceptionNode = XMLTools.simpleXPath(doc, "ServiceExceptionReport/ServiceException");
                s = serviceExceptionNode.getFirstChild().getNodeValue();
            }
            catch (SAXException saxe) {
                throw new IOException(saxe.toString());
            }
        } else {
            InputStreamReader ireader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(ireader);
            StringBuffer sb = new StringBuffer(50000);
            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            s = sb.toString();
            br.close();
        }
        return s;
    }

    public void setVersion(String ver) {
        this.version = ver;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<MapStyle> getLayerStyles() {
        return this.layerStyles;
    }

    public void setLayerStyles(List<MapStyle> styles) {
        this.layerStyles = styles;
    }

    public void setExceptionFormat(String exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
    }
}

