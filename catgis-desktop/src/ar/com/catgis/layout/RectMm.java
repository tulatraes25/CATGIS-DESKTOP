package ar.com.catgis.layout;

/**
 * Immutable millimeter-precision rectangle with pixel-to-mm scale for
 * converting between page-pixel coordinates and real-world millimeters.
 */
public class RectMm {
    public final double xMm, yMm, wMm, hMm, pxToMmScale;

    public RectMm(double x, double y, double w, double h, double s) {
        xMm = x;
        yMm = y;
        wMm = w;
        hMm = h;
        pxToMmScale = s;
    }
}
