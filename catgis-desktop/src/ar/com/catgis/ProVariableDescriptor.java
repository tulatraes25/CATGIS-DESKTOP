package ar.com.catgis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProVariableDescriptor {

    private String name = "";
    private String longName = "";
    private String standardName = "";
    private String units = "";
    private final List<String> dimensions = new ArrayList<>();
    private Double nodata;
    private Double scaleFactor;
    private Double addOffset;
    private Double validMin;
    private Double validMax;
    private String qaDescriptor = "";
    private String bandFamily = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = sanitize(name);
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = sanitize(longName);
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = sanitize(standardName);
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = sanitize(units);
    }

    public List<String> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }

    public void setDimensions(List<String> values) {
        dimensions.clear();
        if (values == null) {
            return;
        }
        for (String value : values) {
            String sanitized = sanitize(value);
            if (!sanitized.isBlank()) {
                dimensions.add(sanitized);
            }
        }
    }

    public Double getNodata() {
        return nodata;
    }

    public void setNodata(Double nodata) {
        this.nodata = nodata;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(Double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public Double getAddOffset() {
        return addOffset;
    }

    public void setAddOffset(Double addOffset) {
        this.addOffset = addOffset;
    }

    public Double getValidMin() {
        return validMin;
    }

    public void setValidMin(Double validMin) {
        this.validMin = validMin;
    }

    public Double getValidMax() {
        return validMax;
    }

    public void setValidMax(Double validMax) {
        this.validMax = validMax;
    }

    public String getQaDescriptor() {
        return qaDescriptor;
    }

    public void setQaDescriptor(String qaDescriptor) {
        this.qaDescriptor = sanitize(qaDescriptor);
    }

    public String getBandFamily() {
        return bandFamily;
    }

    public void setBandFamily(String bandFamily) {
        this.bandFamily = sanitize(bandFamily);
    }

    private static String sanitize(String value) {
        return value != null ? value.trim() : "";
    }
}
