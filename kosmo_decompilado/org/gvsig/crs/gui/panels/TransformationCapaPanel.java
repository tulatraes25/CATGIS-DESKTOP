/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.gui.CRSMainTrPanel;
import org.gvsig.crs.gui.listeners.TransformationCompuestaPanelListener;
import org.gvsig.crs.gui.panels.TransformationEpsgPanel;
import org.gvsig.crs.gui.panels.TransformationManualPanel;
import org.gvsig.crs.gui.panels.TransformationNadgridsPanel;
import org.gvsig.crs.gui.panels.TransformationRecentsPanel;
import org.gvsig.crs.persistence.CompTrData;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;

public class TransformationCapaPanel
extends JPanel
implements ActionListener,
ItemListener {
    private static final long serialVersionUID = 1L;
    private JLabel lblTransCapa = null;
    private JComboBox jComboOptions = null;
    private JPanel pNorth = null;
    private TransformationManualPanel manualTrPanel = null;
    private TransformationEpsgPanel epsgTrPanel = null;
    private TransformationNadgridsPanel nadsTrPanel = null;
    private TransformationRecentsPanel recentsTrPanel = null;
    private JPanel pCenter = null;
    private JLabel lblCrsCapa = null;
    private JLabel lblCrsCapaCode = null;
    String crs_target = null;
    String crs_source = null;
    String sourceTransformation = null;
    CRSMainTrPanel crsMainTrPanel = null;
    boolean recentCompTransformation = false;

    public TransformationCapaPanel(String target, CRSMainTrPanel p) {
        this.crsMainTrPanel = p;
        this.setCrs_target(target);
        this.manualTrPanel = new TransformationManualPanel();
        this.epsgTrPanel = new TransformationEpsgPanel(target);
        this.nadsTrPanel = new TransformationNadgridsPanel(false);
        this.recentsTrPanel = new TransformationRecentsPanel();
        this.setListeners();
        this.setLayout(new BorderLayout());
        this.add((Component)this.getPNorth(), "North");
        this.add((Component)this.getPCenter(), "Center");
    }

    public JPanel getPCenter() {
        if (this.pCenter == null) {
            this.pCenter = new JPanel();
            this.pCenter.setLayout(new CardLayout());
            this.pCenter.add("recents", this.recentsTrPanel);
            this.pCenter.add("manual", this.manualTrPanel);
            this.pCenter.add("epsg", this.epsgTrPanel);
            this.pCenter.add("nad", this.nadsTrPanel);
        }
        return this.pCenter;
    }

    public JLabel getLblTransCapa() {
        if (this.lblTransCapa == null) {
            this.lblTransCapa = new JLabel(CRSI18NConstants.LAYER_TRANSFORMATION_KEY);
        }
        return this.lblTransCapa;
    }

    public JLabel getLblCrsCapa() {
        if (this.lblCrsCapa == null) {
            this.lblCrsCapa = new JLabel(String.valueOf(CRSI18NConstants.LAYER_CRS_KEY) + ":");
        }
        return this.lblCrsCapa;
    }

    public JLabel getLblCrsCapaCode() {
        if (this.lblCrsCapaCode == null) {
            this.lblCrsCapaCode = new JLabel();
        }
        return this.lblCrsCapaCode;
    }

    public JPanel getPNorth() {
        if (this.pNorth == null) {
            this.pNorth = new JPanel();
            this.pNorth.setLayout(new GridLayout(2, 1));
            JPanel p1 = new JPanel(new FlowLayout(0, 10, 3));
            p1.add(this.getLblCrsCapa());
            p1.add(this.getLblCrsCapaCode());
            JPanel p2 = new JPanel(new FlowLayout(0, 10, 3));
            p2.add(this.getLblTransCapa());
            p2.add(this.getJComboOptions());
            this.pNorth.add(p1);
            this.pNorth.add(p2);
        }
        return this.pNorth;
    }

    public JComboBox getJComboOptions() {
        if (this.jComboOptions == null) {
            String[] selection = new String[]{CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY, CRSI18NConstants.EPSG_TRANSFORMATION_KEY, CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY, CRSI18NConstants.GRIDS_KEY};
            this.jComboOptions = new JComboBox<String>(selection);
            this.jComboOptions.setPreferredSize(new Dimension(180, 25));
            this.jComboOptions.addActionListener(this);
            this.jComboOptions.addItemListener(this);
            this.jComboOptions.setSelectedItem(CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY);
        }
        return this.jComboOptions;
    }

    private void setListeners() {
        TransformationCompuestaPanelListener listener = new TransformationCompuestaPanelListener(this, this.crsMainTrPanel);
        this.getJComboOptions().addActionListener(listener);
        this.getRecentsTrPanel().getJButtonInfo().addActionListener(listener);
        ListSelectionModel rowSMEpsgTr = this.getEpsgTrPanel().getJTable().getSelectionModel();
        rowSMEpsgTr.addListSelectionListener(listener);
        ListSelectionModel rowSMRecentsTr = this.getRecentsTrPanel().getJTable().getSelectionModel();
        rowSMRecentsTr.addListSelectionListener(listener);
        this.getNadsTrPanel().getJComboNadFile().addItemListener(listener);
        this.getManualTrPanel().getTx_Translation().addKeyListener(listener);
        this.getManualTrPanel().getTy_Translation().addKeyListener(listener);
        this.getManualTrPanel().getTz_Translation().addKeyListener(listener);
        this.getManualTrPanel().getTx_Rotation().addKeyListener(listener);
        this.getManualTrPanel().getTy_Rotation().addKeyListener(listener);
        this.getManualTrPanel().getTz_Rotation().addKeyListener(listener);
        this.getManualTrPanel().getTscale().addKeyListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public TransformationEpsgPanel getEpsgTrPanel() {
        return this.epsgTrPanel;
    }

    public TransformationManualPanel getManualTrPanel() {
        return this.manualTrPanel;
    }

    public TransformationNadgridsPanel getNadsTrPanel() {
        return this.nadsTrPanel;
    }

    public TransformationRecentsPanel getRecentsTrPanel() {
        return this.recentsTrPanel;
    }

    public void setCrs_target(String cod) {
        this.crs_target = cod;
    }

    public String getCrs_target() {
        return this.crs_target;
    }

    public void setCrs_source(String authority) {
        this.crs_source = authority;
        this.getLblCrsCapaCode().setText(authority);
    }

    public String getCrs_source() {
        return this.crs_source;
    }

    public void fillData() {
        String[] source = this.getCrs_source().split(":");
        String[] target = this.getCrs_target().split(":");
        this.epsgTrPanel.setSourceCompuesta(this.getCrs_source());
        this.nadsTrPanel.setCode(Integer.parseInt(source[1]));
        this.nadsTrPanel.setSourceAbrev(source[0], source[1]);
        this.nadsTrPanel.setTargetAbrev(target[0], target[1]);
        this.nadsTrPanel.getJComboNadFile().setSelectedIndex(0);
        this.manualTrPanel.setCode(Integer.parseInt(source[1]));
        this.manualTrPanel.setSourceAbrev(source[0], source[1]);
        this.manualTrPanel.setTargetAbrev(target[0], target[1]);
        this.getJComboOptions().setSelectedIndex(0);
        this.setRecentCompTransformation(false);
    }

    public void setSourceTransformation(String trans) {
        this.sourceTransformation = trans;
    }

    public String getSourceTransformation() {
        return this.sourceTransformation;
    }

    public String getParamsEpsg(String[] values) {
        String params = "+towgs84=";
        params = String.valueOf(params) + values[0];
        int i = 1;
        while (i < values.length) {
            params = String.valueOf(params) + "," + values[i];
            ++i;
        }
        return params;
    }

    public String getManualParams() {
        if (this.getManualTrPanel().getTx_Translation().getText().equals("")) {
            this.getManualTrPanel().getTx_Translation().setText("0");
        } else if (this.getManualTrPanel().getTy_Translation().getText().equals("")) {
            this.getManualTrPanel().getTy_Translation().setText("0");
        } else if (this.getManualTrPanel().getTz_Translation().getText().equals("")) {
            this.getManualTrPanel().getTz_Translation().setText("0");
        } else if (this.getManualTrPanel().getTx_Rotation().getText().equals("")) {
            this.getManualTrPanel().getTx_Rotation().setText("0");
        } else if (this.getManualTrPanel().getTy_Rotation().getText().equals("")) {
            this.getManualTrPanel().getTy_Rotation().setText("0");
        } else if (this.getManualTrPanel().getTz_Rotation().getText().equals("")) {
            this.getManualTrPanel().getTz_Rotation().setText("0");
        } else if (this.getManualTrPanel().getTscale().getText().equals("")) {
            this.getManualTrPanel().getTscale().setText("0");
        }
        String param = "+towgs84=" + this.getManualTrPanel().getTx_Translation().getText() + "," + this.getManualTrPanel().getTy_Translation().getText() + "," + this.getManualTrPanel().getTz_Translation().getText() + "," + this.getManualTrPanel().getTx_Rotation().getText() + "," + this.getManualTrPanel().getTy_Rotation().getText() + "," + this.getManualTrPanel().getTz_Rotation().getText() + "," + this.getManualTrPanel().getTscale().getText() + " ";
        return param;
    }

    public String getNadsParams(String info) {
        String[] partes = info.split("\\(");
        String nadFile = partes[0];
        return "+nadgrids=" + nadFile;
    }

    public void fillData(String details) {
        RecentTrsPersistence trPersistence = new RecentTrsPersistence();
        TrData[] crsTrDataArray = trPersistence.getArrayOfTrData();
        CompTrData comp = null;
        int iRow = crsTrDataArray.length - 1;
        while (iRow >= 0) {
            if (details.equals(String.valueOf(crsTrDataArray[iRow].getAuthority()) + ":" + crsTrDataArray[iRow].getCode() + " <--> " + crsTrDataArray[iRow].getDetails()) && crsTrDataArray[iRow].getAuthority().equals(CRSI18NConstants.COMPOUND_TRANSFORMATION_KEY)) {
                comp = (CompTrData)crsTrDataArray[iRow];
                break;
            }
            --iRow;
        }
        if (comp.getFirstTr().getAuthority().equals("EPSG")) {
            this.getJComboOptions().setSelectedIndex(1);
            String code = String.valueOf(comp.getFirstTr().getCode());
            int i = 0;
            while (i < this.getEpsgTrPanel().getJTable().getRowCount()) {
                if (code.equals((String)this.getEpsgTrPanel().getJTable().getValueAt(i, 0))) {
                    this.getEpsgTrPanel().getJTable().setRowSelectionInterval(i, i);
                    break;
                }
                ++i;
            }
        } else if (comp.getFirstTr().getAuthority().equals("USR")) {
            this.getJComboOptions().setSelectedIndex(2);
            String data = comp.getFirstTr().getDetails();
            data = data.trim().substring(1, data.length() - 1);
            String[] values = data.split(",");
            this.getManualTrPanel().getTx_Translation().setText(values[0]);
            this.getManualTrPanel().getTy_Translation().setText(values[1]);
            this.getManualTrPanel().getTz_Translation().setText(values[2]);
            this.getManualTrPanel().getTx_Rotation().setText(values[3]);
            this.getManualTrPanel().getTy_Rotation().setText(values[4]);
            this.getManualTrPanel().getTz_Rotation().setText(values[5]);
            this.getManualTrPanel().getTscale().setText(values[6]);
        } else if (comp.getFirstTr().getAuthority().equals("NADGR")) {
            this.getJComboOptions().setSelectedIndex(3);
            String[] data = comp.getFirstTr().getDetails().split(" ");
            String fichero = data[0];
            String[] authority = data[1].substring(1, data[1].length() - 1).split(":");
            this.getNadsTrPanel().setSourceAbrev(authority[0], authority[1]);
            int i = 0;
            while (i < this.getNadsTrPanel().getJComboNadFile().getItemCount()) {
                if (fichero.equals((String)this.getNadsTrPanel().getJComboNadFile().getItemAt(i))) {
                    this.getNadsTrPanel().getJComboNadFile().setSelectedIndex(i);
                    break;
                }
                ++i;
            }
        }
        this.setRecentCompTransformation(true);
    }

    public void setRecentCompTransformation(boolean state) {
        this.recentCompTransformation = state;
    }

    public boolean getRecentCompTransformation() {
        return this.recentCompTransformation;
    }

    public void resetData() {
        this.fillData();
    }

    public boolean isSthSelected() {
        return this.getRecentsTrPanel().selectedRowTable != -1;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
    }
}

