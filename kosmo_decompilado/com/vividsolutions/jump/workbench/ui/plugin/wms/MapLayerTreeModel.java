/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin.wms;

import com.vividsolutions.wms.MapLayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class MapLayerTreeModel
extends DefaultTreeModel {
    private static final long serialVersionUID = 1L;
    private boolean sorted = false;

    public MapLayerTreeModel(MapLayer topLayer) {
        super(new LayerNode(topLayer, null));
        ((LayerNode)this.getRoot()).mapLayerTreeModel = this;
    }

    public void setSorted(boolean sorted) {
        this.sorted = sorted;
        this.reload();
    }

    public static class LayerNode
    implements TreeNode,
    Comparable<LayerNode> {
        private MapLayer layer;
        private MapLayerTreeModel mapLayerTreeModel;

        public LayerNode(MapLayer layer, MapLayerTreeModel mapLayerTreeModel) {
            this.layer = layer;
            this.mapLayerTreeModel = mapLayerTreeModel;
        }

        public boolean isContainer() {
            return this.layer.getName() == null;
        }

        public MapLayer getLayer() {
            return this.layer;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return this.childList().get(childIndex);
        }

        @Override
        public int getChildCount() {
            return this.childList().size();
        }

        @Override
        public TreeNode getParent() {
            return new LayerNode(this.layer.getParent(), this.mapLayerTreeModel);
        }

        @Override
        public int getIndex(TreeNode node) {
            return this.childList().indexOf(node);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return this.getChildCount() == 0;
        }

        @Override
        public Enumeration<LayerNode> children() {
            return new Vector<LayerNode>(this.childList()).elements();
        }

        private List<LayerNode> childList() {
            ArrayList<LayerNode> children = new ArrayList<LayerNode>();
            for (MapLayer layer : this.layer.getSubLayerList()) {
                children.add(new LayerNode(layer, this.mapLayerTreeModel));
            }
            if (this.mapLayerTreeModel.sorted) {
                Collections.sort(children);
            }
            return children;
        }

        public boolean equals(Object o) {
            if (!(o instanceof LayerNode)) {
                return false;
            }
            LayerNode other = (LayerNode)o;
            return this.layer == other.layer;
        }

        @Override
        public int compareTo(LayerNode o) {
            return this.layer.getTitle().compareTo(o.layer.getTitle());
        }
    }
}

