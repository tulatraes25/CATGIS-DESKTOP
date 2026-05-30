/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.model.feature.FeatureProperty
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.model.spatialschema.GeometryFactory
 *  org.deegree.model.spatialschema.Point
 *  org.deegree.model.spatialschema.Position
 */
package org.deegree.model.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.deegree.datatypes.QualifiedName;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;

abstract class AbstractFeature
implements Feature {
    private String id;
    protected FeatureType featureType;
    protected Envelope envelope;
    protected boolean envelopeCalculated;
    protected String description;
    protected FeatureProperty owner;
    private Map<String, String> attributeMap = new HashMap<String, String>();

    AbstractFeature(String id, FeatureType featureType) {
        this.id = id;
        this.featureType = featureType;
    }

    AbstractFeature(String id, FeatureType featureType, FeatureProperty owner) {
        this.id = id;
        this.featureType = featureType;
        this.owner = owner;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public QualifiedName getName() {
        return this.featureType.getName();
    }

    @Override
    public Envelope getBoundedBy() throws GeometryException {
        if (!this.envelopeCalculated) {
            this.getBoundedBy(new HashSet<Feature>());
        }
        return this.envelope;
    }

    @Override
    public void setEnvelopesUpdated() {
        this.envelopeCalculated = false;
    }

    @Override
    public FeatureProperty getOwner() {
        return this.owner;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String fid) {
        this.id = fid;
    }

    @Override
    public FeatureType getFeatureType() {
        return this.featureType;
    }

    @Override
    public void setFeatureType(FeatureType ft) {
        this.featureType = ft;
    }

    protected void resetBounds() {
        this.envelope = null;
    }

    @Override
    public String getAttribute(String name) {
        return this.attributeMap.get(name);
    }

    @Override
    public Map<String, String> getAttributes() {
        return this.attributeMap;
    }

    @Override
    public void setAttribute(String name, String value) {
        this.attributeMap.put(name, value);
    }

    private Envelope getBoundedBy(Set<Feature> features) throws GeometryException {
        if (!this.envelopeCalculated) {
            this.envelope = this.calcEnvelope(features);
            this.envelopeCalculated = true;
        }
        return this.envelope;
    }

    private Envelope calcEnvelope(Set<Feature> features) throws GeometryException {
        FeatureProperty[] props;
        Envelope combinedEnvelope = null;
        if (features.contains(this)) {
            return combinedEnvelope;
        }
        features.add(this);
        FeatureProperty[] featurePropertyArray = props = this.getProperties();
        int n = props.length;
        int n2 = 0;
        while (n2 < n) {
            FeatureProperty prop = featurePropertyArray[n2];
            if (prop != null) {
                Object propValue = prop.getValue();
                if (propValue instanceof Geometry) {
                    Geometry geom = (Geometry)propValue;
                    Envelope env = null;
                    env = geom instanceof Point ? GeometryFactory.createEnvelope((Position)((Point)geom).getPosition(), (Position)((Point)geom).getPosition(), (CoordinateSystem)geom.getCoordinateSystem()) : geom.getEnvelope();
                    combinedEnvelope = combinedEnvelope == null ? env : combinedEnvelope.merge(env);
                } else if (propValue instanceof AbstractFeature) {
                    Envelope subEnvelope = ((AbstractFeature)propValue).getBoundedBy(features);
                    if (combinedEnvelope == null) {
                        combinedEnvelope = subEnvelope;
                    } else if (subEnvelope != null) {
                        combinedEnvelope = combinedEnvelope.merge(subEnvelope);
                    }
                }
            }
            ++n2;
        }
        return combinedEnvelope;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

