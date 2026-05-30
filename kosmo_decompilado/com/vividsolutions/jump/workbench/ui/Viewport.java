/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import com.vividsolutions.jump.workbench.ui.ZoomHistory;
import com.vividsolutions.jump.workbench.ui.cursortool.MeasureTool;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

public class Viewport
implements Java2DConverter.PointConverter {
    public static final Logger LOGGER = Logger.getLogger((String)"com.vividsolutions.jump.workbench.ui.Viewport");
    private static final int INITIAL_VIEW_ORIGIN_X = 0;
    private static final int INITIAL_VIEW_ORIGIN_Y = 0;
    private List<ViewportListener> listeners = new ArrayList<ViewportListener>();
    private Java2DConverter java2DConverter;
    private LayerViewPanel panel;
    private Point2D viewOriginAsPerceivedByModel = new Point2D.Double(0.0, 0.0);
    private double scale = 1.0;
    private AffineTransform modelToViewTransform;
    private ZoomHistory zoomHistory;
    private double angle;

    public double getAngle() {
        return this.angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public Viewport(LayerViewPanel panel) {
        this.panel = panel;
        this.zoomHistory = new ZoomHistory(panel);
        this.java2DConverter = new Java2DConverter(this);
        panel.addComponentListener(new ComponentAdapter(){

            @Override
            public void componentResized(ComponentEvent e) {
                Viewport.this.fireZoomChanged(Viewport.this.getEnvelopeInModelCoordinates());
            }
        });
    }

    public Viewport(AffineTransform aft) {
        this.modelToViewTransform = aft;
    }

    public LayerViewPanel getPanel() {
        return this.panel;
    }

    public void addListener(ViewportListener l) {
        this.listeners.add(l);
    }

    public void removeListener(ViewportListener l) {
        this.listeners.remove(l);
    }

    public Collection<ViewportListener> getListeners() {
        return this.listeners;
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

    public Java2DConverter getJava2DConverter() {
        return this.java2DConverter;
    }

    public ZoomHistory getZoomHistory() {
        return this.zoomHistory;
    }

    public void update() throws NoninvertibleTransformException {
        Coordinate point = this.getEnvelopeInModelCoordinates().centre();
        this.modelToViewTransform = Viewport.modelToViewTransform(this.scale, this.viewOriginAsPerceivedByModel, this.panel.getSize().height, this.angle, new Point2D.Double(point.x, point.y));
        this.panel.repaint();
    }

    public void update(boolean repaint) throws NoninvertibleTransformException {
        Coordinate point = this.getEnvelopeInModelCoordinates().centre();
        this.modelToViewTransform = Viewport.modelToViewTransform(this.scale, this.viewOriginAsPerceivedByModel, this.panel.getSize().height, this.angle, new Point2D.Double(point.x, point.y));
        this.panel.repaint(repaint);
    }

    public static AffineTransform modelToViewTransform(double scale, Point2D viewOriginAsPerceivedByModel, double panelHeight, double angle, Point2D rotationPoint) {
        AffineTransform modelToViewTransform = new AffineTransform();
        modelToViewTransform.translate(0.0, panelHeight);
        modelToViewTransform.scale(1.0, -1.0);
        modelToViewTransform.scale(scale, scale);
        modelToViewTransform.translate(-viewOriginAsPerceivedByModel.getX(), -viewOriginAsPerceivedByModel.getY());
        if (rotationPoint != null) {
            modelToViewTransform.rotate(angle, rotationPoint.getX(), rotationPoint.getY());
        }
        return modelToViewTransform;
    }

    public double getScale() {
        return this.scale;
    }

    public void initialize(double newScale, Point2D newViewOriginAsPerceivedByModel) {
        this.setScale(newScale);
        this.viewOriginAsPerceivedByModel = newViewOriginAsPerceivedByModel;
    }

    public Point2D getOriginInModelCoordinates() {
        return this.viewOriginAsPerceivedByModel;
    }

    private Rectangle getRotatedWindowRectangle() {
        Point2D.Double p1 = new Point2D.Double(0.0, 0.0);
        Point2D.Double p2 = new Point2D.Double(0.0, this.getPanel().getHeight());
        Point2D.Double p3 = new Point2D.Double(this.getPanel().getWidth(), 0.0);
        Point2D.Double p4 = new Point2D.Double(this.getPanel().getWidth(), this.getPanel().getHeight());
        AffineTransform trans = new AffineTransform();
        trans.rotate(this.angle, this.getPanel().getWidth() / 2, this.getPanel().getHeight() / 2);
        trans.transform(p1, p1);
        trans.transform(p2, p2);
        trans.transform(p3, p3);
        trans.transform(p4, p4);
        int minx = (int)Math.min(Math.min(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.min(((Point2D)p3).getX(), ((Point2D)p4).getX()));
        int maxx = (int)Math.max(Math.max(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.max(((Point2D)p3).getX(), ((Point2D)p4).getX()));
        int miny = (int)Math.min(Math.min(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.min(((Point2D)p3).getY(), ((Point2D)p4).getY()));
        int maxy = (int)Math.max(Math.max(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.max(((Point2D)p3).getY(), ((Point2D)p4).getY()));
        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    public double getRotatedWidht() {
        return this.getRotatedWindowRectangle().getWidth();
    }

    public double getRotatedHeight() {
        return this.getRotatedWindowRectangle().getHeight();
    }

    public void zoom(Point2D centreOfNewViewAsPerceivedByOldView, double widthOfNewViewAsPerceivedByOldView, double heightOfNewViewAsPerceivedByOldView) throws NoninvertibleTransformException {
        double zoomFactor = Math.min((double)this.panel.getSize().width / widthOfNewViewAsPerceivedByOldView, (double)this.panel.getSize().height / heightOfNewViewAsPerceivedByOldView);
        double realWidthOfNewViewAsPerceivedByOldView = (double)this.panel.getSize().width / zoomFactor;
        double realHeightOfNewViewAsPerceivedByOldView = (double)this.panel.getSize().height / zoomFactor;
        this.zoom(this.toModelEnvelope(centreOfNewViewAsPerceivedByOldView.getX() - 0.5 * realWidthOfNewViewAsPerceivedByOldView, centreOfNewViewAsPerceivedByOldView.getX() + 0.5 * realWidthOfNewViewAsPerceivedByOldView, centreOfNewViewAsPerceivedByOldView.getY() - 0.5 * realHeightOfNewViewAsPerceivedByOldView, centreOfNewViewAsPerceivedByOldView.getY() + 0.5 * realHeightOfNewViewAsPerceivedByOldView));
    }

    public Point2D toModelPoint(Point2D viewPoint) throws NoninvertibleTransformException {
        return this.getModelToViewTransform().inverseTransform(this.toPoint2DDouble(viewPoint), null);
    }

    private Point2D.Double toPoint2DDouble(Point2D p) {
        if (p instanceof Point2D.Double) {
            return (Point2D.Double)p;
        }
        return new Point2D.Double(p.getX(), p.getY());
    }

    public Coordinate toModelCoordinate(Point2D viewPoint) throws NoninvertibleTransformException {
        return CoordUtil.toCoordinate(this.toModelPoint(viewPoint));
    }

    public Point2D toViewPoint(Point2D modelPoint) throws NoninvertibleTransformException {
        return this.getModelToViewTransform().transform(this.toPoint2DDouble(modelPoint), null);
    }

    @Override
    public Point2D toViewPoint(Coordinate modelCoordinate) throws NoninvertibleTransformException {
        return this.toViewPoint(new Point2D.Double(modelCoordinate.x, modelCoordinate.y));
    }

    public Envelope toModelEnvelope(double x1, double x2, double y1, double y2) throws NoninvertibleTransformException {
        Coordinate c1 = this.toModelCoordinate(new Point2D.Double(x1, y1));
        Coordinate c2 = this.toModelCoordinate(new Point2D.Double(x2, y2));
        return new Envelope(c1, c2);
    }

    public AffineTransform getModelToViewTransform() throws NoninvertibleTransformException {
        if (this.modelToViewTransform == null) {
            this.update();
        }
        return this.modelToViewTransform;
    }

    public double toMapDistance(int d) {
        double dist = -1.0;
        try {
            dist = (double)d / this.getModelToViewTransform().getScaleX();
        }
        catch (NoninvertibleTransformException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return dist;
    }

    public Envelope getEnvelopeInModelCoordinatesForQuery() {
        double widthAsPerceivedByModel = (double)this.panel.getWidth() / this.scale;
        double heightAsPerceivedByModel = (double)this.panel.getHeight() / this.scale;
        Envelope env = new Envelope(this.viewOriginAsPerceivedByModel.getX(), this.viewOriginAsPerceivedByModel.getX() + widthAsPerceivedByModel, this.viewOriginAsPerceivedByModel.getY(), this.viewOriginAsPerceivedByModel.getY() + heightAsPerceivedByModel);
        Point2D.Double p1 = new Point2D.Double(env.getMinX(), env.getMinY());
        Point2D.Double p2 = new Point2D.Double(env.getMaxX(), env.getMinY());
        Point2D.Double p3 = new Point2D.Double(env.getMinX(), env.getMaxY());
        Point2D.Double p4 = new Point2D.Double(env.getMaxX(), env.getMaxY());
        AffineTransform trans = new AffineTransform();
        trans.rotate(this.angle, (env.getMaxX() + env.getMinX()) / 2.0, (env.getMinY() + env.getMaxY()) / 2.0);
        trans.transform(p1, p1);
        trans.transform(p2, p2);
        trans.transform(p3, p3);
        trans.transform(p4, p4);
        return new Envelope(Math.min(Math.min(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.min(((Point2D)p3).getX(), ((Point2D)p4).getX())), Math.max(Math.max(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.max(((Point2D)p3).getX(), ((Point2D)p4).getX())), Math.min(Math.min(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.min(((Point2D)p3).getY(), ((Point2D)p4).getY())), Math.max(Math.max(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.max(((Point2D)p3).getY(), ((Point2D)p4).getY())));
    }

    public Envelope getEnvelopeInModelCoordinates() {
        double widthAsPerceivedByModel = (double)this.panel.getWidth() / this.scale;
        double heightAsPerceivedByModel = (double)this.panel.getHeight() / this.scale;
        Envelope env = new Envelope(this.viewOriginAsPerceivedByModel.getX(), this.viewOriginAsPerceivedByModel.getX() + widthAsPerceivedByModel, this.viewOriginAsPerceivedByModel.getY(), this.viewOriginAsPerceivedByModel.getY() + heightAsPerceivedByModel);
        Point2D.Double p1 = new Point2D.Double(env.getMinX(), env.getMinY());
        Point2D.Double p2 = new Point2D.Double(env.getMaxX(), env.getMinY());
        Point2D.Double p3 = new Point2D.Double(env.getMinX(), env.getMaxY());
        Point2D.Double p4 = new Point2D.Double(env.getMaxX(), env.getMaxY());
        return new Envelope(Math.min(Math.min(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.min(((Point2D)p3).getX(), ((Point2D)p4).getX())), Math.max(Math.max(((Point2D)p1).getX(), ((Point2D)p2).getX()), Math.max(((Point2D)p3).getX(), ((Point2D)p4).getX())), Math.min(Math.min(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.min(((Point2D)p3).getY(), ((Point2D)p4).getY())), Math.max(Math.max(((Point2D)p1).getY(), ((Point2D)p2).getY()), Math.max(((Point2D)p3).getY(), ((Point2D)p4).getY())));
    }

    public void zoom(Envelope modelEnvelope) throws NoninvertibleTransformException {
        this.zoom(modelEnvelope, true);
    }

    public void zoom(Envelope modelEnvelope, boolean fireZoomChanged) throws NoninvertibleTransformException {
        this.zoom(modelEnvelope, fireZoomChanged, true);
    }

    public void zoom(Envelope modelEnvelope, boolean fireZoomChanged, boolean update) throws NoninvertibleTransformException {
        if (modelEnvelope.isNull()) {
            return;
        }
        if (!this.zoomHistory.hasNext() && !this.zoomHistory.hasPrev()) {
            this.zoomHistory.add(this.getEnvelopeInModelCoordinates());
        }
        this.setScale(Math.min((double)this.panel.getWidth() / modelEnvelope.getWidth(), (double)this.panel.getHeight() / modelEnvelope.getHeight()));
        double xCenteringOffset = ((double)this.panel.getWidth() / this.scale - modelEnvelope.getWidth()) / 2.0;
        double yCenteringOffset = ((double)this.panel.getHeight() / this.scale - modelEnvelope.getHeight()) / 2.0;
        this.viewOriginAsPerceivedByModel = new Point2D.Double(modelEnvelope.getMinX() - xCenteringOffset, modelEnvelope.getMinY() - yCenteringOffset);
        this.panel.fireRenderingStarted();
        this.update(update);
        Envelope realEnvelope = this.getEnvelopeInModelCoordinates();
        this.zoomHistory.add(realEnvelope);
        if (fireZoomChanged) {
            this.fireZoomChanged(realEnvelope);
        }
    }

    private void setScale(double scale) {
        this.scale = scale;
    }

    private void fireZoomChanged(Envelope modelEnvelope) {
        for (ViewportListener l : this.listeners) {
            l.zoomChanged(modelEnvelope);
        }
    }

    public void zoomToFullExtent() throws NoninvertibleTransformException {
        this.zoom(this.fullExtent());
    }

    public Envelope fullExtent() {
        return this.panel.getLayerManager().getEnvelopeOfAllLayers();
    }

    public void zoomToViewPoint(Point2D centreOfNewViewAsPerceivedByOldView, double zoomFactor) throws NoninvertibleTransformException {
        double widthOfNewViewAsPerceivedByOldView = (double)this.panel.getWidth() / zoomFactor;
        double heightOfNewViewAsPerceivedByOldView = (double)this.panel.getHeight() / zoomFactor;
        this.zoom(centreOfNewViewAsPerceivedByOldView, widthOfNewViewAsPerceivedByOldView, heightOfNewViewAsPerceivedByOldView);
    }

    public Collection<Point2D> toViewPoints(Collection<Coordinate> modelCoordinates) throws NoninvertibleTransformException {
        ArrayList<Point2D> viewPoints = new ArrayList<Point2D>();
        for (Coordinate modelCoordinate : modelCoordinates) {
            viewPoints.add(this.toViewPoint(modelCoordinate));
        }
        return viewPoints;
    }

    public Rectangle2D toViewRectangle(Envelope envelope) throws NoninvertibleTransformException {
        Point2D p1 = this.toViewPoint(new Coordinate(envelope.getMinX(), envelope.getMinY()));
        Point2D p2 = this.toViewPoint(new Coordinate(envelope.getMaxX(), envelope.getMaxY()));
        return new Rectangle2D.Double(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()), Math.abs(p1.getX() - p2.getX()), Math.abs(p1.getY() - p2.getY()));
    }

    public void setModelToViewTransform(AffineTransform modelToViewTransform) {
        this.modelToViewTransform = modelToViewTransform;
    }

    public void dispose() {
        this.panel = null;
        this.zoomHistory.dispose();
        this.zoomHistory = null;
    }

    public double getPixelSize() {
        Envelope env = this.getEnvelopeInModelCoordinates();
        Point2D.Double p1 = new Point2D.Double(env.getMinX(), env.getMaxY());
        Point2D.Double p2 = new Point2D.Double(env.getMaxX(), env.getMaxY());
        double envWidth = MeasureTool.distanceWorld(p1, p2, this.getPanel().getProjection());
        return envWidth / (double)this.getPanel().getWidth();
    }
}

