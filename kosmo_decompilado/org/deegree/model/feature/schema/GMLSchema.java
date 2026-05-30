/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.datatypes.UnknownTypeException
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.framework.xml.schema.ComplexTypeDeclaration
 *  org.deegree.framework.xml.schema.ElementDeclaration
 *  org.deegree.framework.xml.schema.SimpleTypeDeclaration
 *  org.deegree.framework.xml.schema.UndefinedXSDTypeException
 *  org.deegree.framework.xml.schema.XMLSchema
 *  org.deegree.framework.xml.schema.XMLSchemaException
 *  org.deegree.model.crs.UnknownCRSException
 *  org.deegree.model.feature.FeatureFactory
 *  org.deegree.model.feature.schema.ComplexPropertyType
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.feature.schema.GeometryPropertyType
 *  org.deegree.model.feature.schema.PropertyType
 *  org.deegree.model.feature.schema.SimplePropertyType
 *  org.deegree.ogcbase.CommonNamespaces
 */
package org.deegree.model.feature.schema;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.SimpleTypeDeclaration;
import org.deegree.framework.xml.schema.UndefinedXSDTypeException;
import org.deegree.framework.xml.schema.XMLSchema;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.schema.ComplexPropertyType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.ogcbase.CommonNamespaces;
import org.saig.jump.lang.I18N;

public class GMLSchema
extends XMLSchema {
    private static final ILogger LOG = LoggerFactory.getLogger(GMLSchema.class);
    private static URI XSDNS = CommonNamespaces.XSNS;
    private static URI GMLNS = CommonNamespaces.GMLNS;
    private static final QualifiedName ABSTRACT_FEATURE = new QualifiedName("_Feature", GMLNS);
    protected Map<QualifiedName, FeatureType> featureTypeMap = new HashMap<QualifiedName, FeatureType>();
    protected Map<FeatureType, List<FeatureType>> substitutionMap = new HashMap<FeatureType, List<FeatureType>>();

    public GMLSchema(URI targetNamespace, SimpleTypeDeclaration[] simpleTypes, ComplexTypeDeclaration[] complexTypes, ElementDeclaration[] elementDeclarations) throws XMLParsingException, UnknownCRSException {
        super(targetNamespace, simpleTypes, complexTypes, elementDeclarations);
        this.buildFeatureTypeMap(elementDeclarations);
        this.buildSubstitutionMap(elementDeclarations);
    }

    protected GMLSchema(ElementDeclaration[] elementDeclarations, URI targetNamespace, SimpleTypeDeclaration[] simpleTypes, ComplexTypeDeclaration[] complexTypes) throws XMLSchemaException {
        super(targetNamespace, simpleTypes, complexTypes, elementDeclarations);
    }

    public FeatureType[] getFeatureTypes() {
        return this.featureTypeMap.values().toArray(new FeatureType[this.featureTypeMap.size()]);
    }

    public FeatureType getFeatureType(QualifiedName qName) {
        return this.featureTypeMap.get(qName);
    }

    public FeatureType getFeatureType(String localName) {
        return this.getFeatureType(new QualifiedName(localName, this.getTargetNamespace()));
    }

    public boolean hasSeveralImplementations(FeatureType ft) {
        return this.getSubstitutions(ft).length > 1;
    }

    public FeatureType[] getSubstitutions(FeatureType featureType) {
        FeatureType[] substitutions = new FeatureType[]{};
        List<FeatureType> featureTypeList = this.substitutionMap.get(featureType);
        if (featureTypeList != null) {
            substitutions = featureTypeList.toArray(new FeatureType[featureTypeList.size()]);
        }
        return substitutions;
    }

    public boolean isValidSubstitution(FeatureType ft, FeatureType substitution) {
        FeatureType[] substitutions = this.getSubstitutions(ft);
        int i = 0;
        while (i < substitutions.length) {
            if (substitutions[i].getName().equals((Object)substitution.getName())) {
                return true;
            }
            ++i;
        }
        return false;
    }

    public Set<FeatureType> getSubstitutables(FeatureType substitution) {
        FeatureType[] allFts;
        HashSet<FeatureType> ftSet = new HashSet<FeatureType>();
        FeatureType[] featureTypeArray = allFts = this.getFeatureTypes();
        int n = allFts.length;
        int n2 = 0;
        while (n2 < n) {
            FeatureType ft = featureTypeArray[n2];
            if (this.isValidSubstitution(ft, substitution)) {
                ftSet.add(ft);
            }
            ++n2;
        }
        return ftSet;
    }

    protected void buildFeatureTypeMap(ElementDeclaration[] elementDeclarations) throws XMLParsingException, UnknownCRSException {
        int i = 0;
        while (i < elementDeclarations.length) {
            LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.is-element")) + " '" + elementDeclarations[i].getName() + "' " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.a-feature-type-definition"));
            if (elementDeclarations[i].isSubstitutionFor(ABSTRACT_FEATURE)) {
                LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.yes")) + ".");
                FeatureType featureType = this.buildFeatureType(elementDeclarations[i]);
                this.featureTypeMap.put(featureType.getName(), featureType);
            } else {
                LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.no")) + ".");
            }
            ++i;
        }
    }

    protected void buildSubstitutionMap(ElementDeclaration[] elementDeclarations) {
        for (FeatureType featureType : this.featureTypeMap.values()) {
            ArrayList<FeatureType> substitutionList = new ArrayList<FeatureType>();
            LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.collecting-possible-substitutions-for-feature-type")) + " '" + featureType.getName() + "'.");
            int i = 0;
            while (i < elementDeclarations.length) {
                if (elementDeclarations[i].isAbstract()) {
                    LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.skipping")) + " '" + elementDeclarations[i].getName() + "' " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.as-it-is-abstract"));
                } else if (elementDeclarations[i].isSubstitutionFor(featureType.getName())) {
                    LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.feture-type")) + " '" + elementDeclarations[i].getName() + "' " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.is-a-concrete-substitution-for-feature-type") + " '" + featureType.getName() + "'.");
                    FeatureType substitution = this.featureTypeMap.get(elementDeclarations[i].getName());
                    substitutionList.add(substitution);
                }
                ++i;
            }
            this.substitutionMap.put(featureType, substitutionList);
        }
    }

    protected FeatureType buildFeatureType(ElementDeclaration element) throws XMLParsingException, UnknownCRSException {
        LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.building-feature-type-from-element-declaration")) + " '" + element.getName() + "'...");
        QualifiedName name = new QualifiedName(element.getName().getLocalName(), this.getTargetNamespace());
        ComplexTypeDeclaration complexType = (ComplexTypeDeclaration)element.getType().getTypeDeclaration();
        ElementDeclaration[] subElements = complexType.getElements();
        PropertyType[] properties = new PropertyType[subElements.length];
        int i = 0;
        while (i < properties.length) {
            properties[i] = this.buildPropertyType(subElements[i]);
            ++i;
        }
        return FeatureFactory.createFeatureType((QualifiedName)name, (boolean)element.isAbstract(), (PropertyType[])properties);
    }

    protected PropertyType buildPropertyType(ElementDeclaration element) throws XMLSchemaException {
        SimplePropertyType propertyType = null;
        QualifiedName propertyName = new QualifiedName(element.getName().getLocalName(), this.getTargetNamespace());
        QualifiedName typeName = element.getType().getName();
        int type = this.determinePropertyType(element);
        if (typeName == null) {
            throw new XMLSchemaException(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.no-type-defined-for-the-property")) + " '" + propertyName + "'. " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.no-inline-definitions-supported") + ".");
        }
        if (typeName.isInNamespace(XSDNS)) {
            propertyType = FeatureFactory.createSimplePropertyType((QualifiedName)propertyName, (int)type, (int)element.getMinOccurs(), (int)element.getMaxOccurs());
        } else {
            switch (type) {
                case 10014: {
                    propertyType = FeatureFactory.createFeaturePropertyType((QualifiedName)propertyName, (int)element.getMinOccurs(), (int)element.getMaxOccurs());
                    break;
                }
                case 10012: 
                case 10013: 
                case 11012: 
                case 11013: 
                case 11014: 
                case 11015: 
                case 11016: 
                case 11017: {
                    propertyType = new GeometryPropertyType(propertyName, typeName, type, element.getMinOccurs(), element.getMaxOccurs());
                    break;
                }
                default: {
                    propertyType = FeatureFactory.createSimplePropertyType((QualifiedName)propertyName, (int)type, (int)element.getMinOccurs(), (int)element.getMaxOccurs());
                }
            }
        }
        return propertyType;
    }

    protected final int determinePropertyType(ElementDeclaration element) throws UndefinedXSDTypeException {
        QualifiedName typeName = element.getType().getName();
        LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.determining-property-type-code-for-property-type")) + "='" + typeName + "'...");
        int type = 10014;
        if (element.getType().isAnonymous()) {
            LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.inline-declaration")) + ". " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.assuming-generic-gml-feature-of-some-kind") + ".");
        } else {
            if (typeName.isInNamespace(XSDNS)) {
                LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.must-be-a-basic-xsd-type")) + ".");
                try {
                    type = Types.getJavaTypeForXSDType(typeName.getLocalName());
                }
                catch (UnknownTypeException e) {
                    throw new UndefinedXSDTypeException(e.getMessage(), (Throwable)e);
                }
            }
            if (typeName.isInNamespace(GMLNS)) {
                LOG.logDebug(I18N.getString("org.deegree.model.feature.schema.GMLSchema.maybe-a-geometry-property-type"));
                try {
                    type = Types.getJavaTypeForGMLType(typeName.getLocalName());
                    LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.yes")) + ".");
                }
                catch (UnknownTypeException e) {
                    LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.no")) + ". " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.must-be-a-generic-gml-feature-of-some-kind") + ".");
                }
            } else {
                LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.should-be-a-primitive-type-in-our-own-namespace")) + ".");
                if (!typeName.isInNamespace(this.getTargetNamespace())) {
                    try {
                        type = Types.getJavaTypeForXSDType(typeName.getLocalName());
                    }
                    catch (UnknownTypeException e) {
                        throw new UndefinedXSDTypeException(e.getMessage(), (Throwable)e);
                    }
                    return type;
                }
                SimpleTypeDeclaration simpleType = this.getSimpleTypeDeclaration(typeName);
                if (simpleType == null) {
                    throw new UndefinedXSDTypeException(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.simple-type")) + " '" + typeName + "' " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.cannot-be-resolved") + ".");
                }
                typeName = simpleType.getRestrictionBaseType().getName();
                LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.feature.schema.GMLSchema.simple-base-type")) + ": '" + typeName + "'. " + I18N.getString("org.deegree.model.feature.schema.GMLSchema.must-be-a-basic-xsd-type") + ".");
                try {
                    type = Types.getJavaTypeForXSDType(typeName.getLocalName());
                }
                catch (UnknownTypeException e) {
                    throw new UndefinedXSDTypeException((Throwable)e);
                }
            }
        }
        return type;
    }

    public String toString() {
        Map<FeatureType, List<FeatureType>> substitutesMap = this.buildSubstitutesMap();
        StringBuffer sb = new StringBuffer("GML schema targetNamespace='");
        sb.append(this.getTargetNamespace());
        sb.append("'\n");
        sb.append("\n*** ");
        sb.append(this.featureTypeMap.size());
        sb.append(" feature type declarations ***\n");
        Iterator<FeatureType> featureTypeIter = this.featureTypeMap.values().iterator();
        while (featureTypeIter.hasNext()) {
            FeatureType featureType = featureTypeIter.next();
            sb.append(this.featureTypeToString(featureType, substitutesMap));
            if (!featureTypeIter.hasNext()) continue;
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private Map<FeatureType, List<FeatureType>> buildSubstitutesMap() {
        HashMap<FeatureType, List<FeatureType>> substitutesMap = new HashMap<FeatureType, List<FeatureType>>();
        FeatureType[] featureTypeArray = this.getFeatureTypes();
        int n = featureTypeArray.length;
        int n2 = 0;
        while (n2 < n) {
            FeatureType ft = featureTypeArray[n2];
            ArrayList<FeatureType> substitutesList = new ArrayList<FeatureType>();
            FeatureType[] featureTypeArray2 = this.getFeatureTypes();
            int n3 = featureTypeArray2.length;
            int n4 = 0;
            while (n4 < n3) {
                FeatureType substitution = featureTypeArray2[n4];
                if (this.isValidSubstitution(substitution, ft)) {
                    substitutesList.add(substitution);
                }
                ++n4;
            }
            substitutesMap.put(ft, substitutesList);
            ++n2;
        }
        return substitutesMap;
    }

    private String featureTypeToString(FeatureType ft, Map<FeatureType, List<FeatureType>> substitutesMap) {
        StringBuffer sb = new StringBuffer("- ");
        if (ft.isAbstract()) {
            sb.append("(abstract) ");
        }
        sb.append("Feature type '");
        sb.append(ft.getName());
        sb.append("'\n");
        FeatureType[] substFTs = this.getSubstitutions(ft);
        if (substFTs.length > 0) {
            sb.append("  is implemented by: ");
            int i = 0;
            while (i < substFTs.length) {
                sb.append("'");
                sb.append(substFTs[i].getName().getLocalName());
                if (substFTs[i].isAbstract()) {
                    sb.append(" (abstract)");
                }
                sb.append("'");
                if (i != substFTs.length - 1) {
                    sb.append(",");
                } else {
                    sb.append("\n");
                }
                ++i;
            }
        } else {
            sb.append("  has no concrete implementations?!\n");
        }
        List<FeatureType> substitutesList = substitutesMap.get(ft);
        sb.append("  substitutes      : ");
        int i = 0;
        while (i < substitutesList.size()) {
            sb.append("'");
            sb.append(substitutesList.get(i).getName().getLocalName());
            if (substitutesList.get(i).isAbstract()) {
                sb.append(" (abstract)");
            }
            sb.append("'");
            if (i != substitutesList.size() - 1) {
                sb.append(",");
            }
            ++i;
        }
        sb.append("\n");
        PropertyType[] properties = ft.getProperties();
        int i2 = 0;
        while (i2 < properties.length) {
            PropertyType pt = properties[i2];
            sb.append(" + '");
            sb.append(pt.getName());
            if (pt instanceof ComplexPropertyType) {
                sb.append("', Type: '");
                sb.append(((ComplexPropertyType)pt).getTypeName());
            }
            sb.append("', SQLType: ");
            try {
                sb.append(Types.getTypeNameForSQLTypeCode(pt.getType()));
            }
            catch (UnknownTypeException e) {
                sb.append("unknown");
            }
            sb.append(", min: ");
            sb.append(pt.getMinOccurs());
            sb.append(", max: ");
            sb.append(pt.getMaxOccurs());
            if (i2 != properties.length - 1) {
                sb.append("\n");
            }
            ++i2;
        }
        return sb.toString();
    }
}

