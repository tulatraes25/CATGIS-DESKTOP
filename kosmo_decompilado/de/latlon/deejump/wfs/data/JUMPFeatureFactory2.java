/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.ArrayUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 *  org.deegree.datatypes.QualifiedName
 *  org.deegree.framework.util.IDGenerator
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.crs.UnknownCRSException
 *  org.deegree.model.feature.FeatureCollection
 *  org.deegree.model.feature.FeatureFactory
 *  org.deegree.model.feature.FeatureProperty
 *  org.deegree.model.feature.schema.FeatureType
 *  org.deegree.model.feature.schema.PropertyType
 *  org.deegree.model.filterencoding.ComplexFilter
 *  org.deegree.model.filterencoding.Filter
 *  org.deegree.model.filterencoding.Operation
 *  org.deegree.model.filterencoding.PropertyName
 *  org.deegree.model.filterencoding.SpatialOperation
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.model.spatialschema.GeometryFactory
 *  org.deegree.model.spatialschema.Surface
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcwebservices.wfs.XMLFactory
 *  org.deegree.ogcwebservices.wfs.operation.GetFeature
 *  org.deegree.ogcwebservices.wfs.operation.GetFeature$RESULT_TYPE
 *  org.deegree.ogcwebservices.wfs.operation.Query
 */
package de.latlon.deejump.wfs.data;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import de.latlon.deejump.wfs.client.AbstractWFSWrapper;
import de.latlon.deejump.wfs.client.WFSClientHelper;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wfs.XMLFactory;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.saig.core.model.feature.Attribute;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.core.model.sdi.wfs.WFSFeature;
import org.saig.core.model.sdi.wfs.WFSFeatureCollection;
import org.saig.core.model.sdi.wfs.WFSFeatureDataset;
import org.saig.core.model.sdi.wfs.WFSFeatureTypeInfo;
import org.saig.jump.lang.I18N;
import org.xml.sax.SAXException;

public class JUMPFeatureFactory2 {
    private static Logger LOGGER = Logger.getLogger(JUMPFeatureFactory2.class);
    private static int maxFeatures = 1000000000;

    public static WFSFeatureCollection[] createWFSCollectionsFromDeegreeFC(FeatureCollection deegreeFeatCollec, String pkName, String geomColumnName, WFSFeatureTypeInfo ftInfo, AbstractWFSWrapper wrapper, QualifiedName featureType) throws Exception {
        return JUMPFeatureFactory2.createWFSCollectionsFromDeegreeFC(deegreeFeatCollec, null, pkName, geomColumnName, ftInfo, wrapper, featureType);
    }

    public static com.vividsolutions.jump.feature.FeatureCollection[] createFromDeegreeFC(FeatureCollection deegreeFeatCollec, String pkName, String geomColumnName) throws Exception {
        return JUMPFeatureFactory2.createFromDeegreeFC(deegreeFeatCollec, null, pkName, geomColumnName);
    }

    public static com.vividsolutions.jump.feature.FeatureCollection[] createFromDeegreeFC(FeatureCollection deegreeFeatCollec, com.vividsolutions.jts.geom.Geometry defaultGeometry, String pkName, String geomColumnName) throws Exception {
        boolean hasPolygons;
        FeatureSchema fsBase = new FeatureSchema();
        boolean autoGeneratePK = false;
        int currentPk = 0;
        FeatureSchema pointSchema = null;
        FeatureSchema lineSchema = null;
        FeatureSchema polygonSchema = null;
        org.deegree.model.feature.Feature[] feats = deegreeFeatCollec.toArray();
        if (feats == null || feats.length < 1) {
            throw new Exception("No data found");
        }
        FeatureType ft = feats[0].getFeatureType();
        String geoProName = null;
        if (geomColumnName == null) {
            Object[] geoTypeProps = ft.getGeometryProperties();
            if (geoTypeProps.length > 1) {
                LOGGER.warn((Object)I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.this-feature-type-has-more-than-one-geometry-property-only-the-first-one-will-be-used"));
            }
            if (ArrayUtils.isEmpty((Object[])geoTypeProps)) {
                LOGGER.debug((Object)I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.guessing-geometry-property-name"));
                geoProName = "GEOMETRY";
            } else {
                geoProName = geoTypeProps[0].getName().getLocalName();
                LOGGER.debug((Object)(String.valueOf(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.geometry-property-name")) + geoProName));
            }
        } else {
            geoProName = geomColumnName;
        }
        PropertyType[] featTypeProps = ft.getProperties();
        int j = 0;
        while (j < featTypeProps.length) {
            String name = featTypeProps[j].getName().getLocalName();
            if (!geoProName.equals(name)) {
                fsBase.addAttribute(name, JUMPFeatureFactory2.findType(featTypeProps[j].getType()));
            } else {
                fsBase.addAttribute(geoProName, AttributeType.GEOMETRY);
            }
            ++j;
        }
        if (defaultGeometry == null && fsBase.getGeometryIndex() == -1) {
            throw new RuntimeException(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.no-geometry-property-found"));
        }
        if (defaultGeometry != null && fsBase.getGeometryIndex() == -1) {
            fsBase.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        }
        fsBase.setGeometryType(FeatureSchema.getGeometryType(JTSAdapter.export(feats[0].getDefaultGeometryPropertyValue())));
        if (pkName != null && fsBase.hasAttribute(pkName)) {
            fsBase.getAttribute(pkName).setPrimaryKey(true);
        } else if (pkName != null) {
            fsBase.addAttribute(pkName, AttributeType.INTEGER, Boolean.TRUE);
            autoGeneratePK = true;
        } else {
            fsBase.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        }
        pointSchema = (FeatureSchema)fsBase.clone();
        pointSchema.setGeometryType(1);
        lineSchema = (FeatureSchema)fsBase.clone();
        lineSchema.setGeometryType(3);
        polygonSchema = (FeatureSchema)fsBase.clone();
        polygonSchema.setGeometryType(5);
        FeatureDataset jumpPointFc = new FeatureDataset(pointSchema);
        FeatureDataset jumpLineFc = new FeatureDataset(lineSchema);
        FeatureDataset jumpPolygonFc = new FeatureDataset(polygonSchema);
        int i = 0;
        while (i < feats.length) {
            String geomType;
            org.deegree.model.feature.Feature currentDeegreFeat = feats[i];
            Geometry geoObject = currentDeegreFeat.getDefaultGeometryPropertyValue();
            com.vividsolutions.jts.geom.Geometry currentGeom = null;
            if (defaultGeometry == null && geoObject != null) {
                try {
                    currentGeom = JTSAdapter.export(geoObject);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    throw e;
                }
            } else {
                currentGeom = defaultGeometry;
            }
            BasicFeature jf = new BasicFeature(fsBase);
            jf.setGeometry(currentGeom);
            int geoIndex = jf.getSchema().getGeometryIndex();
            int j2 = 0;
            while (j2 < jf.getSchema().getAttributeCount()) {
                if (j2 != geoIndex) {
                    QualifiedName qn = new QualifiedName(fsBase.getAttributeName(j2), featTypeProps[0].getName().getNamespace());
                    FeatureProperty fp = currentDeegreFeat.getDefaultProperty(qn);
                    Object value = null;
                    if (fp != null) {
                        value = FeatureUtil.getGoodAttribute(fsBase.getAttribute(j2).getType(), fp.getValue());
                    }
                    jf.setAttribute(j2, value);
                }
                ++j2;
            }
            if (autoGeneratePK) {
                jf.setAttribute(pkName, (Object)new Integer(currentPk++));
            }
            if ((geomType = currentGeom.getGeometryType()).equals("Point") || geomType.equals("MultiPoint")) {
                jumpPointFc.addWithNewKey(jf);
            } else if (geomType.equals("LineString") || geomType.equals("MultiLineString")) {
                jumpLineFc.addWithNewKey(jf);
            } else if (geomType.equals("Polygon") || geomType.equals("MultiPolygon")) {
                jumpPolygonFc.addWithNewKey(jf);
            } else {
                LOGGER.warn((Object)(String.valueOf(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.ignoring-current-feature")) + jf));
            }
            ++i;
        }
        boolean hasPoints = jumpPointFc.size() > 0;
        boolean hasLines = jumpLineFc.size() > 0;
        boolean bl = hasPolygons = jumpPolygonFc.size() > 0;
        if (hasPoints) {
            if (hasLines) {
                if (hasPolygons) {
                    return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpPointFc, jumpLineFc, jumpPolygonFc};
                }
                return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpPointFc, jumpLineFc};
            }
            if (hasPolygons) {
                return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpPointFc, jumpPolygonFc};
            }
            return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpPointFc};
        }
        if (hasLines) {
            if (hasPolygons) {
                return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpLineFc, jumpPolygonFc};
            }
            return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpLineFc};
        }
        if (hasPolygons) {
            return new com.vividsolutions.jump.feature.FeatureCollection[]{jumpPolygonFc};
        }
        return null;
    }

    public static GetFeature createFeatureRequest(String version, QualifiedName qualName, Envelope envelope) throws Exception {
        String srs = null;
        CoordinateSystem cs = null;
        Surface boxGeom = null;
        try {
            boxGeom = GeometryFactory.createSurface((Envelope)envelope, cs);
        }
        catch (GeometryException e) {
            e.printStackTrace();
            throw new RuntimeException(String.valueOf(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.cannot-create-surface-from-bbox")) + e.getMessage());
        }
        SpatialOperation op = new SpatialOperation(9, new PropertyName(new QualifiedName("GEOM")), (Geometry)boxGeom);
        ComplexFilter filter = new ComplexFilter((Operation)op);
        Query query = Query.create(null, null, null, null, (String)version, (QualifiedName[])new QualifiedName[]{qualName}, srs, (Filter)filter, (int)maxFeatures, (int)0, (GetFeature.RESULT_TYPE)GetFeature.RESULT_TYPE.RESULTS);
        IDGenerator idg = IDGenerator.getInstance();
        int maxDepth = 100;
        int traverseExpiry = -999;
        GetFeature gfr = GetFeature.create((String)version, (String)("" + idg.generateUniqueID()), (GetFeature.RESULT_TYPE)GetFeature.RESULT_TYPE.RESULTS, (String)"GML3", null, (int)maxFeatures, (int)0, (int)maxDepth, (int)traverseExpiry, (Query[])new Query[]{query});
        return gfr;
    }

    public static FeatureCollection createDeegreeFCfromWFS(AbstractWFSWrapper serverUrl, GetFeature request) throws Exception {
        return JUMPFeatureFactory2.createDeegreeFCfromWFS(serverUrl, XMLFactory.export((GetFeature)request).getAsString(), null);
    }

    public static FeatureCollection createDeegreeFCfromWFS(AbstractWFSWrapper server, String request, QualifiedName featureType) throws Exception {
        String s = WFSClientHelper.createResponsefromWFS(server.getGetFeatureURL(), request, server.getBasicAuthData());
        if (s.indexOf("<Exception>") >= 0 || s.indexOf("<ServiceExceptionReport") >= 0 || s.indexOf("ExceptionReport") >= 0) {
            Exception re = new Exception("Couldn't get data from WFS:\n" + s);
            LOGGER.debug((Object)I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.could-not-get-data-from-wfs"), (Throwable)re);
            throw re;
        }
        StringReader sr = new StringReader(s);
        GMLFeatureCollectionDocument gfDoc = new GMLFeatureCollectionDocument();
        FeatureCollection newFeatCollec = null;
        try {
            gfDoc.load(sr, "http://dummySysId");
            newFeatCollec = gfDoc.parse();
        }
        catch (SAXException e) {
            String mesg = "Error parsing response - " + e.getMessage();
            LOGGER.error((Object)mesg, (Throwable)e);
            throw new Exception(mesg, e);
        }
        catch (IOException e) {
            String mesg = "Error parsing response - " + e.getMessage();
            LOGGER.error((Object)mesg, (Throwable)e);
            throw new Exception(mesg, e);
        }
        catch (XMLParsingException e) {
            String mesg = "Error parsing response - " + e.getMessage();
            LOGGER.error((Object)mesg, (Throwable)e);
            throw new Exception(mesg, e);
        }
        return newFeatCollec;
    }

    public static WFSFeatureCollection[] createWFSCollectionsFromDeegreeFC(FeatureCollection deegreeFeatCollec, com.vividsolutions.jts.geom.Geometry defaultGeometry, String pkName, String geomColumnName, WFSFeatureTypeInfo ftInfo, AbstractWFSWrapper wrapper, QualifiedName qn) throws Exception {
        boolean hasPolygons;
        FeatureSchema fsBase = new FeatureSchema();
        boolean autoGeneratePK = false;
        int currentPk = 0;
        FeatureSchema pointSchema = null;
        FeatureSchema lineSchema = null;
        FeatureSchema polygonSchema = null;
        Object[] feats = deegreeFeatCollec.toArray();
        List<String> selectedAttrs = ftInfo.getSelectedAttributes();
        GMLSchema schema = wrapper.getSchemaForFeatureType(qn.getLocalName());
        FeatureType ft = schema.getFeatureType(qn);
        if (ft == null) {
            ft = schema.getFeatureType(qn.getLocalName());
        }
        String geoProName = null;
        if (geomColumnName == null) {
            Object[] geoTypeProps = ft.getGeometryProperties();
            if (geoTypeProps.length > 1) {
                LOGGER.warn((Object)I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.this-feature-type-has-more-than-one-geometry-property-only-the-first-one-will-be-used"));
            }
            if (ArrayUtils.isEmpty((Object[])geoTypeProps)) {
                LOGGER.debug((Object)I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.guessing-geometry-property-name"));
                geoProName = "GEOMETRY";
            } else {
                geoProName = geoTypeProps[0].getName().getLocalName();
                LOGGER.debug((Object)(String.valueOf(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.geometry-property-name")) + geoProName));
            }
        } else {
            geoProName = geomColumnName;
        }
        PropertyType[] featTypeProps = ft.getProperties();
        int j = 0;
        while (j < featTypeProps.length) {
            String name = featTypeProps[j].getName().getLocalName();
            if (!geoProName.equals(name)) {
                if (selectedAttrs.contains(name)) {
                    fsBase.addAttribute(name, JUMPFeatureFactory2.findType(featTypeProps[j].getType()));
                }
            } else {
                fsBase.addAttribute(geoProName, AttributeType.GEOMETRY);
            }
            ++j;
        }
        if (defaultGeometry == null && fsBase.getGeometryIndex() == -1) {
            throw new RuntimeException(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.no-geometry-property-found"));
        }
        if (defaultGeometry != null && fsBase.getGeometryIndex() == -1) {
            fsBase.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        }
        int gType = JUMPFeatureFactory2.guessGeometryType(ft.getProperty(new QualifiedName("", geoProName, qn.getNamespace())), (org.deegree.model.feature.Feature)(ArrayUtils.isEmpty((Object[])feats) ? null : feats[0]), defaultGeometry);
        fsBase.setGeometryType(gType);
        if (pkName != null && fsBase.hasAttribute(pkName)) {
            fsBase.getAttribute(pkName).setPrimaryKey(true);
        } else if (pkName != null) {
            fsBase.addAttribute(pkName, AttributeType.INTEGER, Boolean.TRUE);
            autoGeneratePK = true;
        } else {
            fsBase.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        }
        pointSchema = (FeatureSchema)fsBase.clone();
        if (fsBase.getGeometryType() == 8) {
            pointSchema.setGeometryType(8);
        } else {
            pointSchema.setGeometryType(1);
        }
        lineSchema = (FeatureSchema)fsBase.clone();
        if (fsBase.getGeometryType() == 2) {
            lineSchema.setGeometryType(2);
        } else {
            lineSchema.setGeometryType(3);
        }
        polygonSchema = (FeatureSchema)fsBase.clone();
        if (fsBase.getGeometryType() == 4) {
            polygonSchema.setGeometryType(4);
        } else {
            polygonSchema.setGeometryType(5);
        }
        WFSFeatureCollection jumpPointFc = new WFSFeatureCollection(pointSchema, ftInfo, wrapper);
        WFSFeatureCollection jumpLineFc = new WFSFeatureCollection(lineSchema, ftInfo, wrapper);
        WFSFeatureCollection jumpPolygonFc = new WFSFeatureCollection(polygonSchema, ftInfo, wrapper);
        boolean has3d = false;
        int i = 0;
        while (i < feats.length) {
            block50: {
                com.vividsolutions.jts.geom.Geometry currentGeom;
                Object currentDeegreFeat;
                block49: {
                    currentDeegreFeat = feats[i];
                    Geometry geoObject = currentDeegreFeat.getDefaultGeometryPropertyValue();
                    currentGeom = null;
                    if (defaultGeometry == null && geoObject != null) {
                        try {
                            currentGeom = JTSAdapter.export(geoObject);
                            has3d = has3d || !geoObject.isEmpty() && geoObject.getCoordinateDimension() > 2;
                            break block49;
                        }
                        catch (Exception e) {
                            LOGGER.error((Object)("Feature " + currentDeegreFeat.getId() + " could not be loaded, ignoring it" + ": " + e));
                            break block50;
                        }
                    }
                    currentGeom = defaultGeometry;
                }
                WFSFeature jf = new WFSFeature(fsBase, currentDeegreFeat.getId());
                jf.setGeometry(currentGeom);
                int geoIndex = jf.getSchema().getGeometryIndex();
                int j2 = 0;
                while (j2 < jf.getSchema().getAttributeCount()) {
                    if (j2 != geoIndex) {
                        QualifiedName qName = new QualifiedName(fsBase.getAttributeName(j2), featTypeProps[0].getName().getNamespace());
                        FeatureProperty[] fp = currentDeegreFeat.getProperties(qName);
                        Object value = JUMPFeatureFactory2.generateValue(fp, currentDeegreFeat.getFeatureType(), fsBase.getAttribute(j2));
                        jf.setAttribute(j2, value);
                    }
                    ++j2;
                }
                if (autoGeneratePK) {
                    jf.setAttribute(pkName, (Object)new Integer(currentPk++));
                }
                String geomType = null;
                if (currentGeom == null) {
                    switch (gType) {
                        case 1: 
                        case 8: {
                            jumpPointFc.add(jf);
                            break;
                        }
                        case 2: 
                        case 3: {
                            jumpLineFc.add(jf);
                            break;
                        }
                        case 4: 
                        case 5: {
                            jumpPolygonFc.add(jf);
                            break;
                        }
                    }
                } else {
                    geomType = currentGeom.getGeometryType();
                    if (geomType.equals("Point") || geomType.equals("MultiPoint")) {
                        jumpPointFc.add(jf);
                    } else if (geomType.equals("LineString") || geomType.equals("MultiLineString")) {
                        jumpLineFc.add(jf);
                    } else if (geomType.equals("Polygon") || geomType.equals("MultiPolygon")) {
                        jumpPolygonFc.add(jf);
                    } else {
                        LOGGER.warn((Object)(String.valueOf(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.ignoring-current-feature")) + jf));
                    }
                }
            }
            ++i;
        }
        jumpPointFc.commit(false);
        jumpLineFc.commit(false);
        jumpPolygonFc.commit(false);
        jumpPointFc.set3d(has3d);
        jumpLineFc.set3d(has3d);
        jumpPolygonFc.set3d(has3d);
        boolean hasPoints = jumpPointFc.size() > 0;
        boolean hasLines = jumpLineFc.size() > 0;
        boolean bl = hasPolygons = jumpPolygonFc.size() > 0;
        if (hasPoints) {
            if (hasLines) {
                if (hasPolygons) {
                    jumpPointFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Points"));
                    jumpLineFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Lines"));
                    jumpPolygonFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Polygons"));
                    return new WFSFeatureCollection[]{jumpPointFc, jumpLineFc, jumpPolygonFc};
                }
                jumpPointFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Points"));
                jumpLineFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Lines"));
                return new WFSFeatureCollection[]{jumpPointFc, jumpLineFc};
            }
            if (hasPolygons) {
                jumpPointFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Points"));
                jumpPolygonFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Polygons"));
                return new WFSFeatureCollection[]{jumpPointFc, jumpPolygonFc};
            }
            return new WFSFeatureCollection[]{jumpPointFc};
        }
        if (hasLines) {
            if (hasPolygons) {
                jumpLineFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Lines"));
                jumpPolygonFc.setName(String.valueOf(ftInfo.getTitle()) + " - " + I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.Polygons"));
                return new WFSFeatureCollection[]{jumpLineFc, jumpPolygonFc};
            }
            return new WFSFeatureCollection[]{jumpLineFc};
        }
        if (hasPolygons) {
            return new WFSFeatureCollection[]{jumpPolygonFc};
        }
        WFSFeatureCollection unknownFc = new WFSFeatureCollection(fsBase, ftInfo, wrapper);
        unknownFc.setName(ftInfo.getLocalName());
        unknownFc.set3d(has3d);
        return new WFSFeatureCollection[]{unknownFc};
    }

    private static Object generateValue(FeatureProperty[] fps, FeatureType featureType, Attribute attr) throws Exception {
        Object value = null;
        if (!ArrayUtils.isEmpty((Object[])fps)) {
            if (fps.length == 1) {
                Object fpValue = fps[0].getValue();
                if (fpValue instanceof org.deegree.model.feature.Feature) {
                    org.deegree.model.feature.Feature dFeat = (org.deegree.model.feature.Feature)fpValue;
                    Feature f = JUMPFeatureFactory2.buildFeature(dFeat);
                    WFSFeatureDataset fd = new WFSFeatureDataset(f.getSchema());
                    fd.setFeatureTypeName(dFeat.getFeatureType().getName());
                    fd.add(f);
                    fd.commit();
                    value = fd;
                } else {
                    value = FeatureUtil.getGoodAttribute(attr.getType(), fpValue);
                }
            } else {
                FeatureDataset fd = null;
                FeatureProperty[] featurePropertyArray = fps;
                int n = fps.length;
                int n2 = 0;
                while (n2 < n) {
                    FeatureProperty fp = featurePropertyArray[n2];
                    Object fpValue = fp.getValue();
                    org.deegree.model.feature.Feature dFeat = (org.deegree.model.feature.Feature)fpValue;
                    Feature f = JUMPFeatureFactory2.buildFeature(dFeat);
                    if (fd == null) {
                        fd = new WFSFeatureDataset(f.getSchema());
                        ((WFSFeatureDataset)fd).setFeatureTypeName(dFeat.getFeatureType().getName());
                    }
                    fd.add(f);
                    ++n2;
                }
                fd.commit();
                value = fd;
            }
        }
        return value;
    }

    private static FeatureSchema buildSchema(FeatureType featureType) {
        PropertyType[] featTypeProps = featureType.getProperties();
        FeatureSchema featSchema = new FeatureSchema();
        int j = 0;
        while (j < featTypeProps.length) {
            String name = featTypeProps[j].getName().getLocalName();
            featSchema.addAttribute(name, JUMPFeatureFactory2.findType(featTypeProps[j].getType()), name.equals("GID"));
            ++j;
        }
        if (featSchema.getPrimaryKey() == null) {
            featSchema.addAttribute("GID", AttributeType.INTEGER, Boolean.TRUE);
        }
        return featSchema;
    }

    private static Feature buildFeature(org.deegree.model.feature.Feature deegreeFeat) throws Exception {
        FeatureSchema featSchema = JUMPFeatureFactory2.buildSchema(deegreeFeat.getFeatureType());
        WFSFeature f = new WFSFeature(featSchema, deegreeFeat.getId());
        int j = 0;
        while (j < featSchema.getAttributeCount()) {
            QualifiedName qName = new QualifiedName(featSchema.getAttributeName(j), deegreeFeat.getFeatureType().getNameSpace());
            FeatureProperty[] fp = deegreeFeat.getProperties(qName);
            Object value = JUMPFeatureFactory2.generateValue(fp, deegreeFeat.getFeatureType(), featSchema.getAttribute(j));
            f.setAttribute(j, value);
            ++j;
        }
        return f;
    }

    private static int guessGeometryType(PropertyType property, org.deegree.model.feature.Feature feature, com.vividsolutions.jts.geom.Geometry defaultGeometry) throws GeometryException {
        int gType = 0;
        if (property != null) {
            int type = property.getType();
            switch (type) {
                case 10012: {
                    gType = 0;
                    break;
                }
                case 11012: {
                    gType = 1;
                    break;
                }
                case 11013: {
                    gType = 3;
                    break;
                }
                case 11014: {
                    gType = 5;
                    break;
                }
                case 10013: {
                    gType = 15;
                    break;
                }
                case 11015: {
                    gType = 8;
                    break;
                }
                case 11016: {
                    gType = 2;
                    break;
                }
                case 11017: {
                    gType = 4;
                    break;
                }
                default: {
                    gType = 0;
                }
            }
        }
        if (gType == 0 && feature != null) {
            Geometry defaultGeometryPropertyValue = feature.getDefaultGeometryPropertyValue();
            gType = defaultGeometryPropertyValue == null ? FeatureSchema.getGeometryType(defaultGeometry) : FeatureSchema.getGeometryType(JTSAdapter.export(defaultGeometryPropertyValue));
        }
        return gType;
    }

    private static AttributeType findType(int type) {
        String xsd = Types.getXSDTypeForSQLType(type, 0);
        if (xsd.equals("date")) {
            return AttributeType.DATE;
        }
        if (xsd.equals("time")) {
            return AttributeType.TIME;
        }
        if (xsd.equals("dateTime")) {
            return AttributeType.TIMESTAMP;
        }
        if (xsd.equals("gml:GeometryPropertyType")) {
            return AttributeType.GEOMETRY;
        }
        if (xsd.equals("integer")) {
            return AttributeType.INTEGER;
        }
        if (xsd.equals("double")) {
            return AttributeType.DOUBLE;
        }
        if (xsd.equals("decimal")) {
            return AttributeType.DECIMAL;
        }
        if (xsd.equals("float")) {
            return AttributeType.FLOAT;
        }
        if (xsd.equals("boolean")) {
            return AttributeType.BOOLEAN;
        }
        if (xsd.equals("gml:FeaturePropertyType")) {
            return AttributeType.OBJECT;
        }
        return AttributeType.STRING;
    }

    public static void setMaxFeatures(int i) {
        if (i > 0) {
            maxFeatures = i;
        }
    }

    public static int getMaxFeatures() {
        return maxFeatures;
    }

    public static String toXSDName(AttributeType type) {
        String t = null;
        if (type == AttributeType.DATE) {
            t = "date";
        } else if (type == AttributeType.INTEGER || type == AttributeType.NUMERIC || type == AttributeType.BIGDECIMAL) {
            t = "integer";
        } else if (type == AttributeType.STRING || type == AttributeType.CHAR || type == AttributeType.VARCHAR || type == AttributeType.LONGVARCHAR || type == AttributeType.TEXT) {
            t = "string";
        } else if (type == AttributeType.DOUBLE || type == AttributeType.REAL) {
            t = "double";
        } else if (type == AttributeType.LONG || type == AttributeType.BIGINT || type == AttributeType.DECIMAL) {
            t = "long";
        } else if (type == AttributeType.DECIMAL) {
            t = "decimal";
        } else if (type == AttributeType.BOOLEAN || type == AttributeType.BIT) {
            t = "boolean";
        } else if (type == AttributeType.TINYINT || type == AttributeType.SMALLINT) {
            t = "short";
        } else if (type == AttributeType.TIME) {
            t = "time";
        } else if (type == AttributeType.TIMESTAMP) {
            t = "dateTime";
        } else if (type == AttributeType.OBJECT) {
            t = "object";
        } else {
            throw new RuntimeException("no xsd type found for: " + type);
        }
        return t;
    }

    public static FeatureCollection createFromJUMPFeatureCollection(com.vividsolutions.jump.feature.FeatureCollection jumpFeatureCollection, IProjection proj) throws Exception {
        return JUMPFeatureFactory2.createFromJUMPFeatureCollection(jumpFeatureCollection, Long.MAX_VALUE, proj);
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static FeatureCollection createFromJUMPFeatureCollection(com.vividsolutions.jump.feature.FeatureCollection jumpFeatureCollection, long maxNumberOfFeatures, IProjection proj) throws Exception {
        if (jumpFeatureCollection.size() == 0) throw new IllegalArgumentException(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.featurecollection-can-not-be-null-and-must-have-at-least-one-feature"));
        if (jumpFeatureCollection == null) {
            throw new IllegalArgumentException(I18N.getString("de.latlon.deejump.wfs.data.JUMPFeatureFactory2.featurecollection-can-not-be-null-and-must-have-at-least-one-feature"));
        }
        FeatureCollection fc = FeatureFactory.createFeatureCollection((String)"id", (int)jumpFeatureCollection.size());
        FeatureSchema schema = jumpFeatureCollection.getFeatureSchema();
        int count = 0;
        URI GMLNS = CommonNamespaces.GMLNS;
        URI XSNS = CommonNamespaces.XSNS;
        FeatureIterator itFeats = null;
        try {
            block12: {
                CoordinateSystem crs;
                block11: {
                    itFeats = jumpFeatureCollection.iterator();
                    crs = JUMPFeatureFactory2.exportToDeegreeCoordSystem(proj);
                    if (!true) break block11;
                    if (!itFeats.hasNext()) return fc;
                    if ((long)count >= maxNumberOfFeatures) break block12;
                }
                do {
                    Feature feature = itFeats.next();
                    PropertyType[] propType = new PropertyType[schema.getAttributeCount()];
                    int geoIx = schema.getGeometryIndex();
                    int i = 0;
                    while (i < schema.getAttributeCount()) {
                        String type;
                        if (i != geoIx) {
                            type = JUMPFeatureFactory2.toXSDName(schema.getAttributeType(i));
                            propType[i] = FeatureFactory.createPropertyType((QualifiedName)new QualifiedName(schema.getAttributeName(i)), (QualifiedName)new QualifiedName(type, XSNS), (boolean)true);
                        } else {
                            type = "org.deegree.model.geometry.Geometry";
                            propType[i] = FeatureFactory.createPropertyType((QualifiedName)new QualifiedName(schema.getAttributeName(geoIx)), (QualifiedName)new QualifiedName(type, GMLNS), (boolean)true);
                        }
                        ++i;
                    }
                    FeatureType ft = FeatureFactory.createFeatureType((QualifiedName)new QualifiedName("featuretypename"), (boolean)false, (PropertyType[])propType);
                    FeatureProperty[] fp = new FeatureProperty[schema.getAttributeCount()];
                    int i2 = 0;
                    while (i2 < schema.getAttributeCount()) {
                        fp[i2] = i2 != geoIx ? FeatureFactory.createFeatureProperty((QualifiedName)new QualifiedName(schema.getAttributeName(i2)), (Object)feature.getAttribute(i2)) : FeatureFactory.createFeatureProperty((QualifiedName)new QualifiedName(schema.getAttributeName(geoIx)), (Object)JTSAdapter.wrap(feature.getGeometry(), crs));
                        ++i2;
                    }
                    org.deegree.model.feature.Feature fe = FeatureFactory.createFeature((String)("fid_" + count++), (FeatureType)ft, (FeatureProperty[])fp);
                    fc.add(fe);
                    if (!itFeats.hasNext()) return fc;
                } while ((long)count < maxNumberOfFeatures);
            }
            return fc;
        }
        finally {
            if (itFeats != null) {
                itFeats.close();
            }
        }
    }

    private static CoordinateSystem exportToDeegreeCoordSystem(IProjection proj) throws UnknownCRSException {
        Assert.isTrue((proj != null ? 1 : 0) != 0);
        return GMLGeometryAdapter.getCRS(proj.getAbrev());
    }
}

