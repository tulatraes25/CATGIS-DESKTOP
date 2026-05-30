/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.geom.LineSegmentEnvelopeIntersector;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.util.MathUtil;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanelProxy;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import com.vividsolutions.jump.workbench.ui.WorkbenchFrame;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.IncrementChooser;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.RoundQuantity;
import com.vividsolutions.jump.workbench.ui.plugin.scalebar.ScaleBarRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicSliderUI;
import org.apache.log4j.Logger;
import org.saig.core.util.UnitsManager;
import org.saig.jump.lang.I18N;

public class ZoomBar
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String SCALE_KEY = String.valueOf(ZoomBar.class.getName()) + " - SCALE";
    private static final String CENTRE_KEY = String.valueOf(ZoomBar.class.getName()) + " - CENTRE";
    private static final String CENTRE_LOCKED_KEY = String.valueOf(ZoomBar.class.getName()) + " - CENTRE LOCKED";
    private static final String MIN_EXTENT_KEY = String.valueOf(ZoomBar.class.getName()) + " - MIN EXTENT";
    private static final String USER_DEFINED_MIN_SCALE = String.valueOf(ZoomBar.class.getName()) + " - USER DEFINED MIN SCALE";
    private static final String USER_DEFINED_MAX_SCALE = String.valueOf(ZoomBar.class.getName()) + " - USER DEFINED MAX SCALE";
    private static final String MAX_EXTENT_KEY = String.valueOf(ZoomBar.class.getName()) + " - MAX EXTENT";
    private static final Logger LOGGER = Logger.getLogger(ZoomBar.class);
    private Envelope lastGoodEnvelope = null;
    private WorkbenchFrame frame;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JSlider slider = new JSlider();
    private JLabel label = new JLabel();
    private IncrementChooser incrementChooser = new IncrementChooser();
    private Collection<Unit<Length>> metricUnits = UnitsManager.getDefaultLengthUnits();
    private Timer componentUpdateTimer = GUIUtil.createRestartableSingleEventTimer(200, new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                ZoomBar.this.updateComponents();
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
                // empty catch block
            }
        }
    });
    private static final String SEGMENT_CACHE_KEY = String.valueOf(ZoomBar.class.getName()) + " - SEGMENT CACHE";
    private Stroke stroke = new BasicStroke(1.0f);
    private LineSegmentEnvelopeIntersector lineSegmentEnvelopeIntersector = new LineSegmentEnvelopeIntersector();
    private static final int RANDOM_ONSCREEN_GEOMETRIES = 100;
    private static final int RANDOM_GEOMETRIES = 100;
    private Font sliderLabelFont = new Font("Dialog", 0, 10);
    private boolean showingSliderLabels;

    public ZoomBar(boolean showingSliderLabels, boolean showingRightSideLabel, WorkbenchFrame frame) throws NoninvertibleTransformException {
        this.frame = frame;
        this.showingSliderLabels = showingSliderLabels;
        this.slider.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    ZoomBar.this.updateComponents();
                }
                catch (NoninvertibleTransformException noninvertibleTransformException) {
                    // empty catch block
                }
            }
        });
        if (showingSliderLabels) {
            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put(new Integer(0), new JLabel(" "));
            this.slider.setLabelTable(labelTable);
        }
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!showingRightSideLabel) {
            this.remove(this.label);
        }
        this.label.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 3 && SwingUtilities.isRightMouseButton(e)) {
                    ZoomBar.this.viewBlackboard().put(USER_DEFINED_MIN_SCALE, null);
                    ZoomBar.this.viewBlackboard().put(USER_DEFINED_MAX_SCALE, null);
                    ZoomBar.this.clearModelCaches();
                }
            }
        });
        this.slider.addMouseMotionListener(new MouseMotionAdapter(){

            @Override
            public void mouseDragged(MouseEvent e) {
                try {
                    ZoomBar.this.layerViewPanel().erase((Graphics2D)ZoomBar.this.layerViewPanel().getGraphics());
                    try {
                        ZoomBar.this.drawWireframe();
                    }
                    catch (Exception e1) {
                        LOGGER.error((Object)"", (Throwable)e1);
                    }
                    ScaleBarRenderer scaleBarRenderer = (ScaleBarRenderer)ZoomBar.this.layerViewPanel().getRenderingManager().getRenderer("SCALE_BAR");
                    if (scaleBarRenderer != null) {
                        scaleBarRenderer.paint((Graphics2D)ZoomBar.this.layerViewPanel().getGraphics(), ZoomBar.this.getScale());
                    }
                    ZoomBar.this.updateLabel();
                }
                catch (NoninvertibleTransformException noninvertibleTransformException) {
                    // empty catch block
                }
            }
        });
        if (this.slider.getUI() instanceof BasicSliderUI) {
            this.slider.addMouseMotionListener(new MouseMotionAdapter(){

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (ZoomBar.this.layerViewPanel() == null) {
                        return;
                    }
                    try {
                        ZoomBar.this.slider.setToolTipText(String.valueOf(I18N.getString("workbench.ui.zoom.ZoomBar.zoom-to")) + ZoomBar.this.chooseGoodIncrement(ZoomBar.this.toScale(((BasicSliderUI)ZoomBar.this.slider.getUI()).valueForXPosition(e.getX()))).toString());
                    }
                    catch (NoninvertibleTransformException x) {
                        ZoomBar.this.slider.setToolTipText("Zoom");
                    }
                }
            });
        }
        this.label.setPreferredSize(new Dimension(50, this.label.getHeight()));
        this.slider.addKeyListener(new KeyAdapter(){

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == 37 || e.getKeyCode() == 39) {
                        ZoomBar.this.gestureFinished();
                    }
                }
                catch (NoninvertibleTransformException t) {
                    ZoomBar.this.layerViewPanel().getContext().handleThrowable(t);
                }
            }
        });
        this.slider.addMouseListener(new MouseAdapter(){

            @Override
            public void mousePressed(MouseEvent e) {
                if (!ZoomBar.this.slider.isEnabled()) {
                    return;
                }
                ZoomBar.this.layerViewPanel().getRenderingManager().setPaintingEnabled(false);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    ZoomBar.this.gestureFinished();
                }
                catch (NoninvertibleTransformException t) {
                    ZoomBar.this.layerViewPanel().getContext().handleThrowable(t);
                }
            }
        });
        GUIUtil.addInternalFrameListener(frame.getDesktopPane(), GUIUtil.toInternalFrameListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                ZoomBar.this.installListenersOnCurrentPanel();
                try {
                    ZoomBar.this.updateComponents();
                }
                catch (NoninvertibleTransformException noninvertibleTransformException) {
                    // empty catch block
                }
            }
        }));
        this.installListenersOnCurrentPanel();
        this.updateComponents();
    }

    private int totalGeometries() {
        int totalGeometries = 0;
        Iterator<Layer> i = this.layerViewPanel().getLayerManager().iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            int size = 0;
            try {
                size = layer.getUltimateFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            totalGeometries += size;
        }
        return totalGeometries;
    }

    private void installListenersOnCurrentPanel() {
        if (this.layerViewPanel() == null) {
            return;
        }
        this.installViewListeners();
        this.installModelListeners();
    }

    private void installViewListeners() {
        if (this.layerViewPanel() == null) {
            return;
        }
        String VIEW_LISTENERS_INSTALLED_KEY = String.valueOf(Integer.toHexString(this.hashCode())) + " - VIEW LISTENERS INSTALLED";
        if (this.viewBlackboard().get(VIEW_LISTENERS_INSTALLED_KEY) != null) {
            return;
        }
        this.layerViewPanel().getViewport().addListener(new ViewportListener(){

            @Override
            public void zoomChanged(Envelope modelEnvelope) {
                if (!ZoomBar.this.viewBlackboard().get(CENTRE_LOCKED_KEY, false)) {
                    ZoomBar.this.viewBlackboard().put(CENTRE_KEY, null);
                }
                ZoomBar.this.viewBlackboard().put(SCALE_KEY, null);
                try {
                    if (ZoomBar.this.layerViewPanel().getViewport().getScale() < ZoomBar.this.getMinScale()) {
                        ZoomBar.this.viewBlackboard().put(USER_DEFINED_MIN_SCALE, ZoomBar.this.layerViewPanel().getViewport().getScale());
                    }
                    if (ZoomBar.this.layerViewPanel().getViewport().getScale() > ZoomBar.this.getMaxScale()) {
                        ZoomBar.this.viewBlackboard().put(USER_DEFINED_MAX_SCALE, ZoomBar.this.layerViewPanel().getViewport().getScale());
                    }
                    ZoomBar.this.updateComponents();
                }
                catch (NoninvertibleTransformException noninvertibleTransformException) {
                    // empty catch block
                }
            }
        });
        this.viewBlackboard().put(VIEW_LISTENERS_INSTALLED_KEY, new Object());
    }

    private void installModelListeners() {
        if (this.layerViewPanel() == null) {
            return;
        }
        String MODEL_LISTENERS_INSTALLED_KEY = String.valueOf(Integer.toHexString(this.hashCode())) + " - MODEL LISTENERS INSTALLED";
        if (this.viewBlackboard().get(MODEL_LISTENERS_INSTALLED_KEY) != null) {
            return;
        }
        this.layerViewPanel().getLayerManager().addLayerListener(new LayerListener(){

            @Override
            public void categoryChanged(CategoryEvent e) {
            }

            @Override
            public void featuresChanged(FeatureEvent e) {
                if (e.getType() == FeatureEventType.ADDED || e.getType() == FeatureEventType.DELETED || e.getType() == FeatureEventType.GEOMETRY_MODIFIED) {
                    ZoomBar.this.clearModelCaches();
                }
            }

            @Override
            public void layerChanged(LayerEvent e) {
                if (e.getType() == LayerEventType.ADDED || e.getType() == LayerEventType.REMOVED) {
                    ZoomBar.this.clearModelCaches();
                }
            }
        });
        this.viewBlackboard().put(MODEL_LISTENERS_INSTALLED_KEY, new Object());
    }

    private void queueComponentUpdate() {
        this.componentUpdateTimer.restart();
    }

    public void updateComponents() throws NoninvertibleTransformException {
        if (this.layerViewPanel() == null) {
            this.setComponentsEnabled(false);
            return;
        }
        this.setComponentsEnabled(true);
        this.slider.setValue(this.toSliderValue(this.viewBlackboard().get(SCALE_KEY, this.layerViewPanel().getViewport().getScale())));
        this.updateLabel();
        this.updateSliderLabels();
    }

    private void gestureFinished() throws NoninvertibleTransformException {
        if (!this.slider.isEnabled()) {
            return;
        }
        try {
            this.viewBlackboard().put(CENTRE_LOCKED_KEY, true);
            try {
                this.layerViewPanel().getViewport().zoom(this.proposedModelEnvelope());
            }
            finally {
                this.viewBlackboard().put(CENTRE_LOCKED_KEY, false);
            }
        }
        finally {
            this.layerViewPanel().getRenderingManager().setPaintingEnabled(true);
        }
    }

    private Envelope proposedModelEnvelope() throws NoninvertibleTransformException {
        double height;
        Coordinate centre = (Coordinate)this.viewBlackboard().get(CENTRE_KEY, EnvelopeUtil.centre(this.layerViewPanel().getViewport().getEnvelopeInModelCoordinates()));
        double width = (double)this.layerViewPanel().getWidth() / this.getScale();
        Envelope proposedModelEnvelope = new Envelope(centre.x - width / 2.0, centre.x + width / 2.0, centre.y - (height = (double)this.layerViewPanel().getHeight() / this.getScale()) / 2.0, centre.y + height / 2.0);
        if (proposedModelEnvelope.getWidth() == 0.0 || proposedModelEnvelope.getHeight() == 0.0) {
            proposedModelEnvelope = this.lastGoodEnvelope;
        } else {
            this.lastGoodEnvelope = proposedModelEnvelope;
        }
        return proposedModelEnvelope;
    }

    private double getScale() throws NoninvertibleTransformException {
        return this.toScale(this.slider.getValue());
    }

    private void drawWireframe() throws Exception {
        Graphics2D g = (Graphics2D)this.layerViewPanel().getGraphics();
        g.setColor(Color.lightGray);
        g.setStroke(this.stroke);
        g.draw(this.getWireFrame());
    }

    private void clearModelCaches() {
        this.modelBlackboard().put(SEGMENT_CACHE_KEY, null);
        this.modelBlackboard().put(MIN_EXTENT_KEY, null);
        this.modelBlackboard().put(MAX_EXTENT_KEY, null);
        this.queueComponentUpdate();
    }

    private Shape getWireFrame() throws Exception {
        AffineTransform transform = Viewport.modelToViewTransform(this.getScale(), new Point2D.Double(this.proposedModelEnvelope().getMinX(), this.proposedModelEnvelope().getMinY()), this.layerViewPanel().getSize().getHeight(), 0.0, null);
        Envelope proposedModelEnvelope = this.proposedModelEnvelope();
        GeneralPath wireFrame = new GeneralPath();
        ArrayList<Coordinate[]> segments = new ArrayList<Coordinate[]>(this.getSegmentCache());
        segments.addAll(this.toSegments(this.randomOnScreenGeometries()));
        for (Coordinate[] coordinates : segments) {
            boolean drawing = false;
            int j = 1;
            while (j < coordinates.length) {
                if (!this.lineSegmentEnvelopeIntersector.touches(coordinates[j - 1], coordinates[j], proposedModelEnvelope)) {
                    drawing = false;
                } else {
                    if (!drawing) {
                        Point2D p1 = transform.transform(new Point2D.Double(coordinates[j - 1].x, coordinates[j - 1].y), null);
                        wireFrame.moveTo((float)p1.getX(), (float)p1.getY());
                    }
                    Point2D p2 = transform.transform(new Point2D.Double(coordinates[j].x, coordinates[j].y), null);
                    wireFrame.lineTo((float)p2.getX(), (float)p2.getY());
                    drawing = true;
                }
                ++j;
            }
        }
        return wireFrame;
    }

    private Collection<Coordinate[]> getSegmentCache() throws NoninvertibleTransformException {
        if (this.modelBlackboard().get(SEGMENT_CACHE_KEY) == null) {
            this.modelBlackboard().put(SEGMENT_CACHE_KEY, this.toSegments(this.randomGeometries()));
        }
        return (Collection)this.modelBlackboard().get(SEGMENT_CACHE_KEY);
    }

    private Collection<Coordinate[]> toSegments(Collection<Geometry> geometries) {
        ArrayList<Coordinate[]> segments = new ArrayList<Coordinate[]>();
        for (Geometry geometry : geometries) {
            segments.addAll(CoordinateArrays.toCoordinateArrays(geometry, false));
        }
        return segments;
    }

    private Collection<Geometry> randomGeometries(int maxSize, List<Feature> features) {
        if (features.size() <= maxSize) {
            return FeatureUtil.toGeometries(features);
        }
        ArrayList<Geometry> randomGeometries = new ArrayList<Geometry>();
        int j = 0;
        while (j < maxSize) {
            randomGeometries.add(features.get((int)(Math.random() * (double)features.size())).getGeometry());
            ++j;
        }
        return randomGeometries;
    }

    private Collection<Geometry> randomOnScreenGeometries() throws Exception {
        ArrayList<Geometry> randomOnScreenGeometries = new ArrayList<Geometry>();
        if (this.totalGeometries() == 0) {
            return randomOnScreenGeometries;
        }
        Iterator<Layer> i = this.layerViewPanel().getLayerManager().iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            int size = 0;
            try {
                size = layer.getUltimateFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            randomOnScreenGeometries.addAll(this.randomGeometries(100 * size / this.totalGeometries(), layer.getFeatureCollectionWrapper().query(this.layerViewPanel().getViewport().getEnvelopeInModelCoordinates())));
        }
        return randomOnScreenGeometries;
    }

    private Collection<Geometry> randomGeometries() {
        ArrayList<Geometry> randomGeometries = new ArrayList<Geometry>();
        if (this.totalGeometries() == 0) {
            return randomGeometries;
        }
        Iterator<Layer> i = this.layerViewPanel().getLayerManager().iterator();
        while (i.hasNext()) {
            Layer layer = i.next();
            int size = 0;
            try {
                size = layer.getUltimateFeatureCollectionWrapper().size();
            }
            catch (Exception e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            randomGeometries.addAll(this.randomGeometries(100 * size / this.totalGeometries(), layer.getFeatureCollectionWrapper().getFeatures()));
        }
        return randomGeometries;
    }

    private int toSliderValue(double scale) throws NoninvertibleTransformException {
        return this.slider.getMaximum() - (int)((double)this.slider.getMaximum() * (MathUtil.base10Log(scale) - MathUtil.base10Log(this.getMinScale())) / (MathUtil.base10Log(this.getMaxScale()) - MathUtil.base10Log(this.getMinScale())));
    }

    private double getMinExtent() throws NoninvertibleTransformException {
        if (this.modelBlackboard().get(MIN_EXTENT_KEY) == null) {
            double smallSegmentLength = this.chooseSmallSegmentLength(this.getSegmentCache());
            if (smallSegmentLength == -1.0) {
                return -1.0;
            }
            this.modelBlackboard().put(MIN_EXTENT_KEY, smallSegmentLength);
        }
        Assert.isTrue((this.modelBlackboard().getDouble(MIN_EXTENT_KEY) > 0.0 ? 1 : 0) != 0);
        return this.modelBlackboard().getDouble(MIN_EXTENT_KEY);
    }

    private double chooseSmallSegmentLength(Collection<Coordinate[]> segmentCache) {
        int segmentsChecked = 0;
        double smallSegmentLength = -1.0;
        for (Coordinate[] coordinates : segmentCache) {
            int j = 1;
            while (j < coordinates.length) {
                double segmentLength = coordinates[j].distance(coordinates[j - 1]);
                ++segmentsChecked;
                if (segmentLength > 0.0 && (smallSegmentLength == -1.0 || segmentLength < smallSegmentLength)) {
                    smallSegmentLength = segmentLength;
                }
                if (segmentsChecked > 100) break;
                ++j;
            }
            if (segmentsChecked > 100) break;
        }
        return smallSegmentLength;
    }

    private double getMaxExtent() throws NoninvertibleTransformException {
        if (this.modelBlackboard().get(MAX_EXTENT_KEY) == null) {
            if (this.getSegmentCache().isEmpty()) {
                return -1.0;
            }
            this.modelBlackboard().put(MAX_EXTENT_KEY, this.layerViewPanel().getLayerManager().getEnvelopeOfAllLayers().getWidth());
        }
        return this.modelBlackboard().getDouble(MAX_EXTENT_KEY);
    }

    private double getMaxScale() throws NoninvertibleTransformException {
        double maxScale;
        double d = maxScale = this.getMinExtent() == -1.0 || this.getMinExtent() == 0.0 ? 1000.0 : (double)(1000 * this.layerViewPanel().getWidth()) / this.getMinExtent();
        if (this.viewBlackboard().get(USER_DEFINED_MAX_SCALE) != null) {
            return Math.max(maxScale, this.viewBlackboard().getDouble(USER_DEFINED_MAX_SCALE));
        }
        return maxScale;
    }

    private double getMinScale() throws NoninvertibleTransformException {
        double minScale;
        double d = minScale = this.getMaxExtent() == -1.0 || this.getMaxExtent() == 0.0 ? 0.001 : 0.001 * (double)this.layerViewPanel().getWidth() / this.getMaxExtent();
        if (this.viewBlackboard().get(USER_DEFINED_MIN_SCALE) != null) {
            return Math.min(minScale, this.viewBlackboard().getDouble(USER_DEFINED_MIN_SCALE));
        }
        return minScale;
    }

    private double toScale(int sliderValue) throws NoninvertibleTransformException {
        return Math.pow(10.0, (double)(this.slider.getMaximum() - sliderValue) * (MathUtil.base10Log(this.getMaxScale()) - MathUtil.base10Log(this.getMinScale())) / (double)this.slider.getMaximum() + MathUtil.base10Log(this.getMinScale()));
    }

    private void setComponentsEnabled(boolean componentsEnabled) {
        this.slider.setEnabled(componentsEnabled);
        this.label.setEnabled(componentsEnabled);
    }

    private Blackboard viewBlackboard() {
        return this.layerViewPanel().getBlackboard();
    }

    private Blackboard modelBlackboard() {
        return this.layerViewPanel().getLayerManager().getBlackboard();
    }

    private LayerViewPanel layerViewPanel() {
        if (!(this.frame.getActiveInternalFrame() instanceof LayerViewPanelProxy)) {
            return null;
        }
        return ((LayerViewPanelProxy)((Object)this.frame.getActiveInternalFrame())).getLayerViewPanel();
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.label.setText("X");
        this.slider.setPaintLabels(true);
        this.slider.setToolTipText("Zoom");
        this.slider.setMaximum(1000);
        this.add((Component)this.slider, "Center");
        this.add((Component)this.label, "East");
    }

    private void updateLabel() throws NoninvertibleTransformException {
        this.label.setText(this.chooseGoodIncrement(this.getScale()).toString());
    }

    private RoundQuantity chooseGoodIncrement(double scale) {
        return this.incrementChooser.chooseGoodIncrement(this.metricUnits, (double)this.layerViewPanel().getWidth() / scale, this.layerViewPanel().getMapLengthUnit());
    }

    private void updateSliderLabels() throws NoninvertibleTransformException {
        if (!this.showingSliderLabels) {
            return;
        }
        if (!(this.slider.getUI() instanceof BasicSliderUI)) {
            return;
        }
        Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
        int LABEL_WIDTH = 60;
        int lastLabelPosition = -120;
        int i = 0;
        while (i < this.slider.getWidth()) {
            if (i >= lastLabelPosition + 60) {
                int sliderValue = ((BasicSliderUI)this.slider.getUI()).valueForXPosition(i);
                JLabel label = new JLabel(this.chooseGoodIncrement(this.toScale(sliderValue)).toString());
                label.setFont(this.sliderLabelFont);
                labelTable.put(new Integer(sliderValue), label);
                lastLabelPosition = i;
            }
            ++i;
        }
        if (labelTable.isEmpty()) {
            return;
        }
        this.slider.setLabelTable(labelTable);
    }
}

