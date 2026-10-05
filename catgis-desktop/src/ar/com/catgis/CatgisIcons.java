package ar.com.catgis;
import ar.com.catgis.core.model.Layer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public final class CatgisIcons {

    private static final int T = 20;
    private static final int S = 16;

    private CatgisIcons() {
    }

    private static Icon make(BufferedImage img) {
        return new ImageIcon(img);
    }

    private static BufferedImage createCanvas(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return img;
    }

    private static Graphics2D g(BufferedImage img) {
        return (Graphics2D) img.getGraphics();
    }

    // -- TOC layer type icons (16x16) --

    public static Icon tocVectorPoint() {
        BufferedImage img = createCanvas(S);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x1976D2));
        g.fill(new Ellipse2D.Float(4, 4, 8, 8));
        g.setColor(new Color(0x0D47A1));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Float(4, 4, 8, 8));
        g.dispose();
        return make(img);
    }

    public static Icon tocVectorLine() {
        BufferedImage img = createCanvas(S);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x1976D2));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath p = new GeneralPath();
        p.moveTo(2, 12);
        p.lineTo(7, 5);
        p.lineTo(10, 9);
        p.lineTo(14, 3);
        g.draw(p);
        g.dispose();
        return make(img);
    }

    public static Icon tocVectorPolygon() {
        BufferedImage img = createCanvas(S);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xE65100));
        g.setStroke(new BasicStroke(1.5f));
        GeneralPath p = new GeneralPath();
        p.moveTo(8, 2);
        p.lineTo(14, 6);
        p.lineTo(12, 13);
        p.lineTo(4, 13);
        p.lineTo(2, 6);
        p.closePath();
        g.draw(p);
        g.dispose();
        return make(img);
    }

    public static Icon tocRaster() {
        BufferedImage img = createCanvas(S);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x757575));
        g.setStroke(new BasicStroke(1f));
        g.draw(new Rectangle2D.Float(2, 2, 12, 12));
        g.drawLine(2, 6, 14, 6);
        g.drawLine(2, 10, 14, 10);
        g.drawLine(6, 2, 6, 14);
        g.drawLine(10, 2, 10, 14);
        g.dispose();
        return make(img);
    }

    public static Icon tocBaseMap() {
        BufferedImage img = createCanvas(S);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x388E3C));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Rectangle2D.Float(2, 2, 12, 12));
        g.setColor(new Color(0x4CAF50));
        GeneralPath p = new GeneralPath();
        p.moveTo(5, 10);
        p.lineTo(3, 14);
        p.lineTo(3, 8);
        p.closePath();
        g.fill(p);
        g.dispose();
        return make(img);
    }

    // -- Toolbar editing icons (20x20) --

    public static Icon toolbarPoint() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x1976D2));
        g.fill(new Ellipse2D.Float(7, 7, 6, 6));
        g.setColor(new Color(0x0D47A1));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new Ellipse2D.Float(7, 7, 6, 6));
        g.dispose();
        return make(img);
    }

    public static Icon toolbarLine() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x1976D2));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath p = new GeneralPath();
        p.moveTo(3, 15);
        p.lineTo(9, 7);
        p.lineTo(13, 11);
        p.lineTo(17, 4);
        g.draw(p);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarPolygon() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xE65100));
        g.setStroke(new BasicStroke(2f));
        GeneralPath p = new GeneralPath();
        p.moveTo(10, 3);
        p.lineTo(17, 8);
        p.lineTo(15, 16);
        p.lineTo(5, 16);
        p.lineTo(3, 8);
        p.closePath();
        g.draw(p);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarSelect() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x1976D2));
        g.setStroke(new BasicStroke(2f));
        g.draw(new Rectangle2D.Float(3, 3, 14, 14));
        g.setColor(new Color(0x90CAF9));
        g.fill(new Rectangle2D.Float(5, 5, 10, 10));
        g.dispose();
        return make(img);
    }

    public static Icon toolbarDelete() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xD32F2F));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(5, 5, 15, 15);
        g.drawLine(15, 5, 5, 15);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarSave() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x388E3C));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(10, 2, 10, 14);
        g.drawLine(5, 10, 10, 14);
        g.drawLine(10, 14, 15, 10);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarCancel() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0xD32F2F));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(4, 4, 16, 16);
        g.drawLine(16, 4, 4, 16);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarDEM() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x795548));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath p = new GeneralPath();
        p.moveTo(2, 16);
        p.lineTo(6, 6);
        p.lineTo(9, 10);
        p.lineTo(12, 3);
        p.lineTo(15, 8);
        p.lineTo(18, 5);
        g.draw(p);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarContour() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x6D4C41));
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        GeneralPath p = new GeneralPath();
        p.moveTo(2, 8);
        p.curveTo(6, 3, 10, 14, 14, 8);
        p.curveTo(16, 5, 18, 10, 18, 8);
        g.draw(p);
        g.dispose();
        return make(img);
    }

    public static Icon toolbarHydro() {
        BufferedImage img = createCanvas(T);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0x0288D1));
        g.fill(new Ellipse2D.Float(7, 2, 6, 8));
        g.fill(new Ellipse2D.Float(7, 10, 6, 8));
        GeneralPath p = new GeneralPath();
        p.moveTo(10, 8);
        p.curveTo(10, 6, 14, 6, 14, 8);
        p.lineTo(14, 10);
        p.curveTo(14, 12, 10, 12, 10, 10);
        g.fill(p);
        g.dispose();
        return make(img);
    }
}
