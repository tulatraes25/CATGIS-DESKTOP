/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.util.Assert
 *  javax.measure.quantity.Area
 *  javax.measure.quantity.Length
 *  javax.measure.unit.SI
 *  javax.measure.unit.Unit
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.CategoryEventType;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FenceLayerFinder;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelListener;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.SelectionManagerProxy;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.ToolTipWriter;
import com.vividsolutions.jump.workbench.ui.TrackedPopupMenu;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DummyTool;
import com.vividsolutions.jump.workbench.ui.cursortool.LeftClickFilter;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.ViewAttributesPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import com.vividsolutions.jump.workbench.ui.renderer.style.CoordinatesEqualDecorator;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.printing.legend.Legend;
import org.saig.core.printing.scale.Scale;
import org.saig.core.renderer.RenderingHintsManager;
import org.saig.core.util.ScaleManager;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.ZManager;
import org.saig.jump.plugin.rotate.NorthLayerViewPanelListener;
import org.saig.jump.widgets.config.ConfigZoomPanel;

public class LayerViewPanel
extends JPanel
implements LayerListener,
LayerManagerProxy,
SelectionManagerProxy {
    private static final long serialVersionUID = 1L;
    private static final int POINT_HISTORY_SIZE = 2;
    private LinkedList<Point> clickedPointHistory = new LinkedList();
    public static final Logger LOGGER = Logger.getLogger(LayerViewPanel.class);
    protected LayerManager layerManager;
    protected Viewport viewport = new Viewport(this);
    private boolean viewportInitialized = false;
    protected static JPopupMenu popupMenu = new TrackedPopupMenu();
    protected BorderLayout borderLayout1 = new BorderLayout();
    protected CursorTool currentCursorTool = new DummyTool();
    private List<LayerViewPanelListener> listeners = new ArrayList<LayerViewPanelListener>();
    private List<LayerViewPanelListener> renderingListeners = new ArrayList<LayerViewPanelListener>();
    protected LayerViewPanelContext context;
    protected RenderingManager renderingManager = new RenderingManager(this);
    private FenceLayerFinder fenceLayerFinder;
    protected SelectionManager selectionManager;
    private double factor = 1.0;
    private Unit<Length> mapLengthUnit = UnitsManager.DEFAULT_LENGTH_UNIT;
    private Unit<Area> mapAreaUnit = UnitsManager.convertToAreaUnit(this.mapLengthUnit);
    private Unit<Length> userLengthUnit = UnitsManager.DEFAULT_LENGTH_UNIT;
    private Unit<Area> userAreaUnit = UnitsManager.DEFAULT_AREA_UNIT;
    private ToolTipWriter toolTipWriter = new ToolTipWriter(this);
    protected Point2D currentCursorPoint;
    private MouseListener mouseListener;
    private MouseMotionAdapter mouseMotionListener;
    private Dimension oldSize;
    private boolean printingMode = false;
    private Dimension paperSizeMm;
    private Scale graphicScale;
    private List<Legend> legends;
    private NorthLayerViewPanelListener north;
    private Blackboard blackboard = new Blackboard();

    public LayerViewPanel() {
    }

    public void init(LayerManager layerManager, LayerViewPanelContext context) {
        this.setMinimumSize(new Dimension(100, 100));
        this.setToolTipText("");
        GUIUtil.fixClicks(this);
        try {
            this.context = context;
            this.layerManager = layerManager;
            this.selectionManager = new SelectionManager(this, this);
            this.fenceLayerFinder = new FenceLayerFinder(this);
            layerManager.addLayerListener(this);
            try {
                this.jbInit();
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
            }
            this.mouseListener = new MouseAdapter(){

                @Override
                public void mouseEntered(MouseEvent e) {
                    WorkbenchFrame workbenchFrame = (WorkbenchFrame)SwingUtilities.getAncestorOfClass(WorkbenchFrame.class, LayerViewPanel.this);
                    if (workbenchFrame != null && !workbenchFrame.isActive()) {
                        workbenchFrame.requestFocus();
                    }
                }
            };
            this.addMouseListener(this.mouseListener);
            this.mouseMotionListener = new MouseMotionAdapter(){

                @Override
                public void mouseDragged(MouseEvent e) {
                    this.mouseLocationChanged(e);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    this.mouseLocationChanged(e);
                }

                private void mouseLocationChanged(MouseEvent e) {
                    try {
                        Point2D p = LayerViewPanel.this.getViewport().toModelPoint(e.getPoint());
                        LayerViewPanel.this.fireCursorPositionChanged(LayerViewPanel.this.format(p.getX()), LayerViewPanel.this.format(p.getY()));
                    }
                    catch (Throwable t) {
                        LayerViewPanel.this.context.handleThrowable(t);
                    }
                }
            };
            this.addMouseMotionListener(this.mouseMotionListener);
        }
        catch (Throwable t) {
            context.handleThrowable(t);
        }
    }

    public LayerViewPanel(LayerManager layerManager, LayerViewPanelContext context) {
        GUIUtil.fixClicks(this);
        try {
            this.context = context;
            this.layerManager = layerManager;
            this.selectionManager = new SelectionManager(this, this);
            this.fenceLayerFinder = new FenceLayerFinder(this);
            layerManager.addLayerListener(this);
            try {
                this.jbInit();
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
            }
            this.addMouseMotionListener(new MouseMotionAdapter(){

                @Override
                public void mouseDragged(MouseEvent e) {
                    this.mouseLocationChanged(e);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    this.mouseLocationChanged(e);
                }

                private void mouseLocationChanged(MouseEvent e) {
                    try {
                        LayerViewPanel.this.currentCursorPoint = LayerViewPanel.this.getViewport().toModelPoint(e.getPoint());
                        LayerViewPanel.this.fireCursorPositionChanged(LayerViewPanel.this.format(LayerViewPanel.this.currentCursorPoint.getX()), LayerViewPanel.this.format(LayerViewPanel.this.currentCursorPoint.getY()));
                    }
                    catch (Throwable t) {
                        LayerViewPanel.this.context.handleThrowable(t);
                    }
                }
            });
            MouseWheelListener mouseWheelListener = new MouseWheelListener(){

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    int notches = e.getWheelRotation();
                    Envelope env = LayerViewPanel.this.getViewport().getEnvelopeInModelCoordinates();
                    try {
                        ZManager zmanager = ZManager.getInstance();
                        if (zmanager.isMouseWheelIntercepted()) {
                            zmanager.adjustZToNotches(notches);
                        } else {
                            env = this.calculateNewEnvelope(env, notches, e.getPoint());
                            LayerViewPanel.this.getViewport().zoom(env);
                        }
                    }
                    catch (NoninvertibleTransformException e1) {
                        LOGGER.error((Object)"", (Throwable)e1);
                    }
                }

                private Envelope calculateNewEnvelope(Envelope env, int notches, Point refPoint) {
                    Envelope newEnvelope;
                    double zoomLevel = ConfigZoomPanel.getZoomFactor();
                    double factor = Math.pow(zoomLevel, notches);
                    double newEnvelopeWidth = env.getWidth() * factor;
                    double newEnvelopeHeight = env.getHeight() * factor;
                    Coordinate center = EnvelopeUtil.centre(env);
                    if (this.keepCursorLocationOnWheelZoom()) {
                        Coordinate ref;
                        try {
                            ref = LayerViewPanel.this.getViewport().toModelCoordinate(refPoint);
                        }
                        catch (NoninvertibleTransformException e) {
                            LOGGER.warn((Object)I18N.getString("com.vividsolutions.jump.workbench.ui.LayerViewPanel.The-reference-point-could-not-be-calculated-the-current-view-center-will-be-used-instead"));
                            ref = new Coordinate(center);
                        }
                        double xmin = ref.x - ref.x * factor + env.getMinX() * factor;
                        double ymin = ref.y - ref.y * factor + env.getMinY() * factor;
                        newEnvelope = new Envelope(xmin, xmin + newEnvelopeWidth, ymin, ymin + newEnvelopeHeight);
                    } else {
                        newEnvelope = new Envelope(center.x - newEnvelopeWidth / 2.0, center.x + newEnvelopeWidth / 2.0, center.y - newEnvelopeHeight / 2.0, center.y + newEnvelopeHeight / 2.0);
                    }
                    return newEnvelope;
                }

                private boolean keepCursorLocationOnWheelZoom() {
                    Blackboard blackboard = JUMPWorkbench.getFrameInstance().getContext().getBlackboard();
                    boolean keepCursorLocationOnWheelZoom = PersistentBlackboardPlugIn.get(blackboard).get(ConfigZoomPanel.KEEP_CURSOR_LOCATION_ON_WHEEL_ZOOM_KEY, false);
                    return keepCursorLocationOnWheelZoom;
                }
            };
            this.addMouseWheelListener(mouseWheelListener);
        }
        catch (Throwable t) {
            context.handleThrowable(t);
        }
    }

    public static List<Geometry> components(Geometry g) {
        if (!(g instanceof GeometryCollection)) {
            return Arrays.asList(g);
        }
        GeometryCollection c = (GeometryCollection)g;
        ArrayList<Geometry> components = new ArrayList<Geometry>();
        int i = 0;
        while (i < c.getNumGeometries()) {
            components.addAll(LayerViewPanel.components(c.getGeometryN(i)));
            ++i;
        }
        return components;
    }

    public static boolean intersects(Geometry a, Geometry b) {
        GeometryFactory factory = new GeometryFactory(a.getPrecisionModel(), a.getSRID());
        List<Geometry> aComponents = LayerViewPanel.components(a);
        List<Geometry> bComponents = LayerViewPanel.components(b);
        for (Geometry aComponent : aComponents) {
            Assert.isTrue((!(aComponent instanceof GeometryCollection) ? 1 : 0) != 0);
            aComponent = LayerViewPanel.collapseToPointIfPossible(aComponent, factory);
            for (Geometry bComponent : bComponents) {
                Assert.isTrue((!(bComponent instanceof GeometryCollection) ? 1 : 0) != 0);
                bComponent = LayerViewPanel.collapseToPointIfPossible(bComponent, factory);
                if (!aComponent.intersects(bComponent)) continue;
                return true;
            }
        }
        return false;
    }

    private static Geometry collapseToPointIfPossible(Geometry g, GeometryFactory factory) {
        if (!g.isEmpty() && CoordinatesEqualDecorator.coordinatesEqual(g)) {
            g = factory.createPoint(g.getCoordinate());
        }
        return g;
    }

    public Map<Layer, Collection<Feature>> visibleLayerToFeaturesInFenceMap() {
        Map<Layer, Collection<Feature>> visibleLayerToFeaturesInFenceMap = this.visibleLayerToFeaturesInFenceMap(this.getFence());
        visibleLayerToFeaturesInFenceMap.remove(new FenceLayerFinder(this).getLayer());
        return visibleLayerToFeaturesInFenceMap;
    }

    public Map<Layer, Collection<Feature>> visibleLayerToFeaturesInFenceMap(Geometry fence) {
        HashMap<Layer, Collection<Feature>> map = new HashMap<Layer, Collection<Feature>>();
        Iterator<Layer> i = this.getLayerManager().iterator();
        while (i.hasNext()) {
            HashSet<Feature> features;
            Layer layer;
            block12: {
                layer = i.next();
                if (!layer.isVisible() || !layer.hasRulesInScale(this.getScale(this.getProjection())) || !layer.isEnabled()) continue;
                features = new HashSet<Feature>();
                Geometry geom = null;
                if (layer.getCoordTrans() != null) {
                    IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(fence);
                    pathGeom.reProject(layer.getCoordTrans());
                    geom = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                } else {
                    geom = fence;
                }
                FeatureIterator itCandidates = null;
                try {
                    try {
                        itCandidates = layer.getUltimateFeatureCollectionWrapper().queryIterator(geom.getEnvelopeInternal());
                        while (itCandidates.hasNext()) {
                            Feature candidate = itCandidates.next();
                            if (candidate == null || !LayerViewPanel.intersects(candidate.getGeometry(), geom)) continue;
                            features.add(candidate);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (itCandidates != null) {
                            itCandidates.close();
                        }
                        break block12;
                    }
                }
                catch (Throwable throwable) {
                    if (itCandidates != null) {
                        itCandidates.close();
                    }
                    throw throwable;
                }
                if (itCandidates != null) {
                    itCandidates.close();
                }
            }
            if (features.isEmpty()) continue;
            map.put(layer, features);
        }
        return map;
    }

    public IProjection getProjection() {
        Container cont;
        IProjection proj = null;
        if (this.getRootPane() != null && this.getRootPane().getParent() != null && (cont = this.getRootPane().getParent()) instanceof TaskFrame) {
            TaskFrame taskFrame = (TaskFrame)cont;
            proj = taskFrame.getTask().getProjection();
        }
        return proj;
    }

    public Map<Layer, Collection<Feature>> selectedLayerToFeaturesInFenceMap(Geometry fence) {
        return this.layersToFeaturesInFenceMap(Arrays.asList(JUMPWorkbench.getFrameInstance().getContext().getWorkbench().getContext().getLayerNamePanel().getSelectedLayers()), fence);
    }

    public Map<Layer, Collection<Feature>> layersToFeaturesInFenceMap(List<Layerable> layersToSelect, Geometry fence) {
        HashMap<Layer, Collection<Feature>> map = new HashMap<Layer, Collection<Feature>>();
        if (layersToSelect == null || layersToSelect.isEmpty()) {
            return map;
        }
        for (Layerable currentElement : layersToSelect) {
            HashSet<Feature> features;
            Layer layer;
            block14: {
                if (!(currentElement instanceof Layer) || !(layer = (Layer)currentElement).isVisible() || !layer.hasRulesInScale(this.getScale(this.getProjection()))) continue;
                features = new HashSet<Feature>();
                Geometry geom = null;
                if (layer.getCoordTrans() != null) {
                    IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(fence);
                    pathGeom.reProject(layer.getCoordTrans().getInverted());
                    geom = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                } else {
                    geom = fence;
                }
                FeatureIterator it = null;
                try {
                    try {
                        it = layer.getUltimateFeatureCollectionWrapper().queryIterator(geom.getEnvelopeInternal());
                        while (it.hasNext()) {
                            Feature candidate = it.next();
                            if (candidate == null || (candidate = candidate.clone(true)).getGeometry() == null || !LayerViewPanel.intersects(candidate.getGeometry(), geom)) continue;
                            if (layer.getCoordTrans() != null) {
                                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(candidate.getGeometry());
                                pathGeom.reProject(layer.getCoordTrans());
                                Geometry geomRepro = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                                candidate.setGeometry(geomRepro);
                            }
                            features.add(candidate);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (it != null) {
                            it.close();
                        }
                        break block14;
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
            }
            if (features.isEmpty()) continue;
            map.put(layer, features);
        }
        return map;
    }

    public static JPopupMenu popupMenu() {
        return popupMenu;
    }

    public void setCurrentCursorTool(CursorTool currentCursorTool) {
        if (this.currentCursorTool != null) {
            this.currentCursorTool.deactivate();
            this.removeMouseListener(this.currentCursorTool);
            this.removeMouseMotionListener(this.currentCursorTool);
        }
        this.currentCursorTool = currentCursorTool;
        currentCursorTool.activate(this);
        this.setCursor(currentCursorTool.getCursor());
        this.addMouseListener(currentCursorTool);
        this.addMouseMotionListener(currentCursorTool);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled && this.currentCursorTool != null) {
            this.currentCursorTool.deactivate();
            this.removeMouseListener(this.currentCursorTool);
            this.removeMouseMotionListener(this.currentCursorTool);
        }
    }

    public void setViewportInitialized(boolean viewportInitialized) {
        this.viewportInitialized = viewportInitialized;
    }

    public CursorTool getCurrentCursorTool() {
        return this.currentCursorTool;
    }

    public boolean checkCurrentCursorTool(boolean activate, CursorTool tool) {
        boolean equals = false;
        if (this.currentCursorTool != null) {
            if (this.currentCursorTool instanceof QuasimodeTool) {
                CursorTool defaultCursorTool = ((QuasimodeTool)this.currentCursorTool).getDefaultTool();
                if (defaultCursorTool instanceof LeftClickFilter) {
                    CursorTool wrappeeCursorTool = ((LeftClickFilter)defaultCursorTool).getWrappee();
                    if (wrappeeCursorTool instanceof DelegatingTool) {
                        wrappeeCursorTool = ((DelegatingTool)wrappeeCursorTool).getDelegate();
                    }
                    if (wrappeeCursorTool.equals(tool)) {
                        equals = true;
                    }
                }
            } else {
                equals = this.currentCursorTool.equals(tool);
            }
        }
        return equals;
    }

    public Point getLastClickedPoint() {
        if (this.clickedPointHistory.size() > 0) {
            return this.clickedPointHistory.get(this.clickedPointHistory.size() - 1);
        }
        return null;
    }

    public List<Point> getClickedPointHistory() {
        return this.clickedPointHistory;
    }

    public Viewport getViewport() {
        return this.viewport;
    }

    public Java2DConverter getJava2DConverter() {
        return this.viewport.getJava2DConverter();
    }

    public Geometry getFence() {
        return this.fenceLayerFinder.getFence();
    }

    @Override
    public LayerManager getLayerManager() {
        return this.layerManager;
    }

    @Override
    public void featuresChanged(FeatureEvent e) {
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
        try {
            if (e.getType() == CategoryEventType.ADDED && e.getCategory().getLayerables().size() > 0) {
                for (Layerable element : e.getCategory().getLayerables()) {
                    this.renderingManager.render(element);
                }
            }
        }
        catch (Throwable t) {
            this.context.handleThrowable(t);
        }
    }

    @Override
    public void layerChanged(LayerEvent e) {
        try {
            if (e.getType() == LayerEventType.METADATA_CHANGED || e.getType() == LayerEventType.COMMITED) {
                return;
            }
            this.initializeViewportIfNecessary();
            if (e.getType() == LayerEventType.ADDED || e.getType() == LayerEventType.APPEARANCE_CHANGED) {
                this.renderingManager.render(e.getLayerable());
            } else if (e.getType() == LayerEventType.REMOVED) {
                this.renderingManager.removeRenderer(e.getLayerable());
            } else if (e.getType() == LayerEventType.VISIBILITY_CHANGED) {
                this.renderingManager.render(e.getLayerable(), true);
            } else {
                Assert.shouldNeverReachHere();
            }
        }
        catch (Throwable t) {
            this.context.handleThrowable(t);
        }
    }

    public Image createBlankPanelImage() {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            return new BufferedImage(this.getWidth(), this.getHeight(), 2);
        }
        return null;
    }

    @Override
    public void repaint() {
        if (this.renderingManager == null || this.getSize().height <= 0 || this.getSize().width <= 0) {
            this.superRepaint();
            return;
        }
        this.renderingManager.renderAll();
    }

    public void repaint(boolean isRenderingMananager) {
        if (this.renderingManager == null || this.getSize().height <= 0 || this.getSize().width <= 0) {
            this.superRepaint();
            return;
        }
        if (isRenderingMananager) {
            this.renderingManager.renderAll();
        }
    }

    public void superRepaint() {
        super.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        block2: {
            try {
                ((Graphics2D)g).setRenderingHints(RenderingHintsManager.getRenderingHints());
                super.paintComponent(g);
                this.erase((Graphics2D)g);
                this.renderingManager.copyTo((Graphics2D)g);
                this.firePainted(g);
            }
            catch (Throwable t) {
                if (this.context == null) break block2;
                this.context.handleThrowable(t);
            }
        }
    }

    public void erase(Graphics2D g) {
        this.fill(g, Color.white);
    }

    public void fill(Graphics2D g, Color color) {
        g.setColor(color);
        Rectangle2D.Double r = new Rectangle2D.Double(0.0, 0.0, this.getWidth(), this.getHeight());
        g.fill(r);
    }

    void jbInit() throws Exception {
        this.setBackground(Color.white);
        this.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseReleased(MouseEvent e) {
                LayerViewPanel.this.this_mouseReleased(e);
            }
        });
        this.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent e) {
                LayerViewPanel.this.this_componentResized(e);
            }
        });
        this.setLayout(this.borderLayout1);
    }

    protected void this_componentResized(ComponentEvent e) {
        try {
            this.viewport.update(true);
        }
        catch (Throwable t) {
            this.context.handleThrowable(t);
        }
    }

    public LayerViewPanelContext getContext() {
        return this.context;
    }

    protected void this_mouseReleased(MouseEvent e) {
        this.setLastClickedPoint(e.getPoint());
        if (SwingUtilities.isRightMouseButton(e) && !this.currentCursorTool.isRightMouseButtonUsed()) {
            if (popupMenu.getSubElements().length == 0) {
                return;
            }
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void initializeViewportIfNecessary() throws NoninvertibleTransformException {
        if (!this.viewportInitialized && this.layerManager.size() > 0 && this.layerManager.getEnvelopeOfAllLayers().getWidth() > 0.0) {
            this.setViewportInitialized(true);
            this.viewport.zoomToFullExtent();
            return;
        }
    }

    public void addListener(LayerViewPanelListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(LayerViewPanelListener listener) {
        this.listeners.remove(listener);
    }

    public void addRenderingListener(LayerViewPanelListener listener) {
        this.renderingListeners.add(listener);
    }

    public void removeRenderingListener(LayerViewPanelListener listener) {
        this.renderingListeners.remove(listener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
        this.renderingListeners.clear();
        this.removeMouseListener(this.mouseListener);
        this.mouseListener = null;
        this.removeMouseMotionListener(this.mouseMotionListener);
        this.mouseMotionListener = null;
    }

    public String format(double d) {
        double pixelWidthInModelUnits = this.viewport.getEnvelopeInModelCoordinates().getWidth() / (double)this.getWidth();
        return this.format(d, pixelWidthInModelUnits);
    }

    protected String format(double d, double pixelWidthInModelUnits) {
        int precisionInDecimalPlaces = (int)Math.max(0L, Math.round(-Math.log(pixelWidthInModelUnits) / Math.log(10.0)));
        ++precisionInDecimalPlaces;
        String formatString = "#.";
        int i = 0;
        while (i < precisionInDecimalPlaces) {
            formatString = String.valueOf(formatString) + "#";
            ++i;
        }
        return new DecimalFormat(formatString).format(d);
    }

    private void firePainted(Graphics graphics) {
        for (LayerViewPanelListener l : this.listeners) {
            l.painted(graphics);
        }
    }

    public void fireRenderingStarted() {
        for (LayerViewPanelListener l : this.renderingListeners) {
            l.renderingStarted();
        }
    }

    public void fireRenderingFinished() {
        for (LayerViewPanelListener l : this.renderingListeners) {
            l.renderingFinished();
        }
    }

    public void fireSelectionChanged(boolean check) {
        for (LayerViewPanelListener l : new CopyOnWriteArrayList<LayerViewPanelListener>(this.listeners)) {
            if (check) {
                if (!check || l instanceof ViewAttributesPlugIn.ViewAttributesFrame) continue;
                l.selectionChanged();
                continue;
            }
            l.selectionChanged();
        }
    }

    private void fireCursorPositionChanged(String x, String y) {
        for (LayerViewPanelListener l : this.listeners) {
            l.cursorPositionChanged(x, y);
        }
    }

    public RenderingManager getRenderingManager() {
        return this.renderingManager;
    }

    public Collection<Feature> featuresWithVertex(Point2D viewPoint, double viewTolerance, Collection<Feature> features) throws NoninvertibleTransformException {
        Point2D modelPoint = this.viewport.toModelPoint(viewPoint);
        double modelTolerance = viewTolerance / this.viewport.getScale();
        Envelope searchEnvelope = new Envelope(modelPoint.getX() - modelTolerance, modelPoint.getX() + modelTolerance, modelPoint.getY() - modelTolerance, modelPoint.getY() + modelTolerance);
        ArrayList<Feature> featuresWithVertex = new ArrayList<Feature>();
        for (Feature feature : features) {
            if (!this.geometryHasVertex(feature.getGeometry(), searchEnvelope)) continue;
            featuresWithVertex.add(feature);
        }
        return featuresWithVertex;
    }

    private boolean geometryHasVertex(Geometry geometry, Envelope searchEnvelope) {
        Coordinate[] coordinates = geometry.getCoordinates();
        int i = 0;
        while (i < coordinates.length) {
            if (searchEnvelope.contains(coordinates[i])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public void dispose() {
        WorkbenchContext context;
        this.renderingManager.dispose();
        this.renderingManager = null;
        if (this.selectionManager != null) {
            this.selectionManager.dispose();
        }
        this.selectionManager = null;
        this.layerManager.removeLayerListener(this);
        this.layerManager = null;
        this.currentCursorTool = null;
        this.viewport.removeAllListeners();
        this.viewport.dispose();
        this.viewport = null;
        this.removeAllListeners();
        this.context = null;
        if (this.fenceLayerFinder != null) {
            this.fenceLayerFinder.dispose();
        }
        this.fenceLayerFinder = null;
        this.blackboard = null;
        if (this.toolTipWriter != null) {
            this.toolTipWriter.dispose();
        }
        this.toolTipWriter = null;
        if (JUMPWorkbench.getFrameInstance() != null && (context = JUMPWorkbench.getFrameInstance().getContext()) != null && this.equals(context.getLastClickedLayerViewPanel())) {
            context.setLastClickedLayerViewPanel(null);
        }
        if (this.clickedPointHistory != null) {
            this.clickedPointHistory.clear();
            this.clickedPointHistory = null;
        }
    }

    public void flash(final Shape shape, Color color, Stroke stroke, final int millisecondDelay) {
        final Graphics2D graphics = (Graphics2D)this.getGraphics();
        graphics.setColor(color);
        graphics.setXORMode(Color.white);
        graphics.setStroke(stroke);
        try {
            GUIUtil.invokeOnEventThread(new Runnable(){

                @Override
                public void run() {
                    try {
                        graphics.draw(shape);
                        Thread.sleep(millisecondDelay);
                        graphics.draw(shape);
                    }
                    catch (InternalError ie) {
                        LOGGER.warn((Object)ie);
                    }
                    catch (Throwable t) {
                        LayerViewPanel.this.getContext().handleThrowable(t);
                    }
                }
            });
        }
        catch (Throwable t) {
            this.getContext().handleThrowable(t);
        }
    }

    @Override
    public SelectionManager getSelectionManager() {
        return this.selectionManager;
    }

    public Blackboard getBlackboard() {
        return this.blackboard;
    }

    public void flash(GeometryCollection geometryCollection) throws NoninvertibleTransformException {
        this.flash(this.getViewport().getJava2DConverter().toShape((Geometry)geometryCollection), Color.red, new BasicStroke(5.0f, 1, 1), 100);
    }

    public void setLastClickedPoint(Point lastClickedPoint) {
        if (lastClickedPoint == null) {
            return;
        }
        this.clickedPointHistory.add(lastClickedPoint);
        if (this.clickedPointHistory.size() > 2) {
            this.clickedPointHistory.removeFirst();
        }
        ((WorkbenchFrame)this.context).getContext().setLastClickedLayerViewPanel(this);
    }

    public double getFactor() {
        return this.factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public Unit<Length> getMapLengthUnit() {
        return this.mapLengthUnit;
    }

    public void setMapLengthUnit(Unit<Length> lengthUnit) {
        this.mapLengthUnit = lengthUnit;
        this.mapAreaUnit = UnitsManager.convertToAreaUnit(this.mapLengthUnit);
    }

    public Unit<Area> getMapAreaUnit() {
        return this.mapAreaUnit;
    }

    public void setMapAreaUnit(Unit<Area> areaUnit) {
        this.mapAreaUnit = areaUnit;
    }

    public double getScale() {
        return this.getScale(this.getProjection());
    }

    public double getScale(IProjection proj) {
        if (this.printingMode) {
            return this.getPrintingScale();
        }
        Envelope envelope = this.getViewport().getEnvelopeInModelCoordinates();
        double newScale = ScaleManager.getInstance().generateScaleValue(envelope.getMaxX(), envelope.getMinX(), this.getWidth(), proj, this.getMapLengthUnit());
        return newScale;
    }

    public double getPixelsInMeters(int pixels) {
        Envelope envelope = this.getViewport().getEnvelopeInModelCoordinates();
        double envWidth = envelope.getWidth();
        double pixWidth = this.getSize().getWidth();
        double pm = envWidth / pixWidth * (double)pixels;
        return pm;
    }

    public ToolTipWriter getToolTipWriter() {
        return this.toolTipWriter;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return this.toolTipWriter.write(this.getToolTipText(), event.getPoint());
    }

    public Point2D getCurrentCursorPoint() {
        return this.currentCursorPoint;
    }

    @Override
    public JToolTip createToolTip() {
        return new JToolTip();
    }

    public void setPrintMode(Dimension d, Dimension dmm) {
        TaskFrame taskfr = (TaskFrame)this.getParent().getParent().getParent().getParent().getParent();
        Dimension maxSize = JUMPWorkbench.getFrameInstance().getDesktopPane().getSize();
        double ratio = d.getWidth() / d.getHeight();
        Dimension vsize = this.getSize();
        this.oldSize = taskfr.getSize();
        this.factor = (double)vsize.height / d.getHeight();
        this.renderingManager.setFactor(this.factor);
        double width = ratio * (double)vsize.height;
        double height = vsize.height;
        double corrector = 0.0;
        double printingWidthBuff = taskfr.getSize().getWidth() - vsize.getWidth();
        double printingHeightBuff = taskfr.getSize().getHeight() - vsize.getHeight();
        if (width > (double)maxSize.width - printingWidthBuff) {
            corrector = ((double)maxSize.width - printingWidthBuff) / width;
            width *= corrector;
            height *= corrector;
        }
        if (height > (double)maxSize.height) {
            corrector = (double)maxSize.height / height;
            width *= corrector;
            height *= corrector;
        }
        taskfr.reshape(0, 0, (int)Math.round(width + printingWidthBuff), (int)Math.round(height + printingHeightBuff));
        this.printingMode = true;
        this.paperSizeMm = this.correctPaperSize(dmm);
    }

    private Dimension correctPaperSize(Dimension dmm) {
        Dimension d = this.getSize();
        double ratio = d.getWidth() / d.getHeight();
        Dimension newDimension = new Dimension((int)(ratio * (double)dmm.height), dmm.height);
        return newDimension;
    }

    public void unsetPrintMode() {
        TaskFrame taskfr = (TaskFrame)this.getParent().getParent().getParent().getParent().getParent();
        this.renderingManager.setFactor(1.0);
        taskfr.reshape(0, 0, this.oldSize.width, this.oldSize.height);
        this.printingMode = false;
    }

    public boolean isPrintingModeActive() {
        return this.printingMode;
    }

    public double getPrintingScale() {
        if (this.printingMode) {
            Envelope env = this.viewport.getEnvelopeInModelCoordinates();
            double scale = UnitsManager.convertDistanceValue(env.getHeight(), this.getMapLengthUnit(), (Unit<Length>)SI.MILLIMETER) / this.paperSizeMm.getHeight();
            return scale;
        }
        return 0.0;
    }

    public void setPrintingScale(double scale) {
        if (this.printingMode) {
            double width = UnitsManager.convertDistanceValue(scale * (double)this.paperSizeMm.width, (Unit<Length>)SI.MILLIMETER, this.getMapLengthUnit());
            double height = UnitsManager.convertDistanceValue(scale * (double)this.paperSizeMm.height, (Unit<Length>)SI.MILLIMETER, this.getMapLengthUnit());
            Envelope env = this.viewport.getEnvelopeInModelCoordinates();
            double xc = (env.getMaxX() + env.getMinX()) / 2.0;
            double yc = (env.getMinY() + env.getMaxY()) / 2.0;
            Envelope newenv = new Envelope(xc - width / 2.0, xc + width / 2.0, yc - height / 2.0, yc + height / 2.0);
            try {
                this.viewport.zoom(newenv);
            }
            catch (NoninvertibleTransformException e) {
                e.printStackTrace();
            }
        }
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

    public Legend getLegend(String name) {
        Legend foundlegend = null;
        if (this.legends == null) {
            return null;
        }
        for (Legend legend : this.legends) {
            if (!name.equals(legend.getName())) continue;
            foundlegend = legend;
        }
        return foundlegend;
    }

    public void setLegends(List<Legend> newLegends) {
        this.legends = newLegends;
    }

    public void setUserAreaUnit(Unit<Area> areaUnit) {
        this.userAreaUnit = areaUnit;
    }

    public Unit<Area> getUserAreaUnit() {
        return this.userAreaUnit;
    }

    public Unit<Length> getUserLengthUnit() {
        return this.userLengthUnit;
    }

    public void setUserLengthUnit(Unit<Length> userLengthUnit) {
        this.userLengthUnit = userLengthUnit;
    }

    public Map<Layer, Collection<Feature>> visibleLayerToSelectedFeaturesInFenceMap(Geometry fence) {
        HashMap<Layer, Collection<Feature>> map = new HashMap<Layer, Collection<Feature>>();
        Iterator<Layer> i = this.getLayerManager().iterator();
        while (i.hasNext()) {
            HashSet<Feature> features;
            Layer layer;
            block12: {
                layer = i.next();
                if (!layer.isVisible() || !layer.hasRulesInScale(this.getScale(this.getProjection())) || !layer.isEnabled()) continue;
                Collection<Feature> selectedFeatures = this.getSelectionManager().getFeatureSelection().getFeaturesWithSelectedItems(layer);
                features = new HashSet<Feature>();
                Geometry geom = null;
                if (layer.getCoordTrans() != null) {
                    IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(fence);
                    pathGeom.reProject(layer.getCoordTrans());
                    geom = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                } else {
                    geom = fence;
                }
                FeatureIterator itCandidates = null;
                try {
                    try {
                        itCandidates = layer.getUltimateFeatureCollectionWrapper().queryIterator(geom.getEnvelopeInternal());
                        while (itCandidates.hasNext()) {
                            Feature candidate = itCandidates.next();
                            if (candidate == null || !LayerViewPanel.intersects(candidate.getGeometry(), geom) || !selectedFeatures.contains(candidate)) continue;
                            features.add(candidate);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (itCandidates != null) {
                            itCandidates.close();
                        }
                        break block12;
                    }
                }
                catch (Throwable throwable) {
                    if (itCandidates != null) {
                        itCandidates.close();
                    }
                    throw throwable;
                }
                if (itCandidates != null) {
                    itCandidates.close();
                }
            }
            if (features.isEmpty()) continue;
            map.put(layer, features);
        }
        return map;
    }

    public Map<Layer, Collection<Feature>> layersToSelectedFeaturesInFenceMap(List<Layerable> layersToSelect, Geometry fence) {
        HashMap<Layer, Collection<Feature>> map = new HashMap<Layer, Collection<Feature>>();
        if (CollectionUtils.isEmpty(layersToSelect)) {
            return map;
        }
        for (Layerable currentElement : layersToSelect) {
            HashSet<Feature> features;
            Layer layer;
            block14: {
                if (!(currentElement instanceof Layer) || !(layer = (Layer)currentElement).isVisible() || !layer.hasRulesInScale(this.getScale(this.getProjection()))) continue;
                Collection<Feature> selectedFeatures = this.getSelectionManager().getFeatureSelection().getFeaturesWithSelectedItems(layer);
                features = new HashSet<Feature>();
                Geometry geom = null;
                if (layer.getCoordTrans() != null) {
                    IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(fence);
                    pathGeom.reProject(layer.getCoordTrans().getInverted());
                    geom = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                } else {
                    geom = fence;
                }
                FeatureIterator it = null;
                try {
                    try {
                        it = layer.getUltimateFeatureCollectionWrapper().queryIterator(geom.getEnvelopeInternal());
                        while (it.hasNext()) {
                            Feature candidate = it.next();
                            if (candidate == null || (candidate = candidate.clone(true)).getGeometry() == null || !LayerViewPanel.intersects(candidate.getGeometry(), geom) || !selectedFeatures.contains(candidate)) continue;
                            if (layer.getCoordTrans() != null) {
                                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(candidate.getGeometry());
                                pathGeom.reProject(layer.getCoordTrans());
                                Geometry geomRepro = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                                candidate.setGeometry(geomRepro);
                            }
                            features.add(candidate);
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (it != null) {
                            it.close();
                        }
                        break block14;
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
            }
            if (features.isEmpty()) continue;
            map.put(layer, features);
        }
        return map;
    }

    public void setAngle(double angle) {
        this.viewport.setAngle(angle);
    }

    public double getAngle() {
        return this.viewport.getAngle();
    }

    public void setNorth(NorthLayerViewPanelListener north) {
        if (this.north != null) {
            this.removeListener(this.north);
        }
        this.north = north;
        if (north != null) {
            north.setLayerViewPanel(this);
            this.addListener(north);
        }
    }

    public NorthLayerViewPanelListener getNorth() {
        return this.north;
    }
}

