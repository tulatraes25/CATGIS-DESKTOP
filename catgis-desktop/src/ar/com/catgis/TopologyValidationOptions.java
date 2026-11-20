package ar.com.catgis;

import ar.com.catgis.data.vector.ShapefileData;

public class TopologyValidationOptions {

    private boolean invalidGeometries = true;
    private boolean selfIntersections = true;
    private boolean danglingEndpoints = true;
    private boolean nearMissEndpoints = true;
    private boolean duplicates = true;
    private boolean overlaps = true;
    private boolean holes = true;
    private boolean slivers = true;
    private boolean problematicMultiparts = true;
    private double connectionTolerance = 1.0;
    private double sliverAreaThreshold = 1.0;
    private ShapefileData studyArea;
    private String studyAreaCrs = "";
    private boolean coverageWithinStudyArea;
    private double coverageAreaThreshold = 1.0;

    public boolean isInvalidGeometries() {
        return invalidGeometries;
    }

    public void setInvalidGeometries(boolean invalidGeometries) {
        this.invalidGeometries = invalidGeometries;
    }

    public boolean isSelfIntersections() {
        return selfIntersections;
    }

    public void setSelfIntersections(boolean selfIntersections) {
        this.selfIntersections = selfIntersections;
    }

    public boolean isDanglingEndpoints() {
        return danglingEndpoints;
    }

    public void setDanglingEndpoints(boolean danglingEndpoints) {
        this.danglingEndpoints = danglingEndpoints;
    }

    public boolean isNearMissEndpoints() {
        return nearMissEndpoints;
    }

    public void setNearMissEndpoints(boolean nearMissEndpoints) {
        this.nearMissEndpoints = nearMissEndpoints;
    }

    public boolean isDuplicates() {
        return duplicates;
    }

    public void setDuplicates(boolean duplicates) {
        this.duplicates = duplicates;
    }

    public boolean isOverlaps() {
        return overlaps;
    }

    public void setOverlaps(boolean overlaps) {
        this.overlaps = overlaps;
    }

    public boolean isHoles() {
        return holes;
    }

    public void setHoles(boolean holes) {
        this.holes = holes;
    }

    public boolean isSlivers() {
        return slivers;
    }

    public void setSlivers(boolean slivers) {
        this.slivers = slivers;
    }

    public boolean isProblematicMultiparts() {
        return problematicMultiparts;
    }

    public void setProblematicMultiparts(boolean problematicMultiparts) {
        this.problematicMultiparts = problematicMultiparts;
    }

    public ShapefileData getStudyArea() {
        return studyArea;
    }

    public void setStudyArea(ShapefileData studyArea) {
        this.studyArea = studyArea;
    }

    public String getStudyAreaCrs() {
        return studyAreaCrs;
    }

    public void setStudyAreaCrs(String studyAreaCrs) {
        this.studyAreaCrs = studyAreaCrs == null ? "" : studyAreaCrs;
    }

    public boolean isCoverageWithinStudyArea() {
        return coverageWithinStudyArea;
    }

    public void setCoverageWithinStudyArea(boolean coverageWithinStudyArea) {
        this.coverageWithinStudyArea = coverageWithinStudyArea;
    }

    public double getCoverageAreaThreshold() {
        return coverageAreaThreshold;
    }

    public void setCoverageAreaThreshold(double coverageAreaThreshold) {
        if (coverageAreaThreshold >= 0d) {
            this.coverageAreaThreshold = coverageAreaThreshold;
        }
    }

    public double getConnectionTolerance() {
        return connectionTolerance;
    }

    public void setConnectionTolerance(double connectionTolerance) {
        if (connectionTolerance >= 0d) {
            this.connectionTolerance = connectionTolerance;
        }
    }

    public double getSliverAreaThreshold() {
        return sliverAreaThreshold;
    }

    public void setSliverAreaThreshold(double sliverAreaThreshold) {
        if (sliverAreaThreshold >= 0d) {
            this.sliverAreaThreshold = sliverAreaThreshold;
        }
    }
}
