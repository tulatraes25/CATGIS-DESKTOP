/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.AbstractDecorator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.geom.Point2D;

public class CoordinatesEqualDecorator
extends AbstractDecorator {
    private Color color;
    private Stroke stroke = new BasicStroke(2.0f);
    private static Image image = IconLoader.icon("GreenPinPushedIn.gif").getImage();

    public CoordinatesEqualDecorator() {
        this(Color.black);
    }

    public CoordinatesEqualDecorator(Color color) {
        super(null, null);
        this.setColor(color);
    }

    @Override
    protected void paintGeometry(Geometry geometry, Graphics2D graphics, Viewport viewport) throws Exception {
        if (geometry.isEmpty()) {
            return;
        }
        if (!CoordinatesEqualDecorator.coordinatesEqual(geometry)) {
            return;
        }
        graphics.setColor(this.color);
        Point2D viewCentre = viewport.toViewPoint(geometry.getCoordinate());
        graphics.setStroke(this.stroke);
        graphics.drawImage(image, (int)viewCentre.getX() - 9, (int)viewCentre.getY() - 19, null);
    }

    public static boolean coordinatesEqual(Geometry geometry) {
        Coordinate[] coordinates = geometry.getCoordinates();
        int i = 1;
        while (i < coordinates.length) {
            if (!coordinates[i].equals((Object)coordinates[0])) {
                return false;
            }
            ++i;
        }
        return true;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}

