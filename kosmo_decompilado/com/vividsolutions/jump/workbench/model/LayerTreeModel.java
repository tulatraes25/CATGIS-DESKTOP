/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.SimpleTreeModel;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import org.saig.core.styling.Rule;
import org.saig.core.util.LocaleManager;

public class LayerTreeModel
extends SimpleTreeModel {
    protected LayerManagerProxy layerManagerProxy;

    public LayerTreeModel(LayerManagerProxy layerManagerProxy) {
        super(new Root());
        this.layerManagerProxy = layerManagerProxy;
    }

    @Override
    public List<?> getChildren(Object parent) {
        if (parent == this.getRoot()) {
            return this.layerManagerProxy.getLayerManager().getCategories();
        }
        if (parent instanceof Category) {
            List<Layerable> enabledLayerables = ((Category)parent).getLayerables();
            return enabledLayerables;
        }
        if (parent instanceof Layer) {
            Layer layer = (Layer)parent;
            if (layer.getModelStyle() != null) {
                ArrayList<Rule> ruleList = new ArrayList<Rule>();
                Rule[] rules = layer.getModelStyle().getSelectedFeatureTypeStyle().getRules();
                int i = 0;
                while (i < rules.length) {
                    ruleList.add(rules[i]);
                    ++i;
                }
                return ruleList;
            }
            return new ArrayList();
        }
        if (parent instanceof Rule) {
            return new ArrayList();
        }
        if (parent instanceof WMSLayer) {
            return new ArrayList();
        }
        if (parent instanceof TextBalloonLayer) {
            return new ArrayList();
        }
        Assert.shouldNeverReachHere((String)parent.getClass().getName());
        return null;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        if (path.getLastPathComponent() instanceof Layerable) {
            ((Layerable)path.getLastPathComponent()).setTitle((String)newValue, LocaleManager.getActiveLocale());
            this.fireTreeNodesChanged(new TreeModelEvent((Object)this, path));
        } else if (path.getLastPathComponent() instanceof Category) {
            ((Category)path.getLastPathComponent()).setTitle((String)newValue, LocaleManager.getActiveLocale());
            this.fireTreeNodesChanged(new TreeModelEvent((Object)this, path));
        } else if (path.getLastPathComponent() instanceof Rule) {
            ((Rule)path.getLastPathComponent()).setTitle((String)newValue, LocaleManager.getActiveLocale());
            this.fireTreeNodesChanged(new TreeModelEvent((Object)this, path));
        } else {
            Assert.shouldNeverReachHere();
        }
    }

    public void dispose() {
        this.layerManagerProxy = null;
        this.listeners.clear();
        this.root = null;
    }

    public static class Root {
        private Root() {
        }
    }
}

