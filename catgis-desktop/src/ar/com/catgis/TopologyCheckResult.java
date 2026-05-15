package ar.com.catgis;

import org.locationtech.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TopologyCheckResult {

    private final String severity;
    private final String checkType;
    private final String detail;
    private final List<String> featureIds;
    private final Geometry focusGeometry;
    private final String sourceCrs;

    public TopologyCheckResult(String checkType, String detail, List<String> featureIds) {
        this("Error", checkType, detail, featureIds, null, "");
    }

    public TopologyCheckResult(String severity,
                               String checkType,
                               String detail,
                               List<String> featureIds,
                               Geometry focusGeometry,
                               String sourceCrs) {
        this.severity = severity != null ? severity : "Error";
        this.checkType = checkType != null ? checkType : "";
        this.detail = detail != null ? detail : "";
        this.featureIds = featureIds != null ? new ArrayList<>(featureIds) : new ArrayList<>();
        this.focusGeometry = focusGeometry != null ? (Geometry) focusGeometry.copy() : null;
        this.sourceCrs = sourceCrs != null ? sourceCrs : "";
    }

    public String getSeverity() {
        return severity;
    }

    public String getCheckType() {
        return checkType;
    }

    public String getDetail() {
        return detail;
    }

    public List<String> getFeatureIds() {
        return Collections.unmodifiableList(featureIds);
    }

    public Geometry getFocusGeometry() {
        return focusGeometry != null ? (Geometry) focusGeometry.copy() : null;
    }

    public String getSourceCrs() {
        return sourceCrs;
    }

    public boolean hasFocusGeometry() {
        return focusGeometry != null && !focusGeometry.isEmpty();
    }
}
