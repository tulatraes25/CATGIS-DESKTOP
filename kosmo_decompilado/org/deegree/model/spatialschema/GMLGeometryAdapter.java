/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.util.StringTools
 *  org.deegree.framework.xml.ElementList
 *  org.deegree.framework.xml.NamespaceContext
 *  org.deegree.framework.xml.XMLFragment
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.crs.CRSFactory
 *  org.deegree.model.crs.UnknownCRSException
 *  org.deegree.model.spatialschema.CompositeSurface
 *  org.deegree.model.spatialschema.Curve
 *  org.deegree.model.spatialschema.CurveImpl
 *  org.deegree.model.spatialschema.CurveSegment
 *  org.deegree.model.spatialschema.Envelope
 *  org.deegree.model.spatialschema.Geometry
 *  org.deegree.model.spatialschema.GeometryException
 *  org.deegree.model.spatialschema.GeometryFactory
 *  org.deegree.model.spatialschema.LineString
 *  org.deegree.model.spatialschema.MultiCurve
 *  org.deegree.model.spatialschema.MultiPoint
 *  org.deegree.model.spatialschema.MultiSurface
 *  org.deegree.model.spatialschema.Point
 *  org.deegree.model.spatialschema.Position
 *  org.deegree.model.spatialschema.PositionImpl
 *  org.deegree.model.spatialschema.Ring
 *  org.deegree.model.spatialschema.Surface
 *  org.deegree.model.spatialschema.SurfaceImpl
 *  org.deegree.model.spatialschema.SurfaceInterpolation
 *  org.deegree.model.spatialschema.SurfaceInterpolationImpl
 *  org.deegree.model.spatialschema.SurfacePatch
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcbase.InvalidGMLException
 */
package org.deegree.model.spatialschema;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.CompositeSurface;
import org.deegree.model.spatialschema.Curve;
import org.deegree.model.spatialschema.CurveImpl;
import org.deegree.model.spatialschema.CurveSegment;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.LineString;
import org.deegree.model.spatialschema.MultiCurve;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.MultiSurface;
import org.deegree.model.spatialschema.Point;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.PositionImpl;
import org.deegree.model.spatialschema.Ring;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceImpl;
import org.deegree.model.spatialschema.SurfaceInterpolation;
import org.deegree.model.spatialschema.SurfaceInterpolationImpl;
import org.deegree.model.spatialschema.SurfacePatch;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.InvalidGMLException;
import org.saig.jump.lang.I18N;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class GMLGeometryAdapter {
    protected static final ILogger LOG = LoggerFactory.getLogger(GMLGeometryAdapter.class);
    protected static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
    protected static Map<String, CoordinateSystem> crsMap = new HashMap<String, CoordinateSystem>();
    protected static final String COORD = "gml:coord";
    protected static final String COORDINATES = "gml:coordinates";
    protected static final String POS = "gml:pos";
    protected static final String POSLIST = "gml:posList";
    protected static String EPSG_SINGLE = "EPSG:";
    protected static String EPSG_DOUBLE = "EPSG::";
    protected static String X_OGC = "urn:x-ogc:def:";
    protected static String OGC = "urn:ogc:def:";
    public static String GML2_FORMAT_TYPE = "GML2";

    public static Geometry wrap(String gml, String srsName) throws GeometryException, XMLParsingException {
        StringReader sr = new StringReader(gml);
        Document doc = null;
        try {
            doc = XMLTools.parse(sr);
        }
        catch (Exception e) {
            LOG.logError(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.could-not-parse")) + ": '" + gml + "' " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.as-gml-xml"), (Throwable)e);
            throw new XMLParsingException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.could-not-parse")) + ": '" + gml + "' " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.as-gml-xml") + e.getMessage());
        }
        return GMLGeometryAdapter.wrap(doc.getDocumentElement(), srsName);
    }

    public static Geometry wrap(Element element, String srsName) throws GeometryException {
        Point geometry = null;
        try {
            String name = element.getLocalName();
            if (name.equals("Point") || name.equals("Center")) {
                geometry = GMLGeometryAdapter.wrapPoint(element, srsName);
            } else if (name.equals("LineString")) {
                geometry = GMLGeometryAdapter.wrapLineString(element, srsName);
            } else if (name.equals("Polygon")) {
                geometry = GMLGeometryAdapter.wrapPolygon(element, srsName);
            } else if (name.equals("MultiPoint")) {
                geometry = GMLGeometryAdapter.wrapMultiPoint(element, srsName);
            } else if (name.equals("MultiLineString")) {
                geometry = GMLGeometryAdapter.wrapMultiLineString(element, srsName);
            } else if (name.equals("MultiPolygon")) {
                geometry = GMLGeometryAdapter.wrapMultiPolygon(element, srsName);
            } else if (name.equals("Box") || name.equals("Envelope")) {
                geometry = GMLGeometryAdapter.wrapBoxAsSurface(element, srsName);
            } else if (name.equals("Curve")) {
                geometry = GMLGeometryAdapter.wrapCurveAsCurve(element, srsName);
            } else if (name.equals("Surface")) {
                geometry = GMLGeometryAdapter.wrapSurfaceAsSurface(element, srsName);
            } else if (name.equals("MultiCurve")) {
                geometry = GMLGeometryAdapter.wrapMultiCurveAsMultiCurve(element, srsName);
            } else if (name.equals("MultiSurface")) {
                geometry = GMLGeometryAdapter.wrapMultiSurfaceAsMultiSurface(element, srsName);
            } else if (name.equals("CompositeSurface")) {
                geometry = GMLGeometryAdapter.wrapCompositeSurface(element, srsName);
            } else {
                new GeometryException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.not-a-supported-geometry-type")) + ": " + name);
            }
        }
        catch (Exception e) {
            LOG.logError(e.getMessage(), (Throwable)e);
            throw new GeometryException(StringTools.stackTraceToString((Throwable)e));
        }
        return geometry;
    }

    public static Envelope wrapBox(Element element, String srsName) throws XMLParsingException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        Position[] bb = null;
        List<Node> nl = XMLTools.getNodes(element, COORD, nsContext);
        if (nl != null && nl.size() > 0) {
            bb = new Position[]{GMLGeometryAdapter.createPositionFromCoord((Element)nl.get(0)), GMLGeometryAdapter.createPositionFromCoord((Element)nl.get(1))};
        } else {
            nl = XMLTools.getNodes(element, COORDINATES, nsContext);
            if (nl != null && nl.size() > 0) {
                bb = GMLGeometryAdapter.createPositionFromCoordinates((Element)nl.get(0));
            } else {
                nl = XMLTools.getNodes(element, POS, nsContext);
                if (nl != null && nl.size() > 0) {
                    bb = new Position[]{GMLGeometryAdapter.createPositionFromPos((Element)nl.get(0)), GMLGeometryAdapter.createPositionFromPos((Element)nl.get(1))};
                } else {
                    Element lowerCorner = (Element)XMLTools.getRequiredNode(element, "gml:lowerCorner", nsContext);
                    Element upperCorner = (Element)XMLTools.getRequiredNode(element, "gml:upperCorner", nsContext);
                    bb = new Position[]{GMLGeometryAdapter.createPositionFromCorner(lowerCorner), GMLGeometryAdapter.createPositionFromCorner(upperCorner)};
                }
            }
        }
        Envelope box = GeometryFactory.createEnvelope((Position)bb[0], (Position)bb[1], (CoordinateSystem)crs);
        return box;
    }

    protected static Curve wrapCurveAsCurve(Element element, String srsName) throws XMLParsingException, GeometryException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        String srsDimension = GMLGeometryAdapter.findSrsDimension(element);
        Element segment = (Element)XMLTools.getRequiredNode(element, "gml:segments", nsContext);
        CurveSegment[] segments = GMLGeometryAdapter.parseCurveSegments(crs, srsDimension, segment);
        return GeometryFactory.createCurve((CurveSegment[])segments, (CoordinateSystem)crs);
    }

    private static CurveSegment[] parseCurveSegments(CoordinateSystem crs, String srsDimension, Element segment) throws XMLParsingException, GeometryException {
        List<Node> list = XMLTools.getNodes(segment, "child::*", nsContext);
        CurveSegment[] segments = new CurveSegment[list.size()];
        int i = 0;
        while (i < list.size()) {
            if (list.get(i).getLocalName().equals("LineStringSegment")) {
                segments[i] = GMLGeometryAdapter.parseLineStringSegment((Element)list.get(i), crs, srsDimension);
            } else if (list.get(i).getLocalName().equals("Arc")) {
                segments[i] = GMLGeometryAdapter.parseArc((Element)list.get(i), crs, srsDimension);
            } else {
                throw new GeometryException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.not-supported-type-for-a-curvesegment")) + ": " + list.get(i).getLocalName());
            }
            ++i;
        }
        return segments;
    }

    private static CurveSegment parseArc(Element element, CoordinateSystem crs, String srsDimension) throws GeometryException {
        CurveSegment segment = null;
        try {
            Position[] pos = GMLGeometryAdapter.createPositions(element, null, srsDimension);
            segment = GeometryFactory.createCurveSegment((Position[])pos, (CoordinateSystem)crs);
        }
        catch (Exception e) {
            throw new GeometryException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-creating-segments-for-the-element-arc")) + ".");
        }
        return segment;
    }

    private static CurveSegment parseLineStringSegment(Element element, CoordinateSystem crs, String srsDimension) throws GeometryException {
        CurveSegment segment = null;
        try {
            Position[] pos = GMLGeometryAdapter.createPositions(element, null, srsDimension);
            segment = GeometryFactory.createCurveSegment((Position[])pos, (CoordinateSystem)crs);
        }
        catch (Exception e) {
            throw new GeometryException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-creating-segments-for-the-element-linestringsegment")) + ".");
        }
        return segment;
    }

    protected static MultiCurve wrapMultiCurveAsMultiCurve(Element element, String srsName) throws XMLParsingException, GeometryException, UnknownCRSException, InvalidGMLException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        MultiCurve multiCurve = null;
        try {
            Element curveMembers;
            List<Node> listCurveMember = XMLTools.getNodes(element, "gml:curveMember", nsContext);
            ArrayList<Curve> curveList = new ArrayList<Curve>();
            if (listCurveMember.size() > 0) {
                int i = 0;
                while (i < listCurveMember.size()) {
                    Element curveMember = (Element)listCurveMember.get(i);
                    Element curve = (Element)XMLTools.getNode(curveMember, "gml:Curve", nsContext);
                    if (curve != null) {
                        curveList.add(GMLGeometryAdapter.wrapCurveAsCurve(curve, srsName));
                    } else {
                        curve = (Element)XMLTools.getRequiredNode(curveMember, "gml:LineString", nsContext);
                        curveList.add(GMLGeometryAdapter.wrapLineString(curve, srsName));
                    }
                    ++i;
                }
            }
            if ((curveMembers = (Element)XMLTools.getNode(element, "gml:curveMembers", nsContext)) != null) {
                Element curve;
                List<Node> listCurves = XMLTools.getNodes(curveMembers, "gml:Curve", nsContext);
                if (listCurves != null) {
                    int i = 0;
                    while (i < listCurves.size()) {
                        curve = (Element)listCurves.get(i);
                        curveList.add(GMLGeometryAdapter.wrapCurveAsCurve(curve, srsName));
                        ++i;
                    }
                }
                if ((listCurves = XMLTools.getNodes(curveMembers, "gml:LineString", nsContext)) != null) {
                    int i = 0;
                    while (i < listCurves.size()) {
                        curve = (Element)listCurves.get(i);
                        curveList.add(GMLGeometryAdapter.wrapLineString(curve, srsName));
                        ++i;
                    }
                }
            }
            Curve[] curves = new Curve[curveList.size()];
            multiCurve = GeometryFactory.createMultiCurve((Curve[])curveList.toArray(curves), (CoordinateSystem)crs);
        }
        catch (XMLParsingException e) {
            LOG.logError(e.getMessage(), (Throwable)e);
            throw new XMLParsingException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-parsing")) + "<gml:curveMember> " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.elements") + ". " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.please-check-the-xml-document") + ".");
        }
        catch (GeometryException e) {
            LOG.logError(e.getMessage(), (Throwable)e);
            throw new GeometryException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-creating-a-curve-from-the-curve-element")) + ". " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.please-check-the-gml-specifications-for-correct-element-declaration") + ".");
        }
        return multiCurve;
    }

    protected static Surface wrapSurfaceAsSurface(Element element, String srsName) throws XMLParsingException, GeometryException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        Element patches = GMLGeometryAdapter.extractPatches(element);
        List<Element> polygonList = XMLTools.getRequiredElements(patches, "gml:Polygon | gml:PolygonPatch", nsContext);
        SurfacePatch[] surfacePatches = new SurfacePatch[polygonList.size()];
        int i = 0;
        while (i < polygonList.size()) {
            Curve exteriorRing = null;
            Element polygon = polygonList.get(i);
            try {
                Element exterior = (Element)XMLTools.getNode(polygon, "gml:exterior | gml:outerBounderyIs", nsContext);
                if (exterior == null) {
                    String msg = "Cannot interpret GML surface: surface has no exterior ring. ";
                    throw new XMLParsingException(msg);
                }
                exteriorRing = GMLGeometryAdapter.parseRing(crs, exterior);
                List<Element> interiorList = XMLTools.getElements(polygon, "gml:interior | gml:outerBounderyIs", nsContext);
                Curve[] interiorRings = null;
                if (interiorList != null && interiorList.size() > 0) {
                    interiorRings = new Curve[interiorList.size()];
                    int j = 0;
                    while (j < interiorRings.length) {
                        Element interior = interiorList.get(j);
                        interiorRings[j] = GMLGeometryAdapter.parseRing(crs, interior);
                        ++j;
                    }
                }
                surfacePatches[i] = GeometryFactory.createSurfacePatch((Curve)exteriorRing, interiorRings, (CoordinateSystem)crs);
            }
            catch (InvalidGMLException e) {
                LOG.logError(e.getMessage(), (Throwable)e);
                throw new XMLParsingException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-parsing-the-polygon-element")) + " '" + polygon.getNodeName() + "' " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.to-create-a-surface-geometry") + ".");
            }
            ++i;
        }
        Surface surface = null;
        try {
            surface = GeometryFactory.createSurface((SurfacePatch[])surfacePatches, (CoordinateSystem)crs);
        }
        catch (GeometryException e) {
            throw new GeometryException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-creating-a-surface-from")) + " '" + surfacePatches.length + "' " + "polygons" + ".");
        }
        return surface;
    }

    private static String findSrsName(Element element, String srsName) throws XMLParsingException {
        Node elem = element;
        while (srsName == null && elem != null) {
            srsName = XMLTools.getNodeAsString(elem, "@srsName", nsContext, srsName);
            elem = elem.getParentNode();
        }
        elem = element;
        if (srsName == null) {
            srsName = XMLTools.getNodeAsString(elem, "//@srsName", nsContext, srsName);
        }
        return srsName;
    }

    private static Curve parseRing(CoordinateSystem crs, Element parent) throws XMLParsingException, InvalidGMLException, GeometryException {
        String srsName = null;
        String srsDimension = GMLGeometryAdapter.findSrsDimension(parent);
        if (crs != null) {
            srsName = crs.getIdentifier();
        }
        ArrayList<CurveSegment> curveMembers = null;
        Element ring = (Element)XMLTools.getNode(parent, "gml:LinearRing", nsContext);
        if (ring != null) {
            Position[] exteriorRing = GMLGeometryAdapter.createPositions(ring, srsName, srsDimension);
            curveMembers = new ArrayList<CurveSegment>();
            curveMembers.add(GeometryFactory.createCurveSegment((Position[])exteriorRing, (CoordinateSystem)crs));
        } else {
            List<Node> members = XMLTools.getRequiredNodes(parent, "gml:Ring/gml:curveMember/child::*", nsContext);
            curveMembers = new ArrayList(members.size());
            for (Node node : members) {
                Curve curve = (Curve)GMLGeometryAdapter.wrap((Element)node, srsName);
                CurveSegment[] tmp = curve.getCurveSegments();
                int i = 0;
                while (i < tmp.length) {
                    curveMembers.add(tmp[i]);
                    ++i;
                }
            }
        }
        CurveSegment[] cs = curveMembers.toArray(new CurveSegment[curveMembers.size()]);
        return GeometryFactory.createCurve((CurveSegment[])cs);
    }

    protected static MultiSurface wrapMultiSurfaceAsMultiSurface(Element element, String srsName) throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        MultiSurface multiSurface = null;
        try {
            Element surfaceMembers;
            ArrayList<Surface> surfaceList = new ArrayList<Surface>();
            List<Node> listSurfaceMember = XMLTools.getNodes(element, "gml:surfaceMember", nsContext);
            if (listSurfaceMember != null) {
                int i = 0;
                while (i < listSurfaceMember.size()) {
                    Element surfaceMember = (Element)listSurfaceMember.get(i);
                    Element surface = (Element)XMLTools.getNode(surfaceMember, "gml:Surface", nsContext);
                    if (surface != null) {
                        surfaceList.add(GMLGeometryAdapter.wrapSurfaceAsSurface(surface, srsName));
                    } else {
                        surface = (Element)XMLTools.getRequiredNode(surfaceMember, ".//gml:Polygon", nsContext);
                        surfaceList.add(GMLGeometryAdapter.wrapPolygon(surface, srsName));
                    }
                    ++i;
                }
            }
            if ((surfaceMembers = (Element)XMLTools.getNode(element, "gml:surfaceMembers", nsContext)) != null) {
                Element surface;
                List<Node> listSurfaces = XMLTools.getNodes(surfaceMembers, "gml:Surface", nsContext);
                if (listSurfaces != null) {
                    int i = 0;
                    while (i < listSurfaces.size()) {
                        surface = (Element)listSurfaces.get(i);
                        surfaceList.add(GMLGeometryAdapter.wrapSurfaceAsSurface(surface, srsName));
                        ++i;
                    }
                }
                if ((listSurfaces = XMLTools.getNodes(surfaceMembers, ".//gml:Polygon", nsContext)) != null) {
                    int i = 0;
                    while (i < listSurfaces.size()) {
                        surface = (Element)listSurfaces.get(i);
                        surfaceList.add(GMLGeometryAdapter.wrapPolygon(surface, srsName));
                        ++i;
                    }
                }
            }
            Surface[] surfaces = new Surface[surfaceList.size()];
            surfaces = surfaceList.toArray(surfaces);
            multiSurface = GeometryFactory.createMultiSurface((Surface[])surfaces, (CoordinateSystem)crs);
        }
        catch (XMLParsingException e) {
            LOG.logError(e.getMessage(), (Throwable)e);
            String msg = "Error parsing <gml:surfaceMember> elements. Please check the xml document.";
            throw new XMLParsingException(msg);
        }
        catch (GeometryException e) {
            LOG.logError(e.getMessage(), (Throwable)e);
            String msg = "Error creating a multi surface from the MultiSurface element. Please check the GML specifications for correct element declaration.";
            throw new GeometryException(msg);
        }
        return multiSurface;
    }

    private static Point wrapPoint(Element element, String srsName) throws XMLParsingException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        Position[] bb = null;
        List<Node> nl = XMLTools.getNodes(element, COORD, nsContext);
        if (nl != null && nl.size() > 0) {
            bb = new Position[]{GMLGeometryAdapter.createPositionFromCoord((Element)nl.get(0))};
        } else {
            nl = XMLTools.getNodes(element, COORDINATES, nsContext);
            if (nl != null && nl.size() > 0) {
                bb = GMLGeometryAdapter.createPositionFromCoordinates((Element)nl.get(0));
            } else {
                nl = XMLTools.getNodes(element, POS, nsContext);
                bb = new Position[]{GMLGeometryAdapter.createPositionFromPos((Element)nl.get(0))};
            }
        }
        Point point = GeometryFactory.createPoint((Position)bb[0], (CoordinateSystem)crs);
        return point;
    }

    private static Curve wrapLineString(Element element, String srsName) throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        String srsDimension = GMLGeometryAdapter.findSrsDimension(element);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        Position[] pos = GMLGeometryAdapter.createPositions(element, srsName, srsDimension);
        Curve curve = GeometryFactory.createCurve((Position[])pos, (CoordinateSystem)crs);
        return curve;
    }

    private static String findSrsDimension(Element element) {
        return XMLTools.getAttrValue(element, null, "srsDimension", null);
    }

    private static Surface wrapPolygon(Element element, String srsName) throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        String srsDimension = GMLGeometryAdapter.findSrsDimension(element);
        List<Node> nl = XMLTools.getNodes(element, "gml:outerBoundaryIs", nsContext);
        if (nl == null || nl.size() == 0) {
            nl = XMLTools.getRequiredNodes(element, "gml:exterior", nsContext);
        }
        Element outs = (Element)nl.get(0);
        nl = XMLTools.getRequiredNodes(outs, "gml:LinearRing", nsContext);
        Element ring = (Element)nl.get(0);
        nl = XMLTools.getNodes(ring, COORDINATES, nsContext);
        Position[] outerRing = GMLGeometryAdapter.correctRing(GMLGeometryAdapter.createPositions(ring, srsName, srsDimension));
        Position[][] innerRings = null;
        List<Node> inns = XMLTools.getNodes(element, "gml:innerBoundaryIs", nsContext);
        if (inns == null || inns.size() == 0) {
            inns = XMLTools.getNodes(element, "gml:interior", nsContext);
        }
        if (inns != null && inns.size() > 0) {
            innerRings = new Position[inns.size()][];
            int i = 0;
            while (i < innerRings.length) {
                nl = XMLTools.getRequiredNodes(inns.get(i), "gml:LinearRing", nsContext);
                ring = (Element)nl.get(0);
                innerRings[i] = GMLGeometryAdapter.correctRing(GMLGeometryAdapter.createPositions(ring, srsName, srsDimension));
                ++i;
            }
        }
        SurfaceInterpolationImpl si = new SurfaceInterpolationImpl();
        Surface surface = GeometryFactory.createSurface((Position[])outerRing, (Position[][])innerRings, (SurfaceInterpolation)si, (CoordinateSystem)crs);
        return surface;
    }

    private static MultiPoint wrapMultiPoint(Element element, String srsName) throws XMLParsingException, InvalidGMLException, UnknownCRSException {
        Element pointMembers;
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        ArrayList<Point> pointList = new ArrayList<Point>();
        List<Node> listPointMember = XMLTools.getNodes(element, "gml:pointMember", nsContext);
        if (listPointMember != null) {
            int i = 0;
            while (i < listPointMember.size()) {
                Element pointMember = (Element)listPointMember.get(i);
                Element point = (Element)XMLTools.getNode(pointMember, "gml:Point", nsContext);
                pointList.add(GMLGeometryAdapter.wrapPoint(point, srsName));
                ++i;
            }
        }
        if ((pointMembers = (Element)XMLTools.getNode(element, "gml:pointMembers", nsContext)) != null) {
            List<Node> pointElems = XMLTools.getNodes(pointMembers, "gml:Point", nsContext);
            int j = 0;
            while (j < pointElems.size()) {
                pointList.add(GMLGeometryAdapter.wrapPoint((Element)pointElems.get(j), srsName));
                ++j;
            }
        }
        Point[] points = new Point[pointList.size()];
        return GeometryFactory.createMultiPoint((Point[])pointList.toArray(points), (CoordinateSystem)crs);
    }

    private static MultiCurve wrapMultiLineString(Element element, String srsName) throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        ElementList el = XMLTools.getChildElements("lineStringMember", CommonNamespaces.GMLNS, element);
        Curve[] curves = new Curve[el.getLength()];
        int i = 0;
        while (i < curves.length) {
            curves[i] = GMLGeometryAdapter.wrapLineString(XMLTools.getFirstChildElement(el.item(i)), srsName);
            ++i;
        }
        MultiCurve mp = GeometryFactory.createMultiCurve((Curve[])curves, (CoordinateSystem)crs);
        return mp;
    }

    private static MultiSurface wrapMultiPolygon(Element element, String srsName) throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        CoordinateSystem crs = null;
        if (srsName != null) {
            crs = GMLGeometryAdapter.getCRS(srsName);
        }
        ElementList el = XMLTools.getChildElements("polygonMember", CommonNamespaces.GMLNS, element);
        Surface[] surfaces = new Surface[el.getLength()];
        int i = 0;
        while (i < surfaces.length) {
            surfaces[i] = GMLGeometryAdapter.wrapPolygon(XMLTools.getFirstChildElement(el.item(i)), srsName);
            ++i;
        }
        return GeometryFactory.createMultiSurface((Surface[])surfaces, (CoordinateSystem)crs);
    }

    private static Surface wrapBoxAsSurface(Element element, String srsName) throws XMLParsingException, GeometryException, InvalidGMLException, UnknownCRSException {
        Envelope env = GMLGeometryAdapter.wrapBox(element, srsName);
        return GeometryFactory.createSurface((Envelope)env, (CoordinateSystem)env.getCoordinateSystem());
    }

    private static CompositeSurface wrapCompositeSurface(Element element, String srsName) {
        throw new UnsupportedOperationException("#wrapCompositeSurface(Element) " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.is-not-implemented-as-yet") + ". " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.work-in-progress") + ".");
    }

    private static Element extractPatches(Element surface) throws XMLParsingException {
        Element patches = null;
        try {
            patches = (Element)XMLTools.getRequiredNode(surface, "gml:patches", nsContext);
        }
        catch (XMLParsingException e) {
            throw new XMLParsingException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.error-retrieving-the-patches-element-from-the-surface-element")) + ".");
        }
        return patches;
    }

    public static CoordinateSystem getCRS(String name) throws UnknownCRSException {
        CoordinateSystem crs;
        int p;
        if (name != null && name.length() > 2 && name.startsWith("http://www.opengis.net/gml/srs/") && (p = name.lastIndexOf("/")) >= 0) {
            name = name.substring(p, name.length());
            p = name.indexOf(".");
            String s1 = name.substring(1, p).toUpperCase();
            p = name.indexOf("#");
            String s2 = name.substring(p + 1, name.length());
            name = String.valueOf(s1) + ":" + s2;
        }
        if ((crs = crsMap.get(name)) == null) {
            String epsgCode = GMLGeometryAdapter.transformCRSNameToEPSG(name);
            crs = CRSFactory.create((String)epsgCode);
            crsMap.put(name, crs);
        }
        return crs;
    }

    public static String transformCRSNameToEPSG(String name) {
        String result = null;
        result = StringUtils.startsWithIgnoreCase((String)name, (String)EPSG_SINGLE) ? name : (StringUtils.startsWithIgnoreCase((String)name, (String)EPSG_DOUBLE) ? String.valueOf(EPSG_SINGLE) + StringUtils.substringAfterLast((String)name, (String)":") : (StringUtils.startsWithIgnoreCase((String)name, (String)X_OGC) ? String.valueOf(EPSG_SINGLE) + StringUtils.substringAfterLast((String)name, (String)":") : (StringUtils.startsWithIgnoreCase((String)name, (String)OGC) ? String.valueOf(EPSG_SINGLE) + StringUtils.substringAfterLast((String)name, (String)":") : (StringUtils.containsIgnoreCase((String)name, (String)"epsg.xml") ? String.valueOf(EPSG_SINGLE) + StringUtils.substringAfterLast((String)name, (String)"#") : (StringUtils.containsIgnoreCase((String)name, (String)"urn:epsg") ? String.valueOf(EPSG_SINGLE) + StringUtils.substringAfterLast((String)name, (String)":") : name)))));
        LOG.logDebug("Transformed from " + name + " to " + result);
        return result;
    }

    private static Position createPositionFromCorner(Element corner) throws InvalidGMLException {
        String tmp = XMLTools.getAttrValue(corner, null, "dimension", null);
        int dim = 0;
        if (tmp != null) {
            dim = Integer.parseInt(tmp);
        }
        tmp = XMLTools.getStringValue(corner);
        double[] vals = StringTools.toArrayDouble((String)tmp, (String)", ");
        if (dim != 0) {
            if (vals.length != dim) {
                throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.dimension-must-be-equal-to-the-number-of-coordinate-values-defined-in-pos-element")) + ".");
            }
        } else {
            dim = vals.length;
        }
        Position pos = null;
        pos = dim == 3 ? GeometryFactory.createPosition((double)vals[0], (double)vals[1], (double)vals[2]) : GeometryFactory.createPosition((double)vals[0], (double)vals[1]);
        return pos;
    }

    private static Position createPositionFromCoord(Element element) throws XMLParsingException {
        Position pos = null;
        Element elem = XMLTools.getRequiredChildElement("X", CommonNamespaces.GMLNS, element);
        double x = Double.parseDouble(XMLTools.getStringValue(elem));
        elem = XMLTools.getRequiredChildElement("Y", CommonNamespaces.GMLNS, element);
        double y = Double.parseDouble(XMLTools.getStringValue(elem));
        elem = XMLTools.getChildElement("Z", CommonNamespaces.GMLNS, element);
        if (elem != null) {
            double z = Double.parseDouble(XMLTools.getStringValue(elem));
            pos = GeometryFactory.createPosition((double[])new double[]{x, y, z});
        } else {
            pos = GeometryFactory.createPosition((double[])new double[]{x, y});
        }
        return pos;
    }

    private static Position[] createPositionFromCoordinates(Element element) {
        Position[] points = null;
        String ts = XMLTools.getAttrValue(element, null, "ts", " ");
        String cs = XMLTools.getAttrValue(element, null, "cs", ",");
        String value = XMLTools.getStringValue(element).trim();
        StringTokenizer tuple = new StringTokenizer(value, ts);
        points = new Position[tuple.countTokens()];
        int i = 0;
        while (tuple.hasMoreTokens()) {
            String s = tuple.nextToken();
            StringTokenizer coort = new StringTokenizer(s, cs);
            double[] p = new double[coort.countTokens()];
            int k = 0;
            while (k < p.length) {
                s = coort.nextToken();
                p[k] = Double.parseDouble(s);
                ++k;
            }
            points[i++] = GeometryFactory.createPosition((double[])p);
        }
        return points;
    }

    private static Position createPositionFromPos(Element element) throws InvalidGMLException {
        double[] vals;
        String tmp = XMLTools.getAttrValue(element, null, "dimension", null);
        int dim = 0;
        if (tmp != null) {
            dim = Integer.parseInt(tmp);
        }
        if ((vals = StringTools.toArrayDouble((String)(tmp = XMLTools.getStringValue(element)), (String)"\t\n\r\f ,")) != null) {
            if (dim != 0) {
                if (vals.length != dim) {
                    throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.dimension-must-be-equal-to-the-number-of-coordinate-values-defined-in-pos-element")) + ".");
                }
            } else {
                dim = vals.length;
            }
        } else {
            throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.the-given-element")) + "{" + element.getNamespaceURI() + "}" + element.getLocalName() + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.does-not-contain-any-coordinates") + ". " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.this-may-not-be"));
        }
        Position pos = null;
        pos = dim == 3 ? GeometryFactory.createPosition((double)vals[0], (double)vals[1], (double)vals[2]) : GeometryFactory.createPosition((double)vals[0], (double)vals[1]);
        return pos;
    }

    private static Position[] createPositionFromPosList(Element element, String srsName, String srsDimension) throws InvalidGMLException, XMLParsingException {
        if (srsName == null) {
            srsName = GMLGeometryAdapter.findSrsName(element, srsName);
        }
        if (srsDimension == null) {
            srsDimension = GMLGeometryAdapter.findSrsDimension(element);
        }
        if (LOG.getLevel() == 0) {
            XMLFragment doc = new XMLFragment(element);
            System.out.println(doc.getAsPrettyString());
        }
        int dim = 0;
        if (srsDimension != null) {
            dim = Integer.parseInt(srsDimension);
        }
        if (dim == 0) {
            dim = 2;
        }
        String axisLabels = XMLTools.getAttrValue(element, null, "gml:axisAbbrev", null);
        String uomLabels = XMLTools.getAttrValue(element, null, "uomLabels", null);
        if (srsName == null) {
            if (srsDimension != null) {
                throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.attribute-srsdimension-cannot-be-defined-unless-attribute-srsname-has-been-defined")) + ".");
            }
            if (axisLabels != null) {
                throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.attribute-axislabels-cannot-be-defined-unless-attribute-srsname-has-been-defined")) + ".");
            }
        }
        if (axisLabels == null && uomLabels != null) {
            throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.attribute-uomlabels-cannot-be-defined-unless-attribute-axisLabels-has-been-defined")) + ".");
        }
        String tmp = XMLTools.getStringValue(element);
        double[] values = StringTools.toArrayDouble((String)tmp, (String)"\t\n\r\f ,");
        int size = values.length / dim;
        LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.number-of-points")) + " = ", (Object)size);
        LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.size-of-the-original-array")) + ": ", (Object)values.length);
        LOG.logDebug(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.dimension")) + ": ", (Object)dim);
        if (values.length < 4) {
            throw new InvalidGMLException(String.valueOf(I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.a-point-list-must-have-minimum-two-coordinate-tuples")) + ". " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.here-only") + " '" + size + "' " + I18N.getString("org.deegree.model.spatialschema.GMLGeometryAdapter.are-defined") + ".");
        }
        double[][] positions = new double[size][dim];
        int a = 0;
        int b = 0;
        int i = 0;
        while (i < values.length) {
            if (b == dim) {
                ++a;
                b = 0;
            }
            positions[a][b] = values[i];
            ++b;
            ++i;
        }
        Position[] position = new Position[positions.length];
        int i2 = 0;
        while (i2 < positions.length) {
            double[] vals = positions[i2];
            position[i2] = dim == 3 ? GeometryFactory.createPosition((double)vals[0], (double)vals[1], (double)vals[2]) : GeometryFactory.createPosition((double)vals[0], (double)vals[1]);
            ++i2;
        }
        return position;
    }

    private static Position[] createPositions(Element parent, String srsName, String srsDimension) throws XMLParsingException, InvalidGMLException {
        List<Node> nl = XMLTools.getNodes(parent, COORDINATES, nsContext);
        Position[] pos = null;
        if (nl != null && nl.size() > 0) {
            pos = GMLGeometryAdapter.createPositionFromCoordinates((Element)nl.get(0));
        } else {
            nl = XMLTools.getNodes(parent, POS, nsContext);
            if (nl != null && nl.size() > 0) {
                pos = new Position[nl.size()];
                int i = 0;
                while (i < pos.length) {
                    pos[i] = GMLGeometryAdapter.createPositionFromPos((Element)nl.get(i));
                    ++i;
                }
            } else {
                Element posList = (Element)XMLTools.getRequiredNode(parent, POSLIST, nsContext);
                if (posList != null) {
                    pos = GMLGeometryAdapter.createPositionFromPosList(posList, srsName, srsDimension);
                }
            }
        }
        return pos;
    }

    public static PrintWriter export(Geometry geometry, OutputStream target, String formatType) throws GeometryException {
        PrintWriter printwriter = new PrintWriter(target);
        if (geometry instanceof SurfacePatch) {
            geometry = new SurfaceImpl((SurfacePatch)geometry);
        } else if (geometry instanceof LineString) {
            geometry = new CurveImpl((CurveSegment)((LineString)geometry));
        }
        if (geometry instanceof Point) {
            GMLGeometryAdapter.exportPoint((Point)geometry, printwriter);
        } else if (geometry instanceof Curve) {
            GMLGeometryAdapter.exportCurve((Curve)geometry, printwriter);
        } else if (geometry instanceof Surface) {
            GMLGeometryAdapter.exportSurface((Surface)geometry, printwriter, formatType);
        } else if (geometry instanceof MultiPoint) {
            GMLGeometryAdapter.exportMultiPoint((MultiPoint)geometry, printwriter);
        } else if (geometry instanceof MultiCurve) {
            GMLGeometryAdapter.exportMultiCurve((MultiCurve)geometry, printwriter);
        } else if (geometry instanceof MultiSurface) {
            GMLGeometryAdapter.exportMultiSurface((MultiSurface)geometry, printwriter, formatType);
        }
        printwriter.flush();
        return printwriter;
    }

    public static StringBuffer export(Geometry geometry, String formatType) throws GeometryException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(5000);
        GMLGeometryAdapter.export(geometry, bos, formatType);
        return new StringBuffer(new String(bos.toByteArray()));
    }

    public static StringBuffer exportAsBox(Envelope envelope) {
        StringBuffer sb = new StringBuffer("<gml:Box xmlns:gml=\"http://www.opengis.net/gml\">");
        sb.append("<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">");
        sb.append(envelope.getMin().getX()).append(',');
        sb.append(envelope.getMin().getY());
        int dim = envelope.getMax().getCoordinateDimension();
        if (dim == 3) {
            sb.append(',').append(envelope.getMin().getZ());
        }
        sb.append(' ').append(envelope.getMax().getX());
        sb.append(',').append(envelope.getMax().getY());
        if (dim == 3) {
            sb.append(',').append(envelope.getMax().getZ());
        }
        sb.append("</gml:coordinates></gml:Box>");
        return sb;
    }

    public static StringBuffer exportAsEnvelope(Envelope envelope, String formatType) {
        String crs = null;
        if (envelope.getCoordinateSystem() != null) {
            crs = envelope.getCoordinateSystem().getIdentifier().replace(' ', ':');
        }
        String srs = null;
        srs = crs != null ? "<gml:Envelope srsName=\"" + crs + "\"" : "<gml:Envelope";
        StringBuffer sb = new StringBuffer(srs);
        int dim = envelope.getMax().getCoordinateDimension();
        if (formatType.contains(GML2_FORMAT_TYPE)) {
            sb.append("xmlns:gml='http://www.opengis.net/gml'>");
            sb.append("<gml:coordinates cs=\",\" decimal=\".\" ts=\" \">");
            sb.append(envelope.getMin().getX()).append(',');
            sb.append(envelope.getMin().getY());
            if (dim == 3) {
                sb.append(',').append(envelope.getMin().getZ());
            }
            sb.append(' ').append(envelope.getMax().getX());
            sb.append(',').append(envelope.getMax().getY());
            if (dim == 3) {
                sb.append(',').append(envelope.getMax().getZ());
            }
            sb.append("</gml:coordinates>");
        } else {
            sb.append(">");
            sb.append("<gml:lowerCorner>");
            sb.append(envelope.getMin().getX()).append(' ');
            sb.append(envelope.getMin().getY());
            if (dim == 3) {
                sb.append(' ').append(envelope.getMin().getZ());
            }
            sb.append("</gml:lowerCorner>");
            sb.append("<gml:upperCorner>");
            sb.append(envelope.getMax().getX());
            sb.append(' ').append(envelope.getMax().getY());
            if (dim == 3) {
                sb.append(' ').append(envelope.getMax().getZ());
            }
            sb.append("</gml:upperCorner>");
        }
        sb.append("</gml:Envelope>");
        return sb;
    }

    protected static void exportPoint(Point point, PrintWriter pw) {
        String crs = null;
        int dim = point.getCoordinateDimension();
        if (point.getCoordinateSystem() != null) {
            crs = point.getCoordinateSystem().getIdentifier().replace(' ', ':');
            dim = point.getCoordinateSystem().getDimension();
        }
        String srs = null;
        srs = crs != null ? "<gml:Point srsName=\"" + crs + "\">" : "<gml:Point>";
        pw.println(srs);
        if (dim != 0) {
            String dimension = "<gml:pos srsDimension=\"" + dim + "\">";
            pw.print(dimension);
        } else {
            pw.print("<gml:pos>");
        }
        String coordinates = String.valueOf(point.getX()) + " " + point.getY();
        if (dim == 3) {
            coordinates = String.valueOf(coordinates) + " " + point.getZ();
        }
        pw.print(coordinates);
        pw.println("</gml:pos>");
        pw.print("</gml:Point>");
    }

    protected static void exportCurve(Curve o, PrintWriter pw) throws GeometryException {
        String crs = null;
        if (o.getCoordinateSystem() != null) {
            crs = o.getCoordinateSystem().getIdentifier().replace(' ', ':');
        }
        String srs = null;
        srs = crs != null ? "<gml:Curve srsName=\"" + crs + "\">" : "<gml:Curve>";
        pw.println(srs);
        pw.println("<gml:segments>");
        int curveSegments = o.getNumberOfCurveSegments();
        int i = 0;
        while (i < curveSegments) {
            pw.print("<gml:LineStringSegment>");
            CurveSegment segment = o.getCurveSegmentAt(i);
            Position[] p = segment.getAsLineString().getPositions();
            pw.print("<gml:posList>");
            int j = 0;
            while (j < p.length - 1) {
                pw.print(String.valueOf(p[j].getX()) + " " + p[j].getY());
                if (o.getCoordinateDimension() == 3) {
                    pw.print(' ');
                    pw.print(p[j].getZ());
                    pw.print(' ');
                } else {
                    pw.print(' ');
                }
                ++j;
            }
            pw.print(String.valueOf(p[p.length - 1].getX()) + " " + p[p.length - 1].getY());
            if (o.getCoordinateDimension() == 3) {
                pw.print(" " + p[p.length - 1].getZ());
            }
            pw.println("</gml:posList>");
            pw.println("</gml:LineStringSegment>");
            ++i;
        }
        pw.println("</gml:segments>");
        pw.print("</gml:Curve>");
    }

    protected static void exportSurface(Surface surface, PrintWriter pw, String formatType) throws GeometryException {
        String crs = null;
        if (surface.getCoordinateSystem() != null) {
            crs = surface.getCoordinateSystem().getIdentifier().replace(' ', ':');
        }
        if (formatType.contains(GML2_FORMAT_TYPE)) {
            pw.println("<gml:Polygon>");
            int patches = surface.getNumberOfSurfacePatches();
            int i = 0;
            while (i < patches) {
                SurfacePatch patch = surface.getSurfacePatchAt(i);
                if (i == 0) {
                    GMLGeometryAdapter.printExteriorRing(surface, pw, patch, formatType);
                } else {
                    GMLGeometryAdapter.printInteriorRing(surface, pw, patch, formatType);
                }
                ++i;
            }
            pw.println("</gml:Polygon>");
        } else {
            String srs = null;
            srs = crs != null ? "<gml:Surface srsName='" + crs + "'>" : "<gml:Surface>";
            pw.println(srs);
            int patches = surface.getNumberOfSurfacePatches();
            pw.println("<gml:patches>");
            int i = 0;
            while (i < patches) {
                pw.println("<gml:PolygonPatch>");
                SurfacePatch patch = surface.getSurfacePatchAt(i);
                GMLGeometryAdapter.printExteriorRing(surface, pw, patch, formatType);
                GMLGeometryAdapter.printInteriorRing(surface, pw, patch, formatType);
                pw.println("</gml:PolygonPatch>");
                ++i;
            }
            pw.println("</gml:patches>");
            pw.print("</gml:Surface>");
        }
    }

    protected static void printInteriorRing(Surface surface, PrintWriter pw, SurfacePatch patch, String formatType) {
        Position[][] ip = patch.getInteriorRings();
        if (ip != null) {
            int j = 0;
            while (j < ip.length) {
                if (formatType.contains(GML2_FORMAT_TYPE)) {
                    pw.println("<gml:innerBoundaryIs>");
                } else {
                    pw.println("<gml:interior>");
                }
                pw.println("<gml:LinearRing>");
                if (surface.getCoordinateSystem() != null) {
                    GMLGeometryAdapter.printPositions(pw, ip[j], surface.getCoordinateDimension(), formatType);
                } else {
                    GMLGeometryAdapter.printPositions(pw, ip[j], 0, formatType);
                }
                pw.println("</gml:LinearRing>");
                if (formatType.contains(GML2_FORMAT_TYPE)) {
                    pw.println("</gml:innerBoundaryIs>");
                } else {
                    pw.println("</gml:interior>");
                }
                ++j;
            }
        }
    }

    protected static void printExteriorRing(Surface surface, PrintWriter pw, SurfacePatch patch, String formatType) {
        if (formatType.contains(GML2_FORMAT_TYPE)) {
            pw.print("<gml:outerBoundaryIs>");
        } else {
            pw.print("<gml:exterior>");
        }
        pw.print("<gml:LinearRing>");
        if (surface.getCoordinateSystem() != null) {
            GMLGeometryAdapter.printPositions(pw, patch.getExteriorRing(), surface.getCoordinateDimension(), formatType);
        } else {
            GMLGeometryAdapter.printPositions(pw, patch.getExteriorRing(), 0, formatType);
        }
        pw.print("</gml:LinearRing>");
        if (formatType.contains(GML2_FORMAT_TYPE)) {
            pw.print("</gml:outerBoundaryIs>");
        } else {
            pw.print("</gml:exterior>");
        }
    }

    protected static void printRing(PrintWriter pw, Ring ring, int coordinateDimension, String formatType) {
        pw.print("<gml:Ring><gml:curveMember><gml:Curve><gml:segments>");
        CurveSegment[] cs = ring.getCurveSegments();
        int i = 0;
        while (i < cs.length) {
            GMLGeometryAdapter.printCurveSegment(pw, cs[i], coordinateDimension, formatType);
            ++i;
        }
        pw.print("</gml:segments></gml:Curve></gml:curveMember></gml:Ring>");
    }

    private static void printCurveSegment(PrintWriter pw, CurveSegment segment, int coordinateDimension, String formatType) {
        pw.print("<gml:LineStringSegment>");
        GMLGeometryAdapter.printPositions(pw, segment.getPositions(), coordinateDimension, formatType);
        pw.print("</gml:LineStringSegment>");
    }

    private static void printPositions(PrintWriter pw, Position[] p, int coordinateDimension, String formatType) {
        String coordSeparator;
        String startTag = formatType.equals(GML2_FORMAT_TYPE) ? "<gml:coordinates" : "<gml:posList";
        String endTag = formatType.equals(GML2_FORMAT_TYPE) ? "</gml:coordinates>" : "</gml:posList>";
        StringBuilder posList = new StringBuilder(startTag);
        String string = coordSeparator = formatType.equals(GML2_FORMAT_TYPE) ? "," : " ";
        if (formatType.equals(GML2_FORMAT_TYPE)) {
            posList.append(" decimal=\".\" cs=\",\" ts=\" \"");
        } else {
            if (coordinateDimension > 0) {
                posList.append(" srsDimension='").append(coordinateDimension).append("'");
            }
            posList.append(" count='").append(p.length).append("'");
        }
        posList.append(">");
        pw.print(posList);
        int j = 0;
        while (j < p.length - 1) {
            pw.print(String.valueOf(p[j].getX()) + coordSeparator + p[j].getY());
            if (coordinateDimension == 3) {
                pw.print(String.valueOf(coordSeparator) + p[j].getZ() + " ");
            } else {
                pw.print(' ');
            }
            ++j;
        }
        pw.print(String.valueOf(p[p.length - 1].getX()) + coordSeparator + p[p.length - 1].getY());
        if (coordinateDimension == 3) {
            pw.print(String.valueOf(coordSeparator) + p[p.length - 1].getZ());
        }
        pw.print(endTag);
    }

    protected static void exportMultiPoint(MultiPoint mp, PrintWriter pw) {
        String crs = null;
        if (mp.getCoordinateSystem() != null) {
            crs = mp.getCoordinateSystem().getIdentifier().replace(' ', ':');
        }
        String srs = null;
        srs = crs != null ? "<gml:MultiPoint srsName=\"" + crs + "\">" : "<gml:MultiPoint>";
        pw.println(srs);
        pw.println("<gml:pointMembers>");
        int i = 0;
        while (i < mp.getSize()) {
            pw.println("<gml:Point>");
            pw.print("<gml:pos>");
            pw.print(String.valueOf(mp.getPointAt(i).getX()) + " " + mp.getPointAt(i).getY());
            if (mp.getPointAt(i).getCoordinateDimension() == 3) {
                pw.print(" " + mp.getPointAt(i).getZ());
            }
            pw.println("</gml:pos>");
            pw.println("</gml:Point>");
            ++i;
        }
        pw.println("</gml:pointMembers>");
        pw.print("</gml:MultiPoint>");
    }

    protected static void exportMultiCurve(MultiCurve multiCurve, PrintWriter pw) throws GeometryException {
        String crs = null;
        if (multiCurve.getCoordinateSystem() != null) {
            crs = multiCurve.getCoordinateSystem().getIdentifier().replace(' ', ':');
        }
        String srs = null;
        srs = crs != null ? "<gml:MultiCurve srsName=\"" + crs + "\">" : "<gml:MultiCurve>";
        pw.println(srs);
        Curve[] curves = multiCurve.getAllCurves();
        pw.println("<gml:curveMembers>");
        int i = 0;
        while (i < curves.length) {
            Curve curve = curves[i];
            pw.println("<gml:Curve>");
            pw.println("<gml:segments>");
            pw.println("<gml:LineStringSegment>");
            int numberCurveSegments = curve.getNumberOfCurveSegments();
            int j = 0;
            while (j < numberCurveSegments) {
                CurveSegment curveSegment = curve.getCurveSegmentAt(j);
                Position[] p = curveSegment.getAsLineString().getPositions();
                pw.print("<gml:posList>");
                int k = 0;
                while (k < p.length - 1) {
                    pw.print(String.valueOf(p[k].getX()) + " " + p[k].getY());
                    if (curve.getCoordinateDimension() == 3) {
                        pw.print(" " + p[k].getZ() + " ");
                    } else {
                        pw.print(" ");
                    }
                    ++k;
                }
                pw.print(String.valueOf(p[p.length - 1].getX()) + " " + p[p.length - 1].getY());
                if (curve.getCoordinateDimension() == 3) {
                    pw.print(" " + p[p.length - 1].getZ());
                }
                pw.println("</gml:posList>");
                ++j;
            }
            pw.println("</gml:LineStringSegment>");
            pw.println("</gml:segments>");
            pw.println("</gml:Curve>");
            ++i;
        }
        pw.println("</gml:curveMembers>");
        pw.print("</gml:MultiCurve>");
    }

    protected static void exportMultiSurface(MultiSurface multiSurface, PrintWriter pw, String formatType) throws GeometryException {
        String crs = null;
        if (multiSurface.getCoordinateSystem() != null) {
            crs = multiSurface.getCoordinateSystem().getIdentifier().replace(' ', ':');
        }
        String srs = null;
        srs = crs != null ? "<gml:MultiSurface srsName=\"" + crs + "\">" : "<gml:MultiSurface>";
        pw.println(srs);
        Surface[] surfaces = multiSurface.getAllSurfaces();
        pw.println("<gml:surfaceMembers>");
        int i = 0;
        while (i < surfaces.length) {
            Surface surface = surfaces[i];
            GMLGeometryAdapter.exportSurface(surface, pw, formatType);
            ++i;
        }
        pw.println("</gml:surfaceMembers>");
        pw.print("</gml:MultiSurface>");
    }

    @Deprecated
    public static Geometry wrap(String gml) throws GeometryException, XMLParsingException {
        return GMLGeometryAdapter.wrap(gml, null);
    }

    @Deprecated
    public static Geometry wrap(Element gml) throws GeometryException {
        return GMLGeometryAdapter.wrap(gml, null);
    }

    @Deprecated
    public static Envelope wrapBox(Element element) throws XMLParsingException, InvalidGMLException, UnknownCRSException {
        return GMLGeometryAdapter.wrapBox(element, null);
    }

    private static Position[] correctRing(Position[] ringPositions) {
        if (ringPositions != null && ringPositions.length > 2 && !ringPositions[0].equals(ringPositions[ringPositions.length - 1])) {
            ringPositions = (Position[])Arrays.copyOf(ringPositions, ringPositions.length + 1, Position[].class);
            ringPositions[ringPositions.length - 1] = ringPositions[0];
        } else if (ringPositions != null && ringPositions.length == 2) {
            ringPositions = (Position[])Arrays.copyOf(ringPositions, ringPositions.length + 2, Position[].class);
            ringPositions[2] = ringPositions[0];
            ringPositions[3] = ringPositions[0];
        } else if (ringPositions != null && ringPositions.length == 1) {
            ringPositions = (Position[])Arrays.copyOf(ringPositions, ringPositions.length + 3, Position[].class);
            ringPositions[1] = ringPositions[0];
            ringPositions[2] = ringPositions[0];
            ringPositions[3] = ringPositions[0];
        } else if (ringPositions != null && ringPositions.length == 0) {
            ringPositions = new Position[]{new PositionImpl(0.0, 0.0), new PositionImpl(0.0, 0.0), new PositionImpl(0.0, 0.0), new PositionImpl(0.0, 0.0)};
        }
        return ringPositions;
    }
}

