/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TreeUtil {
    public static TreeCellRenderer createSimpleTreeCellRenderer(ImageIcon icon) {
        return TreeUtil.createSimpleTreeCellRenderer(null, icon, new JTree().getFont());
    }

    public static TreeCellRenderer createSimpleTreeCellRenderer(final String text, ImageIcon icon, final Font font) {
        return new DefaultTreeCellRenderer(icon){
            private static final long serialVersionUID = 1L;
            {
                this.setOpenIcon(imageIcon);
                this.setClosedIcon(imageIcon);
                this.setLeafIcon(imageIcon);
                this.setBackgroundNonSelectionColor(new Color(0, 0, 0, 0));
            }

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component component = super.getTreeCellRendererComponent(tree, text == null ? value : text, sel, expanded, leaf, row, hasFocus);
                component.setFont(font);
                return component;
            }
        };
    }

    public static void visit(TreeModel model, Visitor visitor) {
        Stack<Object> path = new Stack<Object>();
        path.push(model.getRoot());
        TreeUtil.visit(model, path, visitor);
    }

    public static void visit(TreeModel model, TreePath path, Visitor visitor) {
        Stack<Object> stack = new Stack<Object>();
        stack.addAll(Arrays.asList(path.getPath()));
        TreeUtil.visit(model, stack, visitor);
    }

    private static void visit(TreeModel model, Stack<Object> path, Visitor visitor) {
        visitor.visit(path);
        int i = 0;
        while (i < model.getChildCount(path.peek())) {
            path.push(model.getChild(path.peek(), i));
            TreeUtil.visit(model, path, visitor);
            path.pop();
            ++i;
        }
    }

    public static TreeModelEvent createTreeModelEvent(Object source, Object node, TreeModel model) {
        TreePath parentPath = TreeUtil.findTreePath(node, model).getParentPath();
        return new TreeModelEvent(source, parentPath, new int[]{model.getIndexOfChild(parentPath.getLastPathComponent(), node)}, new Object[]{node});
    }

    public static TreePath findTreePath(final Object node, TreeModel model) {
        final TreePath[] treePath = new TreePath[1];
        TreeUtil.visit(model, new Visitor(){

            @Override
            public void visit(Stack<Object> path) {
                if (path.peek() != node) {
                    return;
                }
                treePath[0] = new TreePath(path.toArray());
            }
        });
        return treePath[0];
    }

    public static boolean contains(TreeModel model, final Object node) {
        final boolean[] result = new boolean[1];
        TreeUtil.visit(model, new Visitor(){

            @Override
            public void visit(Stack<Object> path) {
                if (path.peek() == node) {
                    result[0] = true;
                }
            }
        });
        return result[0];
    }

    public static List<Object> lastPathComponents(TreePath[] paths) {
        ArrayList<Object> lastPathComponents = new ArrayList<Object>();
        int i = 0;
        while (i < paths.length) {
            lastPathComponents.add(paths[i].getLastPathComponent());
            ++i;
        }
        return lastPathComponents;
    }

    public static void expandAll(final JTree tree, TreePath path) {
        TreeUtil.visit(tree.getModel(), path, new Visitor(){

            @Override
            public void visit(Stack<Object> path) {
                tree.expandPath(TreeUtil.findTreePath(path.peek(), tree.getModel()));
            }
        });
    }

    public static Collection<Object> nodes(TreePath path, TreeModel model) {
        final ArrayList<Object> nodes = new ArrayList<Object>();
        TreeUtil.visit(model, path, new Visitor(){

            @Override
            public void visit(Stack<Object> path) {
                nodes.add(path.peek());
            }
        });
        return nodes;
    }

    public static interface Visitor {
        public void visit(Stack<Object> var1);
    }
}

