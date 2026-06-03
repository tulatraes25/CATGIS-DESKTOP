package ar.com.catgis;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * CAD georeference configuration for a layer.
 * Extracted from Layer to reduce its field count.
 */
public class CadGeoreference {

    private String sourceCRS = "";
    private double offsetX = 0;
    private double offsetY = 0;
    private double scale = 1;
    private double rotationDegrees = 0;
    private String georeferenceMethod = "";
    private double m00 = 1, m01 = 0, m02 = 0;
    private double m10 = 0, m11 = 1, m12 = 0;
    private double residualMean = Double.NaN;
    private double residualMax = Double.NaN;
    private int referenceCount = 0;
    private int checkCount = 0;
    private final Set<String> hiddenInternalLayers = new LinkedHashSet<>();

    // --- Getters/Setters ---

    public String getSourceCRS() { return sourceCRS; }
    public void setSourceCRS(String crs) { sourceCRS = CRSDefinitions.normalizeCode(crs); }
    public double getOffsetX() { return offsetX; }
    public void setOffsetX(double v) { if (Double.isFinite(v)) offsetX = v; }
    public double getOffsetY() { return offsetY; }
    public void setOffsetY(double v) { if (Double.isFinite(v)) offsetY = v; }
    public double getScale() { return scale; }
    public void setScale(double s) { if (Double.isFinite(s) && s > 0) scale = s; }
    public double getRotationDegrees() { return rotationDegrees; }
    public void setRotationDegrees(double r) { if (Double.isFinite(r)) rotationDegrees = r; }
    public String getGeoreferenceMethod() { return georeferenceMethod; }
    public void setGeoreferenceMethod(String m) { georeferenceMethod = m != null ? m.trim() : ""; }
    public double getM00() { return m00; }
    public void setM00(double v) { if (Double.isFinite(v)) m00 = v; }
    public double getM01() { return m01; }
    public void setM01(double v) { if (Double.isFinite(v)) m01 = v; }
    public double getM02() { return m02; }
    public void setM02(double v) { if (Double.isFinite(v)) m02 = v; }
    public double getM10() { return m10; }
    public void setM10(double v) { if (Double.isFinite(v)) m10 = v; }
    public double getM11() { return m11; }
    public void setM11(double v) { if (Double.isFinite(v)) m11 = v; }
    public double getM12() { return m12; }
    public void setM12(double v) { if (Double.isFinite(v)) m12 = v; }
    public double getResidualMean() { return residualMean; }
    public void setResidualMean(double v) { residualMean = Double.isFinite(v) ? v : Double.NaN; }
    public double getResidualMax() { return residualMax; }
    public void setResidualMax(double v) { residualMax = Double.isFinite(v) ? v : Double.NaN; }
    public int getReferenceCount() { return Math.max(0, referenceCount); }
    public void setReferenceCount(int c) { referenceCount = Math.max(0, c); }
    public int getCheckCount() { return Math.max(0, checkCount); }
    public void setCheckCount(int c) { checkCount = Math.max(0, c); }

    // --- Transform ---

    public void setTransform(String method, double m00, double m01, double m02,
                             double m10, double m11, double m12) {
        setGeoreferenceMethod(method);
        setM00(m00); setM01(m01); setM02(m02);
        setM10(m10); setM11(m11); setM12(m12);
    }

    public void setDiagnostics(double mean, double max, int refs, int checks) {
        setResidualMean(mean);
        setResidualMax(max);
        setReferenceCount(refs);
        setCheckCount(checks);
    }

    public void clear() {
        georeferenceMethod = "";
        m00 = 1; m01 = 0; m02 = 0;
        m10 = 0; m11 = 1; m12 = 0;
        residualMean = Double.NaN;
        residualMax = Double.NaN;
        referenceCount = 0;
        checkCount = 0;
    }

    public boolean hasGeoreference() {
        return (georeferenceMethod != null && !georeferenceMethod.isBlank())
                || Math.abs(m00 - 1) > 1e-9 || Math.abs(m01) > 1e-9 || Math.abs(m02) > 1e-9
                || Math.abs(m10) > 1e-9 || Math.abs(m11 - 1) > 1e-9 || Math.abs(m12) > 1e-9;
    }

    public boolean hasAdjustment() {
        return Math.abs(offsetX) > 1e-9 || Math.abs(offsetY) > 1e-9
                || Math.abs(scale - 1) > 1e-9 || Math.abs(rotationDegrees) > 1e-9;
    }

    public boolean hasResidualCheck() {
        return checkCount > 0 && Double.isFinite(residualMean) && Double.isFinite(residualMax);
    }

    // --- Internal layers ---

    public Set<String> getHiddenInternalLayers() { return java.util.Collections.unmodifiableSet(hiddenInternalLayers); }
    public void setHiddenInternalLayers(java.util.Collection<String> names) {
        hiddenInternalLayers.clear();
        if (names == null) return;
        for (String name : names) {
            if (name != null && !name.trim().isBlank()) hiddenInternalLayers.add(name.trim());
        }
    }
    public boolean hasInternalLayerFilter() { return !hiddenInternalLayers.isEmpty(); }
    public boolean isInternalLayerVisible(String name) {
        return name == null || name.isBlank() || !hiddenInternalLayers.contains(name.trim());
    }
}
