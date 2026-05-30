/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FirableTreeModelWrapper
implements TreeModel {
    private TreeModel model;
    private List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

    public FirableTreeModelWrapper(TreeModel model) {
        this.model = model;
    }

    @Override
    public Object getRoot() {
        return this.model.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        return this.model.getChild(parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        return this.model.getChildCount(parent);
    }

    @Override
    public boolean isLeaf(Object node) {
        return this.model.isLeaf(node);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        this.model.valueForPathChanged(path, newValue);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return this.model.getIndexOfChild(parent, child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        this.listeners.add(l);
        this.model.addTreeModelListener(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        this.listeners.remove(l);
        this.model.removeTreeModelListener(l);
    }

    public void fireTreeNodesChanged(TreeModelEvent e) {
        this.starting();
        try {
            for (TreeModelListener l : this.listeners) {
                l.treeNodesChanged(e);
            }
        }
        finally {
            this.finishing();
        }
    }

    public void fireTreeNodesInserted(TreeModelEvent e) {
        this.starting();
        try {
            for (TreeModelListener l : this.listeners) {
                l.treeNodesInserted(e);
            }
        }
        finally {
            this.finishing();
        }
    }

    public void fireTreeNodesRemoved(TreeModelEvent e) {
        this.starting();
        try {
            for (TreeModelListener l : this.listeners) {
                l.treeNodesRemoved(e);
            }
        }
        finally {
            this.finishing();
        }
    }

    public void fireTreeStructureChanged(TreeModelEvent e) {
        this.starting();
        try {
            for (TreeModelListener l : this.listeners) {
                l.treeStructureChanged(e);
            }
        }
        finally {
            this.finishing();
        }
    }

    protected void finishing() {
    }

    protected void starting() {
    }

    public TreeModel getModel() {
        return this.model;
    }
}

