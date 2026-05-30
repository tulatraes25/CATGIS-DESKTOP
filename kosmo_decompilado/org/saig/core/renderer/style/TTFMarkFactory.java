/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer.style;

import com.vividsolutions.jump.feature.Feature;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.saig.core.filter.Expression;
import org.saig.core.renderer.style.FontCache;
import org.saig.core.renderer.style.MarkFactory;
import org.saig.core.renderer.style.shape.ExplicitBoundsShape;

public class TTFMarkFactory
implements MarkFactory {
    private static FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(new AffineTransform(), false, false);

    @Override
    public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature) throws Exception {
        char character;
        String markUrl = symbolUrl.getValue(feature).toString();
        if (!markUrl.startsWith("ttf://")) {
            return null;
        }
        if (!markUrl.matches("ttf://.+#.+")) {
            throw new IllegalArgumentException("Mark URL font found, but does not match the required structure font://<fontName>#<charNumber>, e.g., ttf://wingdigs#0x7B. You specified " + markUrl);
        }
        String[] fontElements = markUrl.substring(6).split("#");
        Font font = FontCache.getDefaultInstance().getFont(fontElements[0]);
        if (font == null) {
            throw new IllegalArgumentException("Unkown font " + fontElements[0]);
        }
        String code = fontElements[1];
        try {
            if (code.startsWith("U+") || code.startsWith("\\u")) {
                code = "0x" + code.substring(2);
            }
            if (!font.canDisplay(character = (char)Integer.decode(code).intValue())) {
                character = (char)(0xF000 | character);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid character specification " + fontElements[1], e);
        }
        GlyphVector textGlyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, new char[]{character});
        Shape s = textGlyphVector.getOutline();
        Rectangle2D bounds = s.getBounds2D();
        AffineTransform tx = new AffineTransform();
        double max = Math.max(bounds.getWidth(), bounds.getHeight());
        tx.scale(1.0 / max, -1.0 / max);
        tx.translate(-bounds.getCenterX(), -bounds.getCenterY());
        ExplicitBoundsShape shape = new ExplicitBoundsShape(tx.createTransformedShape(s));
        shape.setBounds(new Rectangle2D.Double(-0.5, 0.5, 1.0, 1.0));
        return shape;
    }

    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(300, 300, 6);
        Graphics2D g2d = (Graphics2D)image.getGraphics();
        g2d.setColor(Color.BLACK);
        char c = '\uf041';
        System.out.println((int)c);
        Font font = new Font("Wingdings", 0, 60);
        int i = 0;
        while (i < 65536) {
            if (font.canDisplay(i)) {
                System.out.println(String.valueOf(i) + ": " + Long.toHexString(i));
            }
            ++i;
        }
        GlyphVector textGlyphVector = font.createGlyphVector(FONT_RENDER_CONTEXT, new char[]{c});
        Shape shape = textGlyphVector.getOutline();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(150, 150);
        g2d.setColor(Color.BLUE);
        g2d.fill(shape);
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);
        g2d.drawString(new String(new char[]{c}), 0, 50);
        g2d.dispose();
        JFrame frame = new JFrame("Test");
        frame.setContentPane(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setDefaultCloseOperation(3);
        frame.setVisible(true);
    }
}

