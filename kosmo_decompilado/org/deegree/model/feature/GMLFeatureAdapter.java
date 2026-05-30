/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.util.CharsetUtils
 *  org.deegree.framework.util.StringTools
 *  org.deegree.framework.util.TimeTools
 *  org.deegree.framework.xml.DOMPrinter
 *  org.deegree.framework.xml.XMLException
 *  org.deegree.io.datastore.schema.MappedFeaturePropertyType
 *  org.deegree.io.datastore.schema.MappedFeatureType
 *  org.deegree.model.feature.FeatureCollection
 *  org.deegree.model.feature.FeatureException
 *  org.deegree.model.feature.FeatureProperty
 *  org.deegree.model.feature.FeatureTupleCollection
 *  org.deegree.model.feature.Messages
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.feature.schema.PropertyType
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.ogcbase.CommonNamespaces
 */
package org.deegree.model.feature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.DOMPrinter;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.FeatureTupleCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.GMLFeatureDocument;
import org.deegree.model.feature.Messages;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class GMLFeatureAdapter {
    private static final ILogger LOG = LoggerFactory.getLogger(GMLFeatureAdapter.class);
    private static final String WFS_SCHEMA_BINDING = "http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";
    private Set<String> exportedFeatures = new HashSet<String>();
    private Set<String> localFeatures = new HashSet<String>();
    private boolean suppressXLinkOutput;
    private String schemaURL;
    private boolean nsBindingsExported;

    public GMLFeatureAdapter() {
        this.suppressXLinkOutput = false;
    }

    public GMLFeatureAdapter(String schemaURL) {
        this.suppressXLinkOutput = false;
        if (schemaURL != null) {
            this.schemaURL = StringTools.replace((String)schemaURL, (String)"&", (String)"&amp;", (boolean)true);
        }
    }

    public GMLFeatureAdapter(boolean suppressXLinkOutput) {
        this.suppressXLinkOutput = suppressXLinkOutput;
    }

    public GMLFeatureAdapter(boolean suppressXLinkOutput, String schemaURL) {
        this.suppressXLinkOutput = suppressXLinkOutput;
        if (schemaURL != null) {
            this.schemaURL = StringTools.replace((String)schemaURL, (String)"&", (String)"&amp;", (boolean)true);
        }
    }

    public void append(Element root, Feature feature, String formatType) throws FeatureException, IOException, SAXException {
        GMLFeatureDocument doc = this.export(feature, formatType);
        XMLTools.insertNodeInto(doc.getRootElement(), root);
    }

    public GMLFeatureDocument export(Feature feature, String formatType) throws IOException, FeatureException, XMLException, SAXException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(20000);
        this.export(feature, bos, formatType);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        bos.close();
        GMLFeatureDocument doc = new GMLFeatureDocument();
        doc.load(bis, "http://www.deegree.org");
        return doc;
    }

    public void append(Element root, FeatureCollection fc) throws FeatureException, IOException, SAXException {
        GMLFeatureCollectionDocument doc = this.exportFC(fc);
        XMLTools.insertNodeInto(doc.getRootElement(), root);
    }

    public GMLFeatureCollectionDocument exportFC(FeatureCollection fc) throws IOException, FeatureException, XMLException, SAXException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(20000);
        this.exportFC(fc, bos);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        bos.close();
        GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
        doc.load(bis, "http://www.deegree.org");
        return doc;
    }

    public void exportFC(FeatureCollection fc, OutputStream os) throws IOException, FeatureException {
        this.exportFC(fc, os, CharsetUtils.getSystemCharset(), CharsetUtils.getSystemCharset());
    }

    public void exportFC(FeatureCollection fc, OutputStream os, String charsetName, String formatType) throws IOException, FeatureException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, charsetName));
        pw.println("<?xml version=\"1.0\" encoding=\"" + charsetName + "\"?>");
        if (fc instanceof FeatureTupleCollection) {
            this.exportTupleCollection((FeatureTupleCollection)fc, pw, formatType);
        } else {
            this.exportRootCollection(fc, pw, formatType);
        }
        pw.close();
    }

    private void exportRootCollection(FeatureCollection fc, PrintWriter pw, String formatType) throws FeatureException {
        String fid;
        Set<Feature> additionalRootLevelFeatures = this.determineAdditionalRootLevelFeatures(fc);
        if (this.suppressXLinkOutput && additionalRootLevelFeatures.size() > 0) {
            String msg = Messages.getString((String)"ERROR_REFERENCE_TYPE");
            throw new FeatureException(msg);
        }
        if (fc.getId() != null && !"".equals(fc.getId())) {
            this.exportedFeatures.add(fc.getId());
        }
        pw.print("<");
        pw.print(fc.getName().getPrefixedName());
        if (fc.size() > 0) {
            int hackedFeatureCount = fc.size() + additionalRootLevelFeatures.size();
            fc.setAttribute("numberOfFeatures", "" + hackedFeatureCount);
        }
        Map attributes = fc.getAttributes();
        for (String name : attributes.keySet()) {
            String value = (String)attributes.get(name);
            pw.print(' ');
            pw.print(name);
            pw.print("='");
            pw.print(value);
            pw.print("'");
        }
        Map<String, URI> nsBindings = this.determineUsedNSBindings(fc);
        nsBindings.put("gml", CommonNamespaces.GMLNS);
        nsBindings.put("xlink", CommonNamespaces.XLNNS);
        if (this.schemaURL != null) {
            nsBindings.put("xsi", CommonNamespaces.XSINS);
        }
        this.appendNSBindings(nsBindings, pw);
        if (this.schemaURL != null && fc.size() > 0) {
            pw.print(" xsi:schemaLocation=\"" + fc.getFeature(0).getName().getNamespace() + " ");
            pw.print(String.valueOf(this.schemaURL) + " ");
            pw.print("http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\"");
        }
        pw.print('>');
        Envelope env = null;
        try {
            env = fc.getBoundedBy();
        }
        catch (GeometryException value) {
            // empty catch block
        }
        if (env != null) {
            pw.print("<gml:boundedBy><gml:Envelope");
            if (env.getCoordinateSystem() != null) {
                pw.print(" srsName='" + env.getCoordinateSystem().getPrefixedName() + "'");
            }
            pw.print("><gml:pos srsDimension='2'>");
            pw.print(env.getMin().getX());
            pw.print(' ');
            pw.print(env.getMin().getY());
            pw.print("</gml:pos><gml:pos srsDimension='2'>");
            pw.print(env.getMax().getX());
            pw.print(' ');
            pw.print(env.getMax().getY());
            pw.print("</gml:pos></gml:Envelope></gml:boundedBy>");
        }
        int i = 0;
        while (i < fc.size()) {
            Feature feature = fc.getFeature(i);
            fid = feature.getId();
            if (fid != null && !fid.equals("") && this.exportedFeatures.contains(fid) && !this.suppressXLinkOutput) {
                pw.print("<gml:featureMember xlink:href=\"#");
                pw.print(fid);
                pw.print("\"/>");
            } else {
                pw.print("<gml:featureMember>");
                this.export(feature, pw, formatType);
                pw.print("</gml:featureMember>");
            }
            ++i;
        }
        for (Feature feature : additionalRootLevelFeatures) {
            fid = feature.getId();
            if (fid != null && !fid.equals("") && this.exportedFeatures.contains(fid)) {
                pw.print("<gml:featureMember xlink:href=\"#");
                pw.print(fid);
                pw.print("\"/>");
                continue;
            }
            pw.print("<gml:featureMember>");
            this.export(feature, pw, formatType);
            pw.print("</gml:featureMember>");
        }
        pw.print("</");
        pw.print(fc.getName().getPrefixedName());
        pw.print('>');
    }

    private Set<Feature> determineAdditionalRootLevelFeatures(FeatureCollection fc) {
        HashSet<Feature> rootFeatures = new HashSet<Feature>(fc.size());
        int i = 0;
        while (i < fc.size()) {
            rootFeatures.add(fc.getFeature(i));
            ++i;
        }
        HashSet<Feature> additionalRootFeatures = new HashSet<Feature>();
        HashSet<Feature> checkedFeatures = new HashSet<Feature>();
        int i2 = 0;
        while (i2 < fc.size()) {
            Feature feature = fc.getFeature(i2);
            this.determineAdditionalRootLevelFeatures(feature, additionalRootFeatures, rootFeatures, checkedFeatures);
            ++i2;
        }
        return additionalRootFeatures;
    }

    private void determineAdditionalRootLevelFeatures(Feature feature, Set<Feature> additionalFeatures, Set<Feature> rootFeatures, Set<Feature> checkedFeatures) {
        FeatureProperty[] featurePropertyArray = feature.getProperties();
        int n = featurePropertyArray.length;
        int n2 = 0;
        while (n2 < n) {
            Feature subFeature;
            FeatureProperty property = featurePropertyArray[n2];
            Object value = property.getValue();
            if (value instanceof Feature && !checkedFeatures.contains(subFeature = (Feature)value)) {
                if (feature.getFeatureType() != null && feature.getFeatureType() instanceof MappedFeatureType) {
                    MappedFeatureType ft = (MappedFeatureType)feature.getFeatureType();
                    MappedFeaturePropertyType pt = (MappedFeaturePropertyType)ft.getProperty(property.getName());
                    assert (pt != null);
                    if (pt.isReferenceType() && !rootFeatures.contains(subFeature)) {
                        additionalFeatures.add((Feature)value);
                    }
                }
                checkedFeatures.add(subFeature);
                this.determineAdditionalRootLevelFeatures(subFeature, additionalFeatures, rootFeatures, checkedFeatures);
            }
            ++n2;
        }
    }

    private void exportTupleCollection(FeatureTupleCollection fc, PrintWriter pw, String formatType) throws FeatureException {
        if (fc.getId() != null && !"".equals(fc.getId())) {
            this.exportedFeatures.add(fc.getId());
        }
        pw.print("<");
        pw.print(fc.getName().getPrefixedName());
        Map attributes = fc.getAttributes();
        for (String name : attributes.keySet()) {
            String value = (String)attributes.get(name);
            pw.print(' ');
            pw.print(name);
            pw.print("='");
            pw.print(value);
            pw.print("'");
        }
        Map<String, URI> nsBindings = this.determineUsedNSBindings((FeatureCollection)fc);
        nsBindings.put("gml", CommonNamespaces.GMLNS);
        nsBindings.put("xlink", CommonNamespaces.XLNNS);
        if (this.schemaURL != null) {
            nsBindings.put("xsi", CommonNamespaces.XSINS);
        }
        this.appendNSBindings(nsBindings, pw);
        if (this.schemaURL != null && fc.size() > 0) {
            pw.print(" xsi:schemaLocation=\"" + fc.getTuple(0)[0].getName().getNamespace() + " ");
            pw.print(String.valueOf(this.schemaURL) + " ");
            pw.print("http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\"");
        }
        pw.print('>');
        Envelope env = null;
        try {
            env = fc.getBoundedBy();
        }
        catch (GeometryException value) {
            // empty catch block
        }
        if (env != null) {
            pw.print("<gml:boundedBy><gml:Envelope");
            if (env.getCoordinateSystem() != null) {
                pw.print(" srsName='" + env.getCoordinateSystem().getPrefixedName() + "'");
            }
            pw.print("><gml:pos srsDimension='2'>");
            pw.print(env.getMin().getX());
            pw.print(' ');
            pw.print(env.getMin().getY());
            pw.print("</gml:pos><gml:pos srsDimension='2'>");
            pw.print(env.getMax().getX());
            pw.print(' ');
            pw.print(env.getMax().getY());
            pw.print("</gml:pos></gml:Envelope></gml:boundedBy>");
        }
        int i = 0;
        while (i < fc.numTuples()) {
            Feature[] features = fc.getTuple(i);
            pw.print("<gml:featureTuple>");
            Feature[] featureArray = features;
            int n = features.length;
            int n2 = 0;
            while (n2 < n) {
                Feature feature = featureArray[n2];
                this.export(feature, pw, formatType);
                ++n2;
            }
            pw.print("</gml:featureTuple>");
            ++i;
        }
        pw.print("</");
        pw.print(fc.getName().getPrefixedName());
        pw.print('>');
    }

    private Map<String, URI> determineUsedNSBindings(FeatureCollection fc) {
        HashMap<String, URI> nsBindings = new HashMap<String, URI>();
        QualifiedName name = fc.getName();
        nsBindings.put(name.getPrefix(), name.getNamespace());
        if (fc instanceof FeatureTupleCollection) {
            FeatureTupleCollection ftc = (FeatureTupleCollection)fc;
            int i = 0;
            while (i < ftc.numTuples()) {
                Feature[] features;
                Feature[] featureArray = features = ftc.getTuple(i);
                int n = features.length;
                int n2 = 0;
                while (n2 < n) {
                    Feature feature = featureArray[n2];
                    name = feature.getName();
                    nsBindings.put(name.getPrefix(), name.getNamespace());
                    ++n2;
                }
                ++i;
            }
        } else {
            int i = 0;
            while (i < fc.size()) {
                name = fc.getFeature(i).getName();
                nsBindings.put(name.getPrefix(), name.getNamespace());
                ++i;
            }
        }
        return nsBindings;
    }

    private Map<String, URI> determineUsedNSBindings(Feature feature) {
        HashMap<String, URI> nsBindings = new HashMap<String, URI>();
        QualifiedName name = feature.getName();
        nsBindings.put(name.getPrefix(), name.getNamespace());
        return nsBindings;
    }

    private void appendNSBindings(Map<String, URI> bindings, PrintWriter pw) {
        for (String prefix : bindings.keySet()) {
            URI nsURI = bindings.get(prefix);
            pw.print(" xmlns:");
            pw.print(prefix);
            pw.print("=\"");
            pw.print(nsURI);
            pw.print('\"');
        }
        this.nsBindingsExported = true;
    }

    public void export(Feature feature, OutputStream os, String formatType) throws IOException, FeatureException {
        this.export(feature, os, CharsetUtils.getSystemCharset(), formatType);
    }

    public void export(Feature feature, OutputStream os, String charsetName, String formatType) throws IOException, FeatureException {
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, charsetName));
        pw.println("<?xml version=\"1.0\" encoding=\"" + charsetName + "\"?>");
        this.export(feature, pw, formatType);
        pw.close();
    }

    private void export(Feature feature, PrintWriter pw, String formatType) throws FeatureException {
        QualifiedName ftName = feature.getName();
        String fid = feature.getId();
        if (this.suppressXLinkOutput && fid != null && !"".equals(fid)) {
            if (this.localFeatures.contains(fid)) {
                String msg = Messages.format((String)"ERROR_CYLIC_FEATURE", (Object[])new Object[]{fid});
                throw new FeatureException(msg);
            }
            this.localFeatures.add(fid);
        }
        pw.print('<');
        pw.print(ftName.getPrefixedName());
        if (fid != null) {
            this.exportedFeatures.add(fid);
            pw.print(" gml:id=\"");
            pw.print(fid);
            pw.print('\"');
        }
        if (!this.nsBindingsExported) {
            Map<String, URI> nsBindings = this.determineUsedNSBindings(feature);
            nsBindings.put("gml", CommonNamespaces.GMLNS);
            nsBindings.put("xlink", CommonNamespaces.XLNNS);
            if (this.schemaURL != null) {
                nsBindings.put("xsi", CommonNamespaces.XSINS);
            }
            this.appendNSBindings(nsBindings, pw);
        }
        pw.print('>');
        try {
            Envelope env = null;
            env = feature.getBoundedBy();
            if (env != null) {
                pw.print("<gml:boundedBy><gml:Envelope");
                if (env.getCoordinateSystem() != null) {
                    pw.print(" srsName='" + env.getCoordinateSystem().getPrefixedName() + "'");
                }
                pw.print("><gml:pos srsDimension='2'>");
                pw.print(env.getMin().getX());
                pw.print(' ');
                pw.print(env.getMin().getY());
                pw.print("</gml:pos><gml:pos srsDimension=\"2\">");
                pw.print(env.getMax().getX());
                pw.print(' ');
                pw.print(env.getMax().getY());
                pw.print("</gml:pos></gml:Envelope></gml:boundedBy>");
            }
        }
        catch (GeometryException e) {
            LOG.logError(e.getMessage(), (Throwable)e);
        }
        FeatureProperty[] properties = feature.getProperties();
        int i = 0;
        while (i < properties.length) {
            if (properties[i] != null) {
                this.exportProperty(feature, properties[i], pw, formatType);
            }
            ++i;
        }
        pw.print("</");
        pw.print(ftName.getPrefixedName());
        pw.println('>');
        if (this.suppressXLinkOutput || fid != null) {
            this.localFeatures.remove(fid);
        }
    }

    private void exportProperty(Feature feature, FeatureProperty property, PrintWriter pw, String formatType) throws FeatureException {
        QualifiedName propertyName = property.getName();
        Object value = property.getValue();
        if (value instanceof Feature) {
            Feature subfeature = (Feature)value;
            boolean isReferenceType = false;
            if (feature.getFeatureType() != null && feature.getFeatureType() instanceof MappedFeatureType) {
                MappedFeatureType ft = (MappedFeatureType)feature.getFeatureType();
                MappedFeaturePropertyType pt = (MappedFeaturePropertyType)ft.getProperty(property.getName());
                assert (pt != null);
                isReferenceType = pt.isReferenceType();
            }
            if (isReferenceType || this.exportedFeatures.contains(subfeature.getId()) && !this.suppressXLinkOutput) {
                pw.print('<');
                pw.print(propertyName.getPrefixedName());
                pw.print(" xlink:href=\"#");
                pw.print(subfeature.getId());
                pw.print("\"/>");
            } else {
                pw.print('<');
                pw.print(propertyName.getPrefixedName());
                pw.print('>');
                this.exportPropertyValue(subfeature, pw, formatType);
                pw.print("</");
                pw.print(propertyName.getPrefixedName());
                pw.print('>');
            }
        } else {
            pw.print('<');
            pw.print(propertyName.getPrefixedName());
            pw.print('>');
            if (value != null) {
                FeatureType ft = feature.getFeatureType();
                PropertyType pt = ft.getProperty(property.getName());
                if (pt.getType() == 11019) {
                    pw.print(value);
                } else {
                    this.exportPropertyValue(value, pw, formatType);
                }
            }
            pw.print("</");
            pw.print(propertyName.getPrefixedName());
            pw.print('>');
        }
    }

    private void exportPropertyValue(Object value, PrintWriter pw, String formatType) throws FeatureException {
        if (value instanceof Feature) {
            this.export((Feature)value, pw, formatType);
        } else if (value instanceof Feature[]) {
            Feature[] features = (Feature[])value;
            int i = 0;
            while (i < features.length) {
                this.export(features[i], pw, formatType);
                ++i;
            }
        } else if (value instanceof Envelope) {
            this.exportEnvelope((Envelope)value, pw);
        } else if (value instanceof FeatureCollection) {
            this.export((Feature)((FeatureCollection)value), pw, formatType);
        } else if (value instanceof Geometry) {
            this.exportGeometry((Geometry)value, pw, formatType);
        } else if (value instanceof java.util.Date) {
            pw.print(TimeTools.getISOFormattedTime((java.util.Date)((java.util.Date)value)));
        } else if (value instanceof Calendar) {
            pw.print(TimeTools.getISOFormattedTime((Calendar)((Calendar)value)));
        } else if (value instanceof Timestamp) {
            pw.print(TimeTools.getISOFormattedTime((java.util.Date)((Timestamp)value)));
        } else if (value instanceof Date) {
            pw.print(TimeTools.getISOFormattedTime((java.util.Date)((Date)value)));
        } else if (value instanceof Integer || value instanceof Long || value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            pw.print(value.toString());
        } else if (value instanceof String) {
            StringBuffer sb = DOMPrinter.validateCDATA((String)((String)value));
            pw.print(sb);
        } else if (value instanceof Boolean) {
            pw.print(value);
        } else {
            LOG.logInfo("Unhandled property class '" + value.getClass() + "' in GMLFeatureAdapter.");
            StringBuffer sb = DOMPrinter.validateCDATA((String)value.toString());
            pw.print(sb);
        }
    }

    private void exportGeometry(Geometry geo, PrintWriter pw, String formatType) throws FeatureException {
        try {
            pw.print(GMLGeometryAdapter.export(geo, formatType));
        }
        catch (Exception e) {
            LOG.logError("", (Throwable)e);
            throw new FeatureException("Could not export geometry to GML: " + e.getMessage(), e);
        }
    }

    private void exportEnvelope(Envelope geo, PrintWriter pw) throws FeatureException {
        try {
            pw.print(GMLGeometryAdapter.exportAsBox(geo));
        }
        catch (Exception e) {
            throw new FeatureException("Could not export envelope to GML: " + e.getMessage(), e);
        }
    }
}

