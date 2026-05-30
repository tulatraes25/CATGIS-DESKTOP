/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Assert;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public abstract class SimpleTreeModel
implements TreeModel {
    protected List<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
    protected Object root;

    public SimpleTreeModel(Object root) {
        this.root = root;
    }

    @Override
    public Object getRoot() {
        return this.root;
    }

    @Override
    public boolean isLeaf(Object node) {
        return !(node instanceof Folder) && this.getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        int i = 0;
        while (i < this.getChildCount(parent)) {
            if (child instanceof Folder && this.getChild(parent, i) instanceof Folder && this.getChild(parent, i).toString().equals(child.toString())) {
                return i;
            }
            if (this.getChild(parent, i) == child) {
                return i;
            }
            ++i;
        }
        Assert.shouldNeverReachHere((String)(parent + ", " + child));
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        this.listeners.remove(listener);
    }

    public abstract List<?> getChildren(Object var1);

    @Override
    public Object getChild(Object parent, int index) {
        return this.children(parent).get(index);
    }

    private List<?> children(Object parent) {
        return parent instanceof Folder ? ((Folder)parent).getChildren() : this.getChildren(parent);
    }

    @Override
    public int getChildCount(Object parent) {
        return this.children(parent).size();
    }

    protected void fireTreeNodesChanged(TreeModelEvent e) {
        for (TreeModelListener l : this.listeners) {
            l.treeNodesChanged(e);
        }
    }

    protected void fireTreeNodesInserted(TreeModelEvent e) {
        for (TreeModelListener l : this.listeners) {
            l.treeNodesInserted(e);
        }
    }

    protected void fireTreeNodesRemoved(TreeModelEvent e) {
        for (TreeModelListener l : this.listeners) {
            l.treeNodesRemoved(e);
        }
    }

    protected void fireTreeStructureChanged(TreeModelEvent e) {
        for (TreeModelListener l : this.listeners) {
            l.treeStructureChanged(e);
        }
    }

    public static abstract class Folder {
        private Class<?> childrenClass;
        private String name;
        private Object parent;

        public Folder(String name, Object parent, Class<?> childrenClass) {
            this.name = name;
            this.parent = parent;
            this.childrenClass = childrenClass;
        }

        public abstract List<?> getChildren();

        public String toString() {
            return this.name;
        }

        public int hashCode() {
            return 0;
        }

        public boolean equals(Object other) {
            if (!(other instanceof Folder)) {
                return false;
            }
            Folder otherFolder = (Folder)other;
            return this.parent == otherFolder.parent && this.name.equals(otherFolder.name);
        }

        public Class<?> getChildrenClass() {
            return this.childrenClass;
        }

        public Object getParent() {
            return this.parent;
        }
    }
}

