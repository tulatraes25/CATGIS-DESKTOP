/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.qa.ValidationError;
import com.vividsolutions.jump.qa.Validator;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.RingVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.filter.FilterFactory;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.Mark;
import org.saig.core.styling.MarkImpl;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.StyleBuilder;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ValidateSelectedLayersPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(ValidateSelectedLayersPlugIn.class);
    public static final String NAME = String.valueOf(I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.name")) + "...";
    public static final Icon ICON = IconLoader.icon("accept.png");
    private static final String CHECK_BASIC_TOPOLOGY = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-basic-topology");
    private static final String CHECK_POLYGON_ORIENTATION = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-polygon-orientation");
    private static final String CHECK_LINESTRINGS_SIMPLE = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-that-linestrings-are-simple");
    private static final String CHECK_POLYGONS_HAVE_NO_HOLES = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-polygons-and-multipolygons-with-holes");
    private static final String CHECK_NO_REPEATED_CONSECUTIVE_POINTS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-repeated-consecutive-points");
    private static final String CHECK_MIN_SEGMENT_LENGTH = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-minimum-segment-length");
    private static final String CHECK_MIN_ANGLE = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-minimum-angle");
    private static final String MIN_SEGMENT_LENGTH = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.minimum-segment-length");
    private static final String MIN_ANGLE = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.minimim-angle-in-degrees");
    private static final String MIN_POLYGON_AREA = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.minimum-polygon-area");
    private static final String CHECK_MIN_POLYGON_AREA = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-minimum-polygon-area");
    private static final String DISALLOW_POINTS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-points");
    private static final String DISALLOW_LINESTRINGS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-linestrings");
    private static final String DISALLOW_POLYGONS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-polygons");
    private static final String DISALLOW_MULTIPOINTS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-multipoints");
    private static final String DISALLOW_MULTILINESTRINGS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-multilinestrings");
    private static final String DISALLOW_MULTIPOLYGONS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-multipolygons");
    private static final String DISALLOW_GEOMETRYCOLLECTIONS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-geometrycollections");
    private static final String ERROR = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.error");
    private static final String SOURCE_PK = "SOURCE_PK";
    private MultiInputDialog dialog;
    private FeatureSchema schema;
    private Validator validator;
    private int gid = 0;
    private boolean pointFlag;
    private boolean lineFlag;
    private boolean polygonFlag;
    private PlugInContext context;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ValidateSelectedLayersPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.context = context;
        this.initFlags();
        this.validator = this.prompt(context);
        return this.validator != null;
    }

    protected void initFlags() {
        this.pointFlag = false;
        this.lineFlag = false;
        this.polygonFlag = false;
        Layerable[] selectedLayers = this.context.getSelectedLayers();
        int i = 0;
        while (i < selectedLayers.length) {
            Layerable layerable = selectedLayers[i];
            if (layerable instanceof Layer) {
                int type = ((Layer)layerable).getGeometryType();
                this.pointFlag = this.pointFlag || type == 1 || type == 8;
                this.lineFlag = this.lineFlag || type == 3 || type == 2;
                this.polygonFlag = this.polygonFlag || type == 5 || type == 4;
            }
            ++i;
        }
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        Layerable[] selectedLayers = context.getSelectedLayers();
        boolean validate = true;
        int i = 0;
        while (i < selectedLayers.length && !monitor.isCancelRequested()) {
            validate = this.validate((Layer)selectedLayers[i], this.validator, context, monitor) && validate;
            ++i;
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ValidateSelectedLayersPlugIn.Validating-process-has-been-cancelled-by-the-user"));
        } else if (!validate) {
            DialogFactory.showErrorDialog(context.getWorkbenchFrame(), I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.errors-have-been-found-in-the-QA-category-layers-corresponding-to-these-errors-have-been-generated"), I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.topology-errors"));
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.ValidateSelectedLayersPlugIn.The-validating-process-has-finished-without-finding-errors"));
        }
    }

    protected void buildFeatureSchema(Layer layer) {
        this.schema = new FeatureSchema();
        this.schema.addAttribute("GID", AttributeType.INTEGER, new Boolean(true));
        this.schema.addAttribute(ERROR, AttributeType.STRING);
        AttributeType sourcePkType = AttributeType.INTEGER;
        if (layer.getFeatureSchema() != null && layer.getFeatureSchema().getPrimaryKey() != null) {
            sourcePkType = layer.getFeatureSchema().getPrimaryKey().getType();
        }
        this.schema.addAttribute(SOURCE_PK, sourcePkType);
        this.schema.addAttribute("geometry", AttributeType.GEOMETRY);
    }

    protected void disableOptionByFlags() {
        this.dialog.getCheckBox(CHECK_NO_REPEATED_CONSECUTIVE_POINTS).setEnabled(false);
        this.dialog.getCheckBox(CHECK_NO_REPEATED_CONSECUTIVE_POINTS).setSelected(false);
        this.dialog.getCheckBox(CHECK_POLYGON_ORIENTATION).setEnabled(false);
        this.dialog.getCheckBox(CHECK_POLYGON_ORIENTATION).setSelected(false);
        this.dialog.getCheckBox(CHECK_MIN_SEGMENT_LENGTH).setEnabled(false);
        this.dialog.getCheckBox(CHECK_MIN_SEGMENT_LENGTH).setSelected(false);
        this.dialog.getTextField(MIN_SEGMENT_LENGTH).setEnabled(false);
        this.dialog.getLabel(MIN_SEGMENT_LENGTH).setEnabled(false);
        this.dialog.getCheckBox(CHECK_MIN_ANGLE).setEnabled(false);
        this.dialog.getCheckBox(CHECK_MIN_ANGLE).setSelected(false);
        this.dialog.getTextField(MIN_ANGLE).setEnabled(false);
        this.dialog.getLabel(MIN_ANGLE).setEnabled(false);
        this.dialog.getCheckBox(CHECK_MIN_POLYGON_AREA).setEnabled(false);
        this.dialog.getCheckBox(CHECK_MIN_POLYGON_AREA).setSelected(false);
        this.dialog.getTextField(MIN_POLYGON_AREA).setEnabled(false);
        this.dialog.getLabel(MIN_POLYGON_AREA).setEnabled(false);
        this.dialog.getCheckBox(CHECK_LINESTRINGS_SIMPLE).setEnabled(false);
        this.dialog.getCheckBox(CHECK_LINESTRINGS_SIMPLE).setSelected(false);
        if (this.lineFlag) {
            this.dialog.getCheckBox(CHECK_NO_REPEATED_CONSECUTIVE_POINTS).setEnabled(true);
            this.dialog.getCheckBox(CHECK_MIN_SEGMENT_LENGTH).setEnabled(true);
            this.dialog.getTextField(MIN_SEGMENT_LENGTH).setEnabled(true);
            this.dialog.getLabel(MIN_SEGMENT_LENGTH).setEnabled(true);
            this.dialog.getCheckBox(CHECK_MIN_ANGLE).setEnabled(true);
            this.dialog.getTextField(MIN_ANGLE).setEnabled(true);
            this.dialog.getLabel(MIN_ANGLE).setEnabled(true);
            this.dialog.getCheckBox(CHECK_LINESTRINGS_SIMPLE).setEnabled(true);
        }
        if (this.polygonFlag) {
            this.dialog.getCheckBox(CHECK_NO_REPEATED_CONSECUTIVE_POINTS).setEnabled(true);
            this.dialog.getCheckBox(CHECK_POLYGON_ORIENTATION).setEnabled(true);
            this.dialog.getCheckBox(CHECK_MIN_SEGMENT_LENGTH).setEnabled(true);
            this.dialog.getTextField(MIN_SEGMENT_LENGTH).setEnabled(true);
            this.dialog.getLabel(MIN_SEGMENT_LENGTH).setEnabled(true);
            this.dialog.getCheckBox(CHECK_MIN_ANGLE).setEnabled(true);
            this.dialog.getTextField(MIN_ANGLE).setEnabled(true);
            this.dialog.getLabel(MIN_ANGLE).setEnabled(true);
            this.dialog.getCheckBox(CHECK_MIN_POLYGON_AREA).setEnabled(true);
            this.dialog.getTextField(MIN_POLYGON_AREA).setEnabled(true);
            this.dialog.getLabel(MIN_POLYGON_AREA).setEnabled(true);
        }
    }

    protected Validator prompt(PlugInContext context) {
        if (this.dialog == null) {
            this.initDialog(context);
        }
        this.disableOptionByFlags();
        this.dialog.setVisible(true);
        if (!this.dialog.wasOKPressed()) {
            return null;
        }
        Validator validator = new Validator();
        validator.setCheckingBasicTopology(this.dialog.getBoolean(CHECK_BASIC_TOPOLOGY));
        validator.setCheckingNoRepeatedConsecutivePoints(this.dialog.getBoolean(CHECK_NO_REPEATED_CONSECUTIVE_POINTS));
        validator.setCheckingLineStringsSimple(this.dialog.getBoolean(CHECK_LINESTRINGS_SIMPLE));
        validator.setCheckingPolygonOrientation(this.dialog.getBoolean(CHECK_POLYGON_ORIENTATION));
        validator.setCheckingNoHoles(this.dialog.getBoolean(CHECK_POLYGONS_HAVE_NO_HOLES));
        validator.setCheckingMinSegmentLength(this.dialog.getBoolean(CHECK_MIN_SEGMENT_LENGTH));
        validator.setCheckingMinAngle(this.dialog.getBoolean(CHECK_MIN_ANGLE));
        validator.setCheckingMinPolygonArea(this.dialog.getBoolean(CHECK_MIN_POLYGON_AREA));
        validator.setMinSegmentLength(this.dialog.getDouble(MIN_SEGMENT_LENGTH));
        validator.setMinAngle(this.dialog.getDouble(MIN_ANGLE));
        validator.setMinPolygonArea(this.dialog.getDouble(MIN_POLYGON_AREA));
        ArrayList<Class<? extends Geometry>> disallowedGeometryClasses = new ArrayList<Class<? extends Geometry>>();
        if (this.dialog.getBoolean(DISALLOW_POINTS)) {
            disallowedGeometryClasses.add(Point.class);
        }
        if (this.dialog.getBoolean(DISALLOW_LINESTRINGS)) {
            disallowedGeometryClasses.add(LineString.class);
        }
        if (this.dialog.getBoolean(DISALLOW_POLYGONS)) {
            disallowedGeometryClasses.add(Polygon.class);
        }
        if (this.dialog.getBoolean(DISALLOW_MULTIPOINTS)) {
            disallowedGeometryClasses.add(MultiPoint.class);
        }
        if (this.dialog.getBoolean(DISALLOW_MULTILINESTRINGS)) {
            disallowedGeometryClasses.add(MultiLineString.class);
        }
        if (this.dialog.getBoolean(DISALLOW_MULTIPOLYGONS)) {
            disallowedGeometryClasses.add(MultiPolygon.class);
        }
        if (this.dialog.getBoolean(DISALLOW_GEOMETRYCOLLECTIONS)) {
            disallowedGeometryClasses.add(GeometryCollection.class);
        }
        validator.setDisallowedGeometryClasses(disallowedGeometryClasses);
        return validator;
    }

    protected boolean validate(Layer layer, Validator validator, PlugInContext context, TaskMonitor monitor) {
        FeatureIterator itFeatures = null;
        monitor.allowCancellationRequests();
        List<Object> validationErrors = new ArrayList();
        try {
            this.gid = 0;
            this.buildFeatureSchema(layer);
            itFeatures = layer.getUltimateFeatureCollectionWrapper().queryOnlyGeometryIterator(null, new ArrayList<String>());
            int size = -1;
            try {
                size = layer.getUltimateFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            validationErrors = validator.validate(itFeatures, size, monitor, layer.getName());
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        if (!validationErrors.isEmpty()) {
            int geomType = layer.getFeatureCollectionWrapper().getUltimateWrappee().getFeatureSchema().getGeometryType();
            this.addLayer(this.toLayer(String.valueOf(I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.wrong-features")) + " - " + layer.getName(), this.toFeatures(validationErrors, layer), layer, false, context, geomType), context);
            Layer tmpLayer = this.toLayer(String.valueOf(I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.error-locations")) + " - " + layer.getName(), this.toLocationFeatures(validationErrors, layer), layer, false, context, 1);
            this.setConvenientStyle(tmpLayer);
            this.addLayer(tmpLayer, context);
            return false;
        }
        return true;
    }

    protected void setConvenientStyle(Layer tmpLayer) {
        FilterFactory filterFactory = FilterFactory.createFilterFactory();
        StyleFactory styleFactory = StyleFactory.createStyleFactory();
        StyleBuilder styleBuilder = new StyleBuilder();
        MarkImpl marca = new MarkImpl();
        marca.setFill(null);
        marca.setStroke(styleFactory.createStroke(styleBuilder.colorExpression(Color.RED), styleBuilder.literalExpression(3)));
        marca.setSize(styleBuilder.literalExpression(20));
        marca.setWellKnownName(filterFactory.createLiteralExpression("circle"));
        Graphic graphic = styleFactory.getDefaultGraphic();
        graphic.setMarks(new Mark[]{marca});
        graphic.setSize(styleBuilder.literalExpression(20));
        PointSymbolizer pointSymb = StyleFactory.createStyleFactory().createPointSymbolizer(graphic, tmpLayer.getFeatureSchema().getAttributeName(tmpLayer.getFeatureSchema().getGeometryIndex()));
        RuleImpl rule = new RuleImpl();
        rule.setTitle("");
        rule.setSymbolizers(new Symbolizer[]{pointSymb});
        FeatureTypeStyle featStyle = StyleFactory.createStyleFactory().createFeatureTypeStyle(new Rule[]{rule});
        StyleImpl modelStyle = new StyleImpl();
        modelStyle.addFeatureTypeStyle(featStyle);
        tmpLayer.setModelStyle(modelStyle);
    }

    protected void outputSummary(PlugInContext context, Layer layer, List<ValidationError> validationErrors) {
        HTMLFrame htmlFrame = context.getOutputFrame();
        htmlFrame.addHeader(2, String.valueOf(I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.layer")) + ": " + layer.getName());
        if (validationErrors.isEmpty()) {
            htmlFrame.addText(I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.no-validation-errors"));
            return;
        }
        CollectionMap descriptionToErrorMap = new CollectionMap();
        for (ValidationError error : validationErrors) {
            descriptionToErrorMap.addItem(error.getMessage(), error);
        }
        for (String message : descriptionToErrorMap.keySet()) {
            htmlFrame.addField(String.valueOf(message) + ":", String.valueOf(descriptionToErrorMap.getItems(message).size()));
        }
    }

    protected List<Feature> toFeatures(List<ValidationError> validationErrors, Layer sourceLayer) {
        ArrayList<Feature> features = new ArrayList<Feature>(validationErrors.size());
        for (ValidationError error : validationErrors) {
            features.add(this.toFeature(error, sourceLayer, (Geometry)error.getFeature().getGeometry().clone()));
        }
        return features;
    }

    protected List<Feature> toLocationFeatures(List<ValidationError> validationErrors, Layer sourceLayer) {
        ArrayList<Feature> features = new ArrayList<Feature>(validationErrors.size());
        for (ValidationError error : validationErrors) {
            Point geometry = geomFac.createPoint(error.getLocation());
            features.add(this.toFeature(error, sourceLayer, (Geometry)geometry));
        }
        return features;
    }

    protected Feature toFeature(ValidationError error, Layer sourceLayer, Geometry geometry) {
        Feature ringFeature = FeatureUtil.toFeature(geometry, this.schema);
        ringFeature.setAttribute("GID", (Object)new Integer(this.gid++));
        ringFeature.setAttribute(SOURCE_PK, error.getFeature().getPrimaryKey());
        ringFeature.setAttribute(ERROR, (Object)error.getMessage());
        ringFeature.setGeometry(geometry);
        return ringFeature;
    }

    protected void addLayer(Layer errorLayer, PlugInContext context) {
        context.getLayerManager().addLayer(StandardCategoryNames.QA, errorLayer);
    }

    protected Layer toLayer(String name, List<Feature> features, Layer sourceLayer, boolean ringVertices, PlugInContext context, int geomType) {
        boolean firingEvents = context.getLayerManager().isFiringEvents();
        context.getLayerManager().setFiringEvents(false);
        try {
            FeatureDataset errorFeatureCollection = new FeatureDataset(features, (FeatureSchema)this.schema.clone());
            errorFeatureCollection.getFeatureSchema().setGeometryType(geomType);
            Layer errorLayer = new Layer(name, Color.RED, (FeatureCollection)errorFeatureCollection, context.getLayerManager());
            if (ringVertices) {
                errorLayer.getBasicStyle().setEnabled(false);
                this.changeVertexToRing(errorLayer);
            }
            this.showVertices(errorLayer);
            errorLayer.setProjection(sourceLayer.getProjection(), context.getTask().getProjection());
            errorLayer.setCoordTrans(sourceLayer.getCoordTrans());
            Layer layer = errorLayer;
            return layer;
        }
        finally {
            context.getLayerManager().setFiringEvents(firingEvents);
        }
    }

    protected void changeVertexToRing(Layer errorLayer) {
        boolean firingEvents = errorLayer.getLayerManager().isFiringEvents();
        errorLayer.getLayerManager().setFiringEvents(false);
        try {
            errorLayer.removeStyle(errorLayer.getStyle(VertexStyle.class));
            errorLayer.addStyle(new RingVertexStyle());
            errorLayer.getBasicStyle().setLineWidth(5);
        }
        finally {
            errorLayer.getLayerManager().setFiringEvents(firingEvents);
        }
        errorLayer.fireAppearanceChanged();
    }

    protected void showVertices(Layer errorLayer) {
        errorLayer.getVertexStyle().setEnabled(true);
        errorLayer.fireAppearanceChanged();
    }

    protected void initDialog(PlugInContext context) {
        this.dialog = new MultiInputDialog(context.getWorkbenchFrame(), NAME, true);
        this.dialog.setSideBarImage(IconLoader.icon("toolImages/Validate.gif"));
        this.dialog.setSideBarDescription(String.valueOf(I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.tests-layers-against-various-criteria")) + I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.including-polygon-orientation-and-minimum-segment-length"));
        this.dialog.addLabel("<HTML><B>" + I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.geometry-metrics-validation") + "</B></HTML>");
        this.dialog.addSeparator();
        this.dialog.addCheckBox(CHECK_BASIC_TOPOLOGY, true, I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.test"));
        this.dialog.addCheckBox(CHECK_NO_REPEATED_CONSECUTIVE_POINTS, false);
        this.dialog.addCheckBox(CHECK_POLYGON_ORIENTATION, false, I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-that-polygon-shells-are-oriented-clockwise-and-holes-counterclockwise"));
        this.dialog.addCheckBox(CHECK_MIN_SEGMENT_LENGTH, false);
        this.dialog.addPositiveDoubleField(MIN_SEGMENT_LENGTH, 0.001, 5);
        this.dialog.getTextField(MIN_SEGMENT_LENGTH).setEnabled(false);
        this.dialog.getLabel(MIN_SEGMENT_LENGTH).setEnabled(false);
        this.dialog.addCheckBox(CHECK_MIN_ANGLE, false);
        this.dialog.addPositiveDoubleField(MIN_ANGLE, 1.0, 5);
        this.dialog.getTextField(MIN_ANGLE).setEnabled(false);
        this.dialog.getLabel(MIN_ANGLE).setEnabled(false);
        this.dialog.addCheckBox(CHECK_MIN_POLYGON_AREA, false);
        this.dialog.addPositiveDoubleField(MIN_POLYGON_AREA, 0.001, 5);
        this.dialog.getTextField(MIN_POLYGON_AREA).setEnabled(false);
        this.dialog.getLabel(MIN_POLYGON_AREA).setEnabled(false);
        this.dialog.addCheckBox(CHECK_LINESTRINGS_SIMPLE, false, I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.check-that-linestrings-are-simple-do-not-self-intersect"));
        this.dialog.startNewColumn();
        this.dialog.addLabel("<HTML><B>" + I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.geometry-types-validation") + "</B></HTML>");
        this.dialog.addSeparator();
        this.dialog.addCheckBox(DISALLOW_POINTS, false);
        this.dialog.addCheckBox(DISALLOW_LINESTRINGS, false);
        this.dialog.addCheckBox(DISALLOW_POLYGONS, false);
        this.dialog.addCheckBox(DISALLOW_MULTIPOINTS, false);
        this.dialog.addCheckBox(DISALLOW_MULTILINESTRINGS, false);
        this.dialog.addCheckBox(DISALLOW_MULTIPOLYGONS, false);
        this.dialog.addCheckBox(CHECK_POLYGONS_HAVE_NO_HOLES, false);
        this.dialog.addCheckBox(DISALLOW_GEOMETRYCOLLECTIONS, false, I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.geometry-collection-subtypes-are-not-disallowed"));
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.getCheckBox(CHECK_MIN_SEGMENT_LENGTH).addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = ValidateSelectedLayersPlugIn.this.dialog.getCheckBox(CHECK_MIN_SEGMENT_LENGTH).isSelected();
                ValidateSelectedLayersPlugIn.this.dialog.getTextField(MIN_SEGMENT_LENGTH).setEnabled(enabled);
                ValidateSelectedLayersPlugIn.this.dialog.getLabel(MIN_SEGMENT_LENGTH).setEnabled(enabled);
            }
        });
        this.dialog.getCheckBox(CHECK_MIN_ANGLE).addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = ValidateSelectedLayersPlugIn.this.dialog.getCheckBox(CHECK_MIN_ANGLE).isSelected();
                ValidateSelectedLayersPlugIn.this.dialog.getTextField(MIN_ANGLE).setEnabled(enabled);
                ValidateSelectedLayersPlugIn.this.dialog.getLabel(MIN_ANGLE).setEnabled(enabled);
            }
        });
        this.dialog.getCheckBox(CHECK_MIN_POLYGON_AREA).addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = ValidateSelectedLayersPlugIn.this.dialog.getCheckBox(CHECK_MIN_POLYGON_AREA).isSelected();
                ValidateSelectedLayersPlugIn.this.dialog.getTextField(MIN_POLYGON_AREA).setEnabled(enabled);
                ValidateSelectedLayersPlugIn.this.dialog.getLabel(MIN_POLYGON_AREA).setEnabled(enabled);
            }
        });
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createSelectedLayersMustNotBeWMSLayersCheck());
    }
}

