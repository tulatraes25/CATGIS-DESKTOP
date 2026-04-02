package ar.com.catgis;

public class RasterLayer extends Layer {

    private float opacity = 1.0f;
    private boolean grayscale = false;
    private boolean autoContrast = true;
    private int redBand = 0;
    private int greenBand = 1;
    private int blueBand = 2;
    private String rasterMode = "preview";

    public RasterLayer(String name, String path) {
        super(name, path, "RASTER");
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0f, Math.min(1f, opacity));
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public void setGrayscale(boolean grayscale) {
        this.grayscale = grayscale;
    }

    public boolean isAutoContrast() {
        return autoContrast;
    }

    public void setAutoContrast(boolean autoContrast) {
        this.autoContrast = autoContrast;
    }

    public int getRedBand() {
        return redBand;
    }

    public void setRedBand(int redBand) {
        this.redBand = Math.max(0, redBand);
    }

    public int getGreenBand() {
        return greenBand;
    }

    public void setGreenBand(int greenBand) {
        this.greenBand = Math.max(0, greenBand);
    }

    public int getBlueBand() {
        return blueBand;
    }

    public void setBlueBand(int blueBand) {
        this.blueBand = Math.max(0, blueBand);
    }

    public String getRasterMode() {
        return rasterMode;
    }

    public void setRasterMode(String rasterMode) {
        this.rasterMode = rasterMode != null ? rasterMode : "preview";
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

    public void setPreviewMode(boolean previewMode) {
        this.rasterMode = previewMode ? "preview" : "real";
    }
}
