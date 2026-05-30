/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.geom.InteriorPointFinder;
import com.vividsolutions.jump.util.CoordinateArrays;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.util.I18NUnsupportedOperationException;

public class LabelStyle
implements Style {
    public static final int FONT_BASE_SIZE = 12;
    public static final String ABOVE_LINE = "ABOVE_LINE";
    public static final String ON_LINE = "ON_LINE";
    public static final String BELOW_LINE = "BELOW_LINE";
    public static final String FID_COLUMN = "$FID";
    private GeometryFactory factory = new GeometryFactory();
    private Color originalColor;
    private AffineTransform originalTransform;
    private Layer layer;
    private Geometry viewportRectangle = null;
    private InteriorPointFinder interiorPointFinder = new InteriorPointFinder();
    private Quadtree labelsDrawn = null;
    private String attribute = "$FID";
    private String angleAttribute = "";
    private String heightAttribute = "";
    private boolean enabled = false;
    private Color color = Color.black;
    private Color colorGlowing = Color.WHITE;
    private Font font = new Font("Dialog", 0, 12);
    private boolean scaling = false;
    private boolean glowing = false;
    private double height = 12.0;
    private boolean hidingOverlappingLabels = true;
    public String verticalAlignment = "ABOVE_LINE";
    private static final String DEFAULT_ATTRIBUTE = "$FID";
    private List attributeLabels;
    public static final String DEFAULT_SEPARATOR = "-";
    private String separator = "-";

    @Override
    public void initialize(Layer layer) {
        this.labelsDrawn = new Quadtree();
        this.viewportRectangle = null;
        this.layer = layer;
    }

    @Override
    public String getName() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public Icon getIcon() {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void paint(Feature f, Graphics2D g, Viewport viewport) throws NoninvertibleTransformException {
        Object attribute;
        Object object = attribute = this.getAttribute().equals("$FID") ? String.valueOf(f.getID()) : f.getAttribute(this.getAttribute());
        if (attribute == null || attribute.toString().length() == 0) {
            return;
        }
        Geometry viewportIntersection = this.intersection(f.getGeometry(), viewport);
        if (viewportIntersection == null) {
            return;
        }
        ModelSpaceLabelSpec spec = this.modelSpaceLabelSpec(viewportIntersection);
        Point2D labelCentreInViewSpace = viewport.toViewPoint(new Point2D.Double(spec.location.x, spec.location.y));
        this.paint(g, attribute.toString(), viewport.getScale(), labelCentreInViewSpace, LabelStyle.angle(f, this.getAngleAttribute(), spec.angle), LabelStyle.height(f, this.getHeightAttribute(), this.getHeight()), spec.linear);
    }

    public static double angle(Feature feature, String angleAttributeName, double defaultAngle) {
        if (angleAttributeName.equals("")) {
            return defaultAngle;
        }
        Object angleAttribute = feature.getAttribute(angleAttributeName);
        if (angleAttribute == null) {
            return defaultAngle;
        }
        try {
            return Angle.toRadians(Double.parseDouble(angleAttribute.toString().trim()));
        }
        catch (NumberFormatException e) {
            return defaultAngle;
        }
    }

    private ModelSpaceLabelSpec modelSpaceLabelSpec(Geometry geometry) throws NoninvertibleTransformException {
        if (geometry.getDimension() == 1) {
            return this.modelSpaceLabelSpec1D(geometry);
        }
        return new ModelSpaceLabelSpec(this.interiorPointFinder.findPoint(geometry), 0.0, false);
    }

    private ModelSpaceLabelSpec modelSpaceLabelSpec1D(Geometry geometry) {
        LineSegment longestSegment = this.longestSegment(geometry);
        return new ModelSpaceLabelSpec(CoordUtil.average(longestSegment.p0, longestSegment.p1), this.angle(longestSegment), true);
    }

    private double angle(LineSegment segment) {
        double angle = Angle.angle(segment.p0, segment.p1);
        if (angle < -1.5707963267948966) {
            angle += Math.PI;
        }
        if (angle > 1.5707963267948966) {
            angle -= Math.PI;
        }
        return angle;
    }

    private LineSegment longestSegment(Geometry geometry) {
        double maxSegmentLength = -1.0;
        Coordinate c0 = null;
        Coordinate c1 = null;
        List<Coordinate[]> arrays = CoordinateArrays.toCoordinateArrays(geometry, false);
        for (Coordinate[] coordinates : arrays) {
            int j = 1;
            while (j < coordinates.length) {
                if (coordinates[j - 1].distance(coordinates[j]) > maxSegmentLength) {
                    maxSegmentLength = coordinates[j - 1].distance(coordinates[j]);
                    c0 = coordinates[j - 1];
                    c1 = coordinates[j];
                }
                ++j;
            }
        }
        return new LineSegment(c0, c1);
    }

    public static double height(Feature feature, String heightAttributeName, double defaultHeight) {
        if (heightAttributeName.equals("")) {
            return defaultHeight;
        }
        Object heightAttribute = feature.getAttribute(heightAttributeName);
        if (heightAttribute == null) {
            return defaultHeight;
        }
        try {
            return Double.parseDouble(heightAttribute.toString().trim());
        }
        catch (NumberFormatException e) {
            return defaultHeight;
        }
    }

    public void paint(Graphics2D g, String text, double viewportScale, Point2D viewCentre, double angle, double height, boolean linear) {
        this.setup(g);
        try {
            double scale = 1.0;
            if (this.isScaling()) {
                scale *= viewportScale;
            }
            g.setColor(this.getColor());
            TextLayout layout = new TextLayout(text, this.getFont(), g.getFontRenderContext());
            AffineTransform transform = g.getTransform();
            this.configureTransform(transform, viewCentre, scale, layout, angle, linear);
            g.setTransform(transform);
            if (this.isHidingOverlappingLabels()) {
                Envelope transformedLabelBoundsEnvelope;
                Area transformedLabelBounds = new Area(layout.getBounds()).createTransformedArea(transform);
                if (this.collidesWithExistingLabel(transformedLabelBounds, transformedLabelBoundsEnvelope = this.envelope(transformedLabelBounds))) {
                    return;
                }
                this.labelsDrawn.insert(transformedLabelBoundsEnvelope, transformedLabelBounds);
            }
            layout.draw(g, 0.0f, 0.0f);
        }
        finally {
            this.cleanup(g);
        }
    }

    private Envelope envelope(Shape shape) {
        Rectangle2D bounds = shape.getBounds2D();
        return new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    private boolean collidesWithExistingLabel(Area transformedLabelBounds, Envelope transformedLabelBoundsEnvelope) {
        List potentialCollisions = this.labelsDrawn.query(transformedLabelBoundsEnvelope);
        for (Area potentialCollision : potentialCollisions) {
            Area intersection = new Area(potentialCollision);
            intersection.intersect(transformedLabelBounds);
            if (intersection.isEmpty()) continue;
            return true;
        }
        return false;
    }

    private void setup(Graphics2D g) {
        this.originalTransform = g.getTransform();
        this.originalColor = g.getColor();
    }

    private void cleanup(Graphics2D g) {
        g.setTransform(this.originalTransform);
        g.setColor(this.originalColor);
    }

    private Geometry intersection(Geometry geometry, Viewport viewport) throws NoninvertibleTransformException {
        return geometry.intersection(this.viewportRectangle(viewport));
    }

    private Geometry viewportRectangle(Viewport viewport) throws NoninvertibleTransformException {
        if (this.viewportRectangle == null) {
            Envelope e = viewport.toModelEnvelope(0.0, viewport.getPanel().getWidth(), 0.0, viewport.getPanel().getHeight());
            this.viewportRectangle = this.factory.createPolygon(this.factory.createLinearRing(new Coordinate[]{new Coordinate(e.getMinX(), e.getMinY()), new Coordinate(e.getMinX(), e.getMaxY()), new Coordinate(e.getMaxX(), e.getMaxY()), new Coordinate(e.getMaxX(), e.getMinY()), new Coordinate(e.getMinX(), e.getMinY())}), null);
        }
        return this.viewportRectangle;
    }

    private void configureTransform(AffineTransform transform, Point2D viewCentre, double scale, TextLayout layout, double angle, boolean linear) {
        double xTranslation = viewCentre.getX() - scale * layout.getBounds().getWidth() / 2.0;
        double yTranslation = viewCentre.getY() + scale * GUIUtil.trueAscent(layout) / 2.0;
        if (linear) {
            yTranslation -= this.verticalAlignmentOffset(scale * layout.getBounds().getHeight());
        }
        transform.rotate(-angle, viewCentre.getX(), viewCentre.getY());
        transform.translate(xTranslation, yTranslation);
        transform.scale(scale, scale);
    }

    private double verticalAlignmentOffset(double scaledLabelHeight) {
        if (this.getVerticalAlignment().equals(ON_LINE)) {
            return 0.0;
        }
        double buffer = 3.0;
        double offset = buffer + (double)this.layer.getBasicStyle().getLineWidth() / 2.0 + scaledLabelHeight / 2.0;
        if (this.getVerticalAlignment().equals(ABOVE_LINE)) {
            return offset;
        }
        if (this.getVerticalAlignment().equals(BELOW_LINE)) {
            return -offset;
        }
        Assert.shouldNeverReachHere();
        return 0.0;
    }

    public String getAngleAttribute() {
        return this.angleAttribute;
    }

    public String getHeightAttribute() {
        return this.heightAttribute;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public Color getColor() {
        return this.color;
    }

    public Font getFont() {
        return this.font;
    }

    public boolean isGlowing() {
        return this.glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
    }

    public boolean isScaling() {
        return this.scaling;
    }

    public double getHeight() {
        return this.height;
    }

    public boolean isHidingOverlappingLabels() {
        return this.hidingOverlappingLabels;
    }

    public String getVerticalAlignment() {
        return this.verticalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public void setAngleAttribute(String angleAttribute) {
        this.angleAttribute = angleAttribute;
    }

    public void setHeightAttribute(String heightAttribute) {
        this.heightAttribute = heightAttribute;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setScaling(boolean scaling) {
        this.scaling = scaling;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setHidingOverlappingLabels(boolean hidingOverlappingLabels) {
        this.hidingOverlappingLabels = hidingOverlappingLabels;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            Assert.shouldNeverReachHere();
            return null;
        }
    }

    public String getLabelAttribute(Feature feature) {
        String resultado = "";
        try {
            if (this.attributeLabels == null || this.attributeLabels.isEmpty()) {
                resultado = String.valueOf(feature.getID());
            } else {
                for (String item : this.attributeLabels) {
                    resultado = String.valueOf(resultado) + feature.getAttribute(item) + this.separator;
                }
                if (resultado.length() != 0) {
                    resultado = resultado.substring(0, resultado.length() - this.separator.length());
                }
            }
        }
        catch (Exception ex) {
            resultado = null;
        }
        return resultado;
    }

    public String getSeparator() {
        return this.separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public List getAttributeLabels() {
        return this.attributeLabels;
    }

    public String getAttribute() {
        if (this.attribute == null) {
            return "$FID";
        }
        return this.attribute;
    }

    public Color getColorGlowing() {
        return this.colorGlowing;
    }

    public void setColorGlowing(Color colorGlowing) {
        this.colorGlowing = colorGlowing;
    }

    private class ModelSpaceLabelSpec {
        public double angle;
        public Coordinate location;
        public boolean linear;

        public ModelSpaceLabelSpec(Coordinate location, double angle, boolean linear) {
            this.location = location;
            this.angle = angle;
            this.linear = linear;
        }
    }
}

