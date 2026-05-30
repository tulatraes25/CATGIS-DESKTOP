/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import com.vividsolutions.wms.BoundingBox;
import com.vividsolutions.wms.Capabilities;
import com.vividsolutions.wms.FeatureInfoRequest;
import com.vividsolutions.wms.MapLayer;
import com.vividsolutions.wms.MapRequest;
import com.vividsolutions.wms.MapStyle;
import com.vividsolutions.wms.WMService;
import es.kosmo.core.crs.CrsAxisOrder;
import es.kosmo.core.crs.CrsRepositoryManager;
import es.kosmo.core.model.sdi.BasicAuthentificationData;
import java.awt.Image;
import java.awt.MediaTracker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.swing.JButton;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsException;
import org.saig.core.styling.Style;
import org.saig.core.util.I18NUnsupportedOperationException;
import org.saig.jump.lang.I18N;

public class WMSLayer
extends AbstractLayerable
implements Cloneable {
    private static final Logger LOGGER = Logger.getLogger(WMSLayer.class);
    protected Integer id;
    protected String format;
    protected String informationFormat;
    private int informationFeatureCount = 1;
    protected String exceptionFormat;
    protected boolean transparent;
    protected List<String> layerNames;
    protected String srs;
    protected String time;
    protected int alpha = 255;
    protected WMService service;
    protected Blackboard blackboard = new Blackboard();
    protected String serverURL;
    protected boolean lastImageWrong = false;
    protected String lastExceptionMessage = "";
    protected IProjection projection;
    protected boolean useDeclaredCapabilitiesURLs = false;
    protected String serviceVersion;
    protected String vendorParameters;
    protected CrsAxisOrder axisOrder;
    protected BasicAuthentificationData basicAuthData;

    public WMSLayer() {
    }

    public WMSLayer(LayerManager layerManager, WMService initializedService, String srs, List<String> layerNames, String format, String time) {
        super(initializedService.getTitle(), layerManager);
        this.setService(initializedService);
        this.setSrs(srs);
        this.setTime(time);
        this.transparent = initializedService.isTransparent();
        this.layerNames = new ArrayList<String>(layerNames);
        this.setFormat(format);
        this.getBlackboard().put(RenderingManager.USE_MULTI_RENDERING_THREAD_QUEUE_KEY, true);
        this.getBlackboard().put(LayerNameRenderer.USE_CLOCK_ANIMATION_KEY, true);
    }

    public WMSLayer(LayerManager layerManager, String serverURL, String srs, List<String> layerNames, String format, String version, HashMap<String, String> selectedStyles) throws Exception {
        this(layerManager, WMSLayer.initializedService(serverURL, version), srs, layerNames, format, null);
        for (String layerName : layerNames) {
            String styleName;
            MapLayer layer = this.getService().getCapabilities().getTopLayer().getMapLayer(layerName);
            MapStyle style = layer.getStyle(styleName = selectedStyles.get(layerName));
            if (style == null) continue;
            style.setSelected(true, true);
        }
    }

    private static WMService initializedService(String serverURL, String version) throws Exception {
        WMService initializedService = new WMService(serverURL);
        initializedService.initialize();
        return initializedService;
    }

    public void setService(WMService service) {
        this.service = service;
        this.serverURL = service.getServerUrl();
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public Image createImage(LayerViewPanel panel) throws Exception {
        Image image = null;
        try {
            LOGGER.info((Object)I18N.getString("com.vividsolutions.jump.workbench.model.WMSLayer.requesting-wms-image"));
            image = this.createMapRequest(panel).getImage();
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
            this.lastExceptionMessage = e.getMessage();
        }
        if (image == null) {
            this.lastImageWrong = true;
        } else {
            this.lastImageWrong = false;
            this.lastExceptionMessage = "";
        }
        MediaTracker mt = new MediaTracker(new JButton());
        mt.addImage(image, 0);
        try {
            mt.waitForID(0);
        }
        catch (InterruptedException e) {
            Assert.shouldNeverReachHere();
        }
        return image;
    }

    private BoundingBox toBoundingBox(Envelope e) {
        return new BoundingBox(this.srs, e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
    }

    public MapRequest createMapRequest(LayerViewPanel panel) throws IOException {
        MapRequest request = this.getService().createMapRequest();
        request.setBoundingBox(this.toBoundingBox(panel.getViewport().getEnvelopeInModelCoordinates()));
        request.setFormat(this.format);
        request.setImageWidth(panel.getWidth());
        request.setImageHeight(panel.getHeight());
        request.setLayers(this.layerNames);
        request.setTransparent(this.transparent);
        request.setTime(this.time);
        request.setExceptionFormat(this.exceptionFormat);
        request.setLayerStyles(this.getLayerStyles());
        return request;
    }

    public MapRequest createMapRequest(Envelope envelope, int width, int height) throws IOException {
        MapRequest request = this.getService().createMapRequest();
        request.setBoundingBox(this.toBoundingBox(envelope));
        request.setFormat(this.format);
        request.setImageWidth(width);
        request.setImageHeight(height);
        request.setLayers(this.layerNames);
        request.setTransparent(this.transparent);
        request.setTime(this.time);
        request.setExceptionFormat(this.exceptionFormat);
        request.setLayerStyles(this.getLayerStyles());
        return request;
    }

    public FeatureInfoRequest createFeatureInfoRequest(int x, int y, Envelope bbox, int width, int height) throws IOException {
        FeatureInfoRequest request = this.getService().createFeatureInfoRequest();
        ArrayList<String> queryablesLayers = new ArrayList<String>();
        for (String layerName : this.layerNames) {
            MapLayer layer = this.getService().getCapabilities().getTopLayer().getMapLayer(layerName);
            if (!layer.isQueryable()) continue;
            queryablesLayers.add(layerName);
        }
        request.setLayers(queryablesLayers);
        request.setX(x);
        request.setY(y);
        request.setBbox(bbox);
        request.setSrs(this.srs);
        request.setImgWidth(width);
        request.setImgHeight(height);
        return request;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
        if (this.service != null) {
            this.service.setFormat(format);
        }
    }

    public void addLayerName(String layerName) {
        this.layerNames.add(layerName);
    }

    public List<String> getLayerNames() {
        return this.layerNames;
    }

    public Object clone() throws CloneNotSupportedException {
        WMSLayer clone = (WMSLayer)super.clone();
        clone.layerNames = new ArrayList<String>(this.layerNames);
        try {
            clone.setService(this.getService());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        clone.setServerURL(this.getServerURL());
        clone.setProjection(this.getProjection());
        clone.setFormat(this.getFormat());
        return clone;
    }

    public void removeAllLayerNames() {
        this.layerNames.clear();
    }

    @Override
    public Blackboard getBlackboard() {
        return this.blackboard;
    }

    public WMService getService() throws IOException {
        if (!this.service.isInitialized()) {
            try {
                this.service.initialize();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                this.lastImageWrong = true;
                this.lastExceptionMessage = String.valueOf(I18N.getMessage("com.vividsolutions.jump.workbench.model.WMSLayer.An-error-has-been-produced-while-connecting-with-the-server-{0}", new Object[]{this.service.getServerUrl()})) + " - " + e.getMessage();
            }
        }
        return this.service;
    }

    public String getServerURL() {
        return this.serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getWmsVersion() {
        return this.service.getVersion();
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isTime() {
        return this.time != null;
    }

    public List<MapStyle> getLayerStyles() {
        ArrayList<MapStyle> styles = new ArrayList<MapStyle>();
        for (String layerName : this.layerNames) {
            try {
                MapLayer layer = this.getService().getCapabilities().getTopLayer().getMapLayer(layerName);
                MapStyle style = layer.getSelectedStyle();
                if (style == null) {
                    return new ArrayList<MapStyle>();
                }
                styles.add(style);
            }
            catch (IOException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return styles;
    }

    public List<MapLayer> getMapLayers() {
        ArrayList<MapLayer> mapLayers = new ArrayList<MapLayer>();
        try {
            Capabilities capabilities = this.getService().getCapabilities();
            if (capabilities == null) {
                return new ArrayList<MapLayer>();
            }
            for (String layerName : this.layerNames) {
                MapLayer map = capabilities.getTopLayer().getMapLayer(layerName);
                if (map == null) continue;
                mapLayers.add(map);
            }
            return mapLayers;
        }
        catch (IOException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return new ArrayList<MapLayer>();
        }
    }

    public Envelope getFullEnvelope() {
        Envelope envelope = new Envelope();
        List<MapLayer> mapLayers = this.getMapLayers();
        for (MapLayer map : mapLayers) {
            Envelope mapEnvelope = map.getEnvelope(this.getSrs());
            if (mapEnvelope == null) continue;
            envelope.expandToInclude(mapEnvelope);
        }
        return envelope;
    }

    public void setLayerNames(List<String> layerNames) {
        this.layerNames = layerNames;
    }

    @Override
    public boolean isRaster() {
        return false;
    }

    @Override
    public Style getModelStyle() {
        return null;
    }

    public void setTransparent(boolean transp) {
        this.transparent = transp;
        if (this.service != null) {
            this.service.setTransparent(transp);
        }
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public boolean isLastImageWrong() {
        return this.lastImageWrong;
    }

    @Override
    public IProjection getProjection() {
        if (this.projection == null) {
            try {
                this.projection = CrsRepositoryManager.getInstance().getCRS(this.getSrs());
            }
            catch (CrsException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return this.projection;
    }

    @Override
    public void setProjection(IProjection projection) {
        this.projection = projection;
        this.srs = projection.getAbrev();
    }

    public String getSrs() {
        return this.srs;
    }

    public void setSrs(String srs) {
        this.srs = srs;
    }

    @Override
    public ICoordTrans getCoordTrans() {
        throw new I18NUnsupportedOperationException(I18N.getString("com.vividsolutions.jump.workbench.model.WMSLayer.unsupported-operation-in-WMS-layers"));
    }

    @Override
    public void setCoordTrans(ICoordTrans coordTrans) {
        throw new I18NUnsupportedOperationException(I18N.getString("com.vividsolutions.jump.workbench.model.WMSLayer.unsupported-operation-in-WMS-layers"));
    }

    public String getLastExceptionMessage() {
        return this.lastExceptionMessage;
    }

    public String getSRS() {
        return this.srs;
    }

    public void setSRS(String srs) {
        this.srs = srs;
    }

    public HashMap<String, String> getSelectedStyles() {
        HashMap<String, String> styles = new HashMap<String, String>();
        for (String layerName : this.layerNames) {
            try {
                MapLayer layer = this.getService().getCapabilities().getTopLayer().getMapLayer(layerName);
                MapStyle style = layer.getSelectedStyle();
                if (style == null) {
                    return new HashMap<String, String>();
                }
                styles.put(layer.getName(), style.getName());
            }
            catch (IOException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return styles;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public void dispose() {
        this.layerNames = null;
        this.service = null;
        this.blackboard = null;
        this.projection = null;
    }

    public String getInformationFormat() {
        return this.informationFormat;
    }

    public void setInformationFormat(String informationFormat) {
        this.informationFormat = informationFormat;
        if (this.service != null) {
            this.service.setInformationFormat(informationFormat);
        }
    }

    public String getExceptionFormat() {
        return this.exceptionFormat;
    }

    public void setExceptionFormat(String exceptionFormat) {
        this.exceptionFormat = exceptionFormat;
        if (this.service != null) {
            this.service.setExceptionFormat(exceptionFormat);
        }
    }

    public boolean isUseDeclaredCapabilitiesURLs() {
        return this.useDeclaredCapabilitiesURLs;
    }

    public void setUseDeclaredCapabilitiesURLs(boolean useURLs) {
        this.useDeclaredCapabilitiesURLs = useURLs;
        if (this.service != null) {
            this.service.setUseDeclaredCapabilitiesURLs(this.useDeclaredCapabilitiesURLs);
        }
    }

    @Override
    public String getTitle() {
        return this.getName();
    }

    @Override
    public void addLocale(Locale locale) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void removeLocale(Locale locale) {
        throw new I18NUnsupportedOperationException();
    }

    public String getServiceVersion() {
        return this.serviceVersion;
    }

    public void setServiceVersion(String version) {
        this.serviceVersion = version;
        if (this.service != null) {
            this.service.setWmsVersion(this.serviceVersion);
        }
    }

    public String getVendorParameters() {
        return this.vendorParameters;
    }

    public void setVendorParameters(String parameters) {
        this.vendorParameters = parameters;
        if (this.service != null) {
            this.service.setVendorParameters(this.vendorParameters);
        }
    }

    public CrsAxisOrder getAxisOrder() {
        return this.axisOrder;
    }

    public void setAxisOrder(CrsAxisOrder axisOrder) {
        this.axisOrder = axisOrder;
        if (this.service != null) {
            this.service.setAxisOrder(axisOrder);
        }
    }

    public int getInformationFeatureCount() {
        return this.informationFeatureCount;
    }

    public void setInformationFeatureCount(int informationFeatureCount) {
        this.informationFeatureCount = informationFeatureCount;
        if (this.service != null) {
            this.service.setInformationFeatureCount(informationFeatureCount);
        }
    }

    public BasicAuthentificationData getBasicAuthData() {
        return this.basicAuthData;
    }

    public void setBasicAuthData(BasicAuthentificationData basicAuthData) {
        this.basicAuthData = basicAuthData;
        if (this.service != null) {
            this.service.setBasicAuthData(basicAuthData);
        }
    }
}

