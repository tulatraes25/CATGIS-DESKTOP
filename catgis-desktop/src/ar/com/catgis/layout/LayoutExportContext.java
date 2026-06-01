package ar.com.catgis.layout;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Shared render context passed to layout elements during export.
 * Decouples rendering from MapLayoutComposerDialog.
 */
public class LayoutExportContext {

    public final double dpi;
    public final double pageWidthMm;
    public final double pageHeightMm;
    public final int pageWidthPx;
    public final int pageHeightPx;
    public final BufferedImage targetImage;
    public final Graphics2D graphics;

    public LayoutExportContext(double dpi, double pageWidthMm, double pageHeightMm,
                                int pageWidthPx, int pageHeightPx, BufferedImage target) {
        this.dpi = dpi;
        this.pageWidthMm = pageWidthMm;
        this.pageHeightMm = pageHeightMm;
        this.pageWidthPx = pageWidthPx;
        this.pageHeightPx = pageHeightPx;
        this.targetImage = target;
        this.graphics = target.createGraphics();
    }

    public LayoutRenderContext toRenderContext() {
        return new LayoutRenderContext(LayoutRenderContext.Mode.EXPORT_IMAGE, dpi, pageWidthMm, pageHeightMm);
    }

    public int mmToPx(double mm) { return (int) Math.round(mm / 25.4 * dpi); }

    public void dispose() { if (graphics != null) graphics.dispose(); }
}
