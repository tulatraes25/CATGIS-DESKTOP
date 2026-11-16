package ar.com.catgis.layout;

import ar.com.catgis.CRSDefinitions;
import ar.com.catgis.core.model.Project;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.awt.geom.Rectangle2D;

/**
 * Derives the scale denominator of a map frame from its world viewport and pixel
 * size, so CATMAP scale bars follow the real map instead of a manual number.
 */
public final class ScaleResolver {

    /** Metres per degree of latitude used for geographic CRS scale bars. */
    public static final double METERS_PER_DEGREE = 111320.0;

    private ScaleResolver() {}

    /** Scale denominator for the frame, or {@link Double#NaN} when not resolvable. */
    public static double resolveDenominator(LayoutMap map, int dpi) {
        if (map == null || dpi <= 0) {
            return Double.NaN;
        }
        Rectangle2D.Double bounds = map.getBoundsMm();
        if (bounds.width <= 0 || bounds.height <= 0) {
            return Double.NaN;
        }
        int frameWidthPx = (int) Math.round(bounds.width / 25.4 * dpi);
        int frameHeightPx = (int) Math.round(bounds.height / 25.4 * dpi);
        if (frameWidthPx < 10 || frameHeightPx < 10) {
            return Double.NaN;
        }
        MapFrameViewport viewport = map.viewportForFrame(frameWidthPx, frameHeightPx);
        double worldPerPixel = viewport.getWidth() / frameWidthPx;
        if (!Double.isFinite(worldPerPixel) || worldPerPixel <= 0) {
            return Double.NaN;
        }

        double metersPerPixel;
        CoordinateReferenceSystem crs = projectCrs();
        if (crs instanceof org.geotools.api.referencing.crs.GeographicCRS) {
            double latitude = Math.max(-89.0, Math.min(89.0, viewport.getCenterY()));
            double latitudeFactor = Math.max(0.02, Math.cos(Math.toRadians(latitude)));
            metersPerPixel = worldPerPixel * METERS_PER_DEGREE * latitudeFactor;
        } else if (crs != null) {
            metersPerPixel = worldPerPixel * metersPerUnit(crs);
        } else {
            metersPerPixel = worldPerPixel;
        }
        if (!Double.isFinite(metersPerPixel) || metersPerPixel <= 0) {
            return Double.NaN;
        }
        double denominator = metersPerPixel * 1000.0 * dpi / 25.4;
        return Double.isFinite(denominator) && denominator > 0 ? denominator : Double.NaN;
    }

    /**
     * Render-time element: a scale bar with auto scale becomes an independent
     * copy with the derived denominator; the document model is never mutated.
     */
    public static LayoutElement withResolvedScale(LayoutElement element, LayoutModel model,
                                                  int dpi) {
        if (!(element instanceof LayoutScaleBar bar) || !bar.isAutoScale()) {
            return element;
        }
        LayoutMap map = firstVisibleMap(model);
        if (map == null) {
            return element;
        }
        double denominator = resolveDenominator(map, dpi);
        if (!Double.isFinite(denominator)) {
            return element;
        }
        LayoutScaleBar copy = bar.copyForRender();
        copy.setMapScaleDenominator(denominator);
        return copy;
    }

    public static LayoutMap firstVisibleMap(LayoutModel model) {
        if (model == null) {
            return null;
        }
        for (LayoutElement element : model.getVisibleElementsSortedByZ()) {
            if (element instanceof LayoutMap map) {
                return map;
            }
        }
        return null;
    }

    private static CoordinateReferenceSystem projectCrs() {
        Project project = LayoutRenderHosts.get().project();
        String code = project != null ? project.getProjectCRS() : null;
        if (code == null || code.isBlank()) {
            return null;
        }
        try {
            return CRSDefinitions.decode(CRSDefinitions.normalizeCode(code), true);
        } catch (Exception invalid) {
            return null;
        }
    }

    private static double metersPerUnit(CoordinateReferenceSystem crs) {
        try {
            var axis = crs.getCoordinateSystem().getAxis(0);
            javax.measure.Unit<javax.measure.quantity.Length> lengthUnit =
                    axis.getUnit().asType(javax.measure.quantity.Length.class);
            return lengthUnit.getConverterTo(org.geotools.measure.Units.METRE).convert(1.0);
        } catch (Exception notLength) {
            return 1.0;
        }
    }
}
