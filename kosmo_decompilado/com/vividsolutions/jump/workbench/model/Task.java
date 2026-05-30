/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.saig.core.ogcservices.wms.context.model.WMSService;
import org.saig.core.printing.legend.Legend;
import org.saig.core.printing.scale.Scale;
import org.saig.core.util.LocaleManager;
import org.saig.core.util.language.ITranslatable;
import org.saig.jump.plugin.rotate.NorthLayerViewPanelListener;
import org.saig.jump.widgets.config.ConfigDefaultViewOptionsPanel;

public class Task
implements LayerManagerProxy,
ITranslatable {
    private static final Logger LOGGER = Logger.getLogger(Task.class);
    protected String name = "";
    protected LayerManager layerManager;
    protected List<NameListener> nameListeners = new ArrayList<NameListener>();
    protected Envelope currentView;
    protected int frameLocationX;
    protected int frameLocationY;
    protected int frameWidth;
    protected int frameHeight;
    protected Scale graphicScale;
    protected List<Legend> legends;
    protected double angle;
    protected NorthLayerViewPanelListener north;
    protected IProjection projection;
    protected String crsDescription;
    protected String crsWKT;
    protected String nadGrid;
    protected boolean targetNad;
    protected int crsCode;
    protected boolean visible = true;
    protected double mapFactor;
    protected String mapUnits;
    protected double altFactor;
    protected String altUnits;
    protected String mapLengthUnit;
    protected String userLengthUnit;
    protected String userAreaUnit;
    protected WMSService wmsService;
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private Map<Locale, String> titleByLang = new HashMap<Locale, String>();

    public Task() {
        this.layerManager = new LayerManager();
    }

    public void add(NameListener nameListener) {
        this.nameListeners.add(nameListener);
    }

    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public LayerManager getLayerManager() {
        return this.layerManager;
    }

    public String toString() {
        return this.getName();
    }

    public String getName() {
        return this.name;
    }

    public void fireNameChanged(String name) {
        for (NameListener nameListener : this.nameListeners) {
            nameListener.taskNameChanged(name);
        }
    }

    public Collection<Category> getCategories() {
        return this.getLayerManager().getCategories();
    }

    public void addCategory(Category category) {
        this.getLayerManager().addCategory(category.getName());
        Category actual = this.getLayerManager().getCategory(category.getName());
        for (Layerable layerable : category.getLayerables()) {
            actual.addPersistentLayerable(layerable);
        }
    }

    public void setCurrentView(Envelope envelope) {
        this.currentView = envelope;
    }

    public Envelope getCurrentView() {
        return this.currentView;
    }

    public int getFrameHeight() {
        return this.frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }

    public int getFrameWidth() {
        return this.frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }

    public int getFrameLocationX() {
        return this.frameLocationX;
    }

    public void setFrameLocationX(int frameLocationX) {
        this.frameLocationX = frameLocationX;
    }

    public int getFrameLocationY() {
        return this.frameLocationY;
    }

    public void setFrameLocationY(int frameLocationY) {
        this.frameLocationY = frameLocationY;
    }

    public String getCrsDescription() {
        return this.crsDescription;
    }

    public void setCrsDescription(String csDescription) {
        this.crsDescription = csDescription;
    }

    public double getAltFactor() {
        return this.altFactor;
    }

    public void setAltFactor(double altFactor) {
        this.altFactor = altFactor;
    }

    public String getAltUnits() {
        return this.altUnits;
    }

    public void setAltUnits(String altUnits) {
        this.altUnits = altUnits;
    }

    public double getMapFactor() {
        return this.mapFactor;
    }

    public void setMapFactor(double mapFactor) {
        this.mapFactor = mapFactor;
    }

    public String getMapUnits() {
        return this.mapUnits;
    }

    public void setMapUnits(String mapUnits) {
        this.mapUnits = mapUnits;
    }

    public IProjection getProjection() {
        if (this.projection == null) {
            try {
                this.projection = (IProjection)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(ConfigDefaultViewOptionsPanel.DEFAULT_PROJECTION_KEY);
                if (this.projection == null) {
                    this.projection = CrsRepositoryManager.getInstance().getCRS("EPSG:4326");
                }
            }
            catch (CrsException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return this.projection;
    }

    public void setProjection(IProjection projection) {
        if (projection == null) {
            try {
                projection = CrsRepositoryManager.getInstance().getCRS("EPSG:4326");
            }
            catch (CrsException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        this.projection = projection;
        if (projection != null) {
            ICrs crs = (ICrs)projection;
            this.setCrsWKT(crs.getWKT());
            this.setNadGrid(crs.getTransParam());
            this.setTargetNad(crs.isTransInTarget());
            this.setCrsCode(crs.getCode());
        }
    }

    public int getCrsCode() {
        return this.crsCode;
    }

    public void setCrsCode(int crsCode) {
        this.crsCode = crsCode;
    }

    public String getCrsWKT() {
        return this.crsWKT;
    }

    public void setCrsWKT(String crsWKT) {
        this.crsWKT = crsWKT;
    }

    public String getNadGrid() {
        return this.nadGrid;
    }

    public void setNadGrid(String nadGrid) {
        this.nadGrid = nadGrid;
    }

    public boolean isTargetNad() {
        return this.targetNad;
    }

    public void setTargetNad(boolean targetNad) {
        this.targetNad = targetNad;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Scale getGraphicScale() {
        return this.graphicScale;
    }

    public void setGraphicScale(Scale graphicScale) {
        this.graphicScale = graphicScale;
    }

    public List<Legend> getLegends() {
        return this.legends;
    }

    public void setLegends(List<Legend> legends) {
        this.legends = legends;
    }

    public String getUserLengthUnit() {
        return this.userLengthUnit;
    }

    public void setUserLengthUnit(String userLengthUnits) {
        this.userLengthUnit = userLengthUnits;
    }

    public String getUserAreaUnit() {
        return this.userAreaUnit;
    }

    public void setUserAreaUnit(String userAreaUnits) {
        this.userAreaUnit = userAreaUnits;
    }

    public String getMapLengthUnit() {
        return this.mapLengthUnit;
    }

    public void setMapLengthUnit(String mapLengthUnits) {
        this.mapLengthUnit = mapLengthUnits;
    }

    public WMSService getWmsService() {
        return this.wmsService;
    }

    public void setWmsService(WMSService wmsService) {
        this.wmsService = wmsService;
    }

    public double getAngle() {
        return this.angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public NorthLayerViewPanelListener getNorth() {
        return this.north;
    }

    public void setNorth(NorthLayerViewPanelListener north) {
        this.north = north;
    }

    public Map<Object, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(Object key, Object value) {
        this.properties.put(key, value);
    }

    public void removeProperty(Object key) {
        this.properties.remove(key);
    }

    public Object getProperty(Object key) {
        return this.properties.get(key);
    }

    @Override
    public String getTitle(Locale locale) {
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public String getTitle() {
        Locale locale = LocaleManager.getActiveLocale();
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.addLocale(locale);
        }
        return this.titleByLang.get(locale);
    }

    @Override
    public void setTitle(String title, Locale locale) {
        if (this.titleByLang == null) {
            this.titleByLang = new HashMap<Locale, String>();
        }
        this.titleByLang.put(locale, title);
        if (locale.equals(LocaleManager.getActiveLocale())) {
            this.fireNameChanged(title);
        }
    }

    @Override
    public Map<Locale, String> getTitleByLang() {
        for (Locale locale : LocaleManager.getAvailablesLocales()) {
            if (this.titleByLang.containsKey(locale) && !StringUtils.isEmpty((String)this.titleByLang.get(locale))) continue;
            this.titleByLang.put(locale, this.name);
        }
        return this.titleByLang;
    }

    @Override
    public void setTitleByLang(Map<Locale, String> titleByLang) {
        this.titleByLang = titleByLang;
    }

    @Override
    public void addLocale(Locale locale) {
        if (!this.titleByLang.containsKey(locale) || StringUtils.isEmpty((String)this.titleByLang.get(locale))) {
            this.titleByLang.put(locale, this.name);
        }
    }

    @Override
    public void removeLocale(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            this.titleByLang.remove(locale);
        }
    }

    public static interface NameListener {
        public void taskNameChanged(String var1);
    }
}

