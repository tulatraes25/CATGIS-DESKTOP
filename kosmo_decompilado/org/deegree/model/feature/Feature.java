/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.io.datastore.PropertyPathResolvingException
 *  org.deegree.model.feature.FeatureProperty
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.ogcbase.PropertyPath
 */
package org.deegree.model.feature;

import java.util.Map;
import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;

public interface Feature {
    public QualifiedName getName();

    public String getDescription();

    public String getId();

    public void setId(String var1);

    public FeatureType getFeatureType();

    public void setFeatureType(FeatureType var1);

    public FeatureProperty[] getProperties();

    public FeatureProperty getDefaultProperty(QualifiedName var1);

    public FeatureProperty getDefaultProperty(PropertyPath var1) throws PropertyPathResolvingException;

    public FeatureProperty[] getProperties(QualifiedName var1);

    @Deprecated
    public FeatureProperty[] getProperties(int var1);

    public Geometry[] getGeometryPropertyValues();

    public Geometry getDefaultGeometryPropertyValue();

    public void setProperty(FeatureProperty var1, int var2);

    public void addProperty(FeatureProperty var1);

    public void removeProperty(QualifiedName var1);

    public void replaceProperty(FeatureProperty var1, FeatureProperty var2);

    public Envelope getBoundedBy() throws GeometryException;

    public FeatureProperty getOwner();

    public String getAttribute(String var1);

    public Map<String, String> getAttributes();

    public void setAttribute(String var1, String var2);

    public void setEnvelopesUpdated();

    public Object clone() throws CloneNotSupportedException;

    public Feature cloneDeep() throws CloneNotSupportedException;
}

