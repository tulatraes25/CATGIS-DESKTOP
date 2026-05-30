/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.panels.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.dialog.ImportNewCrsDialog;
import org.saig.core.util.DialogManager;

public class DefCrsUsr
extends JPanel
implements ActionListener {
    private JPanel panel;
    private JButton btnCrsExistente;
    private JLabel lblCrs;
    private JRadioButton rbCrsExistente;
    private JRadioButton rbNuevoCrs;
    private JRadioButton rbCadenaWkt;
    private ButtonGroup crsGroup;
    private JTextArea txtAreaWkt;
    private JButton btnImportarWkt;
    private JScrollPane scrollWkt;
    private ICrs currentCrs;
    private int width = 380;
    private static final long serialVersionUID = 1L;
    ICrs crs = null;
    boolean hasChanged = false;

    public DefCrsUsr(ICrs crs) {
        this.currentCrs = crs;
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.add((Component)this.getPanel(), "Center");
        this.habilitarWkt(false);
    }

    public JPanel getPanel() {
        if (this.panel == null) {
            this.panel = new JPanel();
            this.panel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = 1;
            c.weighty = 0.1;
            c.insets = new Insets(8, 8, 8, 8);
            c.weightx = 0.75;
            c.gridx = 0;
            c.gridy = 1;
            this.panel.add((Component)this.getRbCrsExistente(), c);
            c.weightx = 0.25;
            c.gridx = 1;
            c.gridy = 1;
            this.panel.add((Component)this.getLblCrs(), c);
            c.fill = 13;
            c.weightx = 0.0;
            c.gridx = 2;
            c.gridy = 1;
            this.panel.add((Component)this.getBtnCrsExistente(), c);
            c.fill = 1;
            c.weightx = 0.75;
            c.gridx = 0;
            c.gridy = 0;
            this.panel.add((Component)this.getRbNuevoCrs(), c);
            c.gridx = 0;
            c.gridy = 2;
            this.panel.add((Component)this.getRbCadenaWkt(), c);
            this.agruparRadioButtons();
            this.getRbNuevoCrs().setSelected(true);
            c.gridx = 2;
            c.gridy = 2;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.fill = 0;
            c.fill = 3;
            c.anchor = 13;
            c.gridx = 0;
            c.gridy = 4;
            c.gridwidth = 3;
            c.weighty = 0.75;
            this.panel.add((Component)this.getScrollWkt(), c);
        }
        return this.panel;
    }

    private void agruparRadioButtons() {
        if (this.crsGroup == null) {
            this.crsGroup = new ButtonGroup();
            this.crsGroup.add(this.getRbCrsExistente());
            this.crsGroup.add(this.getRbNuevoCrs());
            this.crsGroup.add(this.getRbCadenaWkt());
        }
    }

    public JRadioButton getRbCadenaWkt() {
        if (this.rbCadenaWkt == null) {
            this.rbCadenaWkt = new JRadioButton(String.valueOf(CRSI18NConstants.FROM_WKT_STRING_KEY) + ": ");
            this.rbCadenaWkt.addActionListener(this);
        }
        return this.rbCadenaWkt;
    }

    public JRadioButton getRbCrsExistente() {
        if (this.rbCrsExistente == null) {
            this.rbCrsExistente = new JRadioButton(String.valueOf(CRSI18NConstants.FROM_AN_EXISTENT_SRS_KEY) + ": ");
            this.rbCrsExistente.addActionListener(this);
        }
        return this.rbCrsExistente;
    }

    public JRadioButton getRbNuevoCrs() {
        if (this.rbNuevoCrs == null) {
            this.rbNuevoCrs = new JRadioButton(CRSI18NConstants.FROM_USER_DEFINITION);
            this.rbNuevoCrs.addActionListener(this);
        }
        return this.rbNuevoCrs;
    }

    public JLabel getLblCrs() {
        if (this.lblCrs == null) {
            this.lblCrs = new JLabel(this.currentCrs.getAbrev());
        }
        return this.lblCrs;
    }

    public void habilitarExistente(boolean b) {
        this.getBtnCrsExistente().setEnabled(b);
        this.getLblCrs().setEnabled(b);
    }

    public void habilitarWkt(boolean b) {
        this.getTxtAreaWkt().setEnabled(b);
    }

    public JButton getBtnImportarWkt() {
        if (this.btnImportarWkt == null) {
            this.btnImportarWkt = new JButton();
            this.btnImportarWkt.setText("...");
            this.btnImportarWkt.addActionListener(this);
            this.btnImportarWkt.setToolTipText(CRSI18NConstants.WKT_IMPORT_KEY);
            this.btnImportarWkt.setEnabled(false);
        }
        return this.btnImportarWkt;
    }

    public JButton getBtnCrsExistente() {
        if (this.btnCrsExistente == null) {
            this.btnCrsExistente = new JButton("...");
            Dimension d = new Dimension(this.btnCrsExistente.getPreferredSize());
            d.width = 100;
            this.btnCrsExistente.setSize(d);
            this.btnCrsExistente.addActionListener(this);
            this.btnCrsExistente.setToolTipText(CRSI18NConstants.SELECT_AN_EXISTENT_SRS_KEY);
        }
        return this.btnCrsExistente;
    }

    public JTextArea getTxtAreaWkt() {
        if (this.txtAreaWkt == null) {
            this.txtAreaWkt = new JTextArea();
            this.txtAreaWkt.setLineWrap(true);
            this.txtAreaWkt.setWrapStyleWord(true);
            Dimension d = new Dimension(this.txtAreaWkt.getPreferredSize());
            d.width = this.width;
            this.txtAreaWkt.setSize(d);
        }
        return this.txtAreaWkt;
    }

    public JScrollPane getScrollWkt() {
        if (this.scrollWkt == null) {
            this.scrollWkt = new JScrollPane();
            this.scrollWkt.setViewportView(this.getTxtAreaWkt());
            this.scrollWkt.setHorizontalScrollBarPolicy(31);
            Dimension d = new Dimension(this.txtAreaWkt.getPreferredSize());
            d.width = this.width;
            this.scrollWkt.setSize(d);
        }
        return this.scrollWkt;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.getRbCrsExistente())) {
            this.setHasChange(true);
            this.habilitarExistente(true);
            this.habilitarWkt(false);
        } else if (e.getSource().equals(this.getRbNuevoCrs())) {
            this.setHasChange(true);
            this.habilitarExistente(false);
            this.habilitarWkt(false);
        } else if (e.getSource().equals(this.getRbCadenaWkt())) {
            this.setHasChange(true);
            this.habilitarExistente(false);
            this.habilitarWkt(true);
        } else if (e.getSource().equals(this.getBtnCrsExistente())) {
            ImportNewCrsDialog newCrs = new ImportNewCrsDialog(CRSI18NConstants.IMPORT_CRS_KEY);
            DialogManager.addJDialog(newCrs, CRSI18NConstants.IMPORT_CRS_KEY);
            if (newCrs.getCode() != -1) {
                this.getLblCrs().setText("EPSG:" + newCrs.getCode());
                this.setCrs(newCrs.getCode());
                this.setHasChange(true);
            }
        } else if (e.getSource().equals(this.getBtnImportarWkt())) {
            System.out.println("Boton Importar Wkt");
        }
    }

    public void setCrs(int code) {
        try {
            this.crs = new CrsFactory().getCRS("EPSG:" + code);
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
    }

    public ICrs getCrs() {
        return this.crs;
    }

    public void setHasChange(boolean change) {
        this.hasChanged = change;
    }

    public boolean getHasChanged() {
        return this.hasChanged;
    }
}

