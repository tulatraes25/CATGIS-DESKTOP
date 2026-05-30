/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.MinimumDiameter
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.prep.PreparedGeometry
 */
package es.kosmo.core.renderer.label;

import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import java.awt.geom.Rectangle2D;
import org.saig.core.renderer.lite.LabelCacheItem;
import org.saig.core.renderer.style.TextStyle2D;
import org.saig.core.styling.TextSymbolizer;

class TextStyle2DExt
extends TextStyle2D {
    Double alternateRotation;
    LabelCacheItem item;

    public TextStyle2DExt(LabelCacheItem item) {
        super(item.getTextStyle());
        this.item = item;
    }

    void setupPolygonAlign(PreparedGeometry pg) {
        if (this.item.getPolygonAlign() == TextSymbolizer.PolygonAlignOptions.NONE) {
            return;
        }
    }

    boolean flipRotation(Geometry geometry) {
        if (this.item.getPolygonAlign() == TextSymbolizer.PolygonAlignOptions.NONE) {
            return false;
        }
        if (this.alternateRotation == null) {
            double radians = 0.0;
            if (this.item.getPolygonAlign() == TextSymbolizer.PolygonAlignOptions.ORTHO) {
                radians = this.calcPolygonAlignOrthoAngle(geometry);
            } else if (this.item.getPolygonAlign() == TextSymbolizer.PolygonAlignOptions.MBR) {
                radians = this.calcPolygonAlignMBRAngle(geometry);
            }
            this.alternateRotation = radians;
        }
        double temp = this.getRotation();
        this.setRotation(this.alternateRotation);
        this.alternateRotation = temp;
        return true;
    }

    double calcPolygonAlignOrthoAngle(Geometry geometry) {
        Envelope envelope = geometry.getEnvelopeInternal();
        if (envelope.getHeight() > envelope.getWidth()) {
            return -1.5707963267948966;
        }
        return 0.0;
    }

    double calcPolygonAlignMBRAngle(Geometry geometry) {
        double dy;
        double dx;
        Geometry mbr = new MinimumDiameter(geometry).getMinimumRectangle();
        Coordinate[] coordinates = mbr.getCoordinates();
        if (coordinates[0].distance(coordinates[1]) > coordinates[1].distance(coordinates[2])) {
            dx = coordinates[1].x - coordinates[0].x;
            dy = coordinates[1].y - coordinates[0].y;
        } else {
            dx = coordinates[2].x - coordinates[1].x;
            dy = coordinates[2].y - coordinates[1].y;
        }
        double angle = Math.atan(dy / dx);
        if (Math.abs(angle - 1.5707963267948966) < Math.PI / 180) {
            angle = -1.5707963267948966 + Math.abs(angle - 1.5707963267948966);
        }
        return angle;
    }

    @Override
    public void setTextBounds(Rectangle2D tb) {
        super.setTextBounds(tb);
        if (this.item != null && this.item.getTextStyle() != null) {
            this.item.getTextStyle().setTextBounds(tb);
        }
    }

    @Override
    public void setLineHeight(double lh) {
        super.setLineHeight(lh);
        if (this.item != null && this.item.getTextStyle() != null) {
            this.item.getTextStyle().setLineHeight(lh);
        }
    }

    @Override
    public void setLinePlacementYAnchor(double yAnchor) {
        super.setLinePlacementYAnchor(yAnchor);
        if (this.item != null && this.item.getTextStyle() != null) {
            this.item.getTextStyle().setLinePlacementYAnchor(yAnchor);
        }
    }

    @Override
    public void setRotation(double f) {
        super.setRotation(f);
        if (this.item != null && this.item.getTextStyle() != null) {
            this.item.getTextStyle().setRotation(f);
        }
    }
}

