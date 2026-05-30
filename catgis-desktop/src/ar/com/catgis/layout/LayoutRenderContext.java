package ar.com.catgis.layout;

public class LayoutRenderContext {

    public enum Mode { PREVIEW, EXPORT_IMAGE, EXPORT_PDF, PRINT }

    private final Mode mode;
    private final double dpi;
    private final double pageWidthMm;
    private final double pageHeightMm;

    public LayoutRenderContext(Mode mode, double dpi, double pageWidthMm, double pageHeightMm) {
        this.mode = mode;
        this.dpi = dpi;
        this.pageWidthMm = pageWidthMm;
        this.pageHeightMm = pageHeightMm;
    }

    public Mode getMode() { return mode; }
    public double getDpi() { return dpi; }
    public double getPageWidthMm() { return pageWidthMm; }
    public double getPageHeightMm() { return pageHeightMm; }

    public double mmToPx(double mm) { return (mm / 25.4) * dpi; }
    public double pxToMm(double px) { return (px / dpi) * 25.4; }

    public int mmToPxInt(double mm) { return (int) Math.round(mmToPx(mm)); }

    public RectangleMm pxRectToMm(int xPx, int yPx, int wPx, int hPx) {
        return new RectangleMm(pxToMm(xPx), pxToMm(yPx), pxToMm(wPx), pxToMm(hPx));
    }

    public static class RectangleMm {
        public final double x, y, w, h;
        public RectangleMm(double x, double y, double w, double h) { this.x = x; this.y = y; this.w = w; this.h = h; }
    }
}
