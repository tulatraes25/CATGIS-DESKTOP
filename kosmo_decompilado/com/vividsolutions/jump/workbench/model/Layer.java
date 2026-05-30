/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 *  org.cresques.px.dxf.AcadColor
 *  org.opengis.util.Cloneable
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.HiperLink;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.Range;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerListenerImplement;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.ObservableFeatureCollection;
import com.vividsolutions.jump.workbench.model.Task;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowLineStringSegmentStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ArrowTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.CircleTerminalDecorator;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.ImageFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.LabelStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.LineStringStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.SquareVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexIndexLineSegmentStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexXYLineSegmentStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;
import es.kosmo.core.renderer.decorators.AbstractDecorator;
import es.kosmo.core.renderer.decorators.IDecorator;
import es.kosmo.core.renderer.decorators.impl.EndArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.EndCircleMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.EndFeathersMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.IndexNumberVertexMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.MidSegmentArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.StartArrowMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.StartCircleMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.StartFeathersMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.XYZCoordinatesVertexMarkerDecorator;
import es.kosmo.core.renderer.decorators.impl.ZCoordinateVertexMarkerDecorator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.cresques.px.dxf.AcadColor;
import org.gvsig.crs.ICrs;
import org.opengis.util.Cloneable;
import org.openjump.core.ui.style.decoration.VertexZValueStyle;
import org.saig.core.dao.coverage.Coverage;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.dbdatasource.MySQLDataSource;
import org.saig.core.dao.datasource.filedatasource.AbstractCadDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.AttributeExpressionImpl2;
import org.saig.core.filter.CompareFilterImpl;
import org.saig.core.filter.Expression;
import org.saig.core.filter.ExpressionBuilder;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterFactory;
import org.saig.core.filter.IllegalFilterException;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.filter.LogicFilterImpl;
import org.saig.core.filter.parser.ParseException;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.layerdomain.Domain;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.core.model.relations.topology.ITopologyRelation;
import org.saig.core.styling.AnchorPoint;
import org.saig.core.styling.Displacement;
import org.saig.core.styling.ExternalGraphic;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.Fill;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Halo;
import org.saig.core.styling.LabelPlacement;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Stroke;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.core.styling.WKTGraphic;
import org.saig.core.util.DateFormatManager;
import org.saig.jump.lang.I18N;

public class Layer
extends AbstractLayerable
implements LayerManagerProxy,
Comparable<Layer> {
    public static final String FIRING_APPEARANCE_CHANGED_ON_ATTRIBUTE_CHANGE = String.valueOf(Layer.class.getName()) + " - FIRING APPEARANCE CHANGED ON ATTRIBUTE CHANGE";
    public static final String LAYER_SYSTEM_INTERNAL_NAME_PROPERTY = "system_name";
    public static final String LABEL_LAYER_KEY = "label_layer";
    public static final String WKT_FILL_KEY = "wktFill";
    public static final String WKT_FILL_COLOR = "wktFillColor";
    private static final Logger LOGGER = Logger.getLogger(Layer.class);
    private String description = "";
    private boolean drawingLast = false;
    private FeatureCollectionWrapper featureCollectionWrapper;
    private List<com.vividsolutions.jump.workbench.ui.renderer.style.Style> styles = new ArrayList<com.vividsolutions.jump.workbench.ui.renderer.style.Style>();
    private HiperLink hiperLink;
    private Map<String, Relation<?>> relations = new HashMap();
    private Map<String, String> attributePublicNames = new HashMap<String, String>();
    private Map<String, Map<Locale, String>> attributeTranslationsMap = new HashMap<String, Map<Locale, String>>();
    private Map<String, Boolean> attributeVisibility = new HashMap<String, Boolean>();
    private boolean synchronizingLineColor = false;
    private boolean oneQueryByRule = true;
    private boolean editable = false;
    private LayerListener layerListener = null;
    private Blackboard blackboard = new Blackboard();
    private Style modelStyle = null;
    private List<Style> availableModelStyles = new ArrayList<Style>();
    private boolean featureCollectionModified = false;
    private boolean repeated = false;
    private boolean overlapping = false;
    private boolean memory = false;
    private FeatureDataset fcMemory;
    private ICoordTrans coordTrans;
    private IProjection projection;
    private String crsDescription;
    private String crsWKT;
    private String crsParams;
    private String nadGrid;
    private boolean targetNad;
    private int crsCode;
    private Envelope vista;
    private boolean collapsed = false;
    private boolean versionable = false;
    private String startDateField;
    private String endDateField;
    private String historyField;
    private Timestamp versionableViewDate;
    private int geometryType = 0;
    private Map<Object, Object> properties = new HashMap<Object, Object>();
    private List<ITopologyRelation> topologyRelations = new ArrayList<ITopologyRelation>();
    private Filter layerFilter;
    private boolean internal = false;
    private boolean hidden = false;
    private Hashtable<String, Domain> domains;
    private List<String> finderFields;
    private Set<ITrigger> triggers = new LinkedHashSet<ITrigger>();
    private DataSourceQuery dataSourceQuery;

    public Layer(String name, Color fillColor, FeatureCollection featureCollection, LayerManager layerManager) {
        super(name, layerManager);
        Assert.isTrue((featureCollection != null ? 1 : 0) != 0);
        boolean firingEvents = layerManager.isFiringEvents();
        layerManager.setFiringEvents(false);
        try {
            this.addStyle(new BasicStyle());
            this.addStyle(new SquareVertexStyle());
            this.addStyle(new LabelStyle());
        }
        finally {
            layerManager.setFiringEvents(firingEvents);
        }
        this.getBasicStyle().setFillColor(fillColor);
        this.getBasicStyle().setLineColor(Layer.defaultLineColor(fillColor));
        this.getBasicStyle().setAlpha(255);
        this.setFeatureCollection(featureCollection);
        this.setModelStyle(this.getDefaultModelStyle(fillColor));
        this.modelStyle.setTitle(this.getName());
        this.blackboard.put(FIRING_APPEARANCE_CHANGED_ON_ATTRIBUTE_CHANGE, true);
    }

    public Layer(String name, Symbolizer[] symbol, FeatureCollection featureCollection, LayerManager manager) {
        super(name, manager);
        Assert.isTrue((featureCollection != null ? 1 : 0) != 0);
        this.addStyle(new BasicStyle());
        this.addStyle(new SquareVertexStyle());
        this.addStyle(new LabelStyle());
        this.getBasicStyle().setFillColor(Color.BLACK);
        this.getBasicStyle().setLineColor(Layer.defaultLineColor(Color.BLACK));
        this.getBasicStyle().setAlpha(255);
        this.setFeatureCollection(featureCollection);
        this.setModelStyle(this.getModelStyle(symbol));
        this.modelStyle.setTitle(this.getName());
        this.enabled = true;
        this.visible = true;
    }

    @Override
    public Style getModelStyle() {
        return this.modelStyle;
    }

    public static Color defaultLineColor(Color fillColor) {
        return fillColor.darker();
    }

    public void setDescription(String description) {
        if (description == null) {
            description = "";
        }
        this.description = description;
    }

    public int getSrid() {
        return -1;
    }

    public void setDrawingLast(boolean drawingLast) {
        this.drawingLast = drawingLast;
        this.fireAppearanceChanged();
    }

    public void setFeatureCollection(FeatureCollection featureCollection) {
        if (this.vista == null) {
            try {
                this.vista = featureCollection != null ? featureCollection.getEnvelope() : null;
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        featureCollection.setTopologyRelations(this.topologyRelations);
        ObservableFeatureCollection observableFeatureCollection = new ObservableFeatureCollection(featureCollection);
        observableFeatureCollection.checkNotWrappingSameClass();
        observableFeatureCollection.add(new ObservableFeatureCollection.Listener(){

            @Override
            public void featuresAdded(Collection<Feature> features) {
                Layer.this.getLayerManager().fireFeaturesChanged(features, FeatureEventType.ADDED, Layer.this);
            }

            @Override
            public void featuresRemoved(Collection<Feature> features) {
                Layer.this.getLayerManager().fireFeaturesChanged(features, FeatureEventType.DELETED, Layer.this);
            }
        });
        if (this.getLayerManager() != null && this.getLayerManager().getLayers().contains(this)) {
            SwingUtilities.invokeLater(new Runnable(){

                @Override
                public void run() {
                    Layer.this.fireAppearanceChanged();
                }
            });
        }
        this.setFeatureCollectionWrapper(observableFeatureCollection);
    }

    public boolean isDataBaseDataSource() {
        FeatureCollectionOnDemand fcd;
        return this.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && (fcd = (FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof AbstractJDBCDataSource;
    }

    public boolean isShapeFileDataSource() {
        FeatureCollectionOnDemand fcd;
        return this.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && (fcd = (FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof ShapeFileDataSource;
    }

    public AbstractJDBCDataSource getTransactionalDataSource() {
        FeatureCollectionOnDemand fcd;
        if (this.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && (fcd = (FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper()).getDataAccesor() instanceof AbstractJDBCDataSource) {
            AbstractJDBCDataSource dataSource = (AbstractJDBCDataSource)fcd.getDataAccesor();
            return dataSource;
        }
        return null;
    }

    public void setEditable(boolean editable) {
        if (this.isRaster()) {
            this.editable = false;
            return;
        }
        if (this.editable == editable) {
            return;
        }
        this.editable = editable;
        this.getUltimateFeatureCollectionWrapper().setEditable(editable);
        this.fireLayerChanged(LayerEventType.METADATA_CHANGED);
    }

    @Override
    public boolean isRaster() {
        if (this.getModelStyle() == null || this.getModelStyle().getSelectedFeatureTypeStyle() == null) {
            return false;
        }
        Rule[] rules = this.getModelStyle().getSelectedFeatureTypeStyle().getRules();
        int i = 0;
        while (i < rules.length) {
            Symbolizer[] simbolos = rules[i].getSymbolizers();
            int j = 0;
            while (j < simbolos.length) {
                if (simbolos[j] instanceof RasterSymbolizer) {
                    return true;
                }
                ++j;
            }
            ++i;
        }
        return false;
    }

    public boolean isEditable() {
        return this.editable;
    }

    public void setSynchronizingLineColor(boolean synchronizingLineColor) {
        this.synchronizingLineColor = synchronizingLineColor;
        this.fireAppearanceChanged();
    }

    public BasicStyle getBasicStyle() {
        return (BasicStyle)this.getStyle(BasicStyle.class);
    }

    public VertexStyle getVertexStyle() {
        return (VertexStyle)this.getStyle(VertexStyle.class);
    }

    public LabelStyle getLabelStyle() {
        return (LabelStyle)this.getStyle(LabelStyle.class);
    }

    public String getDescription() {
        return this.description;
    }

    public FeatureCollectionWrapper getFeatureCollectionWrapper() {
        if (this.isMemory()) {
            ObservableFeatureCollection observableFeatureCollection = new ObservableFeatureCollection(this.fcMemory);
            observableFeatureCollection.add(new ObservableFeatureCollection.Listener(){

                @Override
                public void featuresAdded(Collection<Feature> features) {
                    Layer.this.getLayerManager().fireFeaturesChanged(features, FeatureEventType.ADDED, Layer.this);
                }

                @Override
                public void featuresRemoved(Collection<Feature> features) {
                    Layer.this.getLayerManager().fireFeaturesChanged(features, FeatureEventType.DELETED, Layer.this);
                }
            });
            return observableFeatureCollection;
        }
        return this.featureCollectionWrapper;
    }

    public FeatureCollection getUltimateFeatureCollectionWrapper() {
        if (this.isMemory() && this.fcMemory != null) {
            return this.fcMemory;
        }
        if (this.featureCollectionWrapper == null) {
            return null;
        }
        return this.featureCollectionWrapper.getUltimateWrappee();
    }

    protected void setFeatureCollectionWrapper(FeatureCollectionWrapper featureCollectionWrapper) {
        this.featureCollectionWrapper = featureCollectionWrapper;
    }

    public com.vividsolutions.jump.workbench.ui.renderer.style.Style getStyle(Class<?> c) {
        for (com.vividsolutions.jump.workbench.ui.renderer.style.Style p : this.styles) {
            if (!c.isInstance(p)) continue;
            return p;
        }
        return null;
    }

    public List<com.vividsolutions.jump.workbench.ui.renderer.style.Style> getStyles() {
        return this.styles;
    }

    public boolean hasReadableDataSource() {
        return this.dataSourceQuery != null && this.dataSourceQuery.getDataSource().isReadable() && this.dataSourceQuery.getDataSource().isReadableFromProjectFile();
    }

    public boolean isSynchronizingLineColor() {
        return this.synchronizingLineColor;
    }

    public boolean isDrawingLast() {
        return this.drawingLast;
    }

    public void addStyle(com.vividsolutions.jump.workbench.ui.renderer.style.Style style) {
        this.styles.add(style);
    }

    @Override
    public void dispose() {
        List<Feature> features;
        if (this.isRaster() && (features = this.getFeatureCollectionWrapper().getFeatures()).size() > 0) {
            Feature feat = features.get(0);
            Coverage coverage = (Coverage)feat.getAttribute("IMAGE");
            coverage.close();
        }
        if (this.getFeatureCollectionWrapper() != null) {
            this.getFeatureCollectionWrapper().dispose();
            this.setFeatureCollectionWrapper(null);
        }
        this.styles = null;
        this.modelStyle = null;
        this.layerListener = null;
        this.blackboard = null;
        this.dataSourceQuery = null;
        this.layerManager = null;
    }

    public void removeStyle(com.vividsolutions.jump.workbench.ui.renderer.style.Style p) {
        Assert.isTrue((boolean)this.styles.remove(p));
        this.fireAppearanceChanged();
    }

    public Collection<com.vividsolutions.jump.workbench.ui.renderer.style.Style> cloneStyles() {
        ArrayList<com.vividsolutions.jump.workbench.ui.renderer.style.Style> styleClones = new ArrayList<com.vividsolutions.jump.workbench.ui.renderer.style.Style>();
        for (com.vividsolutions.jump.workbench.ui.renderer.style.Style style : this.getStyles()) {
            if (style == null) continue;
            styleClones.add((com.vividsolutions.jump.workbench.ui.renderer.style.Style)style.clone());
        }
        return styleClones;
    }

    public void setStyles(Collection<com.vividsolutions.jump.workbench.ui.renderer.style.Style> newStyles) {
        this.setStyles(newStyles, true);
    }

    public void setStyles(Collection<com.vividsolutions.jump.workbench.ui.renderer.style.Style> newStyles, boolean fireAppearanceChanged) {
        boolean firingEvents = this.getLayerManager().isFiringEvents();
        this.getLayerManager().setFiringEvents(false);
        try {
            for (com.vividsolutions.jump.workbench.ui.renderer.style.Style style : new ArrayList<com.vividsolutions.jump.workbench.ui.renderer.style.Style>(this.getStyles())) {
                this.removeStyle(style);
            }
            for (com.vividsolutions.jump.workbench.ui.renderer.style.Style style : newStyles) {
                if (style == null) continue;
                this.addStyle(style);
            }
        }
        finally {
            this.getLayerManager().setFiringEvents(firingEvents);
        }
        if (fireAppearanceChanged) {
            this.fireAppearanceChanged();
        }
    }

    @Override
    public void setLayerManager(LayerManager layerManager) {
        if (layerManager != null) {
            layerManager.removeLayerListener(this.getLayerListener());
        }
        super.setLayerManager(layerManager);
        if (layerManager != null) {
            layerManager.addLayerListener(this.getLayerListener());
        }
    }

    public LayerListener getLayerListener() {
        if (this.layerListener == null) {
            this.layerListener = new LayerListenerImplement(this);
        }
        return this.layerListener;
    }

    @Override
    public Blackboard getBlackboard() {
        return this.blackboard;
    }

    public static UndoableCommand addUndo(final String layerName, final LayerManagerProxy proxy, final UndoableCommand wrappeeCommand) throws Exception {
        return new UndoableCommand(wrappeeCommand.getName()){
            private Layer layer;
            private String categoryName;
            private Collection<Feature> features;
            private boolean visible;

            private Layer currentLayer() {
                return proxy.getLayerManager().getLayer(layerName);
            }

            @Override
            public void execute() throws Exception {
                this.layer = this.currentLayer();
                if (this.layer != null) {
                    this.features = new ArrayList<Feature>(this.layer.getFeatureCollectionWrapper().getFeatures());
                    this.categoryName = this.layer.getName();
                    this.visible = this.layer.isVisible();
                }
                wrappeeCommand.execute();
            }

            @Override
            public void unexecute() throws Exception {
                wrappeeCommand.unexecute();
                if (this.layer == null && this.currentLayer() != null) {
                    proxy.getLayerManager().remove(this.currentLayer(), true);
                }
                if (this.layer != null && this.currentLayer() == null) {
                    proxy.getLayerManager().addLayer(this.categoryName, this.layer);
                }
                if (this.layer != null) {
                    this.layer.getFeatureCollectionWrapper().clear();
                    try {
                        this.layer.getFeatureCollectionWrapper().addAll(this.features);
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                    this.layer.setVisible(this.visible);
                }
            }
        };
    }

    public DataSourceQuery getDataSourceQuery() {
        return this.dataSourceQuery;
    }

    public Layer setDataSourceQuery(DataSourceQuery dataSourceQuery) {
        this.dataSourceQuery = dataSourceQuery;
        return this;
    }

    public boolean isFeatureCollectionModified() {
        return this.featureCollectionModified;
    }

    public Layer setFeatureCollectionModified(boolean featureCollectionModified) {
        if (this.featureCollectionModified == featureCollectionModified) {
            return this;
        }
        this.featureCollectionModified = featureCollectionModified;
        this.fireLayerChanged(LayerEventType.METADATA_CHANGED);
        return this;
    }

    public int getGeometryType() {
        if (this.featureCollectionWrapper != null && this.getFeatureCollectionWrapper() != null && this.getFeatureCollectionWrapper().getUltimateWrappee() != null && this.getFeatureCollectionWrapper().getFeatureSchema() != null && this.getFeatureCollectionWrapper().getFeatureSchema().getGeometryType() != 0) {
            return this.getFeatureCollectionWrapper().getFeatureSchema().getGeometryType();
        }
        return this.geometryType;
    }

    public void addStyles(List<com.vividsolutions.jump.workbench.ui.renderer.style.Style> estilos) {
        FeatureSchema featureSchema = this.featureCollectionWrapper.getFeatureSchema();
        FeatureTypeStyle featStyle = this.modelStyle.getSelectedFeatureTypeStyle();
        Rule ruleLabel = null;
        ArrayList<Rule> rules = new ArrayList<Rule>();
        ArrayList<IDecorator> decorators = new ArrayList<IDecorator>();
        int cont = 1;
        for (com.vividsolutions.jump.workbench.ui.renderer.style.Style style : estilos) {
            if (style == null || !style.isEnabled()) continue;
            if (style.getClass().equals(LabelStyle.class)) {
                try {
                    ruleLabel = this.getRule(0.0, Double.MAX_VALUE, featureSchema, (String)null, style, String.valueOf(this.getName()) + "_txt_" + cont, false);
                    ++cont;
                }
                catch (ParseException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    ruleLabel = null;
                }
                continue;
            }
            if (style.getClass().equals(BasicStyle.class)) {
                if (!this.getBasicStyle().isEnabled()) continue;
                try {
                    rules.add(this.getRule(0.0, Double.MAX_VALUE, featureSchema, (String)null, style, this.getName(), false));
                }
                catch (ParseException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                continue;
            }
            if (style.getClass().equals(ColorThemingStyle.class)) {
                if (this.getBasicStyle().isEnabled()) continue;
                rules.addAll(this.addClasificationStyle((ColorThemingStyle)style));
                continue;
            }
            if (!com.vividsolutions.jump.workbench.ui.renderer.style.AbstractDecorator.class.isAssignableFrom(style.getClass()) && !LineStringStyle.class.isAssignableFrom(style.getClass())) continue;
            decorators.add(this.translateDecorator(style));
        }
        if (CollectionUtils.isNotEmpty(decorators)) {
            for (Rule rule : rules) {
                Symbolizer[] symbolizerArray = rule.getSymbolizers();
                int n = symbolizerArray.length;
                int n2 = 0;
                while (n2 < n) {
                    Symbolizer symb = symbolizerArray[n2];
                    if (symb instanceof PointSymbolizer) {
                        PointSymbolizer pointSymbolizer = (PointSymbolizer)symb;
                        pointSymbolizer.setDecorators(decorators);
                    } else if (symb instanceof LineSymbolizer) {
                        LineSymbolizer lineSymbolizer = (LineSymbolizer)symb;
                        lineSymbolizer.setDecorators(decorators);
                    } else if (symb instanceof PolygonSymbolizer) {
                        PolygonSymbolizer polygonSymbolizer = (PolygonSymbolizer)symb;
                        polygonSymbolizer.setDecorators(decorators);
                    }
                    ++n2;
                }
            }
        }
        if (ruleLabel != null) {
            rules.add(ruleLabel);
        }
        Rule[] rules_ = new Rule[rules.size()];
        rules.toArray(rules_);
        featStyle.setRules(rules_);
    }

    protected IDecorator translateDecorator(com.vividsolutions.jump.workbench.ui.renderer.style.Style style) {
        AbstractDecorator decorator = null;
        if (style instanceof ArrowTerminalDecorator.FeathersStart) {
            decorator = new StartFeathersMarkerDecorator();
        } else if (style instanceof ArrowTerminalDecorator.FeathersEnd) {
            decorator = new EndFeathersMarkerDecorator();
        } else if (style instanceof ArrowTerminalDecorator.OpenStart) {
            decorator = new StartArrowMarkerDecorator(Color.BLACK, 10.0, 45.0, false, 0.0, "pixel");
        } else if (style instanceof ArrowTerminalDecorator.OpenEnd) {
            decorator = new EndArrowMarkerDecorator(Color.BLACK, 10.0, 45.0, false, 0.0, "pixel");
        } else if (style instanceof ArrowTerminalDecorator.SolidStart) {
            decorator = new StartArrowMarkerDecorator(Color.BLACK, 10.0, 45.0, false, 0.0, "pixel");
        } else if (style instanceof ArrowTerminalDecorator.SolidEnd) {
            decorator = new EndArrowMarkerDecorator(Color.BLACK, 10.0, 45.0, false, 0.0, "pixel");
        } else if (style instanceof ArrowTerminalDecorator.NarrowSolidStart) {
            decorator = new StartArrowMarkerDecorator();
        } else if (style instanceof ArrowTerminalDecorator.NarrowSolidEnd) {
            decorator = new EndArrowMarkerDecorator();
        } else if (style instanceof ArrowLineStringSegmentStyle.Open) {
            decorator = new MidSegmentArrowMarkerDecorator(Color.BLACK, 10.0, 45.0, false, 0.0, "pixel");
        } else if (style instanceof ArrowLineStringSegmentStyle.NarrowSolid) {
            decorator = new MidSegmentArrowMarkerDecorator();
        } else if (style instanceof ArrowLineStringSegmentStyle.Solid) {
            decorator = new MidSegmentArrowMarkerDecorator(Color.BLACK, 10.0, 45.0, false, 0.0, "pixel");
        } else if (style instanceof CircleTerminalDecorator.Start) {
            decorator = new StartCircleMarkerDecorator();
        } else if (style instanceof CircleTerminalDecorator.End) {
            decorator = new EndCircleMarkerDecorator();
        } else if (style instanceof VertexXYLineSegmentStyle.VertexXY) {
            decorator = new XYZCoordinatesVertexMarkerDecorator();
        } else if (style instanceof VertexZValueStyle.VertexZValue) {
            decorator = new ZCoordinateVertexMarkerDecorator();
        } else if (style instanceof VertexIndexLineSegmentStyle.VertexIndex) {
            decorator = new IndexNumberVertexMarkerDecorator();
        } else {
            LOGGER.warn((Object)I18N.getMessage("com.vividsolutions.jump.workbench.model.Layer.Decorator-style-class-{0}-is-not-translatable", new Object[]{style.getClass()}));
        }
        return decorator;
    }

    private Rule getRule(double minScale, double maxScale, FeatureSchema schema, Filter filtro, com.vividsolutions.jump.workbench.ui.renderer.style.Style estilo, String ruleName, boolean isElseRule) throws ParseException {
        RuleImpl rule = new RuleImpl();
        rule.setMinScaleDenominator(minScale);
        rule.setMaxScaleDenominator(maxScale);
        rule.setTitle(ruleName);
        rule.setName(ruleName);
        if (estilo.getClass().equals(BasicStyle.class)) {
            rule.setSymbolizers(new Symbolizer[]{this.addBasicStyle((BasicStyle)estilo)});
        } else if (estilo.getClass().equals(LabelStyle.class)) {
            rule.setSymbolizers(new Symbolizer[]{this.addLabelStyle((LabelStyle)estilo)});
        }
        if (filtro != null) {
            rule.setFilter(filtro);
        }
        rule.setElseFilter(isElseRule);
        return rule;
    }

    private Rule getRule(double minScale, double maxScale, FeatureSchema schema, String condition, com.vividsolutions.jump.workbench.ui.renderer.style.Style estilo, String ruleName, boolean isElseRule) throws ParseException {
        Filter filtro = null;
        if (condition != null) {
            filtro = (Filter)ExpressionBuilder.parse(schema, condition);
        }
        return this.getRule(minScale, maxScale, schema, filtro, estilo, ruleName, isElseRule);
    }

    private Symbolizer addLabelStyle(LabelStyle jumpSt) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        AttributeExpression label = null;
        try {
            label = filterFactory.createAttributeExpression(this.getFeatureSchema(), jumpSt.getAttribute());
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        Font jumpFont = jumpSt.getFont();
        LiteralExpression fontFamily = filterFactory.createLiteralExpression(jumpFont.getFamily());
        boolean isBold = jumpFont.getStyle() == 1 || jumpFont.getStyle() > 2;
        boolean isItalic = jumpFont.getStyle() == 2 || jumpFont.getStyle() > 2;
        LiteralExpression fontWeight = null;
        fontWeight = isBold ? filterFactory.createLiteralExpression("bold") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontStyle = null;
        fontStyle = isItalic ? filterFactory.createLiteralExpression("italic") : filterFactory.createLiteralExpression("normal");
        LiteralExpression fontSize = filterFactory.createLiteralExpression(new Double(jumpSt.getHeight()).intValue());
        org.saig.core.styling.Font modelFont = factory.createFont(fontFamily, fontStyle, fontWeight, fontSize);
        Halo halo = null;
        if (jumpSt.isGlowing()) {
            LiteralExpression haloColor = filterFactory.createLiteralExpression(Layer.decodeColor(jumpSt.getColorGlowing()));
            LiteralExpression haloWidth = filterFactory.createLiteralExpression(2);
            LiteralExpression haloOpacity = filterFactory.createLiteralExpression(0.8);
            Fill fillHalo = factory.createFill(haloColor, haloOpacity);
            halo = factory.createHalo(fillHalo, haloWidth);
        }
        LiteralExpression textColor = filterFactory.createLiteralExpression(Layer.decodeColor(jumpSt.getColor()));
        Fill fillText = factory.createFill(textColor);
        LabelPlacement labelPlac = null;
        if (this.getFeatureSchema().getGeometryType() != 3 && this.getFeatureSchema().getGeometryType() != 2 || StringUtils.isNotEmpty((String)jumpSt.getAngleAttribute())) {
            LiteralExpression x = filterFactory.createLiteralExpression(0);
            LiteralExpression y = filterFactory.createLiteralExpression(0);
            AnchorPoint anchorPoint = factory.createAnchorPoint(x, y);
            LiteralExpression dx = filterFactory.createLiteralExpression(0.5);
            LiteralExpression dy = filterFactory.createLiteralExpression(0);
            Displacement displacement = factory.createDisplacement(dx, dy);
            Expression rotation = null;
            if (StringUtils.isNotEmpty((String)jumpSt.getAngleAttribute())) {
                try {
                    rotation = filterFactory.createAttributeExpression(this.getFeatureSchema(), jumpSt.getAngleAttribute());
                }
                catch (IllegalFilterException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    rotation = filterFactory.createLiteralExpression(0);
                }
            } else {
                rotation = filterFactory.createLiteralExpression(0);
            }
            labelPlac = factory.createPointPlacement(anchorPoint, displacement, rotation);
        } else {
            int fontSizeValue = (int)jumpSt.getHeight();
            int perpendicularOffset = -(fontSizeValue / 2);
            if (jumpSt.getVerticalAlignment() == "ABOVE_LINE") {
                perpendicularOffset = 5;
            } else if (jumpSt.getVerticalAlignment() == "BELOW_LINE") {
                perpendicularOffset = -5 - fontSizeValue;
            }
            LiteralExpression offset = filterFactory.createLiteralExpression(perpendicularOffset);
            labelPlac = factory.createLinePlacement(offset);
        }
        TextSymbolizer text = factory.createTextSymbolizer(fillText, new org.saig.core.styling.Font[]{modelFont}, halo, label, labelPlac, null);
        if (StringUtils.isNotEmpty((String)jumpSt.getHeightAttribute())) {
            AttributeExpression labelHeight = null;
            try {
                labelHeight = filterFactory.createAttributeExpression(this.getFeatureSchema(), jumpSt.getHeightAttribute());
                text.setHeightAttribute(labelHeight);
            }
            catch (IllegalFilterException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return text;
    }

    private Symbolizer addBasicStyle(BasicStyle jumpSt) {
        FeatureSchema schema = this.featureCollectionWrapper.getUltimateWrappee().getFeatureSchema();
        if (!schema.hasAttribute("IMAGE") || !schema.getAttribute("IMAGE").getType().equals(AttributeType.OBJECT)) {
            int geomType = schema.getGeometryType();
            if (geomType == 3 || geomType == 2) {
                return this.addLineSymbolizer(jumpSt);
            }
            if (geomType == 5 || geomType == 4) {
                return this.addPolygonSymbolizer(jumpSt);
            }
            if (geomType == 1 || geomType == 8) {
                return this.addPointSymbolizer(jumpSt);
            }
        } else {
            if (schema.hasAttribute("TRANSPARENCY")) {
                Feature feature = this.featureCollectionWrapper.getUltimateWrappee().getFeaturesSamples(1).get(0);
                feature.setAttribute("TRANSPARENCY", (Object)new Float(jumpSt.getAlpha()));
            }
            int jAlpha = jumpSt.getAlpha();
            float alpha = new Float((float)jAlpha / 255.0f).floatValue();
            return this.addRasterSymbolizer(alpha);
        }
        return null;
    }

    private Symbolizer addLineSymbolizer(BasicStyle jumpSt) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        BasicStroke jumpStroke = jumpSt.getLineStroke();
        int jAlpha = jumpSt.getAlpha();
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = null;
        if (jumpSt.isRenderingLine()) {
            stroke = this.getStroke(jumpStroke, alpha, jumpSt.getLineColor(), filterFactory, factory);
        }
        return factory.createLineSymbolizer(stroke, null);
    }

    private Stroke getStroke(BasicStroke jumpStroke, float alpha, Color color, FilterFactory filterFactory, StyleFactory factory) {
        LiteralExpression strokeColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        LiteralExpression strokeWidth = filterFactory.createLiteralExpression(jumpStroke.getLineWidth());
        LiteralExpression strokeOpacity = filterFactory.createLiteralExpression(alpha);
        float[] dashArray = jumpStroke.getDashArray();
        int lineJoin_ = jumpStroke.getLineJoin();
        LiteralExpression lineJoin = null;
        if (lineJoin_ == 2) {
            lineJoin = filterFactory.createLiteralExpression("bevel");
        } else if (lineJoin_ == 0) {
            lineJoin = filterFactory.createLiteralExpression("mitre");
        } else if (lineJoin_ == 1) {
            lineJoin = filterFactory.createLiteralExpression("round");
        }
        LiteralExpression lineCap = null;
        int lineCap_ = jumpStroke.getEndCap();
        if (lineCap_ == 0) {
            lineCap = filterFactory.createLiteralExpression("butt");
        } else if (lineCap_ == 1) {
            lineCap = filterFactory.createLiteralExpression("round");
        } else if (lineCap_ == 2) {
            lineCap = filterFactory.createLiteralExpression("square");
        }
        LiteralExpression dashOffset = filterFactory.createLiteralExpression(jumpStroke.getDashPhase());
        return factory.createStroke(strokeColor, strokeWidth, strokeOpacity, lineJoin, lineCap, dashArray, dashOffset, null, null);
    }

    private Fill getFill(Color color, float opacity, Graphic graphic, FilterFactory filterFactory, StyleFactory factory) {
        LiteralExpression fillColor = filterFactory.createLiteralExpression(Layer.decodeColor(color));
        LiteralExpression fillOpacity = filterFactory.createLiteralExpression(opacity);
        return factory.createFill(fillColor, fillColor, fillOpacity, graphic);
    }

    private Symbolizer addPointSymbolizer(BasicStyle jumpSt) {
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        VertexStyle vertex = this.getVertexStyle();
        if (vertex == null) {
            vertex = new SquareVertexStyle();
        }
        StyleFactory factory = StyleFactory.createStyleFactory();
        BasicStroke jumpStroke = jumpSt.getLineStroke();
        int jAlpha = jumpSt.getAlpha();
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = null;
        if (jumpSt.isRenderingLine()) {
            stroke = this.getStroke(jumpStroke, alpha, jumpSt.getLineColor(), filterFactory, factory);
        }
        Fill fill = null;
        if (jumpSt.isRenderingFill()) {
            Graphic graphic = null;
            if (jumpSt.isRenderingFillPattern()) {
                graphic = this.createGraphic(jumpSt, alpha, factory, filterFactory);
            }
            fill = this.getFill(jumpSt.getFillColor(), alpha, graphic, filterFactory, factory);
            if (jumpSt.isRenderingLine() && fill != null && graphic == null && fill.getColor() != null) {
                stroke = null;
            }
        }
        LiteralExpression markType = filterFactory.createLiteralExpression("square");
        LiteralExpression markSize = filterFactory.createLiteralExpression(vertex.getSize());
        LiteralExpression markRotation = filterFactory.createLiteralExpression(0);
        Mark mark = factory.createMark(markType, stroke, fill, markSize, markRotation);
        LiteralExpression opacity = filterFactory.createLiteralExpression(alpha);
        Graphic graphic = factory.createGraphic(null, new Mark[]{mark}, null, opacity, markSize, markRotation);
        return factory.createPointSymbolizer(graphic, null);
    }

    private Symbolizer addPolygonSymbolizer(BasicStyle jumpSt) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        BasicStroke jumpStroke = jumpSt.getLineStroke();
        int jAlpha = jumpSt.getAlpha();
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = null;
        if (jumpSt.isRenderingLine()) {
            stroke = this.getStroke(jumpStroke, alpha, jumpSt.getLineColor(), filterFactory, factory);
        }
        Fill fill = null;
        String wktFillName = "";
        if (jumpSt.isRenderingFill()) {
            Graphic graphic = null;
            if (jumpSt.isRenderingFillPattern()) {
                graphic = this.createGraphic(jumpSt, alpha, factory, filterFactory);
                if (jumpSt.getFillPattern().getClass().equals(WKTFillPattern.class)) {
                    wktFillName = jumpSt.getFillPattern().toString();
                    if (graphic.getExternalGraphics().length > 0) {
                        HashMap<String, Object> properties = new HashMap<String, Object>();
                        properties.put(WKT_FILL_KEY, wktFillName);
                        properties.put(WKT_FILL_COLOR, jumpSt.getFillColor());
                        graphic.getExternalGraphics()[0].setCustomProperties(properties);
                    }
                }
            }
            fill = this.getFill(jumpSt.getFillColor(), alpha, graphic, filterFactory, factory);
        }
        PolygonSymbolizer polSym = factory.createPolygonSymbolizer(stroke, fill, null);
        return polSym;
    }

    private Symbolizer addPolygonSymbolizer(Color color, BasicStyle jumpSt) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        BasicStroke jumpStroke = jumpSt.getLineStroke();
        int jAlpha = jumpSt.getAlpha();
        float alpha = new Float((float)jAlpha / 255.0f).floatValue();
        Stroke stroke = null;
        if (jumpSt.isRenderingLine()) {
            stroke = this.getStroke(jumpStroke, alpha, color, filterFactory, factory);
        }
        Fill fill = null;
        String wktFillName = "";
        if (jumpSt.isRenderingFill()) {
            Graphic graphic = null;
            if (jumpSt.isRenderingFillPattern()) {
                graphic = this.createGraphic(jumpSt, alpha, factory, filterFactory);
                if (jumpSt.getFillPattern().getClass().equals(WKTFillPattern.class)) {
                    wktFillName = jumpSt.getFillPattern().toString();
                    if (graphic.getExternalGraphics().length > 0) {
                        HashMap<String, Object> properties = new HashMap<String, Object>();
                        properties.put(WKT_FILL_KEY, wktFillName);
                        graphic.getExternalGraphics()[0].setCustomProperties(properties);
                    }
                }
            }
            fill = this.getFill(color, alpha, graphic, filterFactory, factory);
        }
        return factory.createPolygonSymbolizer(stroke, fill, null);
    }

    private Symbolizer addRasterSymbolizer(float alpha) {
        StyleFactory factory = StyleFactory.createStyleFactory();
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        LiteralExpression opacity = filterFactory.createLiteralExpression(alpha);
        return factory.createRasterSymbolizer(null, opacity, null, null, null, null, null, null);
    }

    private Graphic createGraphic(BasicStyle jumpSt, float opacity, StyleFactory factory, FilterFactory filterFactory) {
        if (jumpSt.getFillPattern().getClass().equals(WKTFillPattern.class)) {
            WKTFillPattern pattern = (WKTFillPattern)jumpSt.getFillPattern();
            HashMap<String, Object> mapa = pattern.getProperties().getProperties();
            Color color = (Color)mapa.get("COLOR");
            int widthLine = (Integer)mapa.get("LINE WIDTH");
            int extent = (Integer)mapa.get("EXTENT");
            String wktPattern = (String)mapa.get("PATTERN WKT");
            WKTGraphic wktGraphic = new WKTGraphic(widthLine, extent, wktPattern, color);
            ExternalGraphic externalGraphic = factory.createExternalGraphic("", "");
            externalGraphic.setWKTGraphic(wktGraphic);
            LiteralExpression graphicOpacity = filterFactory.createLiteralExpression(opacity);
            LiteralExpression size = filterFactory.createLiteralExpression(wktGraphic.getImage().getWidth());
            LiteralExpression rotation = filterFactory.createLiteralExpression(0);
            return factory.createGraphic(new ExternalGraphic[]{externalGraphic}, null, null, graphicOpacity, size, rotation);
        }
        if (jumpSt.getFillPattern().getClass().equals(ImageFillPattern.class)) {
            ImageFillPattern pattern = (ImageFillPattern)jumpSt.getFillPattern();
            HashMap<String, Object> mapa = pattern.getProperties().getProperties();
            String fileName = (String)mapa.get("FILENAME");
            Class className = (Class)mapa.get("CLASS");
            URL url = className.getResource(fileName);
            ExternalGraphic externalGraphic = factory.createExternalGraphic(url, "image/jpeg");
            LiteralExpression graphicOpacity = filterFactory.createLiteralExpression(opacity);
            LiteralExpression size = filterFactory.createLiteralExpression(IconLoader.icon(fileName).getIconWidth());
            LiteralExpression rotation = filterFactory.createLiteralExpression(0);
            return factory.createGraphic(new ExternalGraphic[]{externalGraphic}, null, null, graphicOpacity, size, rotation);
        }
        return null;
    }

    private List<Rule> addClasificationStyle(ColorThemingStyle style) {
        FeatureSchema schema = this.getFeatureCollectionWrapper().getFeatureSchema();
        Map<Object, BasicStyle> valueMap = style.getAttributeValueToBasicStyleMap();
        Set<Object> keys = valueMap.keySet();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        String attrName = style.getAttributeName();
        AttributeType attrType = schema.getAttributeType(attrName);
        String elseCondition = "";
        for (Object key : keys) {
            String value = "";
            String name = "";
            Filter filtro = null;
            if (key instanceof Range) {
                Filter minFilter = null;
                Filter maxFilter = null;
                Range rango = (Range)key;
                Object minValue = null;
                if (rango.getMin() instanceof Range.NegativeInfinity) {
                    minValue = null;
                } else {
                    minValue = rango.getMin();
                    if (minValue.toString().equals("NaN")) continue;
                }
                String strMinValue = "";
                if (minValue != null) {
                    strMinValue = this.getValue(minValue);
                    if (attrType.toJavaClass().equals(String.class)) {
                        strMinValue = "'" + strMinValue + "'";
                    }
                    short minFilterType = 16;
                    if (rango.isIncludingMin()) {
                        value = String.valueOf(attrName) + ">=" + strMinValue;
                        minFilterType = 18;
                    } else {
                        value = String.valueOf(attrName) + ">" + strMinValue;
                    }
                    try {
                        minFilter = this.buildCompareFilterFromValues(attrName, minValue, minFilterType);
                    }
                    catch (IllegalFilterException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                } else {
                    strMinValue = "\u221e";
                }
                Object maxValue = null;
                if (rango.getMax() instanceof Range.PositiveInfinity) {
                    maxValue = null;
                } else {
                    maxValue = rango.getMax();
                    if (maxValue.toString().equals("NaN")) continue;
                }
                String strMaxValue = "";
                if (maxValue != null) {
                    strMaxValue = this.getValue(maxValue);
                    if (attrType.toJavaClass().equals(String.class)) {
                        strMaxValue = "'" + strMaxValue + "'";
                    }
                    short maxFilterType = 15;
                    if (rango.isIncludingMax()) {
                        value = String.valueOf(value) + " and " + attrName + "<=" + strMaxValue;
                        maxFilterType = 17;
                    } else {
                        value = String.valueOf(value) + " and " + attrName + "<" + strMaxValue;
                    }
                    try {
                        maxFilter = this.buildCompareFilterFromValues(attrName, maxValue, maxFilterType);
                    }
                    catch (IllegalFilterException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                } else {
                    strMaxValue = "\u221e";
                }
                name = rango.isIncludingMin() && rango.isIncludingMax() ? "[" + strMinValue + "," + strMaxValue + "]" : (rango.isIncludingMin() ? "[" + strMinValue + "," + strMaxValue + ")" : (rango.isIncludingMax() ? "(" + strMinValue + "," + strMaxValue + "]" : "(" + strMinValue + "," + strMaxValue + ")"));
                if (minFilter != null && maxFilter != null) {
                    try {
                        filtro = new LogicFilterImpl(minFilter, maxFilter, 2);
                    }
                    catch (IllegalFilterException e) {
                        LOGGER.error((Object)"", (Throwable)e);
                    }
                } else if (minFilter != null) {
                    filtro = minFilter;
                } else if (maxFilter != null) {
                    filtro = maxFilter;
                }
            } else {
                String strValue = key.toString();
                if (strValue.equals("NaN")) continue;
                if (Timestamp.class.isAssignableFrom(attrType.toJavaClass()) || Time.class.isAssignableFrom(attrType.toJavaClass())) {
                    strValue = DateFormatManager.getDateTimeFormat().format(key);
                    value = String.valueOf(attrName) + "=" + "'" + strValue + "'";
                } else if (Date.class.isAssignableFrom(attrType.toJavaClass())) {
                    strValue = DateFormatManager.getDateFormat().format(key);
                    value = String.valueOf(attrName) + "=" + "'" + strValue + "'";
                } else {
                    value = attrType.toJavaClass().equals(String.class) ? String.valueOf(attrName) + "=" + "'" + strValue + "'" : String.valueOf(attrName) + "=" + strValue;
                }
                name = strValue.trim();
                try {
                    filtro = this.buildCompareFilterFromValues(attrName, strValue, (short)14);
                }
                catch (IllegalFilterException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
            BasicStyle estilo = valueMap.get(key);
            elseCondition = String.valueOf(elseCondition) + "not (" + value + ") and ";
            try {
                if (filtro == null) {
                    rules.add(this.getRule(0.0, Double.MAX_VALUE, schema, value, (com.vividsolutions.jump.workbench.ui.renderer.style.Style)estilo, name, false));
                    continue;
                }
                rules.add(this.getRule(0.0, Double.MAX_VALUE, schema, filtro, (com.vividsolutions.jump.workbench.ui.renderer.style.Style)estilo, name, false));
            }
            catch (ParseException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        if (style.getDefaultStyle() != null) {
            if (elseCondition.length() > 0) {
                elseCondition = elseCondition.substring(0, elseCondition.length() - 4);
                elseCondition = "(" + elseCondition + " ) or (isNull(" + attrName + "))";
            }
            try {
                rules.add(this.getRule(0.0, Double.MAX_VALUE, schema, (String)null, (com.vividsolutions.jump.workbench.ui.renderer.style.Style)style.getDefaultStyle(), I18N.getString("workbench.model.Layer.other"), true));
            }
            catch (ParseException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return rules;
    }

    private String getValue(Object value) {
        if (value instanceof Timestamp) {
            return DateFormatManager.getDateTimeFormat().format(value);
        }
        if (value instanceof Date) {
            return DateFormatManager.getDateFormat().format(value);
        }
        return value.toString();
    }

    private Filter buildCompareFilterFromValues(String attrName, Object value, short filterType) throws IllegalFilterException {
        LiteralExpressionImpl valueExpression = new LiteralExpressionImpl(value);
        AttributeExpressionImpl2 attrExpression = new AttributeExpressionImpl2(attrName);
        CompareFilterImpl compareFilter = null;
        try {
            compareFilter = new CompareFilterImpl(filterType);
            compareFilter.addLeftValue(attrExpression);
            compareFilter.addRightValue(valueExpression);
        }
        catch (IllegalFilterException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return compareFilter;
    }

    public Style getDefaultModelStyle(Color defaultColor) {
        Symbolizer symbol;
        boolean isImage;
        RuleImpl regla = new RuleImpl();
        regla.setMinScaleDenominator(0.0);
        regla.setMaxScaleDenominator(Double.MAX_VALUE);
        int geometryType = this.featureCollectionWrapper.getFeatureSchema().getGeometryType();
        boolean bl = isImage = this.featureCollectionWrapper.getFeatureSchema().hasAttribute("IMAGE") && this.featureCollectionWrapper.getFeatureSchema().getAttribute("IMAGE").getType().equals(AttributeType.OBJECT);
        if (!isImage) {
            if (geometryType == 1 || geometryType == 8) {
                symbol = this.addPointSymbolizer(this.getBasicStyle());
                regla.setSymbolizers(new Symbolizer[]{symbol});
            } else if (geometryType == 3 || geometryType == 2) {
                symbol = this.addLineSymbolizer(this.getBasicStyle());
                regla.setSymbolizers(new Symbolizer[]{symbol});
            } else if (geometryType == 5 || geometryType == 4) {
                symbol = this.addPolygonSymbolizer(this.getBasicStyle());
                regla.setSymbolizers(new Symbolizer[]{symbol});
            } else if (geometryType == 15) {
                return this.getCadStyle();
            }
        } else {
            symbol = this.addRasterSymbolizer(1.0f);
            regla.setSymbolizers(new Symbolizer[]{symbol});
        }
        regla.setName(this.getName());
        regla.setTitle(this.getName());
        FeatureTypeStyleImpl estilo = new FeatureTypeStyleImpl(new Rule[]{regla});
        estilo.setFeatureTypeName(this.getName());
        estilo.setName(this.getName());
        StyleImpl style = new StyleImpl();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{estilo});
        style.setName(this.getName());
        style.setTitle(this.getName());
        return style;
    }

    private Style getModelStyle(Symbolizer[] symbols) {
        RuleImpl regla = new RuleImpl();
        regla.setMinScaleDenominator(0.0);
        regla.setMaxScaleDenominator(Double.MAX_VALUE);
        regla.setSymbolizers(symbols);
        regla.setName(this.getName());
        regla.setTitle(this.getName());
        FeatureTypeStyleImpl estilo = new FeatureTypeStyleImpl(new Rule[]{regla});
        estilo.setFeatureTypeName(this.getName());
        estilo.setName(this.getName());
        StyleImpl style = new StyleImpl();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{estilo});
        style.setName(this.getName());
        style.setTitle(this.getName());
        return style;
    }

    private Style getCadStyle() {
        ArrayList<RuleImpl> cadRules = new ArrayList<RuleImpl>();
        this.getLabelStyle().setAttribute("Text");
        Symbolizer textSymbolizer = this.addLabelStyle(this.getLabelStyle());
        FeatureCollection fc = this.getUltimateFeatureCollectionWrapper();
        if (fc instanceof FeatureCollectionOnDemand) {
            FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper();
            Hashtable<String, Set<Integer>> layerToColor = ((AbstractCadDataSource)fcd.getDataAccesor()).getLayerToColor();
            for (String key : layerToColor.keySet()) {
                Set<Integer> colores = layerToColor.get(key);
                for (Integer color : colores) {
                    RuleImpl regla = new RuleImpl();
                    regla.setName(key);
                    regla.setTitle(key);
                    regla.setMinScaleDenominator(0.0);
                    regla.setMaxScaleDenominator(Double.MAX_VALUE);
                    Symbolizer symbol = this.addPolygonSymbolizer(AcadColor.getColor((int)color), this.getBasicStyle());
                    regla.setSymbolizers(new Symbolizer[]{symbol, textSymbolizer});
                    cadRules.add(regla);
                }
            }
        } else {
            RuleImpl regla = new RuleImpl();
            regla.setName("Default");
            regla.setTitle("Default");
            regla.setMinScaleDenominator(0.0);
            regla.setMaxScaleDenominator(Double.MAX_VALUE);
            Symbolizer symbol = this.addPolygonSymbolizer(AcadColor.getColor((int)0), this.getBasicStyle());
            regla.setSymbolizers(new Symbolizer[]{symbol, textSymbolizer});
            cadRules.add(regla);
        }
        Rule[] rules = new Rule[cadRules.size()];
        cadRules.toArray(rules);
        FeatureTypeStyleImpl estilo = new FeatureTypeStyleImpl(rules);
        estilo.setFeatureTypeName(this.getName());
        estilo.setName(this.getName());
        StyleImpl style = new StyleImpl();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{estilo});
        style.setName(this.getName());
        style.setTitle(this.getName());
        return style;
    }

    public void setModelStyle(Style modelStyle) {
        int index = -1;
        int i = 0;
        Iterator<Style> iterator = this.availableModelStyles.iterator();
        while (iterator.hasNext() && index == -1) {
            Style style = iterator.next();
            if (style.getName().equals(modelStyle.getName())) {
                index = i;
            }
            ++i;
        }
        if (index != -1) {
            this.availableModelStyles.remove(index);
            this.availableModelStyles.add(index, modelStyle);
        } else {
            this.availableModelStyles.add(modelStyle);
        }
        this.modelStyle = modelStyle;
    }

    public static String decodeColor(Color color) {
        String blueValue;
        String greenValue;
        String redValue = Integer.toHexString(color.getRed());
        if (redValue.length() == 1) {
            redValue = "0" + redValue;
        }
        if ((greenValue = Integer.toHexString(color.getGreen())).length() == 1) {
            greenValue = "0" + greenValue;
        }
        if ((blueValue = Integer.toHexString(color.getBlue())).length() == 1) {
            blueValue = "0" + blueValue;
        }
        return "#" + redValue + greenValue + blueValue;
    }

    public boolean hasRulesInScale(double scale) {
        boolean solucion = false;
        if (this.getModelStyle() != null && this.getModelStyle().getSelectedFeatureTypeStyle() != null) {
            Rule[] rules = this.getModelStyle().getSelectedFeatureTypeStyle().getRules();
            int i = 0;
            while (i < rules.length) {
                Rule rule = rules[i];
                if ((rule.getMinScaleDenominator() <= scale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= scale || Double.isNaN(rule.getMaxScaleDenominator()))) {
                    return true;
                }
                ++i;
            }
        }
        return solucion;
    }

    public Map<String, String> getAttributePublicNames() {
        return this.attributePublicNames;
    }

    public void setAttributePublicNames(Map<String, String> attrPublicNames) {
        this.attributePublicNames = attrPublicNames;
    }

    public Map<String, Boolean> getAttributeVisibility() {
        return this.attributeVisibility;
    }

    public void setAttributeVisibility(Map<String, Boolean> attributeVisibility) {
        this.attributeVisibility = attributeVisibility;
    }

    public Collection<Attribute> getAtributosConsultables() {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        FeatureSchema schema = this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
        int i = 0;
        while (i < schema.getAttributeCount()) {
            Attribute att = schema.getAttribute(i);
            if (att.isVisibility() && !att.getType().equals(AttributeType.GEOMETRY)) {
                attributes.add(att);
            }
            ++i;
        }
        return attributes;
    }

    @Override
    public void setName(String name) {
        Rule rule;
        Rule[] rules;
        if (this.getModelStyle() != null && this.getModelStyle().getSelectedFeatureTypeStyle() != null && (rules = this.getModelStyle().getSelectedFeatureTypeStyle().getRules()).length == 1 && (rule = rules[0]).getTitle().equals(this.getName())) {
            rule.setTitle(name);
        }
        super.setName(name);
    }

    public boolean isOverlapping() {
        return this.overlapping;
    }

    public void setOverlapping(boolean overlapping) {
        this.overlapping = overlapping;
    }

    public boolean isRepeated() {
        return this.repeated;
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }

    @Override
    public int compareTo(Layer obj) {
        return Collator.getInstance(I18N.getLocale()).compare(this.getName(), obj.getName());
    }

    public void clearTransaction() {
        FeatureCollectionOnDemand fcd;
        FeatureCollection fc = this.getUltimateFeatureCollectionWrapper();
        if (fc == null) {
            return;
        }
        if (fc instanceof FeatureCollectionOnDemand && (fcd = (FeatureCollectionOnDemand)fc).getDataAccesor() instanceof AbstractJDBCDataSource) {
            try {
                ((AbstractJDBCDataSource)fcd.getDataAccesor()).clearTransaction();
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
    }

    @Override
    public ICoordTrans getCoordTrans() {
        return this.coordTrans;
    }

    @Override
    public void setCoordTrans(ICoordTrans coordTrans) {
        this.coordTrans = coordTrans;
    }

    public boolean isCollapsed() {
        return this.collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public String getCrsDescription() {
        return this.crsDescription;
    }

    public boolean isMemory() {
        return this.memory;
    }

    public void setMemory(boolean memory) {
        block12: {
            if (memory) {
                FeatureCollection fc = this.getUltimateFeatureCollectionWrapper();
                if (fc == null) {
                    this.memory = true;
                    return;
                }
                this.fcMemory = new FeatureDataset(fc.getFeatureSchema());
                FeatureIterator it = null;
                try {
                    try {
                        it = fc.iterator();
                        while (it.hasNext()) {
                            Feature feat = it.next();
                            feat.setAttribute(fc.getFeatureSchema().getPrimaryKeyIndex(), null);
                            this.fcMemory.add(feat);
                        }
                        this.fcMemory.commit();
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        this.fcMemory.rollBack();
                        if (it != null) {
                            it.close();
                        }
                        break block12;
                    }
                }
                catch (Throwable throwable) {
                    if (it != null) {
                        it.close();
                    }
                    throw throwable;
                }
                if (it != null) {
                    it.close();
                }
            } else {
                this.fcMemory = null;
                System.gc();
            }
        }
        this.memory = memory;
    }

    public void setInMemory(boolean memory) {
        this.memory = memory;
    }

    public Envelope getVista() {
        return this.vista;
    }

    public void setVista(Envelope vista) {
        AbstractDataSource ds;
        this.vista = vista;
        if (this.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && (ds = ((FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper()).getDataAccesor()) instanceof MySQLDataSource) {
            this.getUltimateFeatureCollectionWrapper().setEnvelope(vista);
        }
    }

    public boolean isInScale(double scale) {
        Rule[] rules = this.getModelStyle().getSelectedFeatureTypeStyle().getRules();
        int i = 0;
        while (i < rules.length) {
            Rule rule = rules[i];
            if ((rule.getMinScaleDenominator() <= scale || Double.isNaN(rule.getMinScaleDenominator())) && (rule.getMaxScaleDenominator() >= scale || Double.isNaN(rule.getMaxScaleDenominator()))) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public Object clone() {
        Layer layer = new Layer();
        layer.setName(this.getName());
        layer.setDescription(this.getDescription());
        layer.setDataSourceQuery(this.getDataSourceQuery());
        layer.setVisible(true);
        layer.setProjection(this.getProjection(), this.getCoordTrans());
        layer.setFeatureCollection((FeatureCollection)this.getFeatureCollectionWrapper().getUltimateWrappee().clone());
        layer.setInternal(this.isInternal());
        layer.setLayerListener(null);
        layer.setGeometryType(this.getGeometryType());
        layer.setInMemory(this.isMemory());
        layer.setRepeated(this.isRepeated());
        layer.setOverlapping(this.isOverlapping());
        layer.setOneQueryByRule(this.isOneQueryByRule());
        layer.setFcMemory(this.getFcMemory());
        HashMap<Object, Object> cloneMap = new HashMap<Object, Object>();
        for (Object key : this.properties.keySet()) {
            cloneMap.put(key, this.properties.get(key));
        }
        layer.setProperties(cloneMap);
        HashMap<Locale, String> newTitleByLang = new HashMap<Locale, String>();
        for (Locale locale : this.titleByLang.keySet()) {
            newTitleByLang.put(locale, (String)this.titleByLang.get(locale));
        }
        layer.setTitleByLang(newTitleByLang);
        if (this.layerFilter != null) {
            layer.setLayerFilter((Filter)((Cloneable)this.layerFilter).clone());
        }
        layer.setModelStyle((Style)((StyleImpl)this.getModelStyle()).clone());
        return layer;
    }

    public Layer cloneForWebServer() {
        Layer layer = new Layer();
        layer.setName(this.getName());
        layer.setDescription(this.getDescription());
        layer.setVisible(true);
        if (this.isRaster()) {
            layer.setDataSourceQuery(this.getDataSourceQuery());
            FeatureCollection fc = this.getFeatureCollectionWrapper().getUltimateWrappee();
            if (fc == null) {
                LOGGER.warn((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.model.Layer.The-layer-associated-feature-collection-is-null")) + this.getDataSourceQuery().getQuery()));
            }
            layer.setFeatureCollection(fc);
        }
        layer.setCrsCode(this.getCrsCode());
        layer.geometryType = this.geometryType;
        layer.setOneQueryByRule(this.oneQueryByRule);
        layer.setProperties(this.properties);
        layer.setInMemory(this.memory);
        layer.setRepeated(this.repeated);
        layer.setOverlapping(this.overlapping);
        layer.setFcMemory(this.getFcMemory());
        layer.setTitleByLang(this.getTitleByLang());
        layer.setModelStyle((Style)((StyleImpl)this.getModelStyle()).clone());
        layer.setStyles(this.getStyles());
        return layer;
    }

    protected FeatureDataset getFcMemory() {
        return this.fcMemory;
    }

    protected void setFcMemory(FeatureDataset fcMemory) {
        this.fcMemory = fcMemory;
    }

    protected void setStyles(List<com.vividsolutions.jump.workbench.ui.renderer.style.Style> styles) {
        this.styles = styles;
    }

    public boolean isVersionable() {
        return this.versionable;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            FeatureSchema schema = this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
            schema.setVersionable(versionable);
            if (!versionable) {
                this.startDateField = null;
                this.endDateField = null;
                schema.setFieldEndDate(null);
                schema.setFieldStartDate(null);
                schema.setVersionableViewDate(null);
                this.versionableViewDate = null;
            }
        }
    }

    public Filter getEndDateFilter() {
        Filter fechaBajaFilter = null;
        if (this.isVersionable()) {
            try {
                fechaBajaFilter = (Filter)ExpressionBuilder.parse(this.getUltimateFeatureCollectionWrapper().getFeatureSchema(), "isNull(" + this.getEndDateField() + ")");
            }
            catch (ParseException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return fechaBajaFilter;
    }

    public String getStartDateField() {
        return this.startDateField;
    }

    public void setStartDateField(String fieldStartDate) {
        this.startDateField = fieldStartDate;
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            FeatureSchema schema = this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
            schema.setFieldStartDate(fieldStartDate);
        }
    }

    public String getEndDateField() {
        return this.endDateField;
    }

    public void setEndDateField(String fieldEndDate) {
        this.endDateField = fieldEndDate;
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            FeatureSchema schema = this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
            schema.setFieldEndDate(fieldEndDate);
        }
    }

    public String getHistoryField() {
        return this.historyField;
    }

    public void setHistoryField(String historyField) {
        this.historyField = historyField;
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            FeatureSchema schema = this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
            schema.setHistoryField(historyField);
        }
    }

    public void setGeometryType(int geometryType) {
        this.geometryType = geometryType;
        if (this.getUltimateFeatureCollectionWrapper() != null && this.getUltimateFeatureCollectionWrapper().getFeatureSchema() != null) {
            this.getUltimateFeatureCollectionWrapper().getFeatureSchema().setGeometryType(geometryType);
        }
    }

    public Envelope getTransformedEnvelope() {
        Envelope layerEnvelope = null;
        if (this.getFeatureCollectionWrapper() != null) {
            try {
                layerEnvelope = this.getFeatureCollectionWrapper().getEnvelope();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
                layerEnvelope = new Envelope();
            }
        }
        if (layerEnvelope != null && this.getCoordTrans() != null) {
            try {
                Rectangle2D.Double layerView = new Rectangle2D.Double(layerEnvelope.getMinX(), layerEnvelope.getMinY(), layerEnvelope.getWidth() + 0.001, layerEnvelope.getHeight() + 0.001);
                Rectangle2D transformedView = this.getCoordTrans().convert((Rectangle2D)layerView);
                layerEnvelope = new Envelope(transformedView.getMinX(), transformedView.getMaxX(), transformedView.getMinY(), transformedView.getMaxY());
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        return layerEnvelope;
    }

    public Timestamp getVersionableViewDate() {
        return this.versionableViewDate;
    }

    public void setVersionableViewDate(Timestamp versionableViewDate) {
        this.versionableViewDate = versionableViewDate;
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            FeatureSchema schema = this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
            schema.setVersionableViewDate(versionableViewDate);
        }
    }

    public boolean isCadLayer() {
        FeatureCollection fc = this.getUltimateFeatureCollectionWrapper();
        if (!(fc instanceof FeatureCollectionOnDemand)) {
            return false;
        }
        FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)fc;
        return fcd.getDataAccesor() instanceof AbstractCadDataSource;
    }

    @Override
    public IProjection getProjection() {
        return this.projection;
    }

    public void setProjection(IProjection proj, ICoordTrans trans) {
        this.projection = proj;
        this.coordTrans = trans;
        if (proj != null) {
            ICrs crs = (ICrs)proj;
            this.setCrsWKT(crs.getWKT());
            this.setNadGrid(crs.getTransParam());
            this.setTargetNad(crs.isTransInTarget());
            this.setCrsCode(crs.getCode());
            this.setCrsParams(crs.getSourceTransformationParams());
        }
    }

    @Override
    public void setProjection(IProjection proj) {
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        Task task = context.getTask();
        IProjection projTask = null;
        if (task != null) {
            projTask = task.getProjection();
        }
        this.setProjection(proj, projTask);
    }

    public void setProjection(IProjection proj, IProjection taskProjection) {
        this.projection = proj;
        if (proj != null) {
            if (taskProjection != null && !proj.getAbrev().equals(taskProjection.getAbrev())) {
                ICoordTrans ct = proj.getCT(taskProjection);
                this.setCoordTrans(ct);
                LOGGER.debug((Object)("Cambio proyecci\u00f3n: vista con " + taskProjection.getAbrev() + " y capa " + this.getName() + " con " + proj.getAbrev()));
            } else if (taskProjection != null && proj.getAbrev().equals(taskProjection.getAbrev()) && this.getCoordTrans() != null) {
                this.setCoordTrans(null);
            }
        } else {
            this.projection = taskProjection;
            this.setCoordTrans(null);
        }
        if (this.projection != null) {
            ICrs crs = (ICrs)this.projection;
            this.setCrsWKT(crs.getWKT());
            this.setNadGrid(crs.getTransParam());
            this.setTargetNad(crs.isTransInTarget());
            this.setCrsCode(crs.getCode());
            this.setCrsParams(crs.getSourceTransformationParams());
        }
    }

    public void setCrsDescription(String crsDescription) {
        this.crsDescription = crsDescription;
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

    public int getCrsCode() {
        return this.crsCode;
    }

    public void setCrsCode(int crsCode) {
        this.crsCode = crsCode;
    }

    public String getCrsParams() {
        return this.crsParams;
    }

    public void setCrsParams(String crsParams) {
        this.crsParams = crsParams;
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

    public void addTopologyRelation(ITopologyRelation topologyRelation) {
        this.topologyRelations.add(topologyRelation);
        this.getUltimateFeatureCollectionWrapper().setTopologyRelations(this.topologyRelations);
    }

    public void setTopologyRelations(List<ITopologyRelation> topologyRelations) {
        this.topologyRelations.clear();
        this.topologyRelations.addAll(topologyRelations);
        this.getUltimateFeatureCollectionWrapper().setTopologyRelations(this.topologyRelations);
    }

    public List<ITopologyRelation> getTopologyRelations() {
        return this.topologyRelations;
    }

    public boolean removeTopologyRelation(ITopologyRelation topologyRelation) {
        boolean removed = this.topologyRelations.remove(topologyRelation);
        this.getUltimateFeatureCollectionWrapper().setTopologyRelations(this.topologyRelations);
        return removed;
    }

    public void removeAllTopologiesRelations() {
        this.topologyRelations.clear();
        this.getUltimateFeatureCollectionWrapper().setTopologyRelations(this.topologyRelations);
    }

    public boolean isOneQueryByRule() {
        return this.oneQueryByRule;
    }

    public void setOneQueryByRule(boolean oneQueryByRule) {
        this.oneQueryByRule = oneQueryByRule;
    }

    public void refreshSelection(Collection<Feature> selectedFeatures) {
        this.getUltimateFeatureCollectionWrapper().refreshSelection(selectedFeatures);
    }

    public Filter getLayerFilter() {
        return this.layerFilter;
    }

    public void setLayerFilter(Filter layerFilter) {
        this.layerFilter = layerFilter;
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            this.getUltimateFeatureCollectionWrapper().setLayerFilter(layerFilter);
        }
    }

    public void setLayerListener(LayerListener layerListener) {
        this.layerListener = layerListener;
    }

    public boolean isInternal() {
        return this.internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    protected void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public List<Style> getAvailableModelStyles() {
        return this.availableModelStyles;
    }

    public void setAvailableModelStyles(List<Style> modelStyles) {
        this.availableModelStyles = modelStyles;
    }

    @Override
    public void addLocale(Locale locale) {
        if (!this.titleByLang.containsKey(locale)) {
            this.titleByLang.put(locale, this.name);
            for (Style style : this.availableModelStyles) {
                FeatureTypeStyle[] featTypeStyles = style.getFeatureTypeStyles();
                int i = 0;
                while (i < featTypeStyles.length) {
                    featTypeStyles[i].addLocale(locale);
                    ++i;
                }
            }
            FeatureSchema fs = this.getFeatureSchema();
            Iterator<String> iterator = fs.getAttributes().keySet().iterator();
            while (iterator.hasNext()) {
                Attribute attr = fs.getAttribute(iterator.next());
                attr.addLocale(locale);
            }
        }
    }

    @Override
    public void removeLocale(Locale locale) {
        if (this.titleByLang.containsKey(locale)) {
            this.titleByLang.remove(locale);
            for (Style style : this.availableModelStyles) {
                FeatureTypeStyle[] featTypeStyles = style.getFeatureTypeStyles();
                int i = 0;
                while (i < featTypeStyles.length) {
                    featTypeStyles[i].removeLocale(locale);
                    ++i;
                }
            }
            FeatureSchema fs = this.getFeatureSchema();
            Iterator<String> iterator = fs.getAttributes().keySet().iterator();
            while (iterator.hasNext()) {
                Attribute attr = fs.getAttribute(iterator.next());
                attr.removeLocale(locale);
            }
        }
    }

    public Map<String, Map<Locale, String>> getAttributeTranslationsMap() {
        return this.attributeTranslationsMap;
    }

    public void setAttributeTranslationsMap(Map<String, Map<Locale, String>> attributeTranslationsMap) {
        this.attributeTranslationsMap = attributeTranslationsMap;
    }

    public List<String> getFinderFields() {
        return this.finderFields;
    }

    public void setFinderFields(String ... finderFields) {
        this.setFinderFields(Arrays.asList(finderFields));
    }

    public void setFinderFields(List<String> finderFields) {
        this.finderFields = finderFields;
    }

    public Set<ITrigger> getTriggers() {
        return this.triggers;
    }

    public void setTriggers(Set<ITrigger> triggers) {
        this.triggers.clear();
        this.triggers.addAll(triggers);
        this.getUltimateFeatureCollectionWrapper().setTriggers(this.triggers);
    }

    public Hashtable<String, Domain> getDomains() {
        return this.domains;
    }

    public void setDomains(Hashtable<String, Domain> domains) {
        this.domains = domains;
    }

    public Layer() {
        this.blackboard.put(FIRING_APPEARANCE_CHANGED_ON_ATTRIBUTE_CHANGE, true);
    }

    public void addDefaultStyles() {
        this.addStyle(new BasicStyle());
        this.addStyle(new SquareVertexStyle());
        this.addStyle(new LabelStyle());
    }

    public void removeRelation(Relation<?> relation) {
        if (this.relations.containsKey(relation.getRelationName())) {
            this.relations.remove(relation.getRelationName());
            if (this.getUltimateFeatureCollectionWrapper() != null) {
                FeatureCollectionOnDemand fc;
                this.getUltimateFeatureCollectionWrapper().getFeatureSchema().removeRelation(relation);
                if (this.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && (fc = (FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper()).getDataAccesor() != null && fc.getDataAccesor() instanceof AbstractJDBCDataSource) {
                    ((AbstractJDBCDataSource)fc.getDataAccesor()).resetLabels();
                }
            }
            relation.destroy();
        }
    }

    public void removeAllRelations(Collection<Relation<?>> relations) {
        for (Relation<?> element : relations) {
            this.removeRelation(element);
        }
    }

    public void addRelation(Relation<?> relation) {
        LayerRelation layerRelation;
        this.relations.put(relation.getRelationName(), relation);
        if (relation instanceof LayerRelation && !(layerRelation = (LayerRelation)relation).getTargetLayer().isEnabled()) {
            return;
        }
        if (this.getUltimateFeatureCollectionWrapper() != null) {
            try {
                FeatureCollectionOnDemand fc;
                this.getUltimateFeatureCollectionWrapper().getFeatureSchema().addRelation(relation);
                if (this.getUltimateFeatureCollectionWrapper() instanceof FeatureCollectionOnDemand && (fc = (FeatureCollectionOnDemand)this.getUltimateFeatureCollectionWrapper()).getDataAccesor() != null && fc.getDataAccesor() instanceof AbstractJDBCDataSource) {
                    ((AbstractJDBCDataSource)fc.getDataAccesor()).resetLabels();
                }
            }
            catch (Exception e) {
                LOGGER.error((Object)I18N.getMessage("com.vividsolutions.jump.workbench.model.Layer.The-relation-{0}-has-failed", new Object[]{relation.getRelationName()}), (Throwable)e);
            }
        }
    }

    public Relation<?> getRelation(String name) {
        return this.relations.get(name);
    }

    public boolean hasRelation(String name) {
        return this.relations.containsKey(name);
    }

    public boolean hasRelations() {
        return !this.relations.isEmpty();
    }

    public Collection<Relation<?>> getAllRelations() {
        return this.relations.values();
    }

    public Map<String, Relation<?>> getRelations() {
        return this.relations;
    }

    public void setRelations(Map<String, Relation<?>> relations) {
        for (Relation<?> relation : relations.values()) {
            this.addRelation(relation);
        }
    }

    public boolean hashiperLink() {
        return this.getHiperLink() != null;
    }

    public HiperLink getHiperLink() {
        return this.hiperLink;
    }

    public void setHiperLink(HiperLink hiperLink) {
        this.hiperLink = hiperLink;
    }

    public FeatureSchema getFeatureSchema() {
        if (this.getUltimateFeatureCollectionWrapper() == null) {
            return null;
        }
        return this.getUltimateFeatureCollectionWrapper().getFeatureSchema();
    }

    public Map<Object, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    public Envelope getTransformedEnvelope(Envelope viewEnv) {
        if (this.getCoordTrans() != null) {
            Rectangle2D.Double view = new Rectangle2D.Double(viewEnv.getMinX(), viewEnv.getMinY(), viewEnv.getWidth(), viewEnv.getHeight());
            Rectangle2D viewConverted = this.getCoordTrans().getInverted().convert((Rectangle2D)view);
            return new Envelope(viewConverted.getMinX(), viewConverted.getMaxX(), viewConverted.getMinY(), viewConverted.getMaxY());
        }
        return viewEnv;
    }
}

