/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jdesktop.swingx.JXLabel
 *  org.jdesktop.swingx.JXPanel
 */
package org.saig.jump.widgets.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.widgets.util.CheckBoxNode;
import org.saig.jump.widgets.util.CheckBoxTreeSelectionListener;
import org.saig.jump.widgets.util.trees.INavigableTree;
import org.saig.jump.widgets.util.trees.INavigableTreeVisitor;

public abstract class CheckboxTree
extends JTree
implements INavigableTree {
    private static final long serialVersionUID = 1L;
    protected List<CheckBoxTreeSelectionListener> listeners;

    public CheckboxTree() {
        CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
        this.setCellRenderer(renderer);
        this.setCellEditor(new CheckBoxNodeEditor(this));
        this.setEditable(true);
        this.setRootVisible(false);
        this.setShowsRootHandles(true);
        this.listeners = new ArrayList<CheckBoxTreeSelectionListener>();
    }

    public abstract void buildTree(Map<String, Vector<?>> var1);

    @Override
    public abstract INavigableTreeVisitor getTreeVisitor();

    public Map<String, List<?>> getSelectedElements() {
        HashMap selectedElements = new HashMap();
        this.getTreeVisitor().visit(this, selectedElements);
        return selectedElements;
    }

    public void addCheckBoxTreeSelectionListener(CheckBoxTreeSelectionListener listener) {
        this.listeners.add(listener);
    }

    public void removeCheckBoxTreeSelectionListener(CheckBoxTreeSelectionListener listener) {
        this.listeners.remove(listener);
    }

    public void fireCheckBoxTreeSelectionChanged() {
        for (CheckBoxTreeSelectionListener listener : this.listeners) {
            listener.selectionChanged();
        }
    }

    protected class CheckBoxNodeEditor
    extends AbstractCellEditor
    implements TreeCellEditor {
        private static final long serialVersionUID = 1L;
        CheckBoxNodeRenderer renderer;
        ChangeEvent changeEvent;
        JTree tree;

        public CheckBoxNodeEditor(JTree tree) {
            this.renderer = new CheckBoxNodeRenderer();
            this.changeEvent = null;
            this.tree = tree;
        }

        @Override
        public Object getCellEditorValue() {
            CheckBoxPanel checkbox = this.renderer.getRenderer();
            CheckBoxNode checkBoxNode = new CheckBoxNode(checkbox.getText(), checkbox.getIcon(), checkbox.isSelected(), checkbox.isEnabled());
            return checkBoxNode;
        }

        @Override
        public boolean isCellEditable(EventObject event) {
            DefaultMutableTreeNode treeNode;
            Object userObject;
            Object node;
            MouseEvent mouseEvent;
            TreePath path;
            boolean returnValue = false;
            if (event instanceof MouseEvent && (path = this.tree.getPathForLocation((mouseEvent = (MouseEvent)event).getX(), mouseEvent.getY())) != null && (node = path.getLastPathComponent()) != null && node instanceof DefaultMutableTreeNode && (userObject = (treeNode = (DefaultMutableTreeNode)node).getUserObject()) instanceof CheckBoxNode) {
                CheckBoxNode checkBoxNode = (CheckBoxNode)userObject;
                returnValue = checkBoxNode.isEditable();
            }
            return returnValue;
        }

        @Override
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {
            Component editor = this.renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
            ItemListener itemListener = new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (CheckBoxNodeEditor.this.stopCellEditing()) {
                        CheckBoxNodeEditor.this.fireEditingStopped();
                        CheckboxTree.this.fireCheckBoxTreeSelectionChanged();
                    }
                }
            };
            if (editor instanceof CheckBoxPanel) {
                ((CheckBoxPanel)((Object)editor)).addItemListener(itemListener);
            }
            return editor;
        }
    }

    protected class CheckBoxNodeRenderer
    implements TreeCellRenderer {
        private CheckBoxPanel leafRenderer;
        private CheckBoxPanel intermediateRenderer;
        private CheckBoxPanel currentRenderer;
        private DefaultTreeCellRenderer nonLeafRenderer = new DefaultTreeCellRenderer();
        Color selectionBorderColor;
        Color selectionForeground;
        Color selectionBackground;
        Color textForeground;
        Color textBackground;

        protected CheckBoxPanel getRenderer() {
            return this.currentRenderer;
        }

        public CheckBoxNodeRenderer() {
            Boolean booleanValue;
            Font fontValue = UIManager.getFont("Tree.font");
            this.leafRenderer = new CheckBoxPanel();
            this.intermediateRenderer = new CheckBoxPanel();
            this.currentRenderer = this.leafRenderer;
            if (fontValue != null) {
                this.leafRenderer.setFont(fontValue);
                this.intermediateRenderer.setFont(fontValue);
            }
            this.leafRenderer.setFocusPainted((booleanValue = (Boolean)UIManager.get("Tree.drawsFocusBorderAroundIcon")) != null && booleanValue != false);
            this.intermediateRenderer.setFocusPainted(booleanValue != null && booleanValue != false);
            this.selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            this.selectionForeground = UIManager.getColor("Tree.selectionForeground");
            this.selectionBackground = UIManager.getColor("Tree.selectionBackground");
            this.textForeground = UIManager.getColor("Tree.textForeground");
            this.textBackground = UIManager.getColor("Tree.textBackground");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Object returnValue;
            if (!tree.getModel().getRoot().equals(value)) {
                Object userObject;
                String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
                if (leaf) {
                    this.leafRenderer.setText(stringValue);
                    this.leafRenderer.setSelected(false);
                    this.leafRenderer.setEnabled(tree.isEnabled());
                    if (selected) {
                        this.leafRenderer.setForeground(this.selectionForeground);
                        this.leafRenderer.setBackground(this.selectionBackground);
                    } else {
                        this.leafRenderer.setForeground(this.textForeground);
                        this.leafRenderer.setBackground(this.textBackground);
                    }
                } else {
                    this.intermediateRenderer.setText(stringValue);
                    this.intermediateRenderer.setSelected(false);
                    this.intermediateRenderer.setEnabled(tree.isEnabled());
                    if (selected) {
                        this.intermediateRenderer.setForeground(this.selectionForeground);
                        this.intermediateRenderer.setBackground(this.selectionBackground);
                    } else {
                        this.intermediateRenderer.setForeground(this.textForeground);
                        this.intermediateRenderer.setBackground(this.textBackground);
                    }
                }
                if (value != null && value instanceof DefaultMutableTreeNode && (userObject = ((DefaultMutableTreeNode)value).getUserObject()) instanceof CheckBoxNode) {
                    CheckBoxNode node = (CheckBoxNode)userObject;
                    if (leaf) {
                        this.leafRenderer.setText(node.getText().toString());
                        this.leafRenderer.setIcon(node.getIcon());
                        this.leafRenderer.setSelected(node.isSelected());
                        if (!node.isEditable()) {
                            this.leafRenderer.setEditable(false);
                        }
                    } else {
                        this.intermediateRenderer.setText(node.getText().toString());
                        this.intermediateRenderer.setSelected(node.isSelected());
                        this.intermediateRenderer.setIcon(node.getIcon());
                        if (!node.isEditable()) {
                            this.intermediateRenderer.setEditable(false);
                        }
                    }
                }
                if (leaf) {
                    returnValue = this.leafRenderer;
                    this.currentRenderer = this.leafRenderer;
                } else {
                    returnValue = this.intermediateRenderer;
                    this.currentRenderer = this.intermediateRenderer;
                }
            } else {
                returnValue = this.nonLeafRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
            return returnValue;
        }
    }

    protected class CheckBoxPanel
    extends JXPanel {
        private static final long serialVersionUID = 1L;
        private JCheckBox checkBox;
        private JXLabel label;

        public CheckBoxPanel() {
            super((LayoutManager)new GridBagLayout());
            this.checkBox = new JCheckBox();
            this.label = new JXLabel();
            this.checkBox.setOpaque(false);
            FormUtils.addRowInGBL((JComponent)((Object)this), 0, 0, (JComponent)this.checkBox, false, false);
            FormUtils.addRowInGBL((JComponent)((Object)this), 0, 1, (JComponent)this.label, true, false);
        }

        public void setEditable(boolean editable) {
            if (!editable) {
                this.label.setForeground(Color.RED);
            } else {
                this.label.setForeground(Color.BLACK);
            }
            this.checkBox.setEnabled(editable);
        }

        public Icon getIcon() {
            return this.label.getIcon();
        }

        public void setIcon(Icon icon) {
            this.label.setIcon(icon);
        }

        public void addItemListener(ItemListener itemListener) {
            this.checkBox.addItemListener(itemListener);
        }

        public boolean isSelected() {
            return this.checkBox.isSelected();
        }

        public void setSelected(boolean selected) {
            this.checkBox.setSelected(selected);
        }

        public String getText() {
            return this.label.getText();
        }

        public void setText(String text) {
            this.label.setText(text);
        }

        public void setFocusPainted(boolean focusPainted) {
            this.checkBox.setFocusPainted(focusPainted);
        }

        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.checkBox.setEnabled(enabled);
            this.label.setEnabled(enabled);
        }

        public void setForeground(Color color) {
            super.setForeground(color);
            if (this.label != null) {
                this.label.setForeground(color);
            }
        }
    }

    protected class NamedVector<T>
    extends Vector<T> {
        private static final long serialVersionUID = 1L;
        private String name;

        public NamedVector(String name) {
            this.name = name;
        }

        public NamedVector(String name, List<T> elements) {
            this.name = name;
            int i = 0;
            int n = elements.size();
            while (i < n) {
                this.add(elements.get(i));
                ++i;
            }
        }

        @Override
        public String toString() {
            return "[" + this.name + "]";
        }
    }
}

