/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  edu.uci.ics.jung.graph.Graph
 *  edu.uci.ics.jung.graph.decorators.EdgeWeightLabeller
 *  edu.uci.ics.jung.graph.filters.UnassembledGraph
 *  edu.uci.ics.jung.graph.filters.impl.WeightedEdgeGraphFilter
 */
package org.saig.core.graph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.decorators.EdgeWeightLabeller;
import edu.uci.ics.jung.graph.filters.UnassembledGraph;
import edu.uci.ics.jung.graph.filters.impl.WeightedEdgeGraphFilter;

public class GraphFilter {
    private Graph graph;

    public GraphFilter(Graph graph) {
        this.graph = graph;
    }

    public UnassembledGraph execute(int value) {
        EdgeWeightLabeller labeller = EdgeWeightLabeller.getLabeller((Graph)this.graph);
        WeightedEdgeGraphFilter filter = new WeightedEdgeGraphFilter(1, labeller);
        return filter.filter(this.graph);
    }
}

