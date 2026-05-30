/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.index.kdtree.KdNode
 *  com.vividsolutions.jts.index.kdtree.KdTree
 *  com.vividsolutions.jts.operation.distance.DistanceOp
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.utils.topology;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.kdtree.KdNode;
import com.vividsolutions.jts.index.kdtree.KdTree;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MainMenuNames;
import es.kosmo.desktop.plugins.utils.topology.AssignByProximityAnchorType;
import es.kosmo.desktop.plugins.utils.topology.AssignByProximityErrorType;
import es.kosmo.desktop.plugins.utils.topology.DistanceComparator;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import es.kosmo.desktop.widgets.utils.topology.AssignmentByProximityOptionsDialog;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.filter.AttributeExpression;
import org.saig.core.filter.CompareFilter;
import org.saig.core.filter.LiteralExpression;
import org.saig.core.filter.LiteralExpressionImpl;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.model.data.DataManager;
import org.saig.core.model.data.Record;
import org.saig.core.model.data.RecordToFeatureWrapper;
import org.saig.core.model.data.Table;
import org.saig.core.model.data.TableFactory;
import org.saig.core.model.data.dao.TableRecordDataSource;
import org.saig.core.model.data.dao.dbf.DBFRecordDataSource;
import org.saig.core.model.data.widgets.ViewTableFrame;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.Graphic;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.Mark;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.RuleImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleFactory;
import org.saig.core.styling.StyleImpl;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.AbstractSaveResultsPlugIn;
import org.saig.jump.plugin.datasource.MemoryDataSource;

public class AssignmentByProximityPlugIn
extends AbstractSaveResultsPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(AssignmentByProximityPlugIn.class);
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Assign-by-proximity");
    public static final Icon ICON = null;
    public static final String DATA_ATTR_LINK_NAME = "ID_DATA";
    public static final String ANCHOR_TYPE_ATTR_NAME = "ANCHOR_TYPE";
    public static final String DISTANCE_ATTR_NAME = "DISTANCE";
    public static final String ORDER_ATTR_NAME = "ORDER";
    public static final String ERROR_CAUSE_ATTR_NAME = "ERROR_CAUSE";
    public static final String OLD_GID_ATTR_NAME = "OLD_GID";
    private AssignmentByProximityOptionsDialog dialog;
    private int recordID = 0;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuItem((PlugIn)this, new String[]{MainMenuNames.TOOLS, MainMenuNames.TOOLS_TOPOLOGYC}, false);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.dialog = new AssignmentByProximityOptionsDialog(context.getWorkbenchFrame(), true, context.getLayerManager());
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return AssignmentByProximityPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck check = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        check.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(3));
        check.add(checkFactory.createAtLeastNLayersTypeGeometryCheck(1, new int[]{3}));
        return check;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        DataSourceQuery errorQuery;
        monitor.allowCancellationRequests();
        this.recordID = 0;
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
        }
        monitor.report(String.valueOf(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Recovering-data-from-the-options-dialog")) + "...");
        String startLayerName = this.dialog.getStartLayerName();
        Layer startLayer = JUMPWorkbench.getLayer(startLayerName);
        String crossLayerName = this.dialog.getCrossLayerName();
        Layer crossLayer = JUMPWorkbench.getLayer(crossLayerName);
        String sourceLayerName = this.dialog.getSourceLayerName();
        Layer sourceLayer = JUMPWorkbench.getLayer(sourceLayerName);
        double crossTolerance = this.dialog.getStartToCrossTolerance();
        double sourceTolerance = this.dialog.getStartToSourceTolerance();
        TableRecordDataSource trds = this.buildTableDataSource(this.dialog.getDataTablePath(), startLayer, sourceLayer);
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
        }
        monitor.report(String.valueOf(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Processing-input-data")) + "...");
        FeatureSchema resultSchema = this.generateResultSchema();
        FeatureSchema validationSchema = this.generateValidationSchema();
        FeatureSchema errorSchema = this.generateErrorSchema(startLayer.getFeatureSchema());
        FeatureDataset fcResult = new FeatureDataset(resultSchema);
        FeatureDataset fcValidation = new FeatureDataset(validationSchema);
        FeatureDataset fcError = new FeatureDataset(errorSchema);
        FeatureIterator itFeats = null;
        KdTree anchorTree = null;
        int current = 0;
        int processed = 0;
        try {
            try {
                itFeats = startLayer.getUltimateFeatureCollectionWrapper().iterator();
                int total = startLayer.getUltimateFeatureCollectionWrapper().size();
                anchorTree = new KdTree(0.1);
                block9: while (!monitor.isCancelRequested() && itFeats.hasNext()) {
                    Feature currentFeat = itFeats.next();
                    Geometry geom = currentFeat.getGeometry();
                    Point[] points = this.getStudyPointsFromGeometry(geom);
                    Point first = points[0];
                    Point last = points[1];
                    Envelope candidateEnvelope = EnvelopeUtil.expand(geom.getEnvelopeInternal(), crossTolerance);
                    List<Feature> crossCandidates = this.filterCandidates(first, last, crossLayer.getFeatureCollectionWrapper().query(candidateEnvelope), crossTolerance);
                    double nearestDistance = Double.MAX_VALUE;
                    Feature selectedCrossingFeature = null;
                    boolean firstNearest = true;
                    boolean displaced = false;
                    Iterator<Feature> itCrossCandidates = crossCandidates.iterator();
                    while (true) {
                        double toLastDistance;
                        if (monitor.isCancelRequested() || !itCrossCandidates.hasNext()) {
                            if (selectedCrossingFeature != null) break;
                            fcError.addWithNewKey(this.generateErrorFeature(currentFeat, errorSchema, AssignByProximityErrorType.CROSS_FEAT_NOT_FOUND, sourceLayerName, crossLayerName, sourceTolerance, crossTolerance));
                            continue block9;
                        }
                        Feature currentCandidate = itCrossCandidates.next();
                        Geometry currentGeom = currentCandidate.getGeometry();
                        double toFirstDistance = currentGeom.distance((Geometry)first);
                        if (toFirstDistance <= (toLastDistance = currentGeom.distance((Geometry)last))) {
                            if (!(nearestDistance > toFirstDistance)) continue;
                            nearestDistance = toFirstDistance;
                            selectedCrossingFeature = currentCandidate;
                            firstNearest = true;
                            continue;
                        }
                        if (!(nearestDistance > toLastDistance)) continue;
                        nearestDistance = toLastDistance;
                        selectedCrossingFeature = currentCandidate;
                        firstNearest = false;
                    }
                    if (nearestDistance != 0.0) {
                        Point anchorPoint = this.findCorrectAnchorPoint(firstNearest ? first : last, selectedCrossingFeature);
                        displaced = true;
                        if (firstNearest) {
                            first = anchorPoint;
                        } else {
                            last = anchorPoint;
                        }
                    }
                    List<Feature> sourceCandidates = this.filterCandidates(firstNearest ? last : first, null, this.getSourceCandidates(firstNearest ? last : first, sourceLayer, sourceTolerance), sourceTolerance);
                    boolean generateResultFeature = false;
                    Feature selectedSourceFeature = null;
                    switch (sourceCandidates.size()) {
                        case 0: {
                            fcError.addWithNewKey(this.generateErrorFeature(currentFeat, errorSchema, AssignByProximityErrorType.CROSS_FEAT_NOT_FOUND, sourceLayerName, crossLayerName, sourceTolerance, crossTolerance));
                            break;
                        }
                        case 1: {
                            generateResultFeature = true;
                            selectedSourceFeature = sourceCandidates.get(0);
                            break;
                        }
                        default: {
                            generateResultFeature = true;
                            selectedSourceFeature = sourceCandidates.get(0);
                            fcValidation.addAllWithNewKey(this.generateValidationFeatures(firstNearest ? last : first, validationSchema, current, sourceCandidates));
                        }
                    }
                    if (generateResultFeature) {
                        Point anchorPoint = firstNearest ? first : last;
                        List anchors = anchorTree.query(anchorPoint.getEnvelopeInternal());
                        if (CollectionUtils.isNotEmpty((Collection)anchors)) {
                            KdNode anchor = (KdNode)anchors.get(0);
                            Integer dataID = (Integer)anchor.getData();
                            this.updateResultFeature(fcResult.getByPrimaryKey(dataID), displaced);
                            trds.addAll(this.generateAssociatedDataRecords(dataID, trds.getSchema(), currentFeat, selectedSourceFeature));
                        } else {
                            anchorTree.insert(anchorPoint.getCoordinate(), (Object)new Integer(current++));
                            fcResult.addWithNewKey(this.generateResultFeature(firstNearest ? first : last, resultSchema, current, displaced ? AssignByProximityAnchorType.ANCHOR_DISPLACED : AssignByProximityAnchorType.ANCHOR_NORMAL));
                            trds.addAll(this.generateAssociatedDataRecords(current, trds.getSchema(), currentFeat, selectedSourceFeature));
                        }
                    }
                    if (processed++ % 10 != 0) continue;
                    monitor.report(processed, total, I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.processed-features"));
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                fcResult.clear();
                fcValidation.clear();
                fcError.clear();
                context.getWorkbenchFrame().warnUser(String.valueOf(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.An-error-has-been-produced")) + ": " + ex.getMessage());
                if (itFeats != null) {
                    itFeats.close();
                }
                anchorTree = null;
                return;
            }
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
            anchorTree = null;
        }
        if (monitor.isCancelRequested()) {
            this.warnOperationCancelled(context);
        }
        monitor.report(String.valueOf(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Saving-the-results")) + "...");
        if (!fcResult.isEmpty()) {
            AssignmentByProximityPlugIn.saveResults(this.dialog.getPointOutputQuery(), fcResult, I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Point-output"), NAME, startLayer.getProjection(), true, this.generateResultsStyle(), monitor);
        }
        if (!fcValidation.isEmpty()) {
            AssignmentByProximityPlugIn.saveResults(this.dialog.getLinealOutputQuery(), fcValidation, I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Validating-layer"), NAME, startLayer.getProjection(), true, this.generateValidationStyle(), monitor);
        }
        if ((errorQuery = this.dialog.getErrorOutputQuery()) != null) {
            if (errorQuery.getDataSource() instanceof MemoryDataSource) {
                Layer newLayer = context.getLayerManager().addLayer(NAME, I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Errors"), fcError, startLayer.getProjection());
                newLayer.setFeatureCollectionModified(true);
            } else if (!fcValidation.isEmpty()) {
                AssignmentByProximityPlugIn.saveResults(this.dialog.getErrorOutputQuery(), fcValidation, I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Errors-layer"), NAME, startLayer.getProjection(), true, monitor);
            }
        }
        trds.commit();
        DataManager dataManager = JUMPWorkbench.getFrameInstance().getContext().getDataManager();
        Table recordCollection = TableFactory.getRecordCollection(trds);
        ViewTableFrame dataFrame = new ViewTableFrame(recordCollection, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        dataManager.addTable(dataFrame);
        if (!monitor.isCancelRequested()) return;
        this.warnOperationCancelled(context);
    }

    private Point[] getStudyPointsFromGeometry(Geometry geom) throws Exception {
        Point[] result = new Point[2];
        if (geom instanceof Point) {
            Point point = (Point)geom;
            result[0] = (Point)point.clone();
            result[1] = (Point)point.clone();
        } else if (geom instanceof LineString) {
            LineString line = (LineString)geom;
            result[0] = line.getStartPoint();
            result[1] = line.getEndPoint();
        } else {
            throw new Exception(String.valueOf(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Unsupported-geometry-type")) + " - " + GUITranslationsUtils.getGeometryName(FeatureSchema.getGeometryType(geom)));
        }
        return result;
    }

    private void updateResultFeature(Feature feat, boolean displaced) {
        Object currentAnchorType = feat.getAttribute(ANCHOR_TYPE_ATTR_NAME);
        if (currentAnchorType.equals(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Normal"))) {
            if (displaced) {
                feat.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-mixed"));
            } else {
                feat.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-normal"));
            }
        } else if (currentAnchorType.equals(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Displaced"))) {
            if (displaced) {
                feat.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-displaced"));
            } else {
                feat.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-mixed"));
            }
        } else if (currentAnchorType.equals(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-normal"))) {
            if (displaced) {
                feat.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-mixed"));
            }
        } else if (currentAnchorType.equals(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-displaced")) && !displaced) {
            feat.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-mixed"));
        }
    }

    private Style generateResultsStyle() throws Exception {
        CompareFilter compFilterDisplaced = SLDEditor.filterFactory.createCompareFilter((short)14);
        AttributeExpression attrExprDisplaced = SLDEditor.filterFactory.createAttributeExpression(ANCHOR_TYPE_ATTR_NAME);
        LiteralExpression literalDisplaced = SLDEditor.filterFactory.createLiteralExpression(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Displaced"));
        compFilterDisplaced.addLeftValue(attrExprDisplaced);
        compFilterDisplaced.addRightValue(literalDisplaced);
        CompareFilter compFilterNormalMultiple = SLDEditor.filterFactory.createCompareFilter((short)14);
        AttributeExpression attrExprNormalMultiple = SLDEditor.filterFactory.createAttributeExpression(ANCHOR_TYPE_ATTR_NAME);
        LiteralExpression literalNormalMultiple = SLDEditor.filterFactory.createLiteralExpression(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-normal"));
        compFilterNormalMultiple.addLeftValue(attrExprNormalMultiple);
        compFilterNormalMultiple.addRightValue(literalNormalMultiple);
        CompareFilter compFilterDisplacedMultiple = SLDEditor.filterFactory.createCompareFilter((short)14);
        AttributeExpression attrExprDisplacedMultiple = SLDEditor.filterFactory.createAttributeExpression(ANCHOR_TYPE_ATTR_NAME);
        LiteralExpression literalDisplacedMultiple = SLDEditor.filterFactory.createLiteralExpression(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-displaced"));
        compFilterDisplacedMultiple.addLeftValue(attrExprDisplacedMultiple);
        compFilterDisplacedMultiple.addRightValue(literalDisplacedMultiple);
        CompareFilter compFilterMixedMultiple = SLDEditor.filterFactory.createCompareFilter((short)14);
        AttributeExpression attrExprMixedMultiple = SLDEditor.filterFactory.createAttributeExpression(ANCHOR_TYPE_ATTR_NAME);
        LiteralExpression literalMixedMultiple = SLDEditor.filterFactory.createLiteralExpression(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-mixed"));
        compFilterMixedMultiple.addLeftValue(attrExprMixedMultiple);
        compFilterMixedMultiple.addRightValue(literalMixedMultiple);
        Mark markNormal = SLDEditor.styleBuilder.createMark("x", Color.BLUE, Color.YELLOW, 1.5);
        Graphic graphicNormal = SLDEditor.styleBuilder.createGraphic(null, markNormal, null);
        graphicNormal.setSize(new LiteralExpressionImpl(12));
        PointSymbolizer symbNormal = SLDEditor.styleBuilder.createPointSymbolizer(graphicNormal);
        Mark markDisplaced = SLDEditor.styleBuilder.createMark("x", Color.RED, Color.YELLOW, 1.5);
        Graphic graphicDisplaced = SLDEditor.styleBuilder.createGraphic(null, markDisplaced, null);
        graphicDisplaced.setSize(new LiteralExpressionImpl(12));
        PointSymbolizer symbDisplaced = SLDEditor.styleBuilder.createPointSymbolizer(graphicDisplaced);
        Mark markNormalMultiple = SLDEditor.styleBuilder.createMark("x", Color.GREEN, Color.YELLOW, 1.5);
        Graphic graphicNormalMultiple = SLDEditor.styleBuilder.createGraphic(null, markNormalMultiple, null);
        graphicNormalMultiple.setSize(new LiteralExpressionImpl(12));
        PointSymbolizer symbNormalMultiple = SLDEditor.styleBuilder.createPointSymbolizer(graphicNormalMultiple);
        Mark markDisplacedMultiple = SLDEditor.styleBuilder.createMark("x", Color.ORANGE, Color.YELLOW, 1.5);
        Graphic graphicDisplacedMultiple = SLDEditor.styleBuilder.createGraphic(null, markDisplacedMultiple, null);
        graphicDisplacedMultiple.setSize(new LiteralExpressionImpl(12));
        PointSymbolizer symbDisplacedMultiple = SLDEditor.styleBuilder.createPointSymbolizer(graphicDisplacedMultiple);
        Mark markMixedMultiple = SLDEditor.styleBuilder.createMark("x", Color.LIGHT_GRAY, Color.YELLOW, 1.5);
        Graphic graphicMixedMultiple = SLDEditor.styleBuilder.createGraphic(null, markMixedMultiple, null);
        graphicMixedMultiple.setSize(new LiteralExpressionImpl(12));
        PointSymbolizer symbMixedMultiple = SLDEditor.styleBuilder.createPointSymbolizer(graphicMixedMultiple);
        RuleImpl ruleNormal = new RuleImpl();
        ruleNormal.setName("anchor_normal");
        ruleNormal.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Normal"));
        ruleNormal.setSymbolizers(new Symbolizer[]{symbNormal});
        ruleNormal.setElseFilter(true);
        RuleImpl ruleDisplaced = new RuleImpl();
        ruleDisplaced.setName("anchor_displaced");
        ruleDisplaced.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Displaced"));
        ruleDisplaced.setSymbolizers(new Symbolizer[]{symbDisplaced});
        ruleDisplaced.setFilter(compFilterDisplaced);
        RuleImpl ruleNormalMultiple = new RuleImpl();
        ruleNormalMultiple.setName("anchor_normal_multiple");
        ruleNormalMultiple.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-normal"));
        ruleNormalMultiple.setSymbolizers(new Symbolizer[]{symbNormalMultiple});
        ruleNormalMultiple.setFilter(compFilterNormalMultiple);
        RuleImpl ruleDisplacedMultiple = new RuleImpl();
        ruleDisplacedMultiple.setName("anchor_displaced_multiple");
        ruleDisplacedMultiple.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-displaced"));
        ruleDisplacedMultiple.setSymbolizers(new Symbolizer[]{symbDisplacedMultiple});
        ruleDisplacedMultiple.setFilter(compFilterDisplacedMultiple);
        RuleImpl ruleMixedMultiple = new RuleImpl();
        ruleMixedMultiple.setName("anchor_displaced_multiple");
        ruleMixedMultiple.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Multiple-mixed"));
        ruleMixedMultiple.setSymbolizers(new Symbolizer[]{symbMixedMultiple});
        ruleMixedMultiple.setFilter(compFilterMixedMultiple);
        FeatureTypeStyle featStyle = StyleFactory.createStyleFactory().createFeatureTypeStyle(new Rule[]{ruleMixedMultiple, ruleDisplacedMultiple, ruleDisplaced, ruleNormalMultiple, ruleNormal});
        StyleImpl style = new StyleImpl();
        style.addFeatureTypeStyle(featStyle);
        return style;
    }

    private Style generateValidationStyle() throws Exception {
        StyleImpl style = new StyleImpl();
        CompareFilter compFilter1 = SLDEditor.filterFactory.createCompareFilter((short)14);
        AttributeExpression attrExpr1 = SLDEditor.filterFactory.createAttributeExpression(ORDER_ATTR_NAME);
        LiteralExpression literal1 = SLDEditor.filterFactory.createLiteralExpression(1);
        compFilter1.addLeftValue(attrExpr1);
        compFilter1.addRightValue(literal1);
        CompareFilter compFilter2 = SLDEditor.filterFactory.createCompareFilter((short)14);
        AttributeExpression attrExpr2 = SLDEditor.filterFactory.createAttributeExpression(ORDER_ATTR_NAME);
        LiteralExpression literal2 = SLDEditor.filterFactory.createLiteralExpression(2);
        compFilter2.addLeftValue(attrExpr2);
        compFilter2.addRightValue(literal2);
        LineSymbolizer symb1 = SLDEditor.styleBuilder.createLineSymbolizer(Color.GREEN, 3.0);
        LineSymbolizer symb2 = SLDEditor.styleBuilder.createLineSymbolizer(Color.ORANGE, 3.0);
        LineSymbolizer symbOthers = SLDEditor.styleBuilder.createLineSymbolizer(Color.RED, 3.0);
        RuleImpl rule = new RuleImpl();
        rule.setName("mas_cercano");
        rule.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Nearest"));
        rule.setFilter(compFilter1);
        rule.setSymbolizers(new Symbolizer[]{symb1});
        RuleImpl rule2 = new RuleImpl();
        rule2.setName("cercano");
        rule2.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Near"));
        rule2.setFilter(compFilter2);
        rule2.setSymbolizers(new Symbolizer[]{symb2});
        RuleImpl ruleOthers = new RuleImpl();
        ruleOthers.setName("otros");
        ruleOthers.setTitle(I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Others"));
        ruleOthers.setSymbolizers(new Symbolizer[]{symbOthers});
        ruleOthers.setElseFilter(true);
        FeatureTypeStyle featStyle = StyleFactory.createStyleFactory().createFeatureTypeStyle(new Rule[]{rule, rule2, ruleOthers});
        style.addFeatureTypeStyle(featStyle);
        return style;
    }

    private Feature generateErrorFeature(Feature currentFeat, FeatureSchema errorSchema, AssignByProximityErrorType type, String sourceLayerName, String crossLayerName, double sourceTolerance, double crossTolerance) {
        BasicFeature newFeat = new BasicFeature(errorSchema);
        FeatureUtil.copyOnlyExistentAttributes(currentFeat, newFeat, true, true);
        newFeat.setGeometry((Geometry)currentFeat.getGeometry().clone());
        newFeat.setAttribute(OLD_GID_ATTR_NAME, currentFeat.getPrimaryKey());
        String errorMessage = "";
        if (type.equals((Object)AssignByProximityErrorType.SOURCE_FEAT_NOT_FOUND)) {
            errorMessage = I18N.getMessage("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.There-are-not-features-from-the-layer-{0}-to-the-given-tolerance-{1}", new Object[]{sourceLayerName, sourceTolerance});
        } else if (type.equals((Object)AssignByProximityErrorType.CROSS_FEAT_NOT_FOUND)) {
            errorMessage = I18N.getMessage("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.There-are-not-features-from-the-layer-{0}-to-the-given-tolerance-{1}", new Object[]{crossLayerName, crossTolerance});
        }
        newFeat.setAttribute(ERROR_CAUSE_ATTR_NAME, (Object)errorMessage);
        return newFeat;
    }

    private FeatureSchema generateErrorSchema(FeatureSchema schema) {
        FeatureSchema errorSchema = new FeatureSchema();
        errorSchema.addAttribute("GID", AttributeType.INTEGER, true);
        errorSchema.addAttribute(OLD_GID_ATTR_NAME, schema.getAttributeType(schema.getPrimaryKeyName()));
        errorSchema.addAttribute(ERROR_CAUSE_ATTR_NAME, AttributeType.STRING);
        errorSchema.addAttribute(schema.getAttributeName(schema.getGeometryIndex()), AttributeType.GEOMETRY);
        errorSchema.setGeometryType(schema.getGeometryType());
        return errorSchema;
    }

    private Point findCorrectAnchorPoint(Point point, Feature selectedCrossingFeature) {
        Coordinate[] coords = DistanceOp.nearestPoints((Geometry)selectedCrossingFeature.getGeometry(), (Geometry)point);
        return geomFac.createPoint(coords[0]);
    }

    private List<Feature> filterCandidates(Point firstPoint, Point lastPoint, List<Feature> candidates, double tolerance) {
        ArrayList<Feature> results = new ArrayList<Feature>();
        boolean exactCandidateFound = false;
        Iterator<Feature> itCandidates = candidates.iterator();
        while (!exactCandidateFound && itCandidates.hasNext()) {
            Feature currentCandidate = itCandidates.next();
            Geometry candidateGeom = currentCandidate.getGeometry();
            double distanceToFirst = candidateGeom.distance((Geometry)firstPoint);
            double distanceToSecond = Double.MAX_VALUE;
            if (lastPoint != null) {
                distanceToSecond = candidateGeom.distance((Geometry)lastPoint);
                if (distanceToFirst <= tolerance || distanceToSecond <= tolerance) {
                    if (distanceToFirst == 0.0 || distanceToSecond == 0.0) {
                        exactCandidateFound = true;
                    }
                    results.add(currentCandidate);
                    continue;
                }
            }
            if (!(distanceToFirst <= tolerance) && !(distanceToSecond <= tolerance)) continue;
            if (distanceToFirst == 0.0) {
                exactCandidateFound = true;
            }
            results.add(currentCandidate);
        }
        return results;
    }

    private Collection<Feature> generateValidationFeatures(Point point, FeatureSchema validationSchema, Integer currentDataLinkID, List<Feature> sourceCandidates) {
        ArrayList<Feature> validationFeats = new ArrayList<Feature>();
        int order = 1;
        for (Feature currentSourceFeat : sourceCandidates) {
            BasicFeature newValidationFeat = new BasicFeature(validationSchema);
            Double distance = point.distance(currentSourceFeat.getGeometry());
            newValidationFeat.setAttribute(DISTANCE_ATTR_NAME, (Object)distance);
            newValidationFeat.setAttribute(ORDER_ATTR_NAME, (Object)new Integer(order++));
            newValidationFeat.setAttribute(DATA_ATTR_LINK_NAME, (Object)currentDataLinkID);
            Coordinate[] nearestCoords = DistanceOp.nearestPoints((Geometry)currentSourceFeat.getGeometry(), (Geometry)point);
            LineString newGeom = geomFac.createLineString(new Coordinate[]{point.getCoordinate(), nearestCoords[0]});
            newValidationFeat.setGeometry((Geometry)newGeom);
            validationFeats.add(newValidationFeat);
        }
        return validationFeats;
    }

    private FeatureSchema generateValidationSchema() {
        FeatureSchema resultSchema = new FeatureSchema();
        resultSchema.addAttribute("GID", AttributeType.INTEGER, true);
        resultSchema.addAttribute(DATA_ATTR_LINK_NAME, AttributeType.INTEGER);
        resultSchema.addAttribute("geometry", AttributeType.GEOMETRY);
        resultSchema.addAttribute(DISTANCE_ATTR_NAME, AttributeType.DOUBLE);
        resultSchema.addAttribute(ORDER_ATTR_NAME, AttributeType.INTEGER);
        resultSchema.setGeometryType(3);
        return resultSchema;
    }

    private Collection<Record> generateAssociatedDataRecords(Integer currentDataLinkID, FeatureSchema dataSchema, Feature currentFeat, Feature selectedSourceFeature) {
        Record result = new Record(dataSchema);
        result.setAttribute(dataSchema.getPrimaryKeyName(), (Object)new Integer(this.recordID++));
        FeatureUtil.copyOnlyExistentAttributes(currentFeat, new RecordToFeatureWrapper(result), true, true);
        FeatureUtil.copyOnlyExistentAttributes(selectedSourceFeature, new RecordToFeatureWrapper(result), true, true);
        result.setAttribute(DATA_ATTR_LINK_NAME, (Object)currentDataLinkID);
        ArrayList<Record> associatedDataRecords = new ArrayList<Record>();
        associatedDataRecords.add(result);
        return associatedDataRecords;
    }

    private TableRecordDataSource buildTableDataSource(String dataTablePath, Layer startLayer, Layer sourceLayer) throws Exception {
        FeatureSchema dataSchema = this.generateDataSchema(startLayer.getFeatureSchema(), sourceLayer.getFeatureSchema());
        File dbfFile = new File(dataTablePath);
        if (dbfFile.exists() && dbfFile.isFile()) {
            dbfFile.delete();
        }
        return new DBFRecordDataSource(dataTablePath, dataSchema);
    }

    private FeatureSchema generateResultSchema() {
        FeatureSchema resultSchema = new FeatureSchema();
        resultSchema.addAttribute("GID", AttributeType.INTEGER, true);
        resultSchema.addAttribute(DATA_ATTR_LINK_NAME, AttributeType.INTEGER);
        resultSchema.addAttribute(ANCHOR_TYPE_ATTR_NAME, AttributeType.STRING);
        resultSchema.addAttribute("geometry", AttributeType.GEOMETRY);
        resultSchema.setGeometryType(1);
        return resultSchema;
    }

    private Feature generateResultFeature(Point point, FeatureSchema resultSchema, Integer dataLinkID, AssignByProximityAnchorType type) {
        BasicFeature result = new BasicFeature(resultSchema);
        result.setGeometry((Geometry)point.clone());
        result.setAttribute(DATA_ATTR_LINK_NAME, (Object)dataLinkID);
        if (type.equals((Object)AssignByProximityAnchorType.ANCHOR_NORMAL)) {
            result.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Normal"));
        } else if (type.equals((Object)AssignByProximityAnchorType.ANCHOR_DISPLACED)) {
            result.setAttribute(ANCHOR_TYPE_ATTR_NAME, (Object)I18N.getString("es.kosmo.desktop.plugins.utils.topology.AssignmentByProximityPlugIn.Displaced"));
        }
        return result;
    }

    private FeatureSchema generateDataSchema(FeatureSchema schemaA, FeatureSchema schemaB) {
        Attribute attr;
        FeatureSchema resultSchema = new FeatureSchema();
        resultSchema.addAttribute("GID", AttributeType.INTEGER, true);
        resultSchema.addAttribute(DATA_ATTR_LINK_NAME, AttributeType.INTEGER);
        int pkIndex = schemaA.getPrimaryKeyIndex();
        int geomIndex = schemaA.getGeometryIndex();
        int i = 0;
        while (i < schemaA.getAttributeCount()) {
            if (i != pkIndex && i != geomIndex && !resultSchema.hasAttribute((attr = schemaA.getAttribute(i)).getName())) {
                resultSchema.addAttribute(attr.getName(), attr.getType());
            }
            ++i;
        }
        pkIndex = schemaB.getPrimaryKeyIndex();
        geomIndex = schemaB.getGeometryIndex();
        i = 0;
        while (i < schemaB.getAttributeCount()) {
            if (i != pkIndex && i != geomIndex && !resultSchema.hasAttribute((attr = schemaB.getAttribute(i)).getName())) {
                resultSchema.addAttribute(attr.getName(), attr.getType());
            }
            ++i;
        }
        return resultSchema;
    }

    private List<Feature> getSourceCandidates(Point point, Layer layer, double tolerance) throws Exception {
        Envelope env = EnvelopeUtil.expand(point.getEnvelopeInternal(), tolerance);
        List<Feature> candidates = layer.getFeatureCollectionWrapper().query(env);
        Collections.sort(candidates, new DistanceComparator((Geometry)point));
        return candidates;
    }
}

