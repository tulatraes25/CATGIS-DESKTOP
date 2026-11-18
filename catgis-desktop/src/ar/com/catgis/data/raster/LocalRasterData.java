package ar.com.catgis.data.raster;

import ar.com.catgis.CRSDefinitions;
import org.locationtech.jts.geom.Envelope;

import java.awt.image.BufferedImage;

public class LocalRasterData {
    private final BufferedImage image;
    private final Envelope envelope;
    private final int bandCount;
    private final boolean georeferenced;
    private final String sourceCRS;
    private final String rasterMode;
    private final String displayCRS;
    private ar.com.catgis.catmap.AnalysisRaster analytical;

    public LocalRasterData(BufferedImage image, Envelope envelope, int bandCount, boolean georeferenced, String sourceCRS) {
        this(image, envelope, bandCount, georeferenced, sourceCRS, "real", sourceCRS);
    }

    public LocalRasterData(BufferedImage image, Envelope envelope, int bandCount, boolean georeferenced, String sourceCRS, String rasterMode) {
        this(image, envelope, bandCount, georeferenced, sourceCRS, rasterMode, sourceCRS);
    }

    public LocalRasterData(BufferedImage image, Envelope envelope, int bandCount, boolean georeferenced, String sourceCRS, String rasterMode, String displayCRS) {
        this.image = image;
        this.envelope = envelope;
        this.bandCount = bandCount;
        this.georeferenced = georeferenced;
        this.sourceCRS = CRSDefinitions.normalizeCode(sourceCRS);
        this.rasterMode = rasterMode != null ? rasterMode : "real";
        this.displayCRS = CRSDefinitions.normalizeCode(displayCRS);
    }

    public BufferedImage getImage() { return image; }
    public Envelope getEnvelope() { return envelope; }
    public int getWidth() { return image != null ? image.getWidth() : 0; }
    public int getHeight() { return image != null ? image.getHeight() : 0; }
    public int getBandCount() { return bandCount; }
    public boolean isGeoreferenced() { return georeferenced; }
    public String getSourceCRS() { return sourceCRS; }
    public String getRasterMode() { return rasterMode; }
    public String getDisplayCRS() { return displayCRS; }

    /** Float analytical result (NoData/units preserved); null for plain images. */
    public ar.com.catgis.catmap.AnalysisRaster getAnalytical() {
        return analytical;
    }

    public void setAnalytical(ar.com.catgis.catmap.AnalysisRaster analytical) {
        this.analytical = analytical;
    }

    public boolean isPreviewMode() {
        return "preview".equalsIgnoreCase(rasterMode);
    }

    public boolean isVirtualMode() {
        return "virtual".equalsIgnoreCase(rasterMode);
    }

    public boolean isRealMode() {
        return "real".equalsIgnoreCase(rasterMode);
    }

    /** Release the backing BufferedImage to free native memory. */
    public void dispose() {
        if (image != null) {
            image.flush();
        }
    }
}
