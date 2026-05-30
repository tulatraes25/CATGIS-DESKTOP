/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.io.ParseException
 *  com.vividsolutions.jts.io.WKTReader
 *  com.vividsolutions.jts.util.Assert
 *  javax.media.jai.JAI
 *  javax.media.jai.RenderedOp
 */
package org.saig.core.styling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.renderer.java2D.Java2DConverter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

public class WKTGraphic {
    private int lineWidth;
    private int extent;
    private String patternWKT;
    private Color color;
    private BufferedImage image;

    public WKTGraphic(int lineWidth, int extent, String patternWKT, Color color) {
        this.lineWidth = lineWidth;
        this.extent = extent;
        this.patternWKT = patternWKT;
        this.color = color;
        this.createImage();
    }

    public WKTGraphic(int lineWidth, int extent, String patternWKT) {
        this.lineWidth = lineWidth;
        this.extent = extent;
        this.patternWKT = patternWKT;
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public BufferedImage createImage() {
        if (this.image == null) {
            this.image = new BufferedImage(this.extent, this.extent, 2);
            Graphics2D g = this.image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Color strokeColor = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
            g.scale(1.0, -1.0);
            g.translate((double)this.extent / 2.0, (double)(-this.extent) / 2.0);
            g.setColor(strokeColor);
            g.setStroke(new BasicStroke(this.lineWidth));
            try {
                g.draw(new Java2DConverter(new Java2DConverter.PointConverter(){

                    @Override
                    public Point2D toViewPoint(Coordinate modelCoordinate) {
                        return new Point2D.Double(modelCoordinate.x, modelCoordinate.y);
                    }
                }).toShape(new WKTReader().read(this.patternWKT)));
            }
            catch (NoninvertibleTransformException noninvertibleTransformException) {
            }
            catch (ParseException e) {
                Assert.shouldNeverReachHere((String)this.patternWKT);
            }
        }
        return this.image;
    }

    public URL getImageURL() throws IOException {
        BufferedImage image = this.createImage();
        File tempFile = FileUtil.createTemporalFile("image_wkt", "png");
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image);
        pb.add(tempFile.getAbsolutePath());
        pb.add("PNG");
        RenderedOp op = JAI.create((String)"filestore", (ParameterBlock)pb);
        op.dispose();
        return tempFile.toURI().toURL();
    }

    public Color getColor() {
        return this.color;
    }

    public int getExtent() {
        return this.extent;
    }

    public int getLineWidth() {
        return this.lineWidth;
    }

    public String getPatternWKT() {
        return this.patternWKT;
    }

    public void setColor(Color color) {
        this.color = color;
        this.createImage();
    }
}

