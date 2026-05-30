/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.core.model.sdi.wfs;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AbstractBasicFeature;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import org.saig.core.model.feature.Attribute;

public class WFSFeature
extends BasicFeature {
    protected String gmlId;

    public WFSFeature(FeatureSchema featureSchema, String gmlId) {
        super(featureSchema);
        this.gmlId = gmlId;
    }

    public WFSFeature(Feature original, String gmlid, boolean generateID) {
        super(original.getSchema());
        this.gmlId = gmlid;
        this.setAttributes(original.getAttributes());
        if (generateID) {
            this.setID(FeatureUtil.nextID());
        } else {
            this.setID(original.getID());
        }
    }

    public WFSFeature(Feature original, String gmlid) {
        this(original, gmlid, false);
    }

    public String getGMLId() {
        return this.gmlId;
    }

    public void setGMLId(String gmlid) {
        this.gmlId = gmlid;
    }

    @Override
    public Object clone() {
        return this.clone(true);
    }

    @Override
    public Feature clone(boolean deep) {
        return this.clone(deep, false);
    }

    @Override
    public Feature clone(boolean deep, boolean generateID) {
        WFSFeature clone = new WFSFeature(this.schema, this.getGMLId());
        int i = 0;
        while (i < this.schema.getAttributeCount()) {
            if (this.schema.getAttributeType(i) == AttributeType.GEOMETRY) {
                if (deep && this.getAttribute(i) != null) {
                    clone.setAttribute(i, ((Geometry)this.getAttribute(i)).clone());
                } else {
                    clone.setAttribute(i, this.getAttribute(i));
                }
            } else {
                clone.setAttribute(i, this.getAttribute(i));
            }
            ++i;
        }
        if (this.isUnsaved() && generateID) {
            clone.setID(FeatureUtil.nextID());
        } else {
            clone.setID(this.getID());
        }
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(WFSFeature.class)) {
            return false;
        }
        AbstractBasicFeature basicFeat = (AbstractBasicFeature)obj;
        Attribute pk = this.schema.getPrimaryKey();
        Attribute pkOther = basicFeat.getSchema().getPrimaryKey();
        if (pk == null && pkOther == null) {
            return this.getID() == basicFeat.getID();
        }
        if (pk == null && pkOther != null || pk != null && pkOther == null) {
            return false;
        }
        Object pkValue = this.getAttribute(pk.getName());
        Object otherPkValue = ((Feature)obj).getAttribute(pkOther.getName());
        if (pkValue == null && otherPkValue == null) {
            return this.getID() == basicFeat.getID();
        }
        if (pkValue == null && otherPkValue != null || pkValue != null && otherPkValue == null) {
            return false;
        }
        return pkValue.equals(otherPkValue);
    }

    @Override
    public int hashCode() {
        Attribute pk = this.schema.getPrimaryKey();
        if (pk != null) {
            Object pkValue = this.getAttribute(pk.getName());
            if (pkValue != null) {
                return pkValue.hashCode();
            }
            return new Integer(this.getID()).hashCode();
        }
        return new Integer(this.getID()).hashCode();
    }
}

