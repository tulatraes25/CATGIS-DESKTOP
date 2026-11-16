package ar.com.catgis.catmap.print;

import ar.com.catgis.catmap.LayoutExportService;
import ar.com.catgis.layout.LayoutModel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Objects;

/**
 * Prints every page of a CATMAP document through the same render path used by
 * export, so printed output cannot drift from the preview.
 */
public final class LayoutPrintable implements Printable {

    /** Resolution used to rasterize each page before it is scaled to the printer page. */
    public static final int PRINT_DPI = 200;

    private final LayoutModel model;

    public LayoutPrintable(LayoutModel model) {
        this.model = Objects.requireNonNull(model, "CATMAP layout model");
    }

    public int pageCount() {
        return model.getPageCount();
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        if (pageFormat == null || pageIndex < 0 || pageIndex >= model.getPageCount()) {
            return NO_SUCH_PAGE;
        }
        BufferedImage page = LayoutExportService.renderPage(model, pageIndex, PRINT_DPI);
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double scaleX = pageFormat.getImageableWidth() / page.getWidth();
            double scaleY = pageFormat.getImageableHeight() / page.getHeight();
            double scale = Math.min(scaleX, scaleY);
            g2.scale(scale, scale);
            g2.drawImage(page, 0, 0, null);
            return PAGE_EXISTS;
        } finally {
            g2.dispose();
            page.flush();
        }
    }
}
