package ar.com.catgis;

public class RasterLayer extends Layer {

    private float opacity = 1.0f;
    private boolean grayscale = false;
    private boolean autoContrast = true;
    private int redBand = 0;
    private int greenBand = 1;
    private int blueBand = 2;
    private String rasterMode = "preview";
    private String derivedOperation = "";
    private String derivedParameters = "";
    private String proDatasetRef = "";
    private String proVariableName = "";
    private String proAcquisitionStart = "";
    private String proMaturityLevel = "";
    private String proMetadataSidecarPath = "";
    private String proJobRef = "";

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

    public String getDerivedOperation() {
        return derivedOperation;
    }

    public void setDerivedOperation(String derivedOperation) {
        this.derivedOperation = derivedOperation != null ? derivedOperation.trim() : "";
    }

    public String getDerivedParameters() {
        return derivedParameters;
    }

    public void setDerivedParameters(String derivedParameters) {
        this.derivedParameters = derivedParameters != null ? derivedParameters.trim() : "";
    }

    public boolean isDerivedLayer() {
        return derivedOperation != null && !derivedOperation.isBlank();
    }

    public String getProDatasetRef() {
        return proDatasetRef;
    }

    public void setProDatasetRef(String proDatasetRef) {
        this.proDatasetRef = proDatasetRef != null ? proDatasetRef.trim() : "";
    }

    public String getProVariableName() {
        return proVariableName;
    }

    public void setProVariableName(String proVariableName) {
        this.proVariableName = proVariableName != null ? proVariableName.trim() : "";
    }

    public String getProAcquisitionStart() {
        return proAcquisitionStart;
    }

    public void setProAcquisitionStart(String proAcquisitionStart) {
        this.proAcquisitionStart = proAcquisitionStart != null ? proAcquisitionStart.trim() : "";
    }

    public String getProMaturityLevel() {
        return proMaturityLevel;
    }

    public void setProMaturityLevel(String proMaturityLevel) {
        this.proMaturityLevel = proMaturityLevel != null ? proMaturityLevel.trim() : "";
    }

    public String getProMetadataSidecarPath() {
        return proMetadataSidecarPath;
    }

    public void setProMetadataSidecarPath(String proMetadataSidecarPath) {
        this.proMetadataSidecarPath = proMetadataSidecarPath != null ? proMetadataSidecarPath.trim() : "";
    }

    public String getProJobRef() {
        return proJobRef;
    }

    public void setProJobRef(String proJobRef) {
        this.proJobRef = proJobRef != null ? proJobRef.trim() : "";
    }

    public boolean hasProMetadata() {
        return !(proDatasetRef == null || proDatasetRef.isBlank())
                || !(proVariableName == null || proVariableName.isBlank())
                || !(proAcquisitionStart == null || proAcquisitionStart.isBlank())
                || !(proMaturityLevel == null || proMaturityLevel.isBlank())
                || !(proMetadataSidecarPath == null || proMetadataSidecarPath.isBlank())
                || !(proJobRef == null || proJobRef.isBlank());
    }
}
