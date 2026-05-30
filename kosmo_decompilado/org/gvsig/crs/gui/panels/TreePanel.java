/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TreePanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private int wComp = 190;
    private int hComp = 360;
    private int hTree = (int)Math.floor((double)this.hComp * 0.68);
    private int hList = this.hComp - this.hTree;
    private JScrollPane pTree = null;
    private JScrollPane pList = null;
    private JTree tree = null;
    private JTextArea list = null;
    private DefaultMutableTreeNode raiz = null;
    private Hashtable<String, String> map;
    String rootName = "";

    public TreePanel(String rootName) {
        this.rootName = rootName;
        this.initialize();
    }

    private void initialize() {
        this.map = new Hashtable();
        this.raiz = new DefaultMutableTreeNode(this.rootName);
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        this.setLayout(flowLayout);
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setPreferredSize(new Dimension(this.wComp, this.hComp));
        this.add((Component)this.getPTree(), gridBagConstraints);
        this.add((Component)this.getPList(), gridBagConstraints1);
    }

    private JScrollPane getPTree() {
        if (this.pTree == null) {
            this.pTree = new JScrollPane();
            this.pTree.setPreferredSize(new Dimension(this.wComp, this.hTree));
            this.pTree.setViewportBorder(BorderFactory.createBevelBorder(1));
            this.pTree.setVerticalScrollBarPolicy(20);
            this.pTree.setViewportView(this.getTree());
        }
        return this.pTree;
    }

    private JScrollPane getPList() {
        if (this.pList == null) {
            this.pList = new JScrollPane();
            this.pList.setPreferredSize(new Dimension(this.wComp, this.hList));
            this.pList.setBorder(BorderFactory.createEtchedBorder(0));
            this.pList.setBackground(Color.white);
            this.pList.setVerticalScrollBarPolicy(20);
            this.pList.setHorizontalScrollBarPolicy(31);
            this.pList.setViewportView(this.getList());
        }
        return this.pList;
    }

    public JTree getTree() {
        if (this.tree == null) {
            this.tree = new JTree(this.raiz);
        }
        return this.tree;
    }

    public JTextArea getList() {
        if (this.list == null) {
            this.list = new JTextArea();
            this.list.setLineWrap(true);
            this.list.setWrapStyleWord(true);
            this.list.setEditable(false);
            this.list.setFont(new JLabel().getFont());
        }
        return this.list;
    }

    public void setPanelSize(int w, int h) {
        this.wComp = w;
        this.hComp = h;
        this.hTree = (int)Math.floor((double)this.hComp * 0.68);
        this.hList = this.hComp - this.hTree;
        this.setPreferredSize(new Dimension(this.wComp, this.hComp));
        this.setPreferredSize(new Dimension(this.wComp, this.hComp));
        this.pTree.setPreferredSize(new Dimension(this.wComp, this.hTree));
        this.pList.setPreferredSize(new Dimension(this.wComp, this.hList));
    }

    public void addClass(String name, int pos) {
        DefaultTreeModel model = (DefaultTreeModel)this.tree.getModel();
        DefaultMutableTreeNode r = new DefaultMutableTreeNode(name);
        model.insertNodeInto(r, this.raiz, pos);
    }

    public void addEntry(String name, String parentName, String value) {
        DefaultTreeModel model = (DefaultTreeModel)this.tree.getModel();
        int i = 0;
        while (i < model.getChildCount(this.raiz)) {
            if (model.getChild(this.raiz, i).toString().equals(parentName)) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getChild(this.raiz, i);
                node.add(new DefaultMutableTreeNode(name));
                if (value != null) {
                    this.map.put(name, value);
                }
            }
            ++i;
        }
    }

    public void setRoot(String name) {
        DefaultTreeModel model = (DefaultTreeModel)this.tree.getModel();
        this.rootName = name;
        this.raiz = new DefaultMutableTreeNode(this.rootName);
        model.setRoot(this.raiz);
        this.map.clear();
    }
}

