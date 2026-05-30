/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.gui.swing.sldeditor.style.full;

import com.vividsolutions.jump.feature.FeatureSchema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.saig.core.gui.swing.sldeditor.SLDEditor;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.LineSymbolizer;
import org.saig.core.styling.PointSymbolizer;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.RasterSymbolizer;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.core.styling.Symbolizer;
import org.saig.core.styling.TextSymbolizer;
import org.saig.jump.lang.I18N;

public class TreeStyleEditor
extends JComponent
implements SLDEditor {
    private static final long serialVersionUID = 1L;
    private boolean blockChangeEvents;
    JTree tree;
    Style style;
    FeatureSchema featureType;
    DefaultTreeModel model;
    private boolean isImage;

    public TreeStyleEditor(Style s, FeatureSchema featureType, boolean isImage) {
        this.featureType = featureType;
        this.isImage = isImage;
        this.init();
        this.setStyle(s);
    }

    private DefaultMutableTreeNode getRootNode() {
        return (DefaultMutableTreeNode)this.tree.getModel().getRoot();
    }

    public void addFeatureTypeStyle(FeatureTypeStyle fts) {
        DefaultMutableTreeNode root = this.getRootNode();
        DefaultMutableTreeNode node = this.createFeatureTypeStyleNode(fts);
        this.model.insertNodeInto(node, root, 0);
        this.tree.setSelectionPath(new TreePath(node.getPath()));
    }

    public boolean addRule(FeatureTypeStyle fts, Rule r) {
        DefaultMutableTreeNode ftsNode = this.findNodeWithUserObject(this.getRootNode(), fts);
        if (ftsNode == null) {
            return false;
        }
        DefaultMutableTreeNode ruleNode = this.createRuleNode(r);
        this.model.insertNodeInto(ruleNode, ftsNode, 0);
        this.tree.setSelectionPath(new TreePath(ruleNode.getPath()));
        return true;
    }

    public boolean addSymbolizer(Rule rule, Symbolizer symbolizer) {
        DefaultMutableTreeNode ruleNode = this.findNodeWithUserObject(this.getRootNode(), rule);
        if (ruleNode == null) {
            return false;
        }
        DefaultMutableTreeNode symbolizerNode = this.createSymbolizerNode(symbolizer);
        this.model.insertNodeInto(symbolizerNode, ruleNode, 0);
        this.tree.setSelectionPath(new TreePath(symbolizerNode.getPath()));
        return true;
    }

    public void addChangeListener(ChangeListener cl) {
        this.listenerList.add(ChangeListener.class, cl);
    }

    protected void fireChange(ChangeEvent e) {
        if (this.blockChangeEvents) {
            return;
        }
        Object[] listeners = this.listenerList.getListenerList();
        int i = listeners.length - 2;
        while (i >= 0) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i + 1]).stateChanged(e);
            }
            i -= 2;
        }
    }

    public Object getSelectionObject() {
        TreePath path = this.tree.getSelectionPath();
        if (path == null) {
            return null;
        }
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
        return node.getUserObject();
    }

    public void setSelectionObject(Object object) {
        if (object == null) {
            this.tree.clearSelection();
        } else {
            TreePath path = this.findPathWithUserObject((DefaultMutableTreeNode)this.tree.getModel().getRoot(), object);
            if (path != null) {
                this.tree.setSelectionPath(path);
            }
        }
    }

    public Object findParentObject(Object object) {
        if (object == null) {
            throw new NullPointerException(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.trying-to-search-a-null-style-component"));
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.tree.getModel().getRoot();
        DefaultMutableTreeNode node = this.findNodeWithUserObject(root, object);
        if (node != null) {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            if (parent != null) {
                return parent.getUserObject();
            }
            return null;
        }
        return null;
    }

    private DefaultMutableTreeNode findNodeWithUserObject(DefaultMutableTreeNode root, Object object) {
        TreePath tp = this.findPathWithUserObject(root, object);
        return (DefaultMutableTreeNode)tp.getLastPathComponent();
    }

    private TreePath findPathWithUserObject(DefaultMutableTreeNode node, Object object) {
        if (node.getUserObject() != null && node.getUserObject() == object) {
            return new TreePath(node.getPath());
        }
        if (node.getChildCount() == 0) {
            return null;
        }
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getFirstChild();
        while (child != null) {
            TreePath path = this.findPathWithUserObject(child, object);
            if (path != null) {
                return path;
            }
            child = child.getNextNode();
        }
        return null;
    }

    public void setStyle(Style s) {
        this.style = s;
        this.buildTree(s);
    }

    public Style getStyle() {
        DefaultMutableTreeNode root = this.getRootNode();
        int ftCount = root.getChildCount();
        FeatureTypeStyle[] featureStyles = new FeatureTypeStyle[ftCount];
        int i = 0;
        while (i < ftCount) {
            DefaultMutableTreeNode ftsNode = (DefaultMutableTreeNode)root.getChildAt(i);
            featureStyles[i] = (FeatureTypeStyle)ftsNode.getUserObject();
            int ruleCount = ftsNode.getChildCount();
            Rule[] rules = new Rule[ruleCount];
            int j = 0;
            while (j < ruleCount) {
                DefaultMutableTreeNode ruleNode = (DefaultMutableTreeNode)ftsNode.getChildAt(j);
                rules[j] = (Rule)ruleNode.getUserObject();
                int symCount = ruleNode.getChildCount();
                Symbolizer[] symbolizers = new Symbolizer[symCount];
                int k = 0;
                while (k < symCount) {
                    DefaultMutableTreeNode symNode = (DefaultMutableTreeNode)ruleNode.getChildAt(k);
                    symbolizers[k] = (Symbolizer)symNode.getUserObject();
                    ++k;
                }
                rules[j].setSymbolizers(symbolizers);
                ++j;
            }
            featureStyles[i].setRules(rules);
            ++i;
        }
        this.style.setFeatureTypeStyles(featureStyles);
        return this.style;
    }

    public void refreshTree() {
        this.blockChangeEvents = true;
        try {
            TreePath path = this.tree.getSelectionPath();
            this.buildTree(this.style);
            if (path != null) {
                this.tree.setSelectionPath(path);
            }
        }
        finally {
            this.blockChangeEvents = true;
        }
    }

    public void expandTree() {
        if (this.tree.getModel() != null) {
            this.expandTree((DefaultMutableTreeNode)this.tree.getModel().getRoot());
        }
    }

    private void expandTree(DefaultMutableTreeNode node) {
        int count = node.getChildCount();
        if (count > 0) {
            int i = 0;
            while (i < count) {
                this.expandTree((DefaultMutableTreeNode)node.getChildAt(i));
                ++i;
            }
        }
        this.tree.expandPath(new TreePath(node.getPath()));
    }

    public void collapseTree() {
        if (this.tree.getModel() == null) {
            return;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.tree.getModel().getRoot();
        int count = root.getChildCount();
        int i = 0;
        while (i < count) {
            this.collapseTree((DefaultMutableTreeNode)root.getChildAt(i));
            ++i;
        }
    }

    private void collapseTree(DefaultMutableTreeNode node) {
        int count = node.getChildCount();
        if (count > 0) {
            int i = 0;
            while (i < count) {
                this.collapseTree((DefaultMutableTreeNode)node.getChildAt(i));
                ++i;
            }
        }
        this.tree.collapsePath(new TreePath(node.getPath()));
    }

    private void buildTree(Style s) {
        if (s == null) {
            new DefaultMutableTreeNode();
        } else {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(s);
            FeatureTypeStyle[] featureStyles = s.getFeatureTypeStyles();
            int i = 0;
            while (i < featureStyles.length) {
                root.add(this.createFeatureTypeStyleNode(featureStyles[i]));
                ++i;
            }
            this.model = new DefaultTreeModel(root);
        }
        this.tree.setModel(this.model);
    }

    private DefaultMutableTreeNode createFeatureTypeStyleNode(FeatureTypeStyle fts) {
        DefaultMutableTreeNode ftsNode = new DefaultMutableTreeNode(fts);
        Rule[] rules = fts.getRules();
        int j = 0;
        while (j < rules.length) {
            ftsNode.add(this.createRuleNode(rules[j]));
            ++j;
        }
        return ftsNode;
    }

    private DefaultMutableTreeNode createRuleNode(Rule rule) {
        DefaultMutableTreeNode ruleNode = new DefaultMutableTreeNode(rule);
        Symbolizer[] symbolizers = rule.getSymbolizers();
        int k = 0;
        while (k < symbolizers.length) {
            ruleNode.add(this.createSymbolizerNode(symbolizers[k]));
            ++k;
        }
        return ruleNode;
    }

    private DefaultMutableTreeNode createSymbolizerNode(Symbolizer symbolizer) {
        DefaultMutableTreeNode symbNode = new DefaultMutableTreeNode(symbolizer);
        return symbNode;
    }

    private void init() {
        this.tree = new JTree();
        this.setLayout(new BorderLayout());
        this.add(this.tree);
        NodeRenderer renderer = new NodeRenderer();
        renderer.setFeatureSample(this.isImage);
        this.tree.setCellRenderer(renderer);
        this.tree.getSelectionModel().setSelectionMode(1);
        this.tree.setExpandsSelectedPaths(true);
        this.tree.addTreeSelectionListener(new TreeSelectionListener(){

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                ChangeEvent event = new ChangeEvent(this);
                TreeStyleEditor.this.fireChange(event);
            }
        });
    }

    public Object[] getSiblings(Object selection) {
        DefaultMutableTreeNode node = this.findNodeWithUserObject(this.getRootNode(), selection);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        ArrayList<Object> siblings = new ArrayList<Object>(parent.getChildCount());
        int i = 0;
        while (i < parent.getChildCount()) {
            DefaultMutableTreeNode curr = (DefaultMutableTreeNode)parent.getChildAt(i);
            if (curr != node) {
                siblings.add(curr.getUserObject());
            }
            ++i;
        }
        return siblings.toArray();
    }

    public boolean wouldRemoveRoot(Object selection) {
        DefaultMutableTreeNode root = this.getRootNode();
        return this.wouldRemoveRootInternal(root, this.findNodeWithUserObject(root, selection));
    }

    private boolean wouldRemoveRootInternal(TreeNode root, TreeNode node) {
        if (node == root) {
            return true;
        }
        if (node.getParent().getChildCount() > 1) {
            return false;
        }
        return this.wouldRemoveRootInternal(root, node.getParent());
    }

    public boolean remove(Object selection) {
        DefaultMutableTreeNode root = this.getRootNode();
        return this.removeInternal(root, this.findNodeWithUserObject(root, selection));
    }

    private boolean removeInternal(TreeNode root, TreeNode node) {
        if (node == root) {
            return false;
        }
        if (node.getParent().getChildCount() > 1) {
            this.model.removeNodeFromParent((DefaultMutableTreeNode)node);
            return true;
        }
        return this.removeInternal(root, node.getParent());
    }

    public Object getSiblingAfter(Object selection) {
        DefaultMutableTreeNode node = this.findNodeWithUserObject(this.getRootNode(), selection);
        DefaultMutableTreeNode next = node.getNextSibling();
        if (next == null) {
            return null;
        }
        return next.getUserObject();
    }

    public Object getSiblingBefore(Object selection) {
        DefaultMutableTreeNode node = this.findNodeWithUserObject(this.getRootNode(), selection);
        DefaultMutableTreeNode previous = node.getPreviousSibling();
        if (previous == null) {
            return null;
        }
        return previous.getUserObject();
    }

    public boolean moveUp(Object selection) {
        DefaultMutableTreeNode node = this.findNodeWithUserObject(this.getRootNode(), selection);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        if (parent == null) {
            return false;
        }
        int index = parent.getIndex(node);
        if (index == 0) {
            return false;
        }
        this.model.removeNodeFromParent(node);
        this.model.insertNodeInto(node, parent, index - 1);
        return true;
    }

    public boolean moveDown(Object selection) {
        DefaultMutableTreeNode node = this.findNodeWithUserObject(this.getRootNode(), selection);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        if (parent == null) {
            return false;
        }
        int index = parent.getIndex(node);
        if (index == parent.getChildCount() - 1) {
            return false;
        }
        this.model.removeNodeFromParent(node);
        this.model.insertNodeInto(node, parent, index + 1);
        return true;
    }

    public void setSelectedRule(Rule rule) {
        DefaultMutableTreeNode rootNode = this.getRootNode();
        Enumeration nodes = rootNode.breadthFirstEnumeration();
        boolean found = false;
        while (nodes.hasMoreElements() && !found) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodes.nextElement();
            Object userObject = node.getUserObject();
            if (!userObject.equals(rule)) continue;
            TreePath path = new TreePath(node.getPath());
            this.tree.setSelectionPath(path);
            this.tree.scrollPathToVisible(path);
            found = true;
        }
    }

    private static class NodeRenderer
    extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;
        private boolean isImage;

        private NodeRenderer() {
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
                Object userObject = node.getUserObject();
                if (userObject instanceof Style) {
                    Style s = (Style)userObject;
                    this.setText(s.getName());
                } else if (userObject instanceof FeatureTypeStyle) {
                    FeatureTypeStyle fts = (FeatureTypeStyle)userObject;
                    this.setText(fts.getName());
                } else if (userObject instanceof Rule) {
                    Rule rule = (Rule)userObject;
                    this.setText(rule.getName());
                    if (!rule.isEnabled()) {
                        this.setForeground(Color.LIGHT_GRAY);
                    }
                } else if (!this.isImage && userObject instanceof Symbolizer) {
                    this.setIcon(LegendIconMaker.makeLegendIcon(20, Color.WHITE, new Symbolizer[]{(Symbolizer)userObject}, null));
                    if (userObject instanceof PolygonSymbolizer) {
                        this.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.polygon"));
                    } else if (userObject instanceof LineSymbolizer) {
                        this.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.line"));
                    } else if (userObject instanceof PointSymbolizer) {
                        this.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.point"));
                    } else if (userObject instanceof TextSymbolizer) {
                        this.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.text"));
                    } else if (userObject instanceof RasterSymbolizer) {
                        this.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.raster"));
                    }
                } else {
                    this.setIcon(null);
                    this.setText(I18N.getString("org.saig.core.gui.swing.sldeditor.style.full.TreeStyleEditor.raster"));
                }
            }
            return this;
        }

        public void setFeatureSample(boolean isImage) {
            this.isImage = isImage;
        }
    }
}

