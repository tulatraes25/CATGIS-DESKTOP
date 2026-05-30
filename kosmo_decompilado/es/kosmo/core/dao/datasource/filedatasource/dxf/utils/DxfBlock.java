/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.core.dao.datasource.filedatasource.dxf.utils;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.saig.core.model.feature.Attribute;

public class DxfBlock {
    protected List<Feature> features = new ArrayList<Feature>();
    protected String name;
    protected double basePointX;
    protected double basePointY;
    protected double basePointZ;
    protected Integer flags;
    protected Map<Attribute, String> attributes = new HashMap<Attribute, String>();

    public List<Feature> getFeatures() {
        return this.features;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBasePointX() {
        return this.basePointX;
    }

    public void setBasePointX(double basePointX) {
        this.basePointX = basePointX;
    }

    public double getBasePointY() {
        return this.basePointY;
    }

    public void setBasePointY(double basePointY) {
        this.basePointY = basePointY;
    }

    public double getBasePointZ() {
        return this.basePointZ;
    }

    public void setBasePointZ(double basePointZ) {
        this.basePointZ = basePointZ;
    }

    public Integer getFlags() {
        return this.flags;
    }

    public void setFlags(Integer flags) {
        this.flags = flags;
    }

    public void addFeature(Feature f) {
        this.features.add(f);
    }

    public void addAttribute(Attribute attr, String defaultValue) {
        this.attributes.put(attr, defaultValue);
    }

    public void assignAttributes(Feature feat, FeatureSchema schema) {
        for (Attribute attr : this.attributes.keySet()) {
            if (schema.hasAttribute(attr.getName())) continue;
            schema.addAttribute(attr.getName(), attr.getPublicName(), attr.isVisibility(), attr.getType(), Boolean.FALSE);
            String defaultValue = this.attributes.get(attr);
            feat.setAttribute(attr.getName(), (Object)defaultValue);
        }
    }

    public Set<Attribute> getAttributes() {
        return this.attributes.keySet();
    }
}

