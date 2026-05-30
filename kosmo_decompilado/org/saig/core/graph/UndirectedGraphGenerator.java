/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineSegment
 *  edu.uci.ics.jung.graph.Edge
 *  edu.uci.ics.jung.graph.Graph
 *  edu.uci.ics.jung.graph.Vertex
 *  edu.uci.ics.jung.graph.impl.UndirectedSparseEdge
 *  edu.uci.ics.jung.graph.impl.UndirectedSparseGraph
 *  edu.uci.ics.jung.graph.impl.UndirectedSparseVertex
 *  edu.uci.ics.jung.utils.UserData
 */
package org.saig.core.graph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.feature.Feature;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.UndirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;
import java.util.HashMap;

public class UndirectedGraphGenerator {
    private HashMap<Coordinate, Vertex> m_coord2node;
    private Graph grafo;
    private final boolean allowParallelEdge;

    public UndirectedGraphGenerator(boolean allowParallelEdge) {
        this.allowParallelEdge = allowParallelEdge;
        this.m_coord2node = new HashMap();
        this.grafo = new UndirectedSparseGraph();
        if (allowParallelEdge) {
            this.grafo.getEdgeConstraints().remove(Graph.NOT_PARALLEL_EDGE);
        }
    }

    public void add(LineSegment line, Feature feature) {
        Edge e;
        Vertex n2;
        Coordinate c = line.p0;
        Vertex n1 = this.m_coord2node.get(c);
        if (n1 == null) {
            n1 = this.grafo.addVertex((Vertex)new UndirectedSparseVertex());
            n1.setUserDatum((Object)"COORDINATE", (Object)c, UserData.SHARED);
            this.m_coord2node.put(c, n1);
        }
        if ((n2 = this.m_coord2node.get(c = line.p1)) == null) {
            n2 = this.grafo.addVertex((Vertex)new UndirectedSparseVertex());
            n2.setUserDatum((Object)"COORDINATE", (Object)c, UserData.SHARED);
            this.m_coord2node.put(c, n2);
        }
        if ((e = this.buildEdge(n1, n2)) != null) {
            e.addUserDatum((Object)"FEATURE", (Object)feature, UserData.SHARED);
            this.grafo.addEdge(e);
        }
    }

    public Edge buildEdge(Vertex nodeA, Vertex nodeB) {
        if (!this.allowParallelEdge) {
            for (Edge element : nodeA.getInEdges()) {
                for (Vertex opVertex : element.getIncidentVertices()) {
                    if (!opVertex.equals(nodeB)) continue;
                    return null;
                }
            }
            for (Edge element : nodeA.getOutEdges()) {
                for (Vertex opVertex : element.getIncidentVertices()) {
                    if (!opVertex.equals(nodeB)) continue;
                    return null;
                }
            }
        }
        return new UndirectedSparseEdge(nodeA, nodeB);
    }

    public Graph getGraph() {
        return this.grafo;
    }
}

