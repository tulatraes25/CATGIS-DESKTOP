package ar.com.catgis.layout;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * Printable wrapper for a BufferedImage, scaling to fit a single page.
 */
public class LayoutImagePrintable implements Printable {
    private final BufferedImage image;

    public LayoutImagePrintable(BufferedImage image) {
        this.image = image;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0 || image == null) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            double availableWidth = pageFormat.getImageableWidth();
            double availableHeight = pageFormat.getImageableHeight();
            double scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
            AffineTransform transform = new AffineTransform();
            transform.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            transform.scale(scale, scale);
            g2.drawImage(image, transform, null);
            return PAGE_EXISTS;
        } finally {
            g2.dispose();
        }
    }
}
