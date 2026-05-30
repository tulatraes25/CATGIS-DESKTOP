/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.documents;

import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.saig.core.model.data.Record;
import org.saig.jump.widgets.documents.DocumentDetailsPanel;
import org.saig.jump.widgets.util.FilteringJList;

public class ManageDocumentsPanel
extends JPanel {
    private JPanel centerPanel;
    private DocumentDetailsPanel documentDetailPanel;
    private JTextField searchTextField;
    private JScrollPane jScrollPane1;
    private FilteringJList<Record> documentList;
    private JPanel westPanel;
    private JButton removeDocumentButton;
    private JButton addDocumentButton;
    private JPanel buttonPanel;

    public ManageDocumentsPanel() {
        this.initGUI();
    }

    private void initGUI() {
        try {
            BorderLayout thisLayout = new BorderLayout();
            this.setLayout(thisLayout);
            this.setPreferredSize(new Dimension(540, 290));
            this.centerPanel = new JPanel();
            BorderLayout centerPanelLayout = new BorderLayout();
            centerPanelLayout.setHgap(4);
            centerPanelLayout.setVgap(4);
            this.add((Component)this.centerPanel, "Center");
            this.centerPanel.setLayout(centerPanelLayout);
            this.documentDetailPanel = new DocumentDetailsPanel();
            this.centerPanel.add((Component)this.documentDetailPanel, "Center");
            this.documentDetailPanel.setBorder(BorderFactory.createTitledBorder(null, "Detalles del documento", 4, 0));
            this.westPanel = new JPanel();
            BorderLayout westPanelLayout = new BorderLayout();
            westPanelLayout.setHgap(4);
            westPanelLayout.setVgap(4);
            this.centerPanel.add((Component)this.westPanel, "West");
            this.westPanel.setLayout(westPanelLayout);
            this.westPanel.setBorder(BorderFactory.createTitledBorder("Documentos"));
            this.searchTextField = new JTextField();
            this.westPanel.add((Component)this.getSearchTextField(), "North");
            this.jScrollPane1 = new JScrollPane();
            this.westPanel.add((Component)this.jScrollPane1, "Center");
            this.jScrollPane1.setPreferredSize(new Dimension(170, 200));
            this.documentList = new FilteringJList();
            this.jScrollPane1.setViewportView(this.getDocumentList());
            this.documentList.setSize(18, 3);
            this.buttonPanel = new JPanel();
            FlowLayout buttonPanelLayout = new FlowLayout();
            buttonPanelLayout.setHgap(15);
            this.buttonPanel.setLayout(buttonPanelLayout);
            this.add((Component)this.buttonPanel, "South");
            this.addDocumentButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Plus.gif")));
            this.buttonPanel.add(this.getAddDocumentButton());
            this.addDocumentButton.setText("A\u00f1adir documento");
            this.removeDocumentButton = new JButton(GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif")));
            this.buttonPanel.add(this.getRemoveDocumentButton());
            this.removeDocumentButton.setText("Eliminar documento");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JButton getAddDocumentButton() {
        return this.addDocumentButton;
    }

    public JButton getRemoveDocumentButton() {
        return this.removeDocumentButton;
    }

    public JTextField getSearchTextField() {
        return this.searchTextField;
    }

    public FilteringJList<Record> getDocumentList() {
        return this.documentList;
    }

    public DocumentDetailsPanel getDocumentDetailPanel() {
        return this.documentDetailPanel;
    }
}

