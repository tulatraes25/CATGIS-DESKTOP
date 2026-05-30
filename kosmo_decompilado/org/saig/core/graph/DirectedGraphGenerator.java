/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.LineSegment
 *  edu.uci.ics.jung.graph.Edge
 *  edu.uci.ics.jung.graph.Graph
 *  edu.uci.ics.jung.graph.Vertex
 *  edu.uci.ics.jung.graph.impl.DirectedSparseEdge
 *  edu.uci.ics.jung.graph.impl.DirectedSparseGraph
 *  edu.uci.ics.jung.graph.impl.DirectedSparseVertex
 *  edu.uci.ics.jung.utils.UserData
 */
package org.saig.core.graph;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.feature.Feature;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.utils.UserData;
import java.util.HashMap;
import java.util.Set;

public class DirectedGraphGenerator {
    private HashMap<Coordinate, Vertex> m_coord2node = new HashMap();
    private Graph grafo = new DirectedSparseGraph();

    @Deprecated
    public void add(Object obj, Feature feature) {
        LineSegment line = (LineSegment)obj;
        this.add(line, feature);
    }

    public void add(LineSegment line, Feature feature) {
        Edge e;
        Vertex n2;
        Coordinate c = line.p0;
        Vertex n1 = this.m_coord2node.get(c);
        if (n1 == null) {
            n1 = this.grafo.addVertex((Vertex)new DirectedSparseVertex());
            n1.setUserDatum((Object)"COORDINATE", (Object)c, UserData.SHARED);
            this.m_coord2node.put(c, n1);
        }
        if ((n2 = this.m_coord2node.get(c = line.p1)) == null) {
            n2 = this.grafo.addVertex((Vertex)new DirectedSparseVertex());
            n2.setUserDatum((Object)"COORDINATE", (Object)c, UserData.SHARED);
            this.m_coord2node.put(c, n2);
        }
        if ((e = this.buildEdge(n1, n2)) != null) {
            e.addUserDatum((Object)"FEATURE", (Object)feature, UserData.SHARED);
            this.grafo.addEdge(e);
        }
    }

    public Edge buildEdge(Vertex nodeA, Vertex nodeB) {
        Set edgesToA = nodeA.getInEdges();
        for (Edge element : edgesToA) {
            for (Object v : element.getIncidentVertices()) {
                Vertex opVertex = (Vertex)v;
                if (!opVertex.equals(nodeB)) continue;
                return null;
            }
        }
        Set edgesFromA = nodeA.getOutEdges();
        for (Edge element : edgesFromA) {
            for (Object v : element.getIncidentVertices()) {
                Vertex opVertex = (Vertex)v;
                if (!opVertex.equals(nodeB)) continue;
                return null;
            }
        }
        return new DirectedSparseEdge(nodeA, nodeB);
    }

    public Graph getGraph() {
        return this.grafo;
    }
}

