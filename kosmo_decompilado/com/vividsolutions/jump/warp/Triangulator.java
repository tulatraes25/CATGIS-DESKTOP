/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.warp;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CollectionMap;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.warp.Quadrilateral;
import com.vividsolutions.jump.warp.TaggedCoordinate;
import com.vividsolutions.jump.warp.Triangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.saig.jump.lang.I18N;

public class Triangulator {
    private GeometryFactory factory = new GeometryFactory();
    private Collection<Geometry> ignoredVectors = new ArrayList<Geometry>();

    public Map<Triangle, Triangle> triangleMap(Envelope datasetEnvelope, Collection<Geometry> vectorLineStrings, TaskMonitor monitor) {
        return this.triangleMap(datasetEnvelope, vectorLineStrings, new ArrayList<TaggedCoordinate>(), new ArrayList<TaggedCoordinate>(), monitor);
    }

    public Map<Triangle, Triangle> triangleMap(Envelope datasetEnvelope, Collection<Geometry> vectorLineStrings, Collection<TaggedCoordinate> sourceHints, Collection<TaggedCoordinate> destinationHints, TaskMonitor monitor) {
        Quadrilateral destQuad;
        Quadrilateral sourceQuad;
        Collection<Geometry> auxList = CollectionUtil.filterByClass(vectorLineStrings, LineString.class);
        ArrayList<LineString> vectorListCopy = new ArrayList<LineString>(auxList.size());
        for (Geometry geom : auxList) {
            vectorListCopy.add((LineString)geom);
        }
        Assert.isTrue((!datasetEnvelope.isNull() ? 1 : 0) != 0);
        Envelope sourceEnvelope = new Envelope(datasetEnvelope);
        while (!(this.outlyingVectors(sourceQuad = this.sourceQuad(sourceEnvelope), destQuad = this.destQuad(sourceQuad, vectorListCopy), vectorListCopy).isEmpty() && sourceQuad.verticesOutside(sourceHints).isEmpty() && destQuad.verticesOutside(destinationHints).isEmpty())) {
            sourceEnvelope = sourceQuad.getEnvelope();
        }
        Quadrilateral taggedSourceQuad = this.tag(sourceQuad, destQuad);
        List<Triangle> taggedSourceTriangles = this.triangulate(taggedSourceQuad, Triangulator.taggedVectorVertices(false, vectorListCopy), monitor);
        return this.triangleMap(taggedSourceTriangles);
    }

    public Collection<Geometry> getIgnoredVectors() {
        return Collections.unmodifiableCollection(this.ignoredVectors);
    }

    public static Collection<Geometry> nonVectors(Collection<Geometry> geometries) {
        TreeSet<Geometry> nonVectors = new TreeSet<Geometry>();
        for (Geometry g : geometries) {
            if (Triangulator.vector(g)) continue;
            nonVectors.add(g);
        }
        return nonVectors;
    }

    public static boolean vector(Geometry g) {
        return g.getClass() == LineString.class && ((LineString)g).getNumPoints() == 2;
    }

    private TreeSet<LineString> outlyingVectors(Quadrilateral sourceQuad, Quadrilateral destQuad, Collection<LineString> vectors) {
        TreeSet<LineString> outliers = new TreeSet<LineString>();
        outliers.addAll(this.toVectors(sourceQuad.verticesOutside(Triangulator.taggedVectorVertices(false, vectors)), false));
        outliers.addAll(this.toVectors(destQuad.verticesOutside(Triangulator.taggedVectorVertices(true, vectors)), true));
        return outliers;
    }

    protected List<Triangle> heightMaximizedTriangles(Triangle PQS, Triangle QRS) {
        List<Triangle> originalTriangles = Arrays.asList(PQS, QRS);
        List<Triangle> alternativeTriangles = this.alternativeTriangles(PQS, QRS);
        if (alternativeTriangles == null) {
            return originalTriangles;
        }
        Triangle t1 = alternativeTriangles.get(0);
        Triangle t2 = alternativeTriangles.get(1);
        if (Math.min(PQS.getMinHeight(), QRS.getMinHeight()) > Math.min(t1.getMinHeight(), t2.getMinHeight())) {
            return originalTriangles;
        }
        return alternativeTriangles;
    }

    protected Triangle triangleContaining(Coordinate p, List<Triangle> triangles) {
        for (Triangle triangle : triangles) {
            if (!triangle.contains(p)) continue;
            return triangle;
        }
        return null;
    }

    protected Coordinate add(Coordinate a, LineString vector) {
        return new Coordinate(a.x + vector.getCoordinateN((int)1).x - vector.getCoordinateN((int)0).x, a.y + vector.getCoordinateN((int)1).y - vector.getCoordinateN((int)0).y);
    }

    protected LineString vectorWithNearestTail(Coordinate x, List<LineString> vectors) {
        Assert.isTrue((vectors.size() > 0 ? 1 : 0) != 0);
        LineString vectorWithNearestTail = vectors.get(0);
        for (LineString candidate : vectors) {
            if (!(candidate.getCoordinateN(0).distance(x) < vectorWithNearestTail.getCoordinateN(0).distance(x))) continue;
            vectorWithNearestTail = candidate;
        }
        return vectorWithNearestTail;
    }

    protected Quadrilateral tag(Quadrilateral sourceQuad, Quadrilateral destQuad) {
        return new Quadrilateral(new TaggedCoordinate(sourceQuad.getP1(), destQuad.getP1()), new TaggedCoordinate(sourceQuad.getP2(), destQuad.getP2()), new TaggedCoordinate(sourceQuad.getP3(), destQuad.getP3()), new TaggedCoordinate(sourceQuad.getP4(), destQuad.getP4()));
    }

    protected List<Triangle> alternativeTriangles(Triangle PQS, Triangle QRS) {
        Quadrilateral quad = this.dissolve(PQS, QRS);
        if (!quad.isConvex()) {
            return null;
        }
        return quad.triangles();
    }

    private Quadrilateral sourceQuad(Envelope datasetEnvelope) {
        double dx = datasetEnvelope.getWidth() * 0.05;
        double dy = datasetEnvelope.getHeight() * 0.05;
        return new Quadrilateral(new Coordinate(datasetEnvelope.getMinX() - dx, datasetEnvelope.getMinY() - dy), new Coordinate(datasetEnvelope.getMaxX() + dx, datasetEnvelope.getMinY() - dy), new Coordinate(datasetEnvelope.getMaxX() + dx, datasetEnvelope.getMaxY() + dy), new Coordinate(datasetEnvelope.getMinX() - dx, datasetEnvelope.getMaxY() + dy));
    }

    private void triangulate(List<Triangle> triangles, Coordinate newVertex) {
        Triangle triangleContainingNewVertex = this.triangleContaining(newVertex, triangles);
        Assert.isTrue((triangleContainingNewVertex != null ? 1 : 0) != 0);
        triangles.remove(triangleContainingNewVertex);
        ArrayList<Triangle> trianglesToAdd = new ArrayList<Triangle>();
        for (Triangle newTriangle : triangleContainingNewVertex.subTriangles(newVertex)) {
            Triangle adjacentTriangle = this.adjacentTriangle(newTriangle, triangles);
            if (adjacentTriangle == null) {
                trianglesToAdd.add(newTriangle);
                continue;
            }
            triangles.remove(adjacentTriangle);
            trianglesToAdd.addAll(this.heightMaximizedTriangles(newTriangle, adjacentTriangle));
        }
        triangles.addAll(trianglesToAdd);
    }

    private Triangle adjacentTriangle(Triangle triangle, List<Triangle> triangles) {
        for (Triangle candidate : triangles) {
            int vertexMatches = 0;
            if (candidate.hasVertex(triangle.getP1())) {
                ++vertexMatches;
            }
            if (candidate.hasVertex(triangle.getP2())) {
                ++vertexMatches;
            }
            if (candidate.hasVertex(triangle.getP3())) {
                ++vertexMatches;
            }
            Assert.isTrue((vertexMatches != 3 ? 1 : 0) != 0, (String)(candidate + "; " + triangle));
            if (vertexMatches != 2) continue;
            return candidate;
        }
        return null;
    }

    private Quadrilateral destQuad(Quadrilateral sourceQuad, List<LineString> vectors) {
        if (vectors.isEmpty()) {
            return (Quadrilateral)sourceQuad.clone();
        }
        return new Quadrilateral(this.addVectorWithNearestTail(sourceQuad.getP1(), vectors), this.addVectorWithNearestTail(sourceQuad.getP2(), vectors), this.addVectorWithNearestTail(sourceQuad.getP3(), vectors), this.addVectorWithNearestTail(sourceQuad.getP4(), vectors));
    }

    private Coordinate addVectorWithNearestTail(Coordinate x, List<LineString> vectors) {
        return this.add(x, this.vectorWithNearestTail(x, vectors));
    }

    private List<Triangle> triangulate(Quadrilateral quad, List<TaggedCoordinate> vertices, TaskMonitor monitor) {
        monitor.allowCancellationRequests();
        monitor.report(String.valueOf(I18N.getString("com.vividsolutions.jump.warp.Triangulator.triangulating")) + "...");
        List<Triangle> triangles = quad.triangles();
        int count = 0;
        Iterator<TaggedCoordinate> i = vertices.iterator();
        while (i.hasNext() && !monitor.isCancelRequested()) {
            TaggedCoordinate vertex = i.next();
            this.triangulate(triangles, vertex);
            monitor.report(++count, vertices.size(), I18N.getString("com.vividsolutions.jump.warp.Triangulator.vectors"));
        }
        return triangles;
    }

    public static List<TaggedCoordinate> taggedVectorVertices(boolean tips, Collection<LineString> vectors) {
        ArrayList<TaggedCoordinate> taggedVectorVertices = new ArrayList<TaggedCoordinate>();
        for (LineString vector : vectors) {
            taggedVectorVertices.add(new TaggedCoordinate(tips ? vector.getCoordinateN(1) : vector.getCoordinateN(0), tips ? vector.getCoordinateN(0) : vector.getCoordinateN(1)));
        }
        return taggedVectorVertices;
    }

    private Map<Triangle, Triangle> triangleMap(List<Triangle> taggedSourceTriangles) {
        HashMap<Triangle, Triangle> triangleMap = new HashMap<Triangle, Triangle>();
        for (Triangle sourceTriangle : taggedSourceTriangles) {
            triangleMap.put(sourceTriangle, new Triangle(((TaggedCoordinate)sourceTriangle.getP1()).getTag(), ((TaggedCoordinate)sourceTriangle.getP2()).getTag(), ((TaggedCoordinate)sourceTriangle.getP3()).getTag()));
        }
        return triangleMap;
    }

    private LineString toVector(TaggedCoordinate c, boolean tips) {
        return this.factory.createLineString(new Coordinate[]{tips ? c.getTag() : c, tips ? c : c.getTag()});
    }

    private Quadrilateral dissolve(Triangle PQS, Triangle QRS) {
        CollectionMap vertexListMap = new CollectionMap(TreeMap.class);
        vertexListMap.addItem(PQS.getP1(), PQS.getP1());
        vertexListMap.addItem(PQS.getP2(), PQS.getP2());
        vertexListMap.addItem(PQS.getP3(), PQS.getP3());
        vertexListMap.addItem(QRS.getP1(), QRS.getP1());
        vertexListMap.addItem(QRS.getP2(), QRS.getP2());
        vertexListMap.addItem(QRS.getP3(), QRS.getP3());
        ArrayList<Coordinate> sharedVertices = new ArrayList<Coordinate>();
        ArrayList<Coordinate> unsharedVertices = new ArrayList<Coordinate>();
        for (Coordinate vertex : vertexListMap.keySet()) {
            if (vertexListMap.getItems(vertex).size() == 1) {
                unsharedVertices.add(vertex);
                continue;
            }
            if (vertexListMap.getItems(vertex).size() == 2) {
                sharedVertices.add(vertex);
                continue;
            }
            Assert.shouldNeverReachHere();
        }
        Assert.isTrue((2 == sharedVertices.size() ? 1 : 0) != 0, (String)(PQS + "; " + QRS));
        Assert.isTrue((2 == unsharedVertices.size() ? 1 : 0) != 0, (String)(PQS + "; " + QRS));
        return new Quadrilateral((Coordinate)unsharedVertices.get(0), (Coordinate)sharedVertices.get(0), (Coordinate)unsharedVertices.get(1), (Coordinate)sharedVertices.get(1));
    }

    private TreeSet<LineString> toVectors(Collection<TaggedCoordinate> taggedVectorVertices, boolean tips) {
        TreeSet<LineString> badVectors = new TreeSet<LineString>();
        for (TaggedCoordinate c : taggedVectorVertices) {
            badVectors.add(this.toVector(c, tips));
        }
        return badVectors;
    }
}

