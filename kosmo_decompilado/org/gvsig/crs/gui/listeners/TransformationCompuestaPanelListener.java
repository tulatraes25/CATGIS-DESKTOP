/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.listeners;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.CRSMainTrPanel;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.gui.panels.TransformationCapaPanel;
import org.gvsig.crs.gui.panels.TransformationVistaPanel;
import org.gvsig.crs.ogr.TransEPSG;
import org.saig.core.util.DialogManager;

public class TransformationCompuestaPanelListener
implements ActionListener,
ListSelectionListener,
ItemListener,
KeyListener {
    TransformationCapaPanel capa = null;
    TransformationVistaPanel vista = null;
    private CRSMainTrPanel panel = null;
    boolean condition = false;

    public TransformationCompuestaPanelListener(TransformationCapaPanel p, CRSMainTrPanel pa) {
        this.capa = p;
        this.panel = pa;
    }

    public TransformationCompuestaPanelListener(TransformationVistaPanel p, CRSMainTrPanel pa) {
        this.vista = p;
        this.panel = pa;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TransEPSG epsgParams;
        String p;
        TransEPSG epsgParams2;
        String[] transform;
        CardLayout cl;
        InfoCRSPanel info;
        String[] data;
        if (this.capa != null) {
            if (e.getSource() == this.capa.getRecentsTrPanel().getJButtonInfo()) {
                data = new String[]{(String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 0), (String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 1), (String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 2), (String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 3), (String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 4)};
                info = new InfoCRSPanel(data[2].split(":")[0], Integer.parseInt(data[2].split(":")[1]), String.valueOf(data[0]) + " <--> " + data[4]);
                DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
            }
            if (e.getSource().equals(this.capa.getJComboOptions())) {
                cl = (CardLayout)this.capa.getPCenter().getLayout();
                if (this.capa.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY)) {
                    cl.show(this.capa.getPCenter(), "recents");
                    if (this.capa.getRecentsTrPanel().selectedRowTable == -1) {
                        this.capa.setSourceTransformation(null);
                        this.panel.getJButtonNext().setEnabled(false);
                    } else {
                        transform = ((String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                        if (transform[0].equals("USR")) {
                            this.capa.getRecentsTrPanel().setParamsManual((String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 4));
                            this.capa.setSourceTransformation(this.capa.getRecentsTrPanel().getParams());
                        }
                        if (transform[0].equals("EPSG")) {
                            epsgParams2 = new TransEPSG(this.capa.getRecentsTrPanel().getTrCode(), this.capa.getEpsgTrPanel().connect, this.capa.getRecentsTrPanel().getInverseTransformation());
                            this.capa.getRecentsTrPanel().setParamsEPGS(epsgParams2.getParamValue());
                            this.capa.setSourceTransformation(this.capa.getRecentsTrPanel().getParams());
                        }
                        if (transform[0].equals("NADGR")) {
                            p = (String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 4);
                            this.capa.setSourceTransformation(this.capa.getNadsParams(p));
                        }
                        this.panel.getJButtonNext().setEnabled(true);
                    }
                } else if (this.capa.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.EPSG_TRANSFORMATION_KEY)) {
                    cl.show(this.capa.getPCenter(), "epsg");
                    if (this.capa.getEpsgTrPanel().selectedRowTable == -1) {
                        this.capa.setSourceTransformation(null);
                        this.panel.getJButtonNext().setEnabled(false);
                    } else {
                        epsgParams = new TransEPSG(this.capa.getEpsgTrPanel().getTransformation_code(), this.capa.getEpsgTrPanel().connect, this.capa.getEpsgTrPanel().inverseTranformation);
                        this.capa.getEpsgTrPanel().setValues(epsgParams.getParamValue());
                        this.capa.setSourceTransformation(this.capa.getParamsEpsg(this.capa.getEpsgTrPanel().getValues()));
                        this.panel.getJButtonNext().setEnabled(true);
                    }
                } else if (this.capa.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY)) {
                    cl.show(this.capa.getPCenter(), "manual");
                    if (!this.capa.getManualTrPanel().getStatus()) {
                        this.panel.getJButtonNext().setEnabled(false);
                    } else {
                        this.panel.getJButtonNext().setEnabled(true);
                        this.capa.setSourceTransformation(this.capa.getManualParams());
                    }
                } else if (this.capa.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.GRIDS_KEY)) {
                    cl.show(this.capa.getPCenter(), "nad");
                    if (this.capa.getNadsTrPanel().getJComboNadFile().getSelectedIndex() != 0) {
                        this.capa.setSourceTransformation("+nadgrids=" + this.capa.getNadsTrPanel().getNadFile());
                        this.panel.getJButtonNext().setEnabled(true);
                    } else {
                        this.capa.setSourceTransformation(null);
                        this.panel.getJButtonNext().setEnabled(false);
                    }
                }
            }
        }
        if (this.vista != null) {
            if (e.getSource() == this.vista.getRecentsTrPanel().getJButtonInfo()) {
                data = new String[]{(String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 0), (String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 1), (String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 2), (String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 3), (String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 4)};
                info = new InfoCRSPanel(data[2].split(":")[0], Integer.parseInt(data[2].split(":")[1]), String.valueOf(data[0]) + " <--> " + data[4]);
                DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
            }
            if (e.getSource().equals(this.vista.getJComboOptions())) {
                cl = (CardLayout)this.vista.getPCenter().getLayout();
                if (this.vista.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY)) {
                    cl.show(this.vista.getPCenter(), "recents");
                    if (this.vista.getRecentsTrPanel().selectedRowTable == -1) {
                        this.vista.setTargetTransformation(null);
                        this.panel.getJButtonAccept().setEnabled(false);
                    } else {
                        transform = ((String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                        if (transform[0].equals("USR")) {
                            this.vista.getRecentsTrPanel().setParamsManual((String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 4));
                            this.vista.setTargetTransformation(this.vista.getRecentsTrPanel().getParams());
                        }
                        if (transform[0].equals("EPSG")) {
                            epsgParams2 = new TransEPSG(this.vista.getRecentsTrPanel().getTrCode(), this.vista.getEpsgTrPanel().connect, this.vista.getRecentsTrPanel().getInverseTransformation());
                            this.vista.getRecentsTrPanel().setParamsEPGS(epsgParams2.getParamValue());
                            this.vista.setTargetTransformation(this.vista.getRecentsTrPanel().getParams());
                        }
                        if (transform[0].equals("NADGR")) {
                            p = (String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 4);
                            this.vista.setTargetTransformation(this.vista.getNadsParams(p));
                        }
                        this.panel.getJButtonAccept().setEnabled(true);
                    }
                } else if (this.vista.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.EPSG_TRANSFORMATION_KEY)) {
                    cl.show(this.vista.getPCenter(), "epsg");
                    if (this.vista.getEpsgTrPanel().selectedRowTable == -1) {
                        this.vista.setTargetTransformation(null);
                        this.panel.getJButtonAccept().setEnabled(false);
                    } else {
                        epsgParams = new TransEPSG(this.vista.getEpsgTrPanel().getTransformation_code(), this.vista.getEpsgTrPanel().connect, this.vista.getEpsgTrPanel().inverseTranformation);
                        this.vista.getEpsgTrPanel().setValues(epsgParams.getParamValue());
                        this.vista.setTargetTransformation(this.vista.getParamsEpsg(this.vista.getEpsgTrPanel().getValues()));
                        this.panel.getJButtonAccept().setEnabled(true);
                    }
                } else if (this.vista.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY)) {
                    cl.show(this.vista.getPCenter(), "manual");
                    if (!this.vista.getManualTrPanel().getStatus()) {
                        this.panel.getJButtonAccept().setEnabled(false);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.vista.setTargetTransformation(this.vista.getManualParams());
                    }
                } else if (this.vista.getJComboOptions().getSelectedItem().equals(CRSI18NConstants.GRIDS_KEY)) {
                    cl.show(this.vista.getPCenter(), "nad");
                    if (this.vista.getNadsTrPanel().getJComboNadFile().getSelectedIndex() != 0) {
                        this.vista.setTargetTransformation("+nadgrids=" + this.vista.getNadsTrPanel().getNadFile());
                        this.panel.getJButtonAccept().setEnabled(true);
                    } else {
                        this.vista.setTargetTransformation(null);
                        this.panel.getJButtonAccept().setEnabled(false);
                    }
                }
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        String p;
        TransEPSG epsgParams;
        String[] transform;
        String[] transformation;
        String[] cad;
        TransEPSG epsgParams2;
        ResultSet result;
        String sentence;
        ListSelectionModel lsm;
        if (this.capa != null) {
            if (e.getSource() == this.capa.getEpsgTrPanel().getJTable().getSelectionModel()) {
                lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    this.capa.getEpsgTrPanel().selectedRowTable = -1;
                    this.panel.getJButtonNext().setEnabled(false);
                    if (this.panel.getJButtonAccept().isEnabled()) {
                        this.condition = true;
                    }
                    this.panel.getJButtonAccept().setEnabled(false);
                } else {
                    this.capa.getEpsgTrPanel().selectedRowTable = lsm.getMinSelectionIndex();
                    this.capa.getEpsgTrPanel().setTrasformation_code(Integer.parseInt((String)this.capa.getEpsgTrPanel().sorter.getValueAt(this.capa.getEpsgTrPanel().selectedRowTable, 0)));
                    sentence = "SELECT target_crs_code FROM epsg_coordoperation WHERE coord_op_code = " + this.capa.getEpsgTrPanel().getTransformation_code();
                    result = Query.select(sentence, this.capa.getEpsgTrPanel().connect.getConnection());
                    try {
                        result.next();
                        int tar = result.getInt("target_crs_code");
                        this.capa.getEpsgTrPanel().inverseTranformation = tar != this.capa.getEpsgTrPanel().crs_target;
                    }
                    catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    epsgParams2 = new TransEPSG(this.capa.getEpsgTrPanel().getTransformation_code(), this.capa.getEpsgTrPanel().connect, this.capa.getEpsgTrPanel().inverseTranformation);
                    this.capa.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.capa.setSourceTransformation(this.capa.getParamsEpsg(this.capa.getEpsgTrPanel().getValues()));
                    if (this.condition) {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.condition = false;
                    }
                    this.panel.getJButtonNext().setEnabled(true);
                }
            }
            if (e.getSource() == this.capa.getRecentsTrPanel().getJTable().getSelectionModel()) {
                lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    this.capa.getRecentsTrPanel().selectedRowTable = -1;
                    this.capa.getRecentsTrPanel().setCode(0);
                    this.panel.getJButtonNext().setEnabled(false);
                    this.capa.getRecentsTrPanel().getJButtonInfo().setEnabled(false);
                    if (this.panel.getJButtonAccept().isEnabled()) {
                        this.condition = true;
                    }
                    this.panel.getJButtonAccept().setEnabled(false);
                } else {
                    this.capa.getRecentsTrPanel().selectedRowTable = lsm.getMinSelectionIndex();
                    cad = ((String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 2)).split(":");
                    this.capa.getRecentsTrPanel().setCode(Integer.parseInt(cad[1]));
                    transformation = ((String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                    if (transformation[0].equals("EPSG")) {
                        this.capa.getRecentsTrPanel().setTrCode(Integer.parseInt(transformation[1]));
                    }
                    this.capa.getRecentsTrPanel().getJButtonInfo().setEnabled(true);
                    transform = ((String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                    if (transform[0].equals("USR")) {
                        this.capa.getRecentsTrPanel().setParamsManual((String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 4));
                        this.capa.setSourceTransformation(this.capa.getRecentsTrPanel().getParams());
                    }
                    if (transform[0].equals("EPSG")) {
                        epsgParams = new TransEPSG(this.capa.getRecentsTrPanel().getTrCode(), this.capa.getEpsgTrPanel().connect, this.capa.getRecentsTrPanel().getInverseTransformation());
                        this.capa.getRecentsTrPanel().setParamsEPGS(epsgParams.getParamValue());
                        this.capa.setSourceTransformation(this.capa.getRecentsTrPanel().getParams());
                    }
                    if (transform[0].equals("NADGR")) {
                        p = (String)this.capa.getRecentsTrPanel().sorter.getValueAt(this.capa.getRecentsTrPanel().selectedRowTable, 4);
                        this.capa.setSourceTransformation(this.capa.getNadsParams(p));
                    }
                    this.panel.getJButtonNext().setEnabled(true);
                    if (this.condition) {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.condition = false;
                    }
                }
            }
        }
        if (this.vista != null) {
            if (e.getSource() == this.vista.getEpsgTrPanel().getJTable().getSelectionModel()) {
                lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    this.vista.getEpsgTrPanel().selectedRowTable = -1;
                    this.panel.getJButtonAccept().setEnabled(false);
                } else {
                    this.vista.getEpsgTrPanel().selectedRowTable = lsm.getMinSelectionIndex();
                    this.vista.getEpsgTrPanel().setTrasformation_code(Integer.parseInt((String)this.vista.getEpsgTrPanel().sorter.getValueAt(this.vista.getEpsgTrPanel().selectedRowTable, 0)));
                    sentence = "SELECT target_crs_code FROM epsg_coordoperation WHERE coord_op_code = " + this.vista.getEpsgTrPanel().getTransformation_code();
                    result = Query.select(sentence, this.vista.getEpsgTrPanel().connect.getConnection());
                    try {
                        result.next();
                        int tar = result.getInt("target_crs_code");
                        this.vista.getEpsgTrPanel().inverseTranformation = tar != this.vista.getEpsgTrPanel().crs_target;
                    }
                    catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    epsgParams2 = new TransEPSG(this.vista.getEpsgTrPanel().getTransformation_code(), this.vista.getEpsgTrPanel().connect, this.vista.getEpsgTrPanel().inverseTranformation);
                    this.vista.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.vista.setTargetTransformation(this.vista.getParamsEpsg(this.vista.getEpsgTrPanel().getValues()));
                    this.panel.getJButtonAccept().setEnabled(true);
                }
            }
            if (e.getSource() == this.vista.getRecentsTrPanel().getJTable().getSelectionModel()) {
                lsm = (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    this.vista.getRecentsTrPanel().selectedRowTable = -1;
                    this.vista.getRecentsTrPanel().setCode(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                    this.vista.getRecentsTrPanel().getJButtonInfo().setEnabled(false);
                } else {
                    this.vista.getRecentsTrPanel().selectedRowTable = lsm.getMinSelectionIndex();
                    cad = ((String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 2)).split(":");
                    this.vista.getRecentsTrPanel().setCode(Integer.parseInt(cad[1]));
                    transformation = ((String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                    if (transformation[0].equals("EPSG")) {
                        this.vista.getRecentsTrPanel().setTrCode(Integer.parseInt(transformation[1]));
                    }
                    this.vista.getRecentsTrPanel().getJButtonInfo().setEnabled(true);
                    transform = ((String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                    if (transform[0].equals("USR")) {
                        this.vista.getRecentsTrPanel().setParamsManual((String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 4));
                        this.vista.setTargetTransformation(this.vista.getRecentsTrPanel().getParams());
                    }
                    if (transform[0].equals("EPSG")) {
                        epsgParams = new TransEPSG(this.vista.getRecentsTrPanel().getTrCode(), this.vista.getEpsgTrPanel().connect, this.vista.getRecentsTrPanel().getInverseTransformation());
                        this.vista.getRecentsTrPanel().setParamsEPGS(epsgParams.getParamValue());
                        this.vista.setTargetTransformation(this.vista.getRecentsTrPanel().getParams());
                    }
                    if (transform[0].equals("NADGR")) {
                        p = (String)this.vista.getRecentsTrPanel().sorter.getValueAt(this.vista.getRecentsTrPanel().selectedRowTable, 4);
                        this.vista.setTargetTransformation(this.vista.getNadsParams(p));
                    }
                    this.panel.getJButtonAccept().setEnabled(true);
                }
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (this.capa != null && e.getSource() == this.capa.getNadsTrPanel().getJComboNadFile()) {
            if (this.capa.getNadsTrPanel().getJComboNadFile().getSelectedIndex() != 0) {
                this.capa.getNadsTrPanel().setNadFile(this.capa.getNadsTrPanel().getJComboNadFile().getSelectedItem().toString());
                this.capa.getNadsTrPanel().getTreePanel().setRoot(String.valueOf(CRSI18NConstants.GRIDS_IN_KEY) + ": " + this.capa.getNadsTrPanel().getNadFile());
                this.capa.getNadsTrPanel().initializeTree();
                this.capa.getNadsTrPanel().saveNadFileName(this.capa.getNadsTrPanel().getNadFile());
                this.capa.setSourceTransformation("+nadgrids=" + this.capa.getNadsTrPanel().getNadFile());
                this.panel.getJButtonNext().setEnabled(true);
                if (this.condition) {
                    this.panel.getJButtonAccept().setEnabled(true);
                    this.condition = false;
                }
            } else {
                this.capa.getNadsTrPanel().setNadFile(null);
                this.capa.getNadsTrPanel().getTreePanel().setRoot("");
                this.capa.setSourceTransformation(null);
                this.panel.getJButtonNext().setEnabled(false);
                if (this.panel.getJButtonAccept().isEnabled()) {
                    this.condition = true;
                }
                this.panel.getJButtonAccept().setEnabled(false);
            }
        }
        if (this.vista != null && e.getSource() == this.vista.getNadsTrPanel().getJComboNadFile()) {
            if (this.vista.getNadsTrPanel().getJComboNadFile().getSelectedIndex() != 0) {
                this.vista.getNadsTrPanel().setNadFile(this.vista.getNadsTrPanel().getJComboNadFile().getSelectedItem().toString());
                this.vista.getNadsTrPanel().getTreePanel().setRoot(String.valueOf(CRSI18NConstants.GRIDS_IN_KEY) + ": " + this.vista.getNadsTrPanel().getNadFile());
                this.vista.getNadsTrPanel().initializeTree();
                this.vista.getNadsTrPanel().saveNadFileName(this.vista.getNadsTrPanel().getNadFile());
                this.vista.setTargetTransformation("+nadgrids=" + this.vista.getNadsTrPanel().getNadFile());
                this.panel.getJButtonAccept().setEnabled(true);
            } else {
                this.vista.getNadsTrPanel().setNadFile(null);
                this.vista.getNadsTrPanel().getTreePanel().setRoot("");
                this.vista.setTargetTransformation(null);
                this.panel.getJButtonAccept().setEnabled(false);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (this.capa != null) {
            if (!this.capa.getManualTrPanel().getStatus()) {
                this.panel.getJButtonNext().setEnabled(false);
            } else {
                this.panel.getJButtonNext().setEnabled(true);
                this.capa.setSourceTransformation(this.capa.getManualParams());
            }
        }
        if (this.vista != null) {
            if (!this.vista.getManualTrPanel().getStatus()) {
                this.panel.getJButtonAccept().setEnabled(false);
            } else {
                this.panel.getJButtonAccept().setEnabled(true);
                this.vista.setTargetTransformation(this.vista.getManualParams());
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}

