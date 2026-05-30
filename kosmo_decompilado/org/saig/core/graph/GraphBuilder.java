/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  edu.uci.ics.jung.algorithms.connectivity.KNeighborhoodExtractor
 *  edu.uci.ics.jung.graph.DirectedGraph
 *  edu.uci.ics.jung.graph.Edge
 *  edu.uci.ics.jung.graph.Graph
 *  edu.uci.ics.jung.graph.Vertex
 *  org.apache.log4j.Logger
 */
package org.saig.core.graph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.workbench.model.Layer;
import edu.uci.ics.jung.algorithms.connectivity.KNeighborhoodExtractor;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import org.apache.log4j.Logger;
import org.saig.core.graph.DirectedGraphGenerator;
import org.saig.core.graph.UndirectedGraphGenerator;
import org.saig.core.model.feature.FeatureIterator;

public class GraphBuilder {
    private static final Logger LOGGER = Logger.getLogger(GraphBuilder.class);
    private static GraphBuilder sInstance = null;
    private static Hashtable<String, Graph> directedGraphs = new Hashtable();
    private static Hashtable<String, Graph> undirectedGraphs = new Hashtable();

    private GraphBuilder() {
    }

    public static synchronized GraphBuilder getUniqueInstance() {
        if (sInstance == null) {
            sInstance = new GraphBuilder();
        }
        return sInstance;
    }

    public Graph getDirectedGraph(Layer layer) {
        if (directedGraphs.containsKey(layer.getName())) {
            return directedGraphs.get(layer.getName());
        }
        FeatureCollectionWrapper fCollection = layer.getFeatureCollectionWrapper();
        FeatureIterator iter = null;
        try {
            iter = fCollection.iterator();
            DirectedGraphGenerator graphGen = new DirectedGraphGenerator();
            Feature feature = null;
            while (iter.hasNext()) {
                feature = iter.next();
                Geometry geom = feature.getGeometry();
                this.processGeometry(geom, graphGen, feature);
            }
            directedGraphs.put(layer.getName(), graphGen.getGraph());
            Graph graph = graphGen.getGraph();
            return graph;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        finally {
            if (iter != null) {
                iter.close();
            }
        }
        return null;
    }

    public Graph getUndirectedGraph(Layer layer) {
        return this.getUndirectedGraph(layer, false);
    }

    public Graph getUndirectedGraph(Layer layer, boolean allowParallelEdge) {
        String layerName = layer.getName();
        if (undirectedGraphs.containsKey(layerName)) {
            return undirectedGraphs.get(layerName);
        }
        FeatureCollectionWrapper fCollection = layer.getFeatureCollectionWrapper();
        FeatureIterator iter = null;
        try {
            iter = fCollection.iterator();
            UndirectedGraphGenerator graphGen = new UndirectedGraphGenerator(allowParallelEdge);
            Feature feature = null;
            while (iter.hasNext()) {
                feature = iter.next();
                Geometry geom = feature.getGeometry();
                this.processGeometry(geom, graphGen, feature);
            }
            Graph graph = graphGen.getGraph();
            undirectedGraphs.put(layerName, graph);
            Graph graph2 = graph;
            return graph2;
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        finally {
            if (iter != null) {
                iter.close();
            }
        }
        return null;
    }

    private void processGeometry(Geometry geom, UndirectedGraphGenerator graphGen, Feature feature) {
        if (geom == null || geom.isEmpty()) {
            return;
        }
        int numGeometries = geom.getNumGeometries();
        if (numGeometries == 1) {
            Coordinate[] coords = geom.getCoordinates();
            Coordinate p0 = coords[0];
            Coordinate p1 = coords[coords.length - 1];
            LineSegment lsg = new LineSegment(p0, p1);
            graphGen.add(lsg, feature);
            return;
        }
        int i = 0;
        while (i < numGeometries) {
            this.processGeometry(geom.getGeometryN(i), graphGen, feature);
            ++i;
        }
    }

    private void processGeometry(Geometry geom, DirectedGraphGenerator graphGen, Feature feature) {
        if (geom == null || geom.isEmpty()) {
            return;
        }
        int numGeometries = geom.getNumGeometries();
        if (numGeometries == 1) {
            Coordinate[] coords = geom.getCoordinates();
            Coordinate p0 = coords[0];
            Coordinate p1 = coords[coords.length - 1];
            LineSegment lsg = new LineSegment(p0, p1);
            graphGen.add(lsg, feature);
            return;
        }
        int i = 0;
        while (i < numGeometries) {
            this.processGeometry(geom.getGeometryN(i), graphGen, feature);
            ++i;
        }
    }

    public Graph getInDirectedWalk(Feature feature, DirectedGraph grafo) {
        Geometry geom = feature.getGeometry();
        Coordinate[] coords = geom.getCoordinates();
        Coordinate p0 = coords[geom.getNumPoints() - 1];
        Set vertices = grafo.getVertices();
        Vertex vertice = null;
        for (Vertex element : vertices) {
            Coordinate pointvertex = (Coordinate)element.getUserDatum((Object)"COORDINATE");
            if (!pointvertex.equals((Object)p0)) continue;
            vertice = element;
            break;
        }
        if (vertice == null) {
            return null;
        }
        HashSet<Vertex> nodos = new HashSet<Vertex>();
        nodos.add(vertice);
        Graph graphResult = KNeighborhoodExtractor.extractInDirectedNeighborhood((DirectedGraph)grafo, nodos, (int)grafo.numEdges());
        return graphResult;
    }

    public Graph getOutDirectedWalk(Feature feature, DirectedGraph grafo) {
        Geometry geom = feature.getGeometry();
        Coordinate[] coords = geom.getCoordinates();
        Coordinate p0 = coords[geom.getNumPoints() - 1];
        Set vertices = grafo.getVertices();
        Vertex vertice = null;
        for (Vertex element : vertices) {
            Coordinate pointvertex = (Coordinate)element.getUserDatum((Object)"COORDINATE");
            if (!pointvertex.equals((Object)p0)) continue;
            vertice = element;
            break;
        }
        if (vertice == null) {
            return null;
        }
        HashSet<Vertex> nodos = new HashSet<Vertex>();
        nodos.add(vertice);
        Graph graphResult = KNeighborhoodExtractor.extractOutDirectedNeighborhood((DirectedGraph)grafo, nodos, (int)grafo.numEdges());
        return graphResult;
    }

    public Graph getUndirectedWalk(Feature feature, Graph grafo) {
        Geometry geom = feature.getGeometry();
        Coordinate[] coords = geom.getCoordinates();
        Coordinate p0 = coords[0];
        Coordinate p1 = coords[geom.getNumPoints() - 1];
        Set vertices = grafo.getVertices();
        Vertex verticeInicial = null;
        Vertex verticeFinal = null;
        for (Vertex element : vertices) {
            Coordinate pointvertex = (Coordinate)element.getUserDatum((Object)"COORDINATE");
            if (verticeInicial == null && pointvertex.equals((Object)p0)) {
                verticeInicial = element;
            }
            if (verticeFinal == null && pointvertex.equals((Object)p1)) {
                verticeFinal = element;
            }
            if (verticeInicial != null && verticeFinal != null) break;
        }
        if (verticeInicial == null || verticeFinal == null) {
            return null;
        }
        HashSet<Vertex> nodos = new HashSet<Vertex>();
        nodos.add(verticeInicial);
        nodos.add(verticeFinal);
        Graph graphResult = KNeighborhoodExtractor.extractNeighborhood((Graph)grafo, nodos, (int)grafo.numEdges());
        return graphResult;
    }

    public static Set<Feature> extractEdges(Graph graph) {
        HashSet<Feature> resultado = new HashSet<Feature>();
        Set aristas = graph.getEdges();
        for (Edge element : aristas) {
            resultado.add((Feature)element.getUserDatum((Object)"FEATURE"));
        }
        return resultado;
    }

    public void invalidateGraphs(Layer layer) {
        this.invalidateDirectedGraph(layer);
        this.invalidateUndirectedGraph(layer);
    }

    public void invalidateDirectedGraph(Layer layer) {
        directedGraphs.remove(layer.getName());
    }

    public void invalidateUndirectedGraph(Layer layer) {
        undirectedGraphs.remove(layer.getName());
    }

    public Graph getSubgraphFrom(Vertex initialVertex, Graph originalGraph) {
        HashSet<Vertex> nodos = new HashSet<Vertex>();
        nodos.add(initialVertex);
        Graph graphResult = KNeighborhoodExtractor.extractNeighborhood((Graph)originalGraph, nodos, (int)originalGraph.numEdges());
        return graphResult;
    }
}

