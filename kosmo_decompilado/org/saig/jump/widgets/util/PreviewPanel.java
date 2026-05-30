/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.saig.jump.widgets.util.FilteringJList;
import org.saig.jump.widgets.util.Previewable;
import org.saig.jump.widgets.util.ScalableImagePanel;

public class PreviewPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private FilteringJList<Previewable> elementsList;
    private JScrollPane jScrollPane1;
    private JTextField searchTextField;
    private ScalableImagePanel imagePanel;
    private JPanel listPanel;
    private JSplitPane splitPane;

    public PreviewPanel() {
        this.initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            this.setPreferredSize(new Dimension(550, 350));
            this.listPanel = new JPanel();
            BorderLayout listPanelLayout = new BorderLayout();
            listPanelLayout.setHgap(4);
            listPanelLayout.setVgap(4);
            this.listPanel.setLayout(listPanelLayout);
            this.jScrollPane1 = new JScrollPane();
            this.listPanel.add((Component)this.jScrollPane1, "Center");
            this.jScrollPane1.setPreferredSize(new Dimension(170, 200));
            this.elementsList = new FilteringJList();
            this.jScrollPane1.setViewportView(this.elementsList);
            this.elementsList.setSize(3, 18);
            this.searchTextField = new JTextField();
            this.listPanel.add((Component)this.getSearchTextField(), "North");
            this.listPanel.setMinimumSize(new Dimension(130, 100));
            this.imagePanel = new ScalableImagePanel();
            this.imagePanel.setBorder(new LineBorder(new Color(0, 0, 0), 1, false));
            this.imagePanel.setMinimumSize(new Dimension(100, 100));
            this.splitPane = new JSplitPane(1, this.listPanel, this.imagePanel);
            this.splitPane.setOneTouchExpandable(true);
            this.splitPane.setDividerLocation(170);
            this.add((Component)this.splitPane, "Center");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FilteringJList<Previewable> getElementsList() {
        return this.elementsList;
    }

    public JTextField getSearchTextField() {
        return this.searchTextField;
    }

    public ScalableImagePanel getImagePanel() {
        return this.imagePanel;
    }
}

