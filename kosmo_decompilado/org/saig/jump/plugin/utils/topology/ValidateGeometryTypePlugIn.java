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
package org.saig.jump.plugin.utils.topology;

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
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.RingVertexStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.VertexStyle;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.util.DialogFactory;

public class ValidateGeometryTypePlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.topology.ValidateGeometryTypePlugIn.Validate-geometry-type");
    public static final Icon ICON = IconLoader.icon("accept.png");
    private static final String CHECK_POLYGONS_HAVE_NO_HOLES = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-polygons-and-multipolygons-with-holes");
    private static final String DISALLOW_POINTS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-points");
    private static final String DISALLOW_LINESTRINGS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-linestrings");
    private static final String DISALLOW_POLYGONS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-polygons");
    private static final String DISALLOW_MULTIPOINTS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-multipoints");
    private static final String DISALLOW_MULTILINESTRINGS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-multilinestrings");
    private static final String DISALLOW_MULTIPOLYGONS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-multipolygons");
    private static final String DISALLOW_GEOMETRYCOLLECTIONS = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.disallow-geometrycollections");
    private static final String ERROR = I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.error");
    private static final String GID = "GID";
    private static final String SOURCE_PK = "SOURCE_PK";
    private static final String GEOMETRY = "GEOMETRY";
    private static final Logger LOGGER = Logger.getLogger(ValidateGeometryTypePlugIn.class);
    private MultiInputDialog dialog;
    private FeatureSchema schemaPoints;
    private FeatureSchema schemaLines;
    private FeatureSchema schemaPolygons;
    private Validator validator;
    private int gid = 0;

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
        return ValidateGeometryTypePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.validator = this.prompt(context);
        return this.validator != null;
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

    private void buildFeatureSchema(Layer layer) {
        this.schemaPoints = new FeatureSchema();
        this.schemaPoints.addAttribute(GID, AttributeType.INTEGER, true);
        this.schemaPoints.addAttribute(ERROR, AttributeType.STRING);
        AttributeType sourcePkType = AttributeType.INTEGER;
        if (layer.getFeatureSchema() != null && layer.getFeatureSchema().getPrimaryKey() != null) {
            sourcePkType = layer.getFeatureSchema().getPrimaryKey().getType();
        }
        this.schemaPoints.addAttribute(SOURCE_PK, sourcePkType);
        this.schemaPoints.addAttribute(GEOMETRY, AttributeType.GEOMETRY);
        this.schemaPoints.setGeometryType(8);
        this.schemaLines = (FeatureSchema)this.schemaPoints.clone();
        this.schemaLines.setGeometryType(2);
        this.schemaPolygons = (FeatureSchema)this.schemaPoints.clone();
        this.schemaPolygons.setGeometryType(4);
    }

    private Validator prompt(PlugInContext context) {
        if (this.dialog == null) {
            this.initDialog(context);
        }
        this.dialog.setVisible(true);
        if (!this.dialog.wasOKPressed()) {
            return null;
        }
        Validator validator = new Validator();
        validator.setCheckingBasicTopology(false);
        validator.setCheckingNoHoles(this.dialog.getBoolean(CHECK_POLYGONS_HAVE_NO_HOLES));
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

    private boolean validate(Layer layer, Validator validator, PlugInContext context, TaskMonitor monitor) {
        FeatureIterator itFeatures = null;
        monitor.allowCancellationRequests();
        List<Object> validationErrors = new ArrayList();
        try {
            this.gid = 0;
            this.buildFeatureSchema(layer);
            itFeatures = layer.getUltimateFeatureCollectionWrapper().queryOnlyGeometryIterator(null, new ArrayList<String>());
            int size = 0;
            try {
                size = layer.getUltimateFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            validationErrors = validator.validate(itFeatures, size, monitor, layer.getName());
        }
        finally {
            if (itFeatures != null && itFeatures instanceof FeatureIterator) {
                itFeatures.close();
            }
        }
        if (!validationErrors.isEmpty()) {
            List<Feature> errorPointsList = this.toFeatures(validationErrors, layer, 8);
            Layer errorPointsLayer = this.toLayer(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.ValidateGeometryTypePlugIn.Wrong-point-features")) + " - " + layer.getName(), errorPointsList, layer, false, context, 8);
            List<Feature> errorLinesList = this.toFeatures(validationErrors, layer, 2);
            Layer errorLinesLayer = this.toLayer(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.ValidateGeometryTypePlugIn.Wrong-line-features")) + " - " + layer.getName(), errorLinesList, layer, false, context, 2);
            List<Feature> errorPolygonsList = this.toFeatures(validationErrors, layer, 4);
            Layer errorPolygonsLayer = this.toLayer(String.valueOf(I18N.getString("org.saig.jump.plugin.utils.topology.ValidateGeometryTypePlugIn.Wrong-polygon-features")) + " - " + layer.getName(), errorPolygonsList, layer, false, context, 4);
            if (!errorPointsList.isEmpty()) {
                this.addLayer(errorPointsLayer, context);
            }
            if (!errorLinesList.isEmpty()) {
                this.addLayer(errorLinesLayer, context);
            }
            if (!errorPolygonsList.isEmpty()) {
                this.addLayer(errorPolygonsLayer, context);
            }
            return false;
        }
        return true;
    }

    private List<Feature> toFeatures(List<ValidationError> validationErrors, Layer sourceLayer, int filterGeomType) {
        ArrayList<Feature> features = new ArrayList<Feature>();
        for (ValidationError error : validationErrors) {
            int errorGeomType = error.getFeature().getSchema().getGeometryType();
            if (!this.filterGeomType(errorGeomType, filterGeomType)) continue;
            features.add(this.toFeature(error, sourceLayer, (Geometry)error.getFeature().getGeometry().clone(), errorGeomType));
        }
        return features;
    }

    private boolean filterGeomType(int gt1, int gt2) {
        switch (gt1) {
            case 1: 
            case 8: {
                return gt2 == 1 || gt2 == 8;
            }
            case 2: 
            case 3: {
                return gt2 == 3 || gt2 == 2;
            }
            case 4: 
            case 5: {
                return gt2 == 5 || gt2 == 4;
            }
        }
        return false;
    }

    private Feature toFeature(ValidationError error, Layer sourceLayer, Geometry geometry, int geomType) {
        Feature ringFeature;
        switch (geomType) {
            case 1: 
            case 8: {
                ringFeature = FeatureUtil.toFeature(geometry, this.schemaPoints);
                break;
            }
            case 2: 
            case 3: {
                ringFeature = FeatureUtil.toFeature(geometry, this.schemaLines);
                break;
            }
            default: {
                ringFeature = FeatureUtil.toFeature(geometry, this.schemaPolygons);
            }
        }
        ringFeature.setAttribute(GID, (Object)new Integer(this.gid++));
        ringFeature.setAttribute(SOURCE_PK, error.getFeature().getPrimaryKey());
        ringFeature.setAttribute(ERROR, (Object)error.getMessage());
        ringFeature.setGeometry(geometry);
        return ringFeature;
    }

    private void addLayer(Layer errorLayer, PlugInContext context) {
        context.getLayerManager().addLayer(StandardCategoryNames.QA, errorLayer);
    }

    private Layer toLayer(String name, List<Feature> features, Layer sourceLayer, boolean ringVertices, PlugInContext context, int geomType) {
        boolean firingEvents = context.getLayerManager().isFiringEvents();
        context.getLayerManager().setFiringEvents(false);
        try {
            FeatureDataset errorFeatureCollection;
            switch (geomType) {
                case 1: 
                case 8: {
                    errorFeatureCollection = new FeatureDataset(features, this.schemaPoints);
                    break;
                }
                case 2: 
                case 3: {
                    errorFeatureCollection = new FeatureDataset(features, this.schemaLines);
                    break;
                }
                default: {
                    errorFeatureCollection = new FeatureDataset(features, this.schemaPolygons);
                }
            }
            Layer errorLayer = new Layer(name, Color.RED, (FeatureCollection)errorFeatureCollection, context.getLayerManager());
            if (ringVertices) {
                errorLayer.getBasicStyle().setEnabled(false);
                this.changeVertexToRing(errorLayer);
            }
            this.showVertices(errorLayer);
            Layer layer = errorLayer;
            return layer;
        }
        finally {
            context.getLayerManager().setFiringEvents(firingEvents);
        }
    }

    private void changeVertexToRing(Layer errorLayer) {
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

    private void showVertices(Layer errorLayer) {
        errorLayer.getVertexStyle().setEnabled(true);
        errorLayer.fireAppearanceChanged();
    }

    private void initDialog(PlugInContext context) {
        this.dialog = new MultiInputDialog(context.getWorkbenchFrame(), NAME, true);
        this.dialog.setSideBarImage(IconLoader.icon("toolImages/Validate.gif"));
        this.dialog.setSideBarDescription(I18N.getString("org.saig.jump.plugin.utils.topology.ValidateGeometryTypePlugIn.Test-features-s-geometry-types-from-layers"));
        this.dialog.addLabel("<HTML><B>&nbsp;&nbsp;" + I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.geometry-types-validation") + "</B></HTML>");
        this.dialog.addSeparator();
        this.dialog.addCheckBox(DISALLOW_POINTS, false);
        this.dialog.addCheckBox(DISALLOW_MULTIPOINTS, false);
        this.dialog.addCheckBox(DISALLOW_LINESTRINGS, false);
        this.dialog.addCheckBox(DISALLOW_MULTILINESTRINGS, false);
        this.dialog.addCheckBox(DISALLOW_POLYGONS, false);
        this.dialog.addCheckBox(DISALLOW_MULTIPOLYGONS, false);
        this.dialog.addCheckBox(CHECK_POLYGONS_HAVE_NO_HOLES, false);
        this.dialog.addCheckBox(DISALLOW_GEOMETRYCOLLECTIONS, false, I18N.getString("workbench.ui.plugin.ValidateSelectedLayersPlugIn.geometry-collection-subtypes-are-not-disallowed"));
        GUIUtil.centreOnWindow(this.dialog);
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createSelectedLayersMustNotBeWMSLayersCheck());
    }
}

