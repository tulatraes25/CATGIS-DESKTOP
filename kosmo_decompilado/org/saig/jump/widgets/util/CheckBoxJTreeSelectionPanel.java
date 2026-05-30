/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;

public class CheckBoxJTreeSelectionPanel
extends JPanel
implements PropertyChangeListener {
    private static final long serialVersionUID = 1L;
    private String panelTitle = I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.elements");
    private JScrollPane scroller;
    private JTree tree;
    private JButton selectAllJButton;
    private JButton invertAllJButton;
    private Map<Object, Boolean> selectionMap;
    private boolean toMark;
    private SelectableTreeCellRendererWrapper treeCellRendererWrapper;
    private final boolean onlySelectLeaves;

    public CheckBoxJTreeSelectionPanel(TreeModel treeModel, boolean rootVisible, String title, Dimension dimension, boolean showButtons, TreeCellRenderer treeCellRenderer, boolean onlySelectLeaves) {
        super(new BorderLayout());
        this.onlySelectLeaves = onlySelectLeaves;
        if (title != null) {
            this.panelTitle = title;
        }
        this.toMark = true;
        this.setBorder(BorderFactory.createTitledBorder(this.panelTitle));
        this.selectionMap = new HashMap<Object, Boolean>();
        if (treeModel == null) {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            this.tree = new JTree(root);
        } else {
            this.tree = new JTree(treeModel);
        }
        this.tree.setRootVisible(rootVisible);
        this.tree.setShowsRootHandles(true);
        this.treeCellRendererWrapper = new SelectableTreeCellRendererWrapper(treeCellRenderer, this.selectionMap, onlySelectLeaves);
        this.tree.setCellRenderer(this.treeCellRendererWrapper);
        this.tree.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseClicked(MouseEvent e) {
                CheckBoxJTreeSelectionPanel.this.handleCheckBoxClick(e);
            }
        });
        this.tree.addPropertyChangeListener("selection_value", this);
        this.scroller = new JScrollPane(this.tree, 22, 30);
        this.scroller.setMinimumSize(dimension);
        this.scroller.setPreferredSize(dimension);
        this.selectAllJButton = new JButton(I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.select-all"));
        this.selectAllJButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ev) {
                CheckBoxJTreeSelectionPanel.this.setAllNodesSelected(CheckBoxJTreeSelectionPanel.this.toMark);
                CheckBoxJTreeSelectionPanel.this.toMark = !CheckBoxJTreeSelectionPanel.this.toMark;
                CheckBoxJTreeSelectionPanel.this.refreshButtonsText();
                CheckBoxJTreeSelectionPanel.this.tree.repaint();
            }
        });
        this.invertAllJButton = new JButton(I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.invert-all"));
        this.invertAllJButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent ev) {
                CheckBoxJTreeSelectionPanel.this.invertAllNodesSelection();
                CheckBoxJTreeSelectionPanel.this.refreshButtonsText();
                CheckBoxJTreeSelectionPanel.this.tree.firePropertyChange("selection_value", false, true);
                CheckBoxJTreeSelectionPanel.this.tree.repaint();
            }
        });
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(this.selectAllJButton);
        buttonPanel.add(this.invertAllJButton);
        this.add((Component)this.scroller, "Center");
        if (showButtons) {
            this.add((Component)buttonPanel, "South");
        }
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        this.tree.addTreeSelectionListener(listener);
    }

    public void addElementSelectionChangedListener(PropertyChangeListener listener) {
        this.addPropertyChangeListener("selection_value", listener);
        this.refreshButtonsText();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.tree && evt.getPropertyName() == "selection_value") {
            this.firePropertyChange("selection_value", evt.getOldValue(), evt.getNewValue());
        }
        this.refreshButtonsText();
    }

    private void refreshButtonsText() {
        List<Object> selected = this.getSelectedObjects();
        if (selected == null) {
            return;
        }
        int numSelected = selected.size();
        this.toMark = numSelected == 0;
        String selectButtonText = "";
        String invertButtonText = I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.invert");
        selectButtonText = this.toMark ? String.valueOf(selectButtonText) + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.select") : String.valueOf(selectButtonText) + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.unselect");
        selectButtonText = String.valueOf(selectButtonText) + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.all");
        invertButtonText = String.valueOf(invertButtonText) + I18N.getString("org.saig.jump.widgets.util.CheckBoxJListSelectionPanel.all");
        this.selectAllJButton.setText(selectButtonText);
        this.invertAllJButton.setText(invertButtonText);
    }

    private void setAllNodesSelected(boolean toMark) {
        TreeModel model = this.tree.getModel();
        if (model != null) {
            Object root = model.getRoot();
            this.recursiveSetAllNodesSelected(model, root, toMark);
        }
    }

    private void recursiveSetAllNodesSelected(TreeModel model, Object o, boolean toMark) {
        int cc = model.getChildCount(o);
        int i = 0;
        while (i < cc) {
            Object child = model.getChild(o, i);
            if (!this.onlySelectLeaves || model.isLeaf(child)) {
                this.selectionMap.put(child, toMark);
            }
            if (!model.isLeaf(child)) {
                this.recursiveSetAllNodesSelected(model, child, toMark);
            }
            ++i;
        }
    }

    private void invertAllNodesSelection() {
        TreeModel model = this.tree.getModel();
        if (model != null) {
            Object root = model.getRoot();
            this.recursiveInvertAllNodesSelection(model, root);
        }
    }

    private void recursiveInvertAllNodesSelection(TreeModel model, Object o) {
        int cc = model.getChildCount(o);
        int i = 0;
        while (i < cc) {
            Object child = model.getChild(o, i);
            if (!this.onlySelectLeaves || model.isLeaf(child)) {
                Boolean bool = this.selectionMap.get(child);
                if (bool == null) {
                    bool = false;
                }
                this.selectionMap.put(child, bool == false);
            }
            if (!model.isLeaf(child)) {
                this.recursiveInvertAllNodesSelection(model, child);
            }
            ++i;
        }
    }

    public List<Object> getSelectedObjects() {
        ArrayList<Object> selectedObjects = new ArrayList<Object>();
        TreeModel model = this.tree.getModel();
        if (model != null) {
            Object root = model.getRoot();
            this.checkSelectedNodes(model, root, selectedObjects);
        }
        return selectedObjects;
    }

    protected void checkSelectedNodes(TreeModel model, Object o, List<Object> selectedObjects) {
        int cc = model.getChildCount(o);
        int i = 0;
        while (i < cc) {
            Object child = model.getChild(o, i);
            if ((!this.onlySelectLeaves || model.isLeaf(child)) && this.selectionMap.containsKey(child) && this.selectionMap.get(child).booleanValue()) {
                selectedObjects.add(((DefaultMutableTreeNode)child).getUserObject());
            }
            if (!model.isLeaf(child)) {
                this.checkSelectedNodes(model, child, selectedObjects);
            }
            ++i;
        }
    }

    public static TreeModel createTreeModelByMap(Map<Object, Collection<Object>> mapa) {
        return CheckBoxJTreeSelectionPanel.createTreeModelByMap(mapa, true, true, null, null);
    }

    public static TreeModel createTreeModelByMap(Map<Object, Collection<Object>> mapa, boolean sortBranch, boolean sortLeaf) {
        return CheckBoxJTreeSelectionPanel.createTreeModelByMap(mapa, sortBranch, sortLeaf, null, null);
    }

    public static TreeModel createTreeModelByMap(Map<Object, Collection<Object>> mapa, boolean sortBranches, boolean sortLeaves, Comparator<Object> branchComparator, Comparator<Object> leafComparator) {
        if (branchComparator == null) {
            branchComparator = new ObjectComparator();
        }
        if (leafComparator == null) {
            leafComparator = new ObjectComparator();
        }
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        ArrayList<Object> branches = new ArrayList<Object>(mapa.keySet());
        if (sortBranches) {
            Collections.sort(branches, branchComparator);
        }
        for (Object e : branches) {
            DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(e);
            root.add(branchNode);
            ArrayList<Object> leaves = new ArrayList<Object>(mapa.get(e));
            if (sortLeaves) {
                Collections.sort(leaves, leafComparator);
            }
            for (Object e2 : leaves) {
                DefaultMutableTreeNode leafNode = new DefaultMutableTreeNode(e2);
                branchNode.add(leafNode);
            }
        }
        DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root);
        return defaultTreeModel;
    }

    private void handleCheckBoxClick(MouseEvent e) {
        TreePath path = this.tree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
            return;
        }
        Object node = path.getLastPathComponent();
        Point nodeLocation = this.tree.getUI().getPathBounds(this.tree, path).getLocation();
        Rectangle checkBoxBounds = this.treeCellRendererWrapper.getCheckBoxBounds(node);
        checkBoxBounds.translate((int)nodeLocation.getX(), (int)nodeLocation.getY());
        if (checkBoxBounds.contains(e.getPoint())) {
            Boolean bool = this.selectionMap.get(node);
            if (bool == null) {
                bool = false;
            }
            this.selectionMap.put(node, bool == false);
            this.tree.repaint();
        }
        this.refreshButtonsText();
    }

    public void setTreeModel(TreeModel newTreeModel) {
        this.selectionMap = new HashMap<Object, Boolean>();
        this.tree.setModel(newTreeModel);
        ((SelectableTreeCellRendererWrapper)this.tree.getCellRenderer()).setSelectionMap(this.selectionMap);
        this.tree.repaint();
        this.toMark = true;
        this.refreshButtonsText();
    }

    private static class ObjectComparator
    implements Comparator<Object>,
    Serializable {
        private static final long serialVersionUID = 1L;

        private ObjectComparator() {
        }

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String one = o1.toString();
            String two = o2.toString();
            return Collator.getInstance(I18N.getLocale()).compare(one, two);
        }
    }

    private static class SelectableTreeCellRendererWrapper
    implements TreeCellRenderer {
        private final TreeCellRenderer originalCellRenderer;
        private Map<Object, Boolean> selectionMap = null;
        private Map<Object, JCheckBox> checkMap = null;
        private Map<Object, JPanel> panelMap = null;
        private final boolean onlySelectLeaves;

        public SelectableTreeCellRendererWrapper(TreeCellRenderer treeCellRenderer, Map<Object, Boolean> selectionMap, boolean onlySelectLeaves) {
            this.originalCellRenderer = treeCellRenderer;
            this.selectionMap = selectionMap;
            this.onlySelectLeaves = onlySelectLeaves;
            this.panelMap = new HashMap<Object, JPanel>();
            this.checkMap = new HashMap<Object, JCheckBox>();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component originalComponent = null;
            if (this.originalCellRenderer != null) {
                originalComponent = this.originalCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
            if (originalComponent == null) {
                String label = "";
                if (value != null) {
                    label = value.toString();
                }
                originalComponent = new JLabel(label);
            }
            JCheckBox checkBox = new JCheckBox();
            checkBox.setVisible(!this.onlySelectLeaves || leaf);
            checkBox.setBackground(tree.getBackground());
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBackground(tree.getBackground());
            checkBox.setSelected(this.selectionMap.containsKey(value) && this.selectionMap.get(value) != false);
            FormUtils.addRowInGBL((JComponent)panel, 0, 0, (JComponent)checkBox, false, false, false);
            FormUtils.addRowInGBL((JComponent)panel, 0, 1, (JComponent)originalComponent, true, true, false);
            this.panelMap.put(value, panel);
            this.checkMap.put(value, checkBox);
            return panel;
        }

        public Rectangle getCheckBoxBounds(Object node) {
            int i = ((GridBagLayout)this.panelMap.get((Object)node).getLayout()).getConstraints((Component)((Component)this.checkMap.get((Object)node))).gridx;
            int x = 0;
            int j = 0;
            while (j < i) {
                x += this.getColumnWidth(j, node);
                ++j;
            }
            return new Rectangle(x, 0, this.getColumnWidth(i, node), this.getRowHeight(node));
        }

        protected int getColumnWidth(int i, Object node) {
            return ((GridBagLayout)this.panelMap.get(node).getLayout()).getLayoutDimensions()[0][i];
        }

        protected int getRowHeight(Object node) {
            return ((GridBagLayout)this.panelMap.get(node).getLayout()).getLayoutDimensions()[1][0];
        }

        public void setSelectionMap(Map<Object, Boolean> selectionMap) {
            this.selectionMap = selectionMap;
            this.panelMap = new HashMap<Object, JPanel>();
            this.checkMap = new HashMap<Object, JCheckBox>();
        }
    }
}

