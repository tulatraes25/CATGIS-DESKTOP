/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.io.datastore.PropertyPathResolvingException
 *  org.deegree.model.feature.DefaultFeatureCollection
 *  org.deegree.model.feature.FeatureException
 *  org.deegree.model.feature.FeatureFactory
 *  org.deegree.model.feature.FeatureProperty
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.feature.schema.PropertyType
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryImpl
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcbase.PropertyPath
 */
package org.deegree.model.feature;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.AbstractFeature;
import org.deegree.model.feature.DefaultFeatureCollection;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;

public class DefaultFeature
extends AbstractFeature
implements Serializable {
    private static final long serialVersionUID = -1636744791597960465L;
    protected Map<QualifiedName, Object> propertyMap;
    protected FeatureProperty[] properties;
    protected Object[] propertyValues;
    protected Geometry[] geometryPropertyValues;

    protected DefaultFeature(String id, FeatureType featureType, FeatureProperty[] properties) {
        this(id, featureType, properties, null);
    }

    protected DefaultFeature(String id, FeatureType featureType, FeatureProperty[] properties, FeatureProperty owner) {
        super(id, featureType, owner);
        int i = 0;
        while (i < properties.length) {
            PropertyType propertyType;
            FeatureProperty property = properties[i];
            URI namespace = property.getName().getNamespace();
            if (namespace == null ? (propertyType = featureType.getProperty(property.getName())) == null : !namespace.equals(CommonNamespaces.GMLNS) && (propertyType = featureType.getProperty(property.getName())) == null) {
                throw new IllegalArgumentException("Unknown property '" + property.getName() + "' for feature with type '" + featureType.getName() + "': the feature type has no such property.");
            }
            ++i;
        }
        this.properties = properties;
    }

    public void validate() throws FeatureException {
        if (this.propertyMap == null) {
            this.propertyMap = this.buildPropertyMap();
        }
        PropertyType[] propertyTypes = this.featureType.getProperties();
        int i = 0;
        while (i < propertyTypes.length) {
            List propertyList = (List)this.propertyMap.get(propertyTypes[i].getName());
            if (propertyList == null) {
                if (propertyTypes[i].getMinOccurs() != 0) {
                    throw new FeatureException("Feature is not a valid instance of type '" + this.featureType.getName() + "', mandatory property '" + propertyTypes[i].getName() + "' is missing.");
                }
            } else {
                if (propertyTypes[i].getMinOccurs() > propertyList.size()) {
                    throw new FeatureException("Feature is not a valid instance of type '" + this.featureType.getName() + "', property '" + propertyTypes[i].getName() + "' has minOccurs=" + propertyTypes[i].getMinOccurs() + ", but is only present " + propertyList.size() + " times.");
                }
                if (propertyTypes[i].getMaxOccurs() != -1 && propertyTypes[i].getMaxOccurs() < propertyList.size()) {
                    throw new FeatureException("Feature is not a valid instance of type '" + this.featureType.getName() + "', property '" + propertyTypes[i].getName() + "' has maxOccurs=" + propertyTypes[i].getMaxOccurs() + ", but is present " + propertyList.size() + " times.");
                }
            }
            ++i;
        }
    }

    private Map<QualifiedName, Object> buildPropertyMap() {
        HashMap<QualifiedName, Object> propertyMap = new HashMap<QualifiedName, Object>();
        int i = 0;
        while (i < this.properties.length) {
            ArrayList<FeatureProperty> propertyList = (ArrayList<FeatureProperty>)propertyMap.get(this.properties[i].getName());
            if (propertyList == null) {
                propertyList = new ArrayList<FeatureProperty>();
            }
            propertyList.add(this.properties[i]);
            propertyMap.put(this.properties[i].getName(), propertyList);
            ++i;
        }
        for (QualifiedName propertyName : propertyMap.keySet()) {
            List propertyList = (List)propertyMap.get(propertyName);
            propertyMap.put(propertyName, propertyList.toArray(new FeatureProperty[propertyList.size()]));
        }
        return propertyMap;
    }

    private Geometry[] extractGeometryPropertyValues() {
        ArrayList<Object> geometryPropertiesList = new ArrayList<Object>();
        int i = 0;
        while (i < this.properties.length) {
            if (this.properties[i].getValue() instanceof Geometry) {
                geometryPropertiesList.add(this.properties[i].getValue());
            } else if (this.properties[i].getValue() instanceof Object[]) {
                Object[] objects = (Object[])this.properties[i].getValue();
                int j = 0;
                while (j < objects.length) {
                    if (objects[j] instanceof Geometry) {
                        geometryPropertiesList.add(objects[j]);
                    }
                    ++j;
                }
            }
            ++i;
        }
        Geometry[] geometryPropertyValues = new Geometry[geometryPropertiesList.size()];
        geometryPropertyValues = geometryPropertiesList.toArray(geometryPropertyValues);
        return geometryPropertyValues;
    }

    @Override
    public FeatureProperty[] getProperties() {
        return this.properties;
    }

    @Override
    public FeatureProperty[] getProperties(QualifiedName name) {
        if (this.propertyMap == null) {
            this.propertyMap = this.buildPropertyMap();
        }
        FeatureProperty[] properties = (FeatureProperty[])this.propertyMap.get(name);
        return properties;
    }

    @Override
    public FeatureProperty getDefaultProperty(PropertyPath path) throws PropertyPathResolvingException {
        Feature currentFeature = this;
        FeatureProperty currentProperty = null;
        int firstPropIdx = 0;
        if (path.getStep(0).getPropertyName().equals((Object)this.getName())) {
            firstPropIdx = 1;
        }
        int i = firstPropIdx;
        while (i < path.getSteps()) {
            QualifiedName propertyName = path.getStep(i).getPropertyName();
            currentProperty = currentFeature.getDefaultProperty(propertyName);
            if (i + 1 < path.getSteps()) {
                QualifiedName featureName = path.getStep(i + 1).getPropertyName();
                Object value = currentProperty.getValue();
                if (!(value instanceof Feature)) {
                    String msg = "PropertyPath '" + path + "' cannot be matched to feature. Value of property '" + propertyName + "' is not a feature, but the path does not stop there.";
                    throw new PropertyPathResolvingException(msg);
                }
                currentFeature = (Feature)value;
                if (!featureName.equals((Object)currentFeature.getName())) {
                    String msg = "PropertyPath '" + path + "' cannot be matched to feature. Property '" + propertyName + "' contains a feature with name '" + currentFeature.getName() + "', but requested was: '" + featureName + "'.";
                    throw new PropertyPathResolvingException(msg);
                }
            }
            i += 2;
        }
        return currentProperty;
    }

    @Override
    public FeatureProperty getDefaultProperty(QualifiedName name) {
        FeatureProperty[] properties = this.getProperties(name);
        if (properties != null) {
            return properties[0];
        }
        return null;
    }

    @Override
    @Deprecated
    public FeatureProperty[] getProperties(int index) {
        QualifiedName s = this.featureType.getPropertyName(index);
        return this.getProperties(s);
    }

    @Override
    public Geometry[] getGeometryPropertyValues() {
        this.geometryPropertyValues = this.extractGeometryPropertyValues();
        return this.geometryPropertyValues;
    }

    @Override
    public Geometry getDefaultGeometryPropertyValue() {
        Geometry[] geometryValues = this.getGeometryPropertyValues();
        if (geometryValues.length < 1) {
            return null;
        }
        return geometryValues[0];
    }

    @Override
    public void setProperty(FeatureProperty property, int index) {
        if ("boundedBy".equals(property.getName().getLocalName())) {
            if (property.getValue() instanceof Envelope) {
                this.envelope = (Envelope)property.getValue();
                this.envelopeCalculated = true;
            }
            return;
        }
        FeatureProperty[] oldProperties = this.getProperties(property.getName());
        if (oldProperties == null) {
            throw new IllegalArgumentException("Cannot set property '" + property.getName() + "': feature has no property with that name.");
        }
        if (index > oldProperties.length - 1) {
            throw new IllegalArgumentException("Cannot set property '" + property.getName() + "' with index " + index + ": feature has only " + oldProperties.length + " properties with that name.");
        }
        oldProperties[index].setValue(property.getValue());
        this.geometryPropertyValues = this.extractGeometryPropertyValues();
    }

    @Override
    public void addProperty(FeatureProperty property) {
        FeatureProperty[] newProperties;
        if (this.properties == null) {
            newProperties = new FeatureProperty[]{property};
        } else {
            newProperties = new FeatureProperty[this.properties.length + 1];
            int i = 0;
            while (i < this.properties.length) {
                newProperties[i] = this.properties[i];
                ++i;
            }
            newProperties[this.properties.length] = property;
        }
        this.properties = newProperties;
        this.propertyMap = this.buildPropertyMap();
        this.geometryPropertyValues = this.extractGeometryPropertyValues();
    }

    @Override
    public void removeProperty(QualifiedName propertyName) {
        ArrayList<FeatureProperty> newProperties = new ArrayList<FeatureProperty>(this.properties.length);
        FeatureProperty[] featurePropertyArray = this.properties;
        int n = this.properties.length;
        int n2 = 0;
        while (n2 < n) {
            FeatureProperty property = featurePropertyArray[n2];
            if (!property.getName().equals((Object)propertyName)) {
                newProperties.add(property);
            }
            ++n2;
        }
        this.properties = newProperties.toArray(new FeatureProperty[newProperties.size()]);
        this.propertyMap = this.buildPropertyMap();
        this.geometryPropertyValues = this.extractGeometryPropertyValues();
    }

    @Override
    public void replaceProperty(FeatureProperty oldProperty, FeatureProperty newProperty) {
        int i = 0;
        while (i < this.properties.length) {
            if (this.properties[i] == oldProperty) {
                this.properties[i] = newProperty;
            }
            ++i;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FeatureProperty[] fp = new FeatureProperty[this.properties.length];
        int i = 0;
        while (i < fp.length) {
            Object v;
            if (this.properties[i].getValue() instanceof DefaultFeatureCollection) {
                v = ((DefaultFeatureCollection)this.properties[i].getValue()).clone();
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)v);
            } else if (this.properties[i].getValue() instanceof DefaultFeature) {
                v = ((DefaultFeature)this.properties[i].getValue()).clone();
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)v);
            } else {
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)this.properties[i].getValue());
            }
            ++i;
        }
        return FeatureFactory.createFeature((String)("UUID_" + UUID.randomUUID().toString()), (FeatureType)this.featureType, (FeatureProperty[])fp);
    }

    @Override
    public Feature cloneDeep() throws CloneNotSupportedException {
        FeatureProperty[] fp = new FeatureProperty[this.properties.length];
        int i = 0;
        while (i < fp.length) {
            Object v;
            if (this.properties[i].getValue() instanceof DefaultFeatureCollection) {
                v = ((DefaultFeatureCollection)this.properties[i].getValue()).clone();
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)v);
            } else if (this.properties[i].getValue() instanceof DefaultFeature) {
                v = ((DefaultFeature)this.properties[i].getValue()).clone();
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)v);
            }
            if (this.properties[i].getValue() instanceof Geometry) {
                Geometry geom = (Geometry)((GeometryImpl)this.properties[i].getValue()).clone();
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)geom);
            } else {
                fp[i] = FeatureFactory.createFeatureProperty((QualifiedName)this.properties[i].getName(), (Object)this.properties[i].getValue());
            }
            ++i;
        }
        return FeatureFactory.createFeature((String)("UUID_" + UUID.randomUUID().toString()), (FeatureType)this.featureType, (FeatureProperty[])fp);
    }

    public String toString() {
        String ret = this.getClass().getName();
        return ret;
    }
}

