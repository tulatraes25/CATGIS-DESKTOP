/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.renderer3;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RenderUtils {
    public static Rectangle2D.Double calculateProportionalImageRectangle(Image image, Envelope envelope) {
        return RenderUtils.calculateProportionalImageRectangle(image.getWidth(null), image.getHeight(null), envelope);
    }

    public static Rectangle2D.Double calculateProportionalImageRectangle(int width, int height, Envelope envelope) {
        int imageWidth = width;
        int imageHeght = height;
        double envelopeWidth = envelope.getWidth();
        double envelopeHeight = envelope.getHeight();
        return RenderUtils.adaptRatio(imageWidth, imageHeght, envelopeWidth, envelopeHeight);
    }

    public static Envelope adaptEnvelopeToImageRectangle(Image image, Envelope envelope) {
        int imageWidth = image.getWidth(null);
        int imageHeght = image.getHeight(null);
        double envelopeWidth = envelope.getWidth();
        double envelopeHeight = envelope.getHeight();
        Rectangle2D.Double adaptRatio = RenderUtils.adaptRatio(envelopeWidth, envelopeHeight, imageWidth, imageHeght);
        double width = adaptRatio.width / 2.0;
        double height = adaptRatio.height / 2.0;
        return new Envelope(envelope.centre().x - width, envelope.centre().x + width, envelope.centre().y - height, envelope.centre().y + height);
    }

    private static Rectangle2D.Double adaptRatio(double adaptableWidth, double adaptableHeght, double adapteeWidth, double adapteeHeight) {
        Rectangle2D.Double rect;
        double imageRatio = adaptableWidth / adaptableHeght;
        double envRatio = adapteeWidth / adapteeHeight;
        if (imageRatio > envRatio) {
            double imgWidht = adaptableWidth;
            double imgHeight = imgWidht / envRatio;
            rect = new Rectangle2D.Double(0.0, 0.0, imgWidht, imgHeight);
        } else {
            double imgHeight = adaptableHeght;
            double imgWidth = imgHeight * envRatio;
            rect = new Rectangle2D.Double(0.0, 0.0, imgWidth, imgHeight);
        }
        return rect;
    }

    public static AffineTransform getModelToViewTransform(Envelope envelope, Image image, double angle) {
        Coordinate point = envelope.centre();
        double scalex = RenderUtils.getScaleX(image, envelope);
        double scaley = RenderUtils.getScaleY(image, envelope);
        AffineTransform modelToViewTransform = RenderUtils.modelToViewTransform(scalex, scaley, new Point2D.Double(envelope.getMinX(), envelope.getMinY()), image.getHeight(null), angle, new Point2D.Double(point.x, point.y));
        return modelToViewTransform;
    }

    public static double getScaleX(Image image, Envelope envelope) {
        return (double)image.getWidth(null) / envelope.getWidth();
    }

    public static double getScaleY(Image image, Envelope envelope) {
        return (double)image.getHeight(null) / envelope.getHeight();
    }

    public static AffineTransform modelToViewTransform(double scalex, double scaley, Point2D viewOriginAsPerceivedByModel, double panelHeight, double angle, Point2D rotationPoint) {
        AffineTransform modelToViewTransform = new AffineTransform();
        modelToViewTransform.translate(0.0, panelHeight);
        modelToViewTransform.scale(1.0, -1.0);
        modelToViewTransform.scale(scalex, scaley);
        modelToViewTransform.translate(-viewOriginAsPerceivedByModel.getX(), -viewOriginAsPerceivedByModel.getY());
        if (rotationPoint != null) {
            modelToViewTransform.rotate(angle, rotationPoint.getX(), rotationPoint.getY());
        }
        return modelToViewTransform;
    }
}

