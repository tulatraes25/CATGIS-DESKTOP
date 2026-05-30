/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.renderer.SimpleRenderer;
import com.vividsolutions.jump.workbench.ui.snap.SnapToGridPolicy;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class GridRenderer
extends SimpleRenderer {
    public static final String CONTENT_ID = "GRID";
    public static final String ENABLED_KEY = GridRenderer.class + " - ENABLED";
    public static final String DOTS_ENABLED_KEY = GridRenderer.class + " - DOTS ENABLED";
    public static final String LINES_ENABLED_KEY = GridRenderer.class + " - LINES ENABLED";
    private Blackboard blackboard;
    private Stroke stroke = new BasicStroke(1.0f, 0, 2, 0.0f, new float[]{1.0f, 2.0f}, 0.0f);

    public GridRenderer(Blackboard blackboard, LayerViewPanel panel) {
        super(CONTENT_ID, panel, 1.0);
        this.blackboard = blackboard;
    }

    @Override
    protected void paint(Graphics2D g) throws NoninvertibleTransformException {
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return;
        }
        double gridSize = PersistentBlackboardPlugIn.get(this.blackboard).get(SnapToGridPolicy.GRID_SIZE_KEY, 20.0);
        double viewGridSize = gridSize * this.panel.getViewport().getScale();
        if (viewGridSize < 5.0) {
            return;
        }
        g.setColor(Color.lightGray);
        double minModelX = Math.floor(this.panel.getViewport().getEnvelopeInModelCoordinates().getMinX() / gridSize) * gridSize;
        double maxModelX = Math.ceil(this.panel.getViewport().getEnvelopeInModelCoordinates().getMaxX() / gridSize) * gridSize;
        double minModelY = Math.floor(this.panel.getViewport().getEnvelopeInModelCoordinates().getMinY() / gridSize) * gridSize;
        double maxModelY = Math.ceil(this.panel.getViewport().getEnvelopeInModelCoordinates().getMaxY() / gridSize) * gridSize;
        if (PersistentBlackboardPlugIn.get(this.blackboard).get(DOTS_ENABLED_KEY, false)) {
            this.paintDots(g, gridSize, minModelX, maxModelX, minModelY, maxModelY);
        }
        if (PersistentBlackboardPlugIn.get(this.blackboard).get(LINES_ENABLED_KEY, false)) {
            this.paintLines(g, gridSize, minModelX, maxModelX, minModelY, maxModelY);
        }
    }

    private void paintDots(Graphics2D g, double gridSize, double minModelX, double maxModelX, double minModelY, double maxModelY) throws NoninvertibleTransformException {
        double x = minModelX;
        while (x < maxModelX) {
            double y = minModelY;
            while (y < maxModelY) {
                Point2D p = this.panel.getViewport().toViewPoint(new Coordinate(x, y));
                g.drawLine((int)p.getX(), (int)p.getY(), (int)p.getX(), (int)p.getY());
                y += gridSize;
            }
            x += gridSize;
        }
    }

    private void paintLines(Graphics2D g, double gridSize, double minModelX, double maxModelX, double minModelY, double maxModelY) throws NoninvertibleTransformException {
        Point2D max;
        Point2D min;
        g.setStroke(this.stroke);
        double x = minModelX;
        while (x < maxModelX) {
            min = this.panel.getViewport().toViewPoint(new Coordinate(x, minModelY));
            max = this.panel.getViewport().toViewPoint(new Coordinate(x, maxModelY));
            g.drawLine((int)min.getX(), (int)min.getY(), (int)max.getX(), (int)max.getY());
            x += gridSize;
        }
        double y = minModelY;
        while (y < maxModelY) {
            min = this.panel.getViewport().toViewPoint(new Coordinate(minModelX, y));
            max = this.panel.getViewport().toViewPoint(new Coordinate(maxModelX, y));
            g.drawLine((int)min.getX(), (int)min.getY(), (int)max.getX(), (int)max.getY());
            y += gridSize;
        }
    }
}

