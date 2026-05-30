/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.TopologyException
 *  com.vividsolutions.jts.operation.distance.DistanceOp
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.cursortool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.renderer.TemporalGeometriesRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.TemporalGeometryItem;
import com.vividsolutions.jump.workbench.ui.snap.SnapIndicatorTool;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.IModifiableCoordinateTrace;
import org.saig.jump.plugin.editing.controllers.MultiClickToolTraceController;
import org.saig.jump.tools.editing.Utils;

public abstract class MultiClickTool
extends AbstractCursorTool
implements IModifiableCoordinateTrace {
    private static final Logger LOGGER = Logger.getLogger(MultiClickTool.class);
    private static final int STATE_INACTIVE = 0;
    private static final int STATE_SELECT_GEOMETRY = 1;
    private static final int STATE_SELECT_START_POINT = 2;
    private static final int STATE_SELECT_END_POINT = 3;
    private static final int STATE_SELECT_PATH = 4;
    private static final int STATE_PROCESSING = 5;
    public static final int TGI_GEOMETRY_KEY = 1;
    public static final int TGI_FINAL_LINESTRING_KEY = 2;
    public static final int TGI_START_POINT_KEY = 3;
    public static final int TGI_END_POINT_KEY = 4;
    private static final String GEOMETRIC_TRACKING_OFF_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.geometry-tracking-mode-off");
    private static final String GEOMETRIC_TRACKING_ON_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.geometry-tracking-mode-on");
    private static final String SELECT_GEOMETRY_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.select-geometry");
    private static final String SELECT_START_POINT_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.select-starting-point");
    private static final String SELECT_END_POINT_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.select-end-point");
    private static final String SELECT_PATH_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.select-path");
    private static final String PROCESSING_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.processing");
    private static final String MUST_SELECT_POINT_FROM_GEOMETRY_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.a-geometry-point-must-be-selected");
    private static final String SELECT_END_POINT_DISTINCT_TO_START_POINT_MESSAGE = I18N.getString("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.select-an-ending-point-different-from-the-starting-one");
    protected SnapIndicatorTool snapIndicatorTool = null;
    private static final int PIXELS_TO_SNAP_GEOMETRY = 10;
    private static final int TRACKING_KEY_CODE = 84;
    private static final int UNDO_STATUS_TRACKING_KEY_CODE = 85;
    private boolean trackingCapable = true;
    private int currentTrackingState;
    protected List<Coordinate> coordinates = new ArrayList<Coordinate>();
    protected Coordinate tentativeCoordinate;
    private boolean closeRing = false;
    private boolean check;
    private LayerViewPanel layerViewPanel;
    private LineString lastSelectedGeometry = null;
    private Coordinate startCoordinate = null;
    private Coordinate endCoordinate = null;
    private LineString finalLineString = null;
    private List<LineString> posiblePaths = null;
    private List<Coordinate> undoneCoordinates = new ArrayList<Coordinate>();

    public MultiClickTool(boolean check, boolean trackingCapable) {
        this.check = check;
        this.trackingCapable = trackingCapable;
    }

    public MultiClickTool(boolean check) {
        this.check = check;
    }

    public MultiClickTool() {
        this.check = true;
    }

    @Override
    public void activate(LayerViewPanel layerViewPanel) {
        this.layerViewPanel = layerViewPanel;
        super.activate(layerViewPanel);
        this.getWorkbench().getFrame().setStatusMessage("");
        this.getWorkbench().getFrame().addEasyKeyListener(this);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.cancelGesture();
        this.getWorkbench().getFrame().removeEasyKeyListener(this);
        this.cancelTracking();
        this.getPanel().getContext().setStatusMessage("");
    }

    protected void setCloseRing(boolean closeRing) {
        this.closeRing = closeRing;
    }

    public void clearCoordinates() {
        this.coordinates.clear();
    }

    public List<Coordinate> getCoordinates() {
        return Collections.unmodifiableList(this.coordinates);
    }

    @Override
    public void cancelGesture() {
        super.cancelGesture();
        this.cancelTracking();
        this.coordinates.clear();
        this.undoneCoordinates.clear();
        this.hideMulticlickDialog();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.currentTrackingState == 0) {
            try {
                if (e.getClickCount() == 1) {
                    this.tentativeCoordinate = this.snap(e.getPoint());
                    this.redrawShape();
                }
                super.mouseReleased(e);
                if (this.isFinishingRelease(e)) {
                    this.finishGesture();
                }
            }
            catch (Throwable t) {
                this.getPanel().getContext().handleThrowable(t);
            }
        } else if (this.currentTrackingState == 1) {
            super.mouseReleased(e);
            if (this.lastSelectedGeometry != null) {
                this.setCurrentTrackingState(2);
            }
            if (!this.coordinates.isEmpty()) {
                this.tentativeCoordinate = this.coordinates.get(this.coordinates.size() - 1);
            }
        } else if (this.currentTrackingState == 2) {
            super.mouseReleased(e);
            Coordinate snap = null;
            try {
                snap = this.snap(e.getPoint());
            }
            catch (NoninvertibleTransformException e1) {
                e1.printStackTrace();
            }
            if (snap != null) {
                double units = 10.0 / this.layerViewPanel.getViewport().getScale();
                DistanceOp distOp = new DistanceOp((Geometry)geomFac.createPoint(snap), (Geometry)this.lastSelectedGeometry);
                if (distOp.distance() > units) {
                    this.getWorkbench().getFrame().warnUser(String.valueOf(MUST_SELECT_POINT_FROM_GEOMETRY_MESSAGE) + ". " + SELECT_START_POINT_MESSAGE);
                } else {
                    this.selectStartCoordinate(distOp.nearestPoints()[1]);
                    this.setCurrentTrackingState(3);
                }
            }
            if (this.startCoordinate != null) {
                this.tentativeCoordinate = this.startCoordinate;
            }
        } else if (this.currentTrackingState == 3) {
            super.mouseReleased(e);
            Coordinate snap = null;
            try {
                snap = this.snap(e.getPoint());
            }
            catch (NoninvertibleTransformException e1) {
                e1.printStackTrace();
            }
            if (snap != null) {
                double units = 10.0 / this.layerViewPanel.getViewport().getScale();
                DistanceOp distOp = new DistanceOp((Geometry)geomFac.createPoint(snap), (Geometry)this.lastSelectedGeometry);
                if (distOp.distance() > units) {
                    this.getWorkbench().getFrame().warnUser(String.valueOf(MUST_SELECT_POINT_FROM_GEOMETRY_MESSAGE) + ". " + SELECT_END_POINT_MESSAGE);
                } else {
                    this.selectEndCoordinate(distOp.nearestPoints()[1]);
                    this.posiblePaths = Utils.calculatePosiblePaths(this.lastSelectedGeometry, this.startCoordinate, this.endCoordinate);
                    if (this.posiblePaths.size() > 1) {
                        this.setCurrentTrackingState(4);
                    } else if (this.posiblePaths.size() == 1) {
                        this.selectFinalLineString(this.posiblePaths.get(0));
                        this.setCurrentTrackingState(5);
                    } else {
                        this.getWorkbench().getFrame().warnUser(SELECT_END_POINT_DISTINCT_TO_START_POINT_MESSAGE);
                        this.selectEndCoordinate(null);
                    }
                }
            }
            if (this.startCoordinate != null) {
                this.tentativeCoordinate = this.startCoordinate;
            }
        } else if (this.currentTrackingState == 4) {
            super.mouseReleased(e);
            if (this.finalLineString != null) {
                this.setCurrentTrackingState(5);
            }
            try {
                this.tentativeCoordinate = this.snap(e.getPoint());
                this.redrawShape();
            }
            catch (Throwable t) {
                this.getPanel().getContext().handleThrowable(t);
            }
        }
    }

    protected void mouseLocationChanged(MouseEvent e) {
        if (this.currentTrackingState == 0 || this.currentTrackingState == 2) {
            try {
                if (this.coordinates.isEmpty()) {
                    return;
                }
                this.tentativeCoordinate = this.snap(e.getPoint());
                this.redrawShape();
            }
            catch (Throwable t) {
                this.getPanel().getContext().handleThrowable(t);
            }
        } else if (this.currentTrackingState == 1) {
            this.selectGeometry(this.getClosestGeometry(e.getPoint()));
        } else if (this.currentTrackingState == 4) {
            this.selectFinalLineString(this.getClosestLineString(e.getPoint()));
        }
    }

    private void selectGeometry(LineString closestLineString) {
        if (closestLineString != this.lastSelectedGeometry) {
            this.lastSelectedGeometry = closestLineString;
            TemporalGeometryItem tgi = new TemporalGeometryItem.Builder((Geometry)this.lastSelectedGeometry).build();
            ((TemporalGeometriesRenderer)this.layerViewPanel.getRenderingManager().getRenderer("TEMPORAL_GEOMETRY")).add(1, tgi);
            this.layerViewPanel.getRenderingManager().render("TEMPORAL_GEOMETRY");
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mouseLocationChanged(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouseLocationChanged(e);
    }

    protected void add(Coordinate c) {
        if (!this.activate) {
            return;
        }
        if (this.check && this.getWorkbench().getContext().getLayerManager().getEditableLayers().size() == 0) {
            this.cancelGesture();
            this.getWorkbench().getContext().getWorkbench().getFrame().warnUser(I18N.getString("workbench.ui.cursortool.MultiClickTool.an-editable-layer-must-exist"));
            return;
        }
        this.coordinates.add(c);
    }

    @Override
    public void undoLastCoordinate() {
        if (this.coordinates.size() > 0) {
            Coordinate coordinate = this.coordinates.get(this.coordinates.size() - 1);
            this.coordinates.remove(this.coordinates.size() - 1);
            this.undoneCoordinates.add(coordinate);
            try {
                this.redrawShape((Graphics2D)this.getPanel().getGraphics());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void redoLastCoordinate() {
        if (this.undoneCoordinates.size() > 0) {
            Coordinate coordinate = this.undoneCoordinates.get(this.undoneCoordinates.size() - 1);
            this.undoneCoordinates.remove(this.undoneCoordinates.size() - 1);
            this.coordinates.add(coordinate);
            try {
                this.redrawShape((Graphics2D)this.getPanel().getGraphics());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reverseCoordinates() {
        if (this.coordinates.size() > 0) {
            Collections.reverse(this.coordinates);
            try {
                this.redrawShape((Graphics2D)this.getPanel().getGraphics());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeTrace() {
        if (this.coordinates.size() > 2) {
            Coordinate coordinate = this.coordinates.get(0);
            this.coordinates.add(coordinate);
            try {
                this.redrawShape((Graphics2D)this.getPanel().getGraphics());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (this.currentTrackingState == 0) {
            try {
                super.mousePressed(e);
                Assert.isTrue((e.getClickCount() > 0 ? 1 : 0) != 0);
                if (e.getClickCount() != 1) {
                    return;
                }
                Coordinate snap = this.snap(e.getPoint());
                this.updateMulticlickDialog(snap);
                this.add(snap);
            }
            catch (Throwable t) {
                this.getPanel().getContext().handleThrowable(t);
            }
        }
    }

    private void updateMulticlickDialog(Coordinate snap) {
        if (this.coordinates.isEmpty()) {
            MultiClickToolTraceController.getInstance().reset();
        } else {
            MultiClickToolTraceController controller = MultiClickToolTraceController.getInstance();
            LineSegment segment = new LineSegment(this.coordinates.get(this.coordinates.size() - 1), snap);
            controller.refresh(this, segment.angle(), segment.getLength());
            controller.show();
            JUMPWorkbench.getFrameInstance().toFront();
        }
    }

    private void hideMulticlickDialog() {
        MultiClickToolTraceController controller = MultiClickToolTraceController.getInstance();
        controller.hide();
    }

    @Override
    protected Shape getShape() throws NoninvertibleTransformException {
        if (this.coordinates.size() == 0) {
            return null;
        }
        Point2D firstPoint = this.getPanel().getViewport().toViewPoint(this.coordinates.get(0));
        GeneralPath path = new GeneralPath();
        path.moveTo((float)firstPoint.getX(), (float)firstPoint.getY());
        int i = 1;
        while (i < this.coordinates.size()) {
            Coordinate nextCoordinate = this.coordinates.get(i);
            Point2D nextPoint = this.getPanel().getViewport().toViewPoint(nextCoordinate);
            path.lineTo((int)nextPoint.getX(), (int)nextPoint.getY());
            ++i;
        }
        Point2D tentativePoint = this.getPanel().getViewport().toViewPoint(this.tentativeCoordinate);
        if (this.currentTrackingState == 3 || this.currentTrackingState == 4 || this.currentTrackingState == 5) {
            Point2D nextPoint = this.getPanel().getViewport().toViewPoint(this.startCoordinate);
            path.lineTo((int)nextPoint.getX(), (int)nextPoint.getY());
        }
        if (this.currentTrackingState == 0 || this.currentTrackingState == 2) {
            path.lineTo((int)tentativePoint.getX(), (int)tentativePoint.getY());
        }
        if (this.closeRing && this.coordinates.size() > 1) {
            path.lineTo((int)firstPoint.getX(), (int)firstPoint.getY());
        }
        return path;
    }

    protected boolean isFinishingRelease(MouseEvent e) {
        return e.getClickCount() == 2;
    }

    protected Coordinate[] toArray(List<Coordinate> coordinates) {
        return coordinates.toArray(new Coordinate[0]);
    }

    public void finishGesture() throws Exception {
        this.clearShape();
        try {
            try {
                this.fireGestureFinished();
            }
            catch (Exception e) {
                e.printStackTrace();
                this.coordinates.clear();
                this.undoneCoordinates.clear();
                this.hideMulticlickDialog();
            }
        }
        finally {
            this.coordinates.clear();
            this.undoneCoordinates.clear();
            this.hideMulticlickDialog();
        }
    }

    public void changeLastSegment(double angle, double length) {
        if (this.coordinates.size() > 1) {
            Coordinate a = this.coordinates.get(this.coordinates.size() - 2);
            Coordinate b = new Coordinate(a.x + Math.cos(angle) * length, a.y + Math.sin(angle) * length, a.z);
            this.coordinates.remove(this.coordinates.size() - 1);
            this.coordinates.add(b);
            try {
                this.redrawShape((Graphics2D)this.getPanel().getGraphics());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 27) {
            try {
                this.cancelGesture();
                CursorTool cursor = this.getWorkbench().getFrame().getToolBar().getDefaultCursorTool();
                this.getWorkbench().getContext().getLayerViewPanel().setCurrentCursorTool(cursor);
                AbstractButton boton = this.getWorkbench().getFrame().getToolBar().getButton(cursor.getClass());
                boton.doClick();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        if (this.isTrackingCapable()) {
            if (e.getKeyCode() == 84) {
                if (this.getCurrentTrackingState() == 0) {
                    this.setCurrentTrackingState(1);
                    this.layerViewPanel.getRenderingManager().render("TEMPORAL_GEOMETRY");
                } else {
                    this.cancelTracking();
                }
            }
            if (e.getKeyCode() == 85) {
                this.undoCurrentTrackingState();
            }
        }
    }

    protected boolean isTrackingCapable() {
        return this.trackingCapable;
    }

    protected void setTrackingCapable(boolean trackingCapable) {
        this.trackingCapable = trackingCapable;
    }

    private int getCurrentTrackingState() {
        return this.currentTrackingState;
    }

    private void setCurrentTrackingState(int trackingState) {
        switch (trackingState) {
            case 0: {
                this.currentTrackingState = trackingState;
                this.selectGeometry(null);
                this.getWorkbench().getFrame().warnUser(GEOMETRIC_TRACKING_OFF_MESSAGE);
                this.enableSnapIndicator(true);
                break;
            }
            case 1: {
                this.currentTrackingState = trackingState;
                this.selectStartCoordinate(null);
                this.selectGeometry(null);
                this.getWorkbench().getFrame().warnUser(String.valueOf(GEOMETRIC_TRACKING_ON_MESSAGE) + ": " + SELECT_GEOMETRY_MESSAGE);
                this.enableSnapIndicator(false);
                break;
            }
            case 2: {
                this.currentTrackingState = trackingState;
                this.selectStartCoordinate(null);
                this.enableSnapIndicator(true);
                if (!this.coordinates.isEmpty()) {
                    this.tentativeCoordinate = this.coordinates.get(this.coordinates.size() - 1);
                }
                this.getSnapManager().setSpecificGeometry((Geometry)this.lastSelectedGeometry);
                this.snapIndicatorTool.getSnapManager().setSpecificGeometry((Geometry)this.lastSelectedGeometry);
                this.getWorkbench().getFrame().warnUser(SELECT_START_POINT_MESSAGE);
                break;
            }
            case 3: {
                this.currentTrackingState = trackingState;
                this.selectEndCoordinate(null);
                this.selectFinalLineString(null);
                this.enableSnapIndicator(true);
                this.getWorkbench().getFrame().warnUser(SELECT_END_POINT_MESSAGE);
                break;
            }
            case 4: {
                this.currentTrackingState = trackingState;
                this.getWorkbench().getFrame().warnUser(SELECT_PATH_MESSAGE);
                this.enableSnapIndicator(false);
                break;
            }
            case 5: {
                this.currentTrackingState = trackingState;
                Coordinate[] coords = this.finalLineString.getCoordinates();
                this.tentativeCoordinate = coords[coords.length - 1];
                int i = 0;
                while (i < coords.length) {
                    this.add(coords[i]);
                    ++i;
                }
                this.cancelTracking();
                LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.added-{0}-coordinates", new Object[]{coords.length}));
                break;
            }
        }
    }

    private void selectStartCoordinate(Coordinate coordinate) {
        if (coordinate != this.startCoordinate) {
            this.startCoordinate = coordinate;
            TemporalGeometryItem tgi = new TemporalGeometryItem.Builder((Geometry)geomFac.createPoint(coordinate)).color(Color.BLUE).stroke(new BasicStroke(5.0f)).build();
            ((TemporalGeometriesRenderer)this.layerViewPanel.getRenderingManager().getRenderer("TEMPORAL_GEOMETRY")).add(3, tgi);
            this.layerViewPanel.getRenderingManager().render("TEMPORAL_GEOMETRY");
        }
    }

    private void selectEndCoordinate(Coordinate coordinate) {
        if (coordinate != this.endCoordinate) {
            this.endCoordinate = coordinate;
            TemporalGeometryItem tgi = new TemporalGeometryItem.Builder((Geometry)geomFac.createPoint(coordinate)).color(Color.GREEN).stroke(new BasicStroke(5.0f)).build();
            ((TemporalGeometriesRenderer)this.layerViewPanel.getRenderingManager().getRenderer("TEMPORAL_GEOMETRY")).add(4, tgi);
            this.layerViewPanel.getRenderingManager().render("TEMPORAL_GEOMETRY");
        }
    }

    private void selectFinalLineString(LineString lineString) {
        if (lineString != this.finalLineString) {
            this.finalLineString = lineString;
            TemporalGeometryItem tgi = new TemporalGeometryItem.Builder((Geometry)this.finalLineString).color(Color.CYAN).build();
            ((TemporalGeometriesRenderer)this.layerViewPanel.getRenderingManager().getRenderer("TEMPORAL_GEOMETRY")).add(2, tgi);
            this.layerViewPanel.getRenderingManager().render("TEMPORAL_GEOMETRY");
        }
    }

    private void undoCurrentTrackingState() {
        switch (this.currentTrackingState) {
            case 0: {
                this.getWorkbench().getFrame().setStatusMessage("");
                if (this.coordinates == null || this.coordinates.isEmpty()) break;
                LOGGER.info((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.cursortool.MultiClickTool.removed-coordinate-{0}", new Object[]{this.coordinates.size()}));
                this.undoLastCoordinate();
                break;
            }
            case 1: {
                this.setCurrentTrackingState(0);
                break;
            }
            case 2: {
                this.setCurrentTrackingState(1);
                break;
            }
            case 3: {
                this.setCurrentTrackingState(2);
                break;
            }
            case 4: {
                this.setCurrentTrackingState(3);
                break;
            }
            case 5: {
                break;
            }
        }
    }

    private void cancelTracking() {
        this.currentTrackingState = 0;
        this.selectGeometry(null);
        this.selectStartCoordinate(null);
        this.selectEndCoordinate(null);
        this.selectFinalLineString(null);
        this.posiblePaths = null;
        ((TemporalGeometriesRenderer)this.layerViewPanel.getRenderingManager().getRenderer("TEMPORAL_GEOMETRY")).clearTemporalGeometries();
        this.getSnapManager().setSpecificGeometry(null);
        if (this.snapIndicatorTool != null) {
            this.snapIndicatorTool.getSnapManager().setSpecificGeometry(null);
        }
        this.enableSnapIndicator(true);
    }

    private void enableSnapIndicator(boolean enabled) {
        if (this.snapIndicatorTool != null) {
            if (enabled) {
                this.snapIndicatorTool.activate(this.layerViewPanel);
            } else {
                this.snapIndicatorTool.deactivate();
            }
        }
    }

    private LineString getClosestGeometry(Point2D point) {
        LineString result = null;
        try {
            double units = 10.0 / this.layerViewPanel.getViewport().getScale();
            double minDistance = Double.MAX_VALUE;
            Point originalPoint = geomFac.createPoint(this.getPanel().getViewport().toModelCoordinate(point));
            Geometry bufferedTransformedCursorLocation = originalPoint.buffer(units);
            for (Geometry candidate : VisiblePointsAndLinesCache.instance(this.layerViewPanel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                int i = 0;
                while (i < candidate.getNumGeometries()) {
                    Geometry geomCand = candidate.getGeometryN(i);
                    int j = 0;
                    while (j < geomCand.getNumGeometries()) {
                        DistanceOp op;
                        double distanceTemp;
                        Geometry intersection;
                        Geometry geomSimple = geomCand.getGeometryN(j);
                        if (!(geomSimple instanceof Point) && !(geomSimple instanceof MultiPoint) && !(intersection = geomSimple.intersection(bufferedTransformedCursorLocation)).isEmpty() && (distanceTemp = (op = new DistanceOp(intersection, (Geometry)originalPoint)).distance()) < minDistance) {
                            minDistance = distanceTemp;
                            result = (LineString)geomSimple;
                        }
                        ++j;
                    }
                    ++i;
                }
            }
        }
        catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        catch (TopologyException e) {
            e.printStackTrace();
        }
        return result;
    }

    private LineString getClosestLineString(java.awt.Point point) {
        LineString result = null;
        try {
            double units = 10.0 / this.layerViewPanel.getViewport().getScale();
            double minDistance = Double.MAX_VALUE;
            Point originalPoint = geomFac.createPoint(this.getPanel().getViewport().toModelCoordinate(point));
            Geometry bufferedTransformedCursorLocation = originalPoint.buffer(units);
            for (LineString candidate : this.posiblePaths) {
                DistanceOp op;
                double distanceTemp;
                Geometry intersection = candidate.intersection(bufferedTransformedCursorLocation);
                if (intersection.isEmpty() || !((distanceTemp = (op = new DistanceOp(intersection, (Geometry)originalPoint)).distance()) < minDistance)) continue;
                minDistance = distanceTemp;
                result = candidate;
            }
        }
        catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        catch (TopologyException e) {
            e.printStackTrace();
        }
        return result;
    }
}

