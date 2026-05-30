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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JComboBox;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsGT;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.CRSMainTrPanel;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.ogr.TransEPSG;
import org.gvsig.crs.persistence.CrsData;
import org.gvsig.crs.persistence.RecentCRSsPersistence;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class CRSMainTrPanelListener
implements ActionListener,
ListSelectionListener,
ItemListener,
MouseListener,
KeyListener {
    int[] valid_method_code = new int[]{9800, 9801, 9802, 9803, 9804, 9805, 9806, 9807, 9809, 9810, 9811, 9812, 9813, 9814, 9815, 9602, 9659, 9818, 9819, 9820, 9821, 9822, 9823, 9827, 9829};
    private CRSMainTrPanel panel = null;

    public CRSMainTrPanelListener(CRSMainTrPanel p) {
        this.panel = p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CardLayout cl;
        if (e.getSource() == this.panel.getJButtonAccept()) {
            String name;
            String authority;
            String authority2;
            if (this.panel.isEpsg_tr()) {
                TransEPSG epsgParams = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
                this.panel.getEpsgTrPanel().setValues(epsgParams.getParamValue());
                this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
                this.panel.setEpsg_tr(false);
                authority2 = "EPSG";
                String name2 = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 1);
                int code = this.panel.getEpsgTrPanel().getTransformation_code();
                String crsSource = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getSource());
                String crsTarget = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getTarget());
                String details = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 5);
                TrData trData = new TrData(authority2, code, name2, crsSource, crsTarget, details);
                RecentTrsPersistence trPersistence = new RecentTrsPersistence();
                trPersistence.addTrData(trData);
                trPersistence.setPersistent();
            } else if (this.panel.isManual_tr()) {
                boolean domain = this.panel.getManualTrPanel().correctJTextField();
                if (!domain || !this.panel.getManualTrPanel().correctDomain()) {
                    if (!domain) {
                        DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.ONLY_NUMERIC_CHARS_KEY, CRSI18NConstants.WARNING_KEY);
                        return;
                    }
                    DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.INVALID_DOMAIN_KEY, CRSI18NConstants.WARNING_KEY);
                    return;
                }
                this.panel.setProjection(this.panel.getManualTrPanel().getProjection());
                this.panel.setManual_tr(false);
                authority2 = "USR";
                String name3 = CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY;
                int code = 0;
                String crsSource = this.panel.getManualTrPanel().getSourceAbrev();
                String crsTarget = this.panel.getManualTrPanel().getTargetAbrev();
                String details = this.panel.getManualTrPanel().getValues();
                TrData trData = new TrData(authority2, code, name3, crsSource, crsTarget, details);
                RecentTrsPersistence trPersistence = new RecentTrsPersistence();
                trPersistence.addTrData(trData);
                trPersistence.setPersistent();
            } else if (this.panel.isNads_tr()) {
                this.panel.setProjection(this.panel.getNadsTrPanel().getProjection());
                this.panel.setTargetNad(this.panel.getNadsTrPanel().getNad());
                this.panel.setNads_tr(false);
                authority = "NADGR";
                name = "----";
                int code = 0;
                String crsSource = this.panel.getNadsTrPanel().getSourceAbrev();
                String crsTarget = this.panel.getNadsTrPanel().getTargetAbrev();
                String details = "";
                details = this.panel.getNadsTrPanel().getNad() ? String.valueOf(this.panel.getNadsTrPanel().getNadFile()) + " (" + this.panel.getNadsTrPanel().getTargetAbrev() + ")" : String.valueOf(this.panel.getNadsTrPanel().getNadFile()) + " (" + this.panel.getNadsTrPanel().getSourceAbrev() + ")";
                TrData trData = new TrData(authority, code, name, crsSource, crsTarget, details);
                RecentTrsPersistence trPersistence = new RecentTrsPersistence();
                trPersistence.addTrData(trData);
                trPersistence.setPersistent();
            } else if (this.panel.isRecents_tr()) {
                String[] transformation = ((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                if (transformation[0].equals("USR")) {
                    this.panel.getRecentsTrPanel().setParamsManual((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4));
                    this.panel.setProjection(this.panel.getRecentsTrPanel().getProjection());
                }
                if (transformation[0].equals("EPSG")) {
                    TransEPSG epsgParams = new TransEPSG(this.panel.getRecentsTrPanel().getTrCode(), this.panel.getRecentsTrPanel().connect, this.panel.getRecentsTrPanel().getInverseTransformation());
                    this.panel.getRecentsTrPanel().setParamsEPGS(epsgParams.getParamValue());
                    this.panel.setProjection(this.panel.getRecentsTrPanel().getProjection());
                }
                if (transformation[0].equals("NADGR")) {
                    this.panel.setProjection(this.panel.getRecentsTrPanel().getProjectionNad((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4)));
                }
                transformation[0].equals("COMP");
                this.panel.setRecents_tr(false);
                String authCode = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0);
                String authority3 = authCode.split(":")[0];
                String name4 = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 1);
                int code = Integer.parseInt(authCode.split(":")[1]);
                String crsSource = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 2);
                String crsTarget = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 3);
                String details = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4);
                TrData trData = new TrData(authority3, code, name4, crsSource, crsTarget, details);
                RecentTrsPersistence trPersistence = new RecentTrsPersistence();
                trPersistence.addTrData(trData);
                trPersistence.setPersistent();
            } else {
                this.panel.setSin_tr(true);
                this.panel.setProjection(this.panel.getProjection());
                authority = this.panel.getProjection().getCrsWkt().getAuthority()[0];
                name = this.panel.getProjection().getCrsWkt().getName();
                int code = this.panel.getProjection().getCode();
                CrsData crsData = new CrsData(authority, code, name);
                RecentCRSsPersistence persistence = new RecentCRSsPersistence();
                persistence.addCrsData(crsData);
            }
            DialogManager.closeJDialog(this.panel);
        }
        if (e.getSource() == this.panel.getJButtonBefore()) {
            cl = (CardLayout)this.panel.getJPanelMain().getLayout();
            if (this.panel.getNewSelection().equals(CRSI18NConstants.COMPOUND_TRANSFORMATION_KEY) && this.panel.isVista_tr()) {
                cl.show(this.panel.getJPanelMain(), "capa");
                this.panel.setVista_tr(false);
                this.panel.getJButtonNext().setEnabled(true);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getJButtonBefore().setEnabled(true);
            } else {
                cl.show(this.panel.getJPanelMain(), "primero");
                if (!this.panel.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                } else {
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                    int sel = this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable;
                    this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getJTable().setRowSelectionInterval(0, 0);
                    this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getJTable().setRowSelectionInterval(sel, sel);
                }
                this.panel.getJButtonBefore().setEnabled(false);
            }
        }
        if (e.getSource() == this.panel.getJButtonCancel()) {
            this.panel.setCancelYes(true);
            this.panel.setProjection(this.panel.getCrsfirst());
            DialogManager.closeJDialog(this.panel);
        }
        if (e.getSource() == this.panel.getJButtonNext()) {
            this.panel.getJButtonNext().setEnabled(false);
            this.panel.getJButtonBefore().setEnabled(true);
            if (this.panel.getNewSelection().equals(CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY)) {
                cl = (CardLayout)this.panel.getJPanelMain().getLayout();
                if (this.panel.getDataSource().equals("EPSG")) {
                    this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getEpsgPanel().getWKT());
                    this.panel.getManualTrPanel().setCode(this.panel.getCrsMainPanel().getEpsgPanel().epsg_code);
                    this.panel.getManualTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                    if (this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs() instanceof CrsGT) {
                        this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs());
                    } else {
                        this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                    }
                    if (!((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3)).equals(CRSI18NConstants.NO_TRANSFORMATION_KEY)) {
                        if (this.getCorrectOption().equals("USR")) {
                            this.panel.getManualTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        } else {
                            this.panel.getManualTrPanel().resetData();
                        }
                    } else {
                        this.panel.getManualTrPanel().resetData();
                    }
                    this.panel.getManualTrPanel().setCode(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                    this.panel.getManualTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals("IAU2000")) {
                    this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getIauPanel().getWKT());
                    this.panel.getManualTrPanel().setCode(this.panel.getCrsMainPanel().getIauPanel().getCodeCRS());
                    this.panel.getManualTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals("ESRI")) {
                    this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getEsriPanel().getWKT());
                    this.panel.getManualTrPanel().setCode(this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS());
                    this.panel.getManualTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.USER_CRS_KEY)) {
                    this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getNewCrsPanel().getWKT());
                    this.panel.getManualTrPanel().setCode(this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS());
                    this.panel.getManualTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                }
                if (!this.panel.getManualTrPanel().getStatus()) {
                    this.panel.getJButtonAccept().setEnabled(false);
                }
                this.panel.setManual_tr(true);
                cl.show(this.panel.getJPanelMain(), "manual");
            } else if (this.panel.getNewSelection().equals(CRSI18NConstants.GRIDS_KEY)) {
                cl = (CardLayout)this.panel.getJPanelMain().getLayout();
                if (this.panel.getDataSource().equals("EPSG")) {
                    this.panel.getNadsTrPanel().setWKT(this.panel.getCrsMainPanel().getEpsgPanel().getWKT());
                    this.panel.getNadsTrPanel().setSourceAbrev("EPSG", "" + this.panel.getCrsMainPanel().getEpsgPanel().epsg_code);
                    this.panel.getNadsTrPanel().setCode(this.panel.getCrsMainPanel().getEpsgPanel().epsg_code);
                    this.panel.getNadsTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                    this.panel.getNadsTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                    String sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                    this.panel.getNadsTrPanel().setSourceAbrev(sour, "" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setCode(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                    if (!((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3)).equals(CRSI18NConstants.NO_TRANSFORMATION_KEY)) {
                        if (this.getCorrectOption().equals("NADGR")) {
                            this.panel.getNadsTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        } else {
                            this.panel.getNadsTrPanel().resetData();
                        }
                    } else {
                        this.panel.getNadsTrPanel().resetData();
                    }
                } else if (this.panel.getDataSource().equals("IAU2000")) {
                    this.panel.getNadsTrPanel().setWKT(this.panel.getCrsMainPanel().getIauPanel().getWKT());
                    this.panel.getNadsTrPanel().setSourceAbrev("IAU2000", "" + this.panel.getCrsMainPanel().getIauPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setCode(this.panel.getCrsMainPanel().getIauPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals("ESRI")) {
                    this.panel.getNadsTrPanel().setWKT(this.panel.getCrsMainPanel().getEsriPanel().getWKT());
                    this.panel.getNadsTrPanel().setSourceAbrev("ESRI", "" + this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setCode(this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.USER_CRS_KEY)) {
                    this.panel.getNadsTrPanel().setWKT(this.panel.getCrsMainPanel().getNewCrsPanel().getWKT());
                    this.panel.getNadsTrPanel().setSourceAbrev("USR", "" + this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setCode(this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS());
                    this.panel.getNadsTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                }
                if (this.panel.getNadsTrPanel().getJComboNadFile().getSelectedIndex() == 0) {
                    this.panel.getJButtonAccept().setEnabled(false);
                } else {
                    this.panel.getJButtonAccept().setEnabled(true);
                }
                this.panel.setNads_tr(true);
                cl.show(this.panel.getJPanelMain(), "nad");
            } else if (this.panel.getNewSelection().equals(CRSI18NConstants.EPSG_TRANSFORMATION_KEY)) {
                cl = (CardLayout)this.panel.getJPanelMain().getLayout();
                if (this.panel.getDataSource().equals("EPSG")) {
                    this.panel.getEpsgTrPanel().setWKT(this.panel.getCrsMainPanel().getEpsgPanel().getWKT());
                    this.panel.getEpsgTrPanel().setSource("EPSG", this.panel.getCrsMainPanel().getEpsgPanel().epsg_code);
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                    this.panel.getEpsgTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                    String sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                    this.panel.getEpsgTrPanel().setSource(sour, this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                    if (!((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3)).equals(CRSI18NConstants.NO_TRANSFORMATION_KEY)) {
                        if (this.getCorrectOption().equals("EPSG")) {
                            this.panel.getEpsgTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        } else {
                            this.panel.getEpsgTrPanel().resetData();
                        }
                    } else {
                        this.panel.getEpsgTrPanel().resetData();
                    }
                } else if (this.panel.getDataSource().equals("IAU2000")) {
                    this.panel.getEpsgTrPanel().setWKT(this.panel.getCrsMainPanel().getIauPanel().getWKT());
                    this.panel.getEpsgTrPanel().setSource("IAU2000", this.panel.getCrsMainPanel().getIauPanel().getCodeCRS());
                } else if (this.panel.getDataSource().equals("ESRI")) {
                    this.panel.getEpsgTrPanel().setWKT(this.panel.getCrsMainPanel().getEsriPanel().getWKT());
                    this.panel.getEpsgTrPanel().setSource("ESRI", this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS());
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.USER_CRS_KEY)) {
                    this.panel.getEpsgTrPanel().setWKT(this.panel.getCrsMainPanel().getNewCrsPanel().getWKT());
                    this.panel.getEpsgTrPanel().setSource("USR", this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS());
                }
                int numr = this.panel.getEpsgTrPanel().dtm.getRowCount();
                if (numr == 0) {
                    this.panel.getJButtonAccept().setEnabled(false);
                }
                this.panel.setEpsg_tr(true);
                cl.show(this.panel.getJPanelMain(), "epsg");
            } else if (this.panel.getNewSelection().equals(CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY)) {
                cl = (CardLayout)this.panel.getJPanelMain().getLayout();
                if (this.panel.getDataSource().equals("EPSG")) {
                    this.panel.getRecentsTrPanel().setWKT(this.panel.getCrsMainPanel().getEpsgPanel().getWKT());
                    this.panel.getRecentsTrPanel().loadRecents("EPSG:" + this.panel.getCrsMainPanel().getEpsgPanel().getCodeCRS(), String.valueOf(this.panel.getCrsWkt_target().getAuthority()[0]) + ":" + this.panel.getCrsWkt_target().getAuthority()[1]);
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                    this.panel.getRecentsTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                    String sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                    this.panel.getRecentsTrPanel().loadRecents(String.valueOf(sour) + ":" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS(), String.valueOf(this.panel.getCrsWkt_target().getAuthority()[0]) + ":" + this.panel.getCrsWkt_target().getAuthority()[1]);
                } else if (this.panel.getDataSource().equals("IAU2000")) {
                    this.panel.getRecentsTrPanel().setWKT(this.panel.getCrsMainPanel().getIauPanel().getWKT());
                    this.panel.getRecentsTrPanel().loadRecents("IAU2000:" + this.panel.getCrsMainPanel().getIauPanel().getCodeCRS(), String.valueOf(this.panel.getCrsWkt_target().getAuthority()[0]) + ":" + this.panel.getCrsWkt_target().getAuthority()[1]);
                } else if (this.panel.getDataSource().equals("ESRI")) {
                    this.panel.getRecentsTrPanel().setWKT(this.panel.getCrsMainPanel().getEsriPanel().getWKT());
                    this.panel.getRecentsTrPanel().loadRecents("ESRI:" + this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS(), String.valueOf(this.panel.getCrsWkt_target().getAuthority()[0]) + ":" + this.panel.getCrsWkt_target().getAuthority()[1]);
                } else if (this.panel.getDataSource().equals(CRSI18NConstants.USER_CRS_KEY)) {
                    this.panel.getRecentsTrPanel().setWKT(this.panel.getCrsMainPanel().getNewCrsPanel().getWKT());
                    this.panel.getRecentsTrPanel().loadRecents("USR:" + this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS(), String.valueOf(this.panel.getCrsWkt_target().getAuthority()[0]) + ":" + this.panel.getCrsWkt_target().getAuthority()[1]);
                }
                int numr = this.panel.getRecentsTrPanel().dtm.getRowCount();
                if (numr == 0) {
                    this.panel.getJButtonAccept().setEnabled(false);
                }
                this.panel.setRecents_tr(true);
                cl.show(this.panel.getJPanelMain(), "recents");
            } else if (this.panel.getNewSelection().equals(CRSI18NConstants.COMPOUND_TRANSFORMATION_KEY)) {
                if (!this.panel.isCapa_tr()) {
                    if (this.panel.getDataSource().equals("EPSG")) {
                        this.panel.getCapaTrPanel().setCrs_source("EPSG:" + this.panel.getCrsMainPanel().getEpsgPanel().epsg_code);
                        this.panel.getVistaTrPanel().setCrs_source("EPSG:" + this.panel.getCrsMainPanel().getEpsgPanel().epsg_code);
                        this.panel.getCapaTrPanel().fillData();
                        this.panel.getVistaTrPanel().fillData();
                    } else if (this.panel.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                        String sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                        this.panel.getCapaTrPanel().setCrs_source(String.valueOf(sour) + ":" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getVistaTrPanel().setCrs_source(String.valueOf(sour) + ":" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getCapaTrPanel().fillData();
                        this.panel.getVistaTrPanel().fillData();
                        if (!((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3)).equals(CRSI18NConstants.NO_TRANSFORMATION_KEY)) {
                            if (this.getCorrectOption().equals(CRSI18NConstants.COMPOUND_TRANSFORMATION_KEY)) {
                                this.panel.getCapaTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                                this.panel.getVistaTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                                this.panel.getJButtonAccept().setEnabled(true);
                            } else {
                                this.panel.getCapaTrPanel().resetData();
                                this.panel.getVistaTrPanel().resetData();
                            }
                        } else {
                            this.panel.getCapaTrPanel().resetData();
                            this.panel.getVistaTrPanel().resetData();
                        }
                    } else if (this.panel.getDataSource().equals("IAU2000")) {
                        this.panel.getCapaTrPanel().setCrs_source("IAU2000:" + this.panel.getCrsMainPanel().getIauPanel().getCodeCRS());
                        this.panel.getVistaTrPanel().setCrs_source("IAU2000:" + this.panel.getCrsMainPanel().getIauPanel().getCodeCRS());
                        this.panel.getCapaTrPanel().fillData();
                        this.panel.getVistaTrPanel().fillData();
                    } else if (this.panel.getDataSource().equals("ESRI")) {
                        this.panel.getCapaTrPanel().setCrs_source("ESRI:" + this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS());
                        this.panel.getVistaTrPanel().setCrs_source("ESRI:" + this.panel.getCrsMainPanel().getEsriPanel().getCodeCRS());
                        this.panel.getCapaTrPanel().fillData();
                        this.panel.getVistaTrPanel().fillData();
                    } else if (this.panel.getDataSource().equals(CRSI18NConstants.USER_CRS_KEY)) {
                        this.panel.getCapaTrPanel().setCrs_source("USR:" + this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS());
                        this.panel.getVistaTrPanel().setCrs_source("USR:" + this.panel.getCrsMainPanel().getNewCrsPanel().getCodeCRS());
                        this.panel.getCapaTrPanel().fillData();
                        this.panel.getVistaTrPanel().fillData();
                    }
                }
                if (this.panel.isCapa_tr()) {
                    this.panel.setVista_tr(true);
                    this.panel.getVistaTrPanel().setSourceTransformation(this.panel.getCapaTrPanel().getSourceTransformation());
                    cl = (CardLayout)this.panel.getJPanelMain().getLayout();
                    cl.show(this.panel.getJPanelMain(), "vista");
                    this.panel.getJButtonNext().setEnabled(false);
                    if (this.panel.getVistaTrPanel().isSthSelected()) {
                        this.panel.getJButtonAccept().setEnabled(true);
                    }
                } else {
                    if (this.panel.getCapaTrPanel().getRecentCompTransformation()) {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(true);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(false);
                        if (this.panel.getCapaTrPanel().isSthSelected()) {
                            this.panel.getJButtonNext().setEnabled(true);
                        }
                    }
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(true);
                    cl = (CardLayout)this.panel.getJPanelMain().getLayout();
                    cl.show(this.panel.getJPanelMain(), "capa");
                }
            }
        }
        if (e.getSource() == this.panel.getJComboOptions()) {
            JComboBox cb = (JComboBox)e.getSource();
            this.panel.setNewSelection((String)cb.getSelectedItem());
            if (this.panel.getNewSelection().equals(CRSI18NConstants.NO_TRANSFORMATION_KEY)) {
                this.panel.getJButtonAccept().setEnabled(true);
                this.panel.getJButtonNext().setEnabled(false);
                this.panel.setEpsg_tr(false);
                this.panel.setNads_tr(false);
                this.panel.setManual_tr(false);
                this.panel.setRecents_tr(false);
                this.panel.setCapa_tr(false);
                this.panel.setVista_tr(false);
            } else {
                if (this.panel.getNewSelection().equals(CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY)) {
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(true);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                } else if (this.panel.getNewSelection().equals(CRSI18NConstants.EPSG_TRANSFORMATION_KEY)) {
                    this.panel.setEpsg_tr(true);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                } else if (this.panel.getNewSelection().equals(CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY)) {
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(true);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                } else if (this.panel.getNewSelection().equals(CRSI18NConstants.COMPOUND_TRANSFORMATION_KEY)) {
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(false);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                } else if (this.panel.getNewSelection().equals(CRSI18NConstants.GRIDS_KEY)) {
                    this.panel.setEpsg_tr(false);
                    this.panel.setNads_tr(true);
                    this.panel.setManual_tr(false);
                    this.panel.setRecents_tr(false);
                    this.panel.setCapa_tr(false);
                    this.panel.setVista_tr(false);
                }
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getJButtonNext().setEnabled(true);
            }
        }
        if (e.getSource() == this.panel.getRecentsTrPanel().getJButtonInfo()) {
            String[] data = new String[]{(String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0), (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 1), (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 2), (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 3), (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4)};
            InfoCRSPanel info = new InfoCRSPanel(data[2].split(":")[0], Integer.parseInt(data[2].split(":")[1]), String.valueOf(data[0]) + " <--> " + data[4]);
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }

    private String getCorrectOption() {
        String item = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3);
        if (item.startsWith("EPSG")) {
            return "EPSG";
        }
        if (item.startsWith("NADGR")) {
            return "NADGR";
        }
        if (item.startsWith("USR")) {
            return "USR";
        }
        if (item.startsWith("COMP")) {
            return "COMP";
        }
        return "";
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        CrsWkt crs;
        String[] not_soported;
        int i;
        String crs_kind;
        ListSelectionModel lsm;
        if (e.getSource() == this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable = -1;
                this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().setCodeCRS(-1);
                this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getInfoCrs().setEnabled(false);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getJComboOptions().setEnabled(false);
                this.panel.getJButtonNext().setEnabled(false);
                this.panel.getJComboOptions().setSelectedIndex(0);
            } else {
                this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().initCrs();
                this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getInfoCrs().setEnabled(true);
                if (((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3)).equals(CRSI18NConstants.NO_TRANSFORMATION_KEY)) {
                    if (this.panel.getJComboOptions().getSelectedIndex() == 0) {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(false);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(false);
                        this.panel.getJButtonNext().setEnabled(true);
                    }
                } else {
                    String sour;
                    String option = this.getCorrectItem();
                    if (option.equals("USR")) {
                        if (this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs() instanceof CrsGT) {
                            this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs());
                        } else {
                            this.panel.getManualTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                        }
                        this.panel.getManualTrPanel().setCode(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getManualTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                        this.panel.getManualTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        this.panel.setManual_tr(true);
                        this.panel.setEpsg_tr(false);
                        this.panel.setNads_tr(false);
                        this.panel.setRecents_tr(false);
                    } else if (option.equals("EPSG")) {
                        this.panel.getEpsgTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                        sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                        this.panel.getEpsgTrPanel().setSource(sour, this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getEpsgTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        this.panel.setManual_tr(false);
                        this.panel.setEpsg_tr(true);
                        this.panel.setNads_tr(false);
                        this.panel.setRecents_tr(false);
                    } else if (option.equals("NADGR")) {
                        this.panel.getNadsTrPanel().setWKT(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getWKT());
                        sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                        this.panel.getNadsTrPanel().setSourceAbrev(sour, "" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getNadsTrPanel().setCode(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getNadsTrPanel().setTargetAuthority(this.panel.getCrsWkt_target().getAuthority());
                        this.panel.getNadsTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        this.panel.setManual_tr(false);
                        this.panel.setEpsg_tr(false);
                        this.panel.setNads_tr(true);
                        this.panel.setRecents_tr(false);
                    } else if (option.equals("COMP")) {
                        sour = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 0);
                        this.panel.getCapaTrPanel().setCrs_source(String.valueOf(sour) + ":" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getVistaTrPanel().setCrs_source(String.valueOf(sour) + ":" + this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCodeCRS());
                        this.panel.getCapaTrPanel().fillData();
                        this.panel.getVistaTrPanel().fillData();
                        this.panel.getCapaTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        this.panel.getVistaTrPanel().fillData((String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3));
                        this.panel.setManual_tr(false);
                        this.panel.setEpsg_tr(false);
                        this.panel.setNads_tr(false);
                        this.panel.setRecents_tr(false);
                        this.panel.setCompuesta_tr(true);
                    }
                    this.panel.getJButtonNext().setEnabled(true);
                    this.panel.getJButtonAccept().setEnabled(true);
                }
                this.panel.getJComboOptions().setEnabled(true);
            }
        }
        if (e.getSource() == this.panel.getCrsMainPanel().getEpsgPanel().getJTable().getSelectionModel()) {
            String[] not_soported2 = new String[]{"engineering", "vertical", "compound", "geocentric", "geographic 3D"};
            boolean soported = true;
            boolean soported2 = false;
            ListSelectionModel lsm2 = (ListSelectionModel)e.getSource();
            if (lsm2.isSelectionEmpty()) {
                this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable = -1;
                this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(-1);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(false);
                this.panel.getJComboOptions().setEnabled(false);
                this.panel.getJButtonNext().setEnabled(false);
                this.panel.getJComboOptions().setSelectedIndex(0);
            } else {
                this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable = lsm2.getMinSelectionIndex();
                crs_kind = (String)this.panel.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable, 2);
                i = 0;
                while (i < not_soported2.length) {
                    if (crs_kind.equals(not_soported2[i])) {
                        soported = false;
                    }
                    ++i;
                }
                i = 0;
                while (i < this.valid_method_code.length) {
                    if (this.panel.getCrsMainPanel().getEpsgPanel().getProjectionCode((String)this.panel.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable, 0)) == this.valid_method_code[i]) {
                        soported2 = true;
                    }
                    ++i;
                }
                if (crs_kind.equals("geographic 2D")) {
                    soported2 = true;
                }
                if (soported && soported2) {
                    this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable, 0)));
                    this.panel.getCrsMainPanel().getEpsgPanel().setWKT();
                    this.panel.getJButtonAccept().setEnabled(true);
                    int base = this.panel.getCrsMainPanel().getEpsgPanel().getCodeCRS();
                    this.panel.getJComboOptions().setEnabled(true);
                    this.panel.getJButtonAccept().setEnabled(true);
                    this.panel.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(true);
                } else {
                    DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
                    this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(-1);
                    this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                    this.panel.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(false);
                }
            }
        }
        if (e.getSource() == this.panel.getCrsMainPanel().getIauPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            not_soported = new String[]{"Oblique_Cylindrical_Equal_Area"};
            boolean soported = true;
            if (lsm.isSelectionEmpty()) {
                this.panel.getCrsMainPanel().getIauPanel().selectedRowTable = -1;
                this.panel.getCrsMainPanel().getIauPanel().setCodeCRS(-1);
                this.panel.getCrsMainPanel().getIauPanel().getInfoCrs().setEnabled(false);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getJComboOptions().setEnabled(false);
                this.panel.getJButtonNext().setEnabled(false);
                this.panel.getJComboOptions().setSelectedIndex(0);
            } else {
                this.panel.getCrsMainPanel().getIauPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.panel.getCrsMainPanel().getIauPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getIauPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getIauPanel().selectedRowTable, 0)));
                this.panel.getCrsMainPanel().getIauPanel().setWKT();
                crs = new CrsWkt(this.panel.getCrsMainPanel().getIauPanel().getWKT());
                crs_kind = crs.getProjection();
                i = 0;
                while (i < not_soported.length) {
                    if (crs_kind.equals(not_soported[i])) {
                        soported = false;
                    }
                    ++i;
                }
                if (soported) {
                    this.panel.getCrsMainPanel().getIauPanel().getInfoCrs().setEnabled(true);
                    this.panel.getJComboOptions().setEnabled(true);
                    this.panel.getJButtonAccept().setEnabled(true);
                } else {
                    DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
                    this.panel.getCrsMainPanel().getIauPanel().setCodeCRS(-1);
                    this.panel.getCrsMainPanel().getIauPanel().setCodeCRS(0);
                    this.panel.getCrsMainPanel().getIauPanel().getInfoCrs().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            }
        }
        if (e.getSource() == this.panel.getCrsMainPanel().getEsriPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            not_soported = new String[]{"Oblique_Cylindrical_Equal_Area"};
            boolean soported = true;
            if (lsm.isSelectionEmpty()) {
                this.panel.getCrsMainPanel().getEsriPanel().selectedRowTable = -1;
                this.panel.getCrsMainPanel().getEsriPanel().setCodeCRS(-1);
                this.panel.getCrsMainPanel().getEsriPanel().getInfoCrs().setEnabled(false);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getJComboOptions().setEnabled(false);
                this.panel.getJButtonNext().setEnabled(false);
                this.panel.getJComboOptions().setSelectedIndex(0);
            } else {
                this.panel.getCrsMainPanel().getEsriPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.panel.getCrsMainPanel().getEsriPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getEsriPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEsriPanel().selectedRowTable, 0)));
                this.panel.getCrsMainPanel().getEsriPanel().setWKT();
                crs = new CrsWkt(this.panel.getCrsMainPanel().getEsriPanel().getWKT());
                crs_kind = crs.getProjection();
                i = 0;
                while (i < not_soported.length) {
                    if (crs_kind.equals(not_soported[i])) {
                        soported = false;
                    }
                    ++i;
                }
                if (soported) {
                    this.panel.getCrsMainPanel().getEsriPanel().getInfoCrs().setEnabled(true);
                    this.panel.getJComboOptions().setEnabled(true);
                    this.panel.getJButtonAccept().setEnabled(true);
                } else {
                    DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
                    this.panel.getCrsMainPanel().getEsriPanel().setCodeCRS(-1);
                    this.panel.getCrsMainPanel().getEsriPanel().setCodeCRS(0);
                    this.panel.getCrsMainPanel().getEsriPanel().getInfoCrs().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            }
        }
        if (e.getSource() == this.panel.getCrsMainPanel().getNewCrsPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                this.panel.getCrsMainPanel().getNewCrsPanel().selectedRowTable = -1;
                this.panel.getCrsMainPanel().getNewCrsPanel().setCodeCRS(-1);
                this.panel.getCrsMainPanel().getNewCrsPanel().getInfoCrs().setEnabled(false);
                this.panel.getCrsMainPanel().getNewCrsPanel().getBtnEditar().setEnabled(false);
                this.panel.getCrsMainPanel().getNewCrsPanel().getBtnEliminar().setEnabled(false);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getJComboOptions().setEnabled(false);
                this.panel.getJButtonNext().setEnabled(false);
                this.panel.getJComboOptions().setSelectedIndex(0);
            } else {
                this.panel.getCrsMainPanel().getNewCrsPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.panel.getCrsMainPanel().getNewCrsPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getNewCrsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getNewCrsPanel().selectedRowTable, 0)));
                this.panel.getCrsMainPanel().getNewCrsPanel().getInfoCrs().setEnabled(true);
                this.panel.getCrsMainPanel().getNewCrsPanel().setWKT();
                this.panel.getCrsMainPanel().getNewCrsPanel().getBtnEditar().setEnabled(true);
                this.panel.getCrsMainPanel().getNewCrsPanel().getBtnEliminar().setEnabled(true);
                this.panel.getJComboOptions().setEnabled(true);
                this.panel.getJButtonAccept().setEnabled(true);
            }
        }
        if (e.getSource() == this.panel.getEpsgTrPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                this.panel.getEpsgTrPanel().selectedRowTable = -1;
                this.panel.getJButtonAccept().setEnabled(false);
            } else {
                this.panel.getEpsgTrPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.panel.getEpsgTrPanel().setTrasformation_code(Integer.parseInt((String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 0)));
                String sentence = "SELECT target_crs_code FROM epsg_coordoperation WHERE coord_op_code = " + this.panel.getEpsgTrPanel().getTransformation_code();
                ResultSet result = Query.select(sentence, this.panel.getEpsgTrPanel().connect.getConnection());
                try {
                    result.next();
                    int tar = result.getInt("target_crs_code");
                    this.panel.getEpsgTrPanel().inverseTranformation = tar != this.panel.getEpsgTrPanel().crs_target;
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
                this.panel.getJButtonAccept().setEnabled(true);
            }
        }
        if (e.getSource() == this.panel.getRecentsTrPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                this.panel.getRecentsTrPanel().selectedRowTable = -1;
                this.panel.getRecentsTrPanel().setCode(0);
                this.panel.getJButtonAccept().setEnabled(false);
                this.panel.getRecentsTrPanel().getJButtonInfo().setEnabled(false);
            } else {
                this.panel.getRecentsTrPanel().selectedRowTable = lsm.getMinSelectionIndex();
                String[] cad = ((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 2)).split(":");
                this.panel.getRecentsTrPanel().setCode(Integer.parseInt(cad[1]));
                String[] transformation = ((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                if (transformation[0].equals("EPSG")) {
                    this.panel.getRecentsTrPanel().setTrCode(Integer.parseInt(transformation[1]));
                    String sentence = "SELECT target_crs_code FROM epsg_coordoperation WHERE coord_op_code = " + this.panel.getRecentsTrPanel().getTrCode();
                    ResultSet result = Query.select(sentence, this.panel.getRecentsTrPanel().connect.getConnection());
                    try {
                        result.next();
                        int tar = result.getInt("target_crs_code");
                        int crs_target = Integer.parseInt(((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 3)).split(":")[1]);
                        int crs_base = -1;
                        sentence = "SELECT source_geogcrs_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + crs_target;
                        result = Query.select(sentence, this.panel.getRecentsTrPanel().connect.getConnection());
                        try {
                            result.next();
                            crs_base = result.getInt("source_geogcrs_code");
                        }
                        catch (SQLException e1) {
                            crs_base = -1;
                        }
                        if (tar == crs_target || tar == crs_base) {
                            this.panel.getRecentsTrPanel().setInverseTransformation(false);
                        } else {
                            this.panel.getRecentsTrPanel().setInverseTransformation(true);
                        }
                    }
                    catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
                this.panel.getRecentsTrPanel().getJButtonInfo().setEnabled(true);
                this.panel.getJButtonAccept().setEnabled(true);
            }
        }
    }

    private String getCorrectItem() {
        String item = (String)this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().selectedRowTable, 3);
        if (item.startsWith("EPSG")) {
            this.panel.getJComboOptions().setSelectedIndex(2);
            return "EPSG";
        }
        if (item.startsWith("NADGR")) {
            this.panel.getJComboOptions().setSelectedIndex(4);
            return "NADGR";
        }
        if (item.startsWith("USR")) {
            this.panel.getJComboOptions().setSelectedIndex(3);
            return "USR";
        }
        return "";
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == this.panel.getCrsMainPanel().getJComboOptions()) {
            CardLayout cl = (CardLayout)this.panel.getCrsMainPanel().getJPanelMain().getLayout();
            String op = (String)e.getItem();
            if (op.equals("EPSG")) {
                String[] not_soported = new String[]{"engineering", "vertical", "compound", "geocentric", "geographic 3D"};
                boolean soported = true;
                boolean soported2 = false;
                this.panel.getCrsMainPanel().getEpsgPanel().connection();
                if (this.panel.getCrsMainPanel().getEpsgPanel().getJTable().getSelectedRowCount() > 0) {
                    this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable = this.panel.getCrsMainPanel().getEpsgPanel().getJTable().getSelectedRow();
                    String crs_kind = (String)this.panel.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable, 2);
                    int i = 0;
                    while (i < not_soported.length) {
                        if (crs_kind.equals(not_soported[i])) {
                            soported = false;
                        }
                        ++i;
                    }
                    i = 0;
                    while (i < this.valid_method_code.length) {
                        if (this.panel.getCrsMainPanel().getEpsgPanel().getProjectionCode((String)this.panel.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable, 0)) == this.valid_method_code[i]) {
                            soported2 = true;
                        }
                        ++i;
                    }
                    if (crs_kind.equals("geographic 2D")) {
                        soported2 = true;
                    }
                    if (soported && soported2) {
                        this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEpsgPanel().selectedRowTable, 0)));
                        this.panel.getCrsMainPanel().getEpsgPanel().setWKT();
                        this.panel.getJButtonAccept().setEnabled(true);
                        int base = this.panel.getCrsMainPanel().getEpsgPanel().getCodeCRS();
                        this.panel.getJComboOptions().setEnabled(true);
                        if (this.panel.getJComboOptions().getSelectedIndex() > 0) {
                            this.panel.getJButtonAccept().setEnabled(false);
                            this.panel.getJButtonNext().setEnabled(true);
                            this.panel.getJButtonNext().setEnabled(true);
                        } else {
                            this.panel.getJButtonAccept().setEnabled(true);
                            this.panel.getJButtonAccept().setEnabled(true);
                            this.panel.getJButtonNext().setEnabled(false);
                        }
                        this.panel.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(true);
                    } else {
                        DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
                        this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(-1);
                        this.panel.getCrsMainPanel().getEpsgPanel().setCodeCRS(0);
                        this.panel.getJButtonAccept().setEnabled(false);
                        this.panel.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(false);
                    }
                } else {
                    this.panel.getJButtonNext().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJComboOptions().setSelectedIndex(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            } else if (op.equals("IAU2000")) {
                this.panel.getCrsMainPanel().getIauPanel().connection();
                if (this.panel.getCrsMainPanel().getIauPanel().getJTable().getSelectedRowCount() > 0) {
                    this.panel.getCrsMainPanel().getIauPanel().selectedRowTable = this.panel.getCrsMainPanel().getIauPanel().getJTable().getSelectedRow();
                    this.panel.getCrsMainPanel().getIauPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getIauPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getIauPanel().selectedRowTable, 0)));
                    this.panel.getCrsMainPanel().getIauPanel().setWKT();
                    this.panel.getJComboOptions().setEnabled(true);
                    if (this.panel.getJComboOptions().getSelectedIndex() > 0) {
                        this.panel.getJButtonAccept().setEnabled(false);
                        this.panel.getJButtonNext().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(true);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(false);
                    }
                } else {
                    this.panel.getJButtonNext().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJComboOptions().setSelectedIndex(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            } else if (op.equals("Recientes")) {
                if (this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getJTable().getSelectedRowCount() > 0) {
                    this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().initCrs();
                    String sour = this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getCrs().getCrsWkt().getAuthority()[0];
                    this.panel.getJComboOptions().setEnabled(true);
                    if (this.panel.getJComboOptions().getSelectedIndex() > 0) {
                        this.panel.getJButtonAccept().setEnabled(false);
                        this.panel.getJButtonNext().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(true);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(false);
                    }
                } else {
                    this.panel.getJButtonNext().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJComboOptions().setSelectedIndex(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            } else if (op.equals("ESRI")) {
                this.panel.getCrsMainPanel().getEsriPanel().connection();
                if (this.panel.getCrsMainPanel().getEsriPanel().getJTable().getSelectedRowCount() > 0) {
                    this.panel.getCrsMainPanel().getEsriPanel().selectedRowTable = this.panel.getCrsMainPanel().getEsriPanel().getJTable().getSelectedRow();
                    this.panel.getCrsMainPanel().getEsriPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getEsriPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getEsriPanel().selectedRowTable, 0)));
                    this.panel.getCrsMainPanel().getEsriPanel().setWKT();
                    this.panel.getJComboOptions().setEnabled(true);
                    if (this.panel.getJComboOptions().getSelectedIndex() > 0) {
                        this.panel.getJButtonAccept().setEnabled(false);
                        this.panel.getJButtonNext().setEnabled(true);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(false);
                    }
                } else {
                    this.panel.getJButtonNext().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJComboOptions().setSelectedIndex(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            } else if (op.equals(CRSI18NConstants.USER_CRS_KEY)) {
                this.panel.getCrsMainPanel().getNewCrsPanel().connection();
                if (this.panel.getCrsMainPanel().getNewCrsPanel().getJTable().getSelectedRowCount() > 0) {
                    if (this.panel.getCrsMainPanel().getNewCrsPanel().getSearchTextField().getText().equals("")) {
                        this.panel.getCrsMainPanel().getNewCrsPanel().initializeTable();
                    }
                    this.panel.getCrsMainPanel().getNewCrsPanel().selectedRowTable = this.panel.getCrsMainPanel().getNewCrsPanel().getJTable().getSelectedRow();
                    this.panel.getCrsMainPanel().getNewCrsPanel().setCodeCRS(Integer.parseInt((String)this.panel.getCrsMainPanel().getNewCrsPanel().sorter.getValueAt(this.panel.getCrsMainPanel().getNewCrsPanel().selectedRowTable, 0)));
                    this.panel.getCrsMainPanel().getNewCrsPanel().setWKT();
                    this.panel.getJComboOptions().setEnabled(true);
                    if (this.panel.getJComboOptions().getSelectedIndex() > 0) {
                        this.panel.getJButtonAccept().setEnabled(false);
                        this.panel.getJButtonNext().setEnabled(true);
                    } else {
                        this.panel.getJButtonAccept().setEnabled(true);
                        this.panel.getJButtonNext().setEnabled(false);
                    }
                } else {
                    this.panel.getJButtonNext().setEnabled(false);
                    this.panel.getJComboOptions().setEnabled(false);
                    this.panel.getJComboOptions().setSelectedIndex(0);
                    this.panel.getJButtonAccept().setEnabled(false);
                }
            }
            cl.show(this.panel.getCrsMainPanel().getJPanelMain(), (String)e.getItem());
            this.panel.getCrsMainPanel().setDataSource((String)e.getItem());
        }
        if (e.getSource() == this.panel.getNadsTrPanel().getJComboNadFile()) {
            if (this.panel.getNadsTrPanel().getJComboNadFile().getSelectedIndex() != 0) {
                this.panel.getNadsTrPanel().setNadFile(this.panel.getNadsTrPanel().getJComboNadFile().getSelectedItem().toString());
                this.panel.getNadsTrPanel().getTreePanel().setRoot(String.valueOf(CRSI18NConstants.GRIDS_IN_KEY) + ": " + this.panel.getNadsTrPanel().getNadFile());
                this.panel.getNadsTrPanel().initializeTree();
                this.panel.getNadsTrPanel().saveNadFileName(this.panel.getNadsTrPanel().getNadFile());
                this.panel.getJButtonAccept().setEnabled(true);
            } else {
                this.panel.getNadsTrPanel().setNadFile(null);
                this.panel.getNadsTrPanel().getTreePanel().setRoot("");
                this.panel.getJButtonAccept().setEnabled(false);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        String crsSource;
        RecentTrsPersistence trPersistence;
        TrData trData;
        String details;
        String crsTarget;
        String crsSource2;
        String authCode;
        TransEPSG epsgParams;
        RecentTrsPersistence trPersistence2;
        TrData trData2;
        String details2;
        String crsTarget2;
        String authority;
        TransEPSG epsgParams2;
        if (this.panel.getJButtonAccept().isEnabled()) {
            RecentCRSsPersistence persistence;
            CrsData crsData;
            int code;
            String name;
            String authority2;
            if (e.getSource() == this.panel.getCrsMainPanel().getCrsAndTransformationRecentsPanel().getJTable() && e.getClickCount() == 2) {
                if (this.panel.isEpsg_tr()) {
                    epsgParams2 = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
                    this.panel.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
                    this.panel.setEpsg_tr(false);
                    authority = "EPSG";
                    String name2 = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 1);
                    int code2 = this.panel.getEpsgTrPanel().getTransformation_code();
                    String crsSource3 = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getSource());
                    crsTarget2 = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getTarget());
                    details2 = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 5);
                    trData2 = new TrData(authority, code2, name2, crsSource3, crsTarget2, details2);
                    trPersistence2 = new RecentTrsPersistence();
                    trPersistence2.addTrData(trData2);
                    trPersistence2.setPersistent();
                } else if (this.panel.isManual_tr()) {
                    boolean domain = this.panel.getManualTrPanel().correctJTextField();
                    if (!domain || !this.panel.getManualTrPanel().correctDomain()) {
                        if (!domain) {
                            DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.ONLY_NUMERIC_CHARS_KEY, CRSI18NConstants.WARNING_KEY);
                            return;
                        }
                        DialogFactory.showWarningDialog(this.panel, CRSI18NConstants.INVALID_DOMAIN_KEY, CRSI18NConstants.WARNING_KEY);
                        return;
                    }
                    this.panel.setProjection(this.panel.getManualTrPanel().getProjection());
                    this.panel.setManual_tr(false);
                    authority = "USR";
                    String name3 = CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY;
                    int code3 = 0;
                    String crsSource4 = this.panel.getManualTrPanel().getSourceAbrev();
                    crsTarget2 = this.panel.getManualTrPanel().getTargetAbrev();
                    details2 = this.panel.getManualTrPanel().getValues();
                    trData2 = new TrData(authority, code3, name3, crsSource4, crsTarget2, details2);
                    trPersistence2 = new RecentTrsPersistence();
                    trPersistence2.addTrData(trData2);
                    trPersistence2.setPersistent();
                } else if (this.panel.isNads_tr()) {
                    this.panel.setProjection(this.panel.getNadsTrPanel().getProjection());
                    this.panel.setTargetNad(this.panel.getNadsTrPanel().getNad());
                    this.panel.setNads_tr(false);
                    authority2 = "NADGR";
                    name = "----";
                    int code4 = 0;
                    String crsSource5 = this.panel.getNadsTrPanel().getSourceAbrev();
                    String crsTarget3 = this.panel.getNadsTrPanel().getTargetAbrev();
                    String details3 = "";
                    details3 = this.panel.getNadsTrPanel().getNad() ? String.valueOf(this.panel.getNadsTrPanel().getNadFile()) + " (" + this.panel.getNadsTrPanel().getTargetAbrev() + ")" : String.valueOf(this.panel.getNadsTrPanel().getNadFile()) + " (" + this.panel.getNadsTrPanel().getSourceAbrev() + ")";
                    TrData trData3 = new TrData(authority2, code4, name, crsSource5, crsTarget3, details3);
                    RecentTrsPersistence trPersistence3 = new RecentTrsPersistence();
                    trPersistence3.addTrData(trData3);
                    trPersistence3.setPersistent();
                } else if (this.panel.isRecents_tr()) {
                    String[] transformation = ((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0)).split(":");
                    if (transformation[0].equals("USR")) {
                        this.panel.getRecentsTrPanel().setParamsManual((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4));
                        this.panel.setProjection(this.panel.getRecentsTrPanel().getProjection());
                    }
                    if (transformation[0].equals("EPSG")) {
                        epsgParams = new TransEPSG(this.panel.getRecentsTrPanel().getTrCode(), this.panel.getRecentsTrPanel().connect, this.panel.getRecentsTrPanel().getInverseTransformation());
                        this.panel.getRecentsTrPanel().setParamsEPGS(epsgParams.getParamValue());
                        this.panel.setProjection(this.panel.getRecentsTrPanel().getProjection());
                    }
                    if (transformation[0].equals("NADGR")) {
                        this.panel.setProjection(this.panel.getRecentsTrPanel().getProjectionNad((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4)));
                    }
                    transformation[0].equals("COMP");
                    this.panel.setRecents_tr(false);
                    authCode = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0);
                    String authority3 = authCode.split(":")[0];
                    String name4 = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 1);
                    int code5 = Integer.parseInt(authCode.split(":")[1]);
                    crsSource2 = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 2);
                    crsTarget = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 3);
                    details = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4);
                    trData = new TrData(authority3, code5, name4, crsSource2, crsTarget, details);
                    trPersistence = new RecentTrsPersistence();
                    trPersistence.addTrData(trData);
                    trPersistence.setPersistent();
                } else {
                    this.panel.setSin_tr(true);
                    this.panel.setProjection(this.panel.getProjection());
                }
                authority2 = this.panel.getProjection().getCrsWkt().getAuthority()[0];
                name = this.panel.getProjection().getCrsWkt().getName();
                code = this.panel.getProjection().getCode();
                crsData = new CrsData(authority2, code, name);
                persistence = new RecentCRSsPersistence();
                persistence.addCrsData(crsData);
                DialogManager.closeJDialog(this.panel);
            }
            if (e.getSource() == this.panel.getCrsMainPanel().getEpsgPanel().getJTable() && e.getClickCount() == 2) {
                if (this.panel.isEpsg_tr()) {
                    epsgParams2 = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
                    this.panel.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
                    this.panel.setEpsg_tr(false);
                    authority = "EPSG";
                    String name5 = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 1);
                    int code6 = this.panel.getEpsgTrPanel().getTransformation_code();
                    crsSource = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getSource());
                    crsTarget2 = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getTarget());
                    details2 = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 5);
                    trData2 = new TrData(authority, code6, name5, crsSource, crsTarget2, details2);
                    trPersistence2 = new RecentTrsPersistence();
                    trPersistence2.addTrData(trData2);
                    trPersistence2.setPersistent();
                } else if (this.panel.isManual_tr()) {
                    this.panel.setProjection(this.panel.getManualTrPanel().getProjection());
                    this.panel.setManual_tr(false);
                } else if (this.panel.isNads_tr()) {
                    this.panel.setProjection(this.panel.getNadsTrPanel().getProjection());
                    this.panel.setTargetNad(this.panel.getNadsTrPanel().getNad());
                    this.panel.setNads_tr(false);
                } else {
                    this.panel.setSin_tr(true);
                    this.panel.setProjection(this.panel.getProjection());
                }
                authority2 = this.panel.getProjection().getCrsWkt().getAuthority()[0];
                name = this.panel.getProjection().getCrsWkt().getName();
                code = this.panel.getProjection().getCode();
                crsData = new CrsData(authority2, code, name);
                persistence = new RecentCRSsPersistence();
                persistence.addCrsData(crsData);
                DialogManager.closeJDialog(this.panel);
            }
            if (e.getSource() == this.panel.getCrsMainPanel().getIauPanel().getJTable() && e.getClickCount() == 2) {
                if (this.panel.isEpsg_tr()) {
                    epsgParams2 = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
                    this.panel.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
                    this.panel.setEpsg_tr(false);
                } else if (this.panel.isManual_tr()) {
                    this.panel.setProjection(this.panel.getManualTrPanel().getProjection());
                    this.panel.setManual_tr(false);
                } else if (this.panel.isNads_tr()) {
                    this.panel.setProjection(this.panel.getNadsTrPanel().getProjection());
                    this.panel.setTargetNad(this.panel.getNadsTrPanel().getNad());
                    this.panel.setNads_tr(false);
                } else {
                    this.panel.setSin_tr(true);
                    this.panel.setProjection(this.panel.getProjection());
                }
                authority2 = this.panel.getProjection().getCrsWkt().getAuthority()[0];
                name = this.panel.getProjection().getCrsWkt().getName();
                code = this.panel.getProjection().getCode();
                crsData = new CrsData(authority2, code, name);
                persistence = new RecentCRSsPersistence();
                persistence.addCrsData(crsData);
                DialogManager.closeJDialog(this.panel);
            }
            if (e.getSource() == this.panel.getCrsMainPanel().getEsriPanel().getJTable() && e.getClickCount() == 2) {
                if (this.panel.isEpsg_tr()) {
                    epsgParams2 = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
                    this.panel.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
                    this.panel.setEpsg_tr(false);
                } else if (this.panel.isManual_tr()) {
                    this.panel.setProjection(this.panel.getManualTrPanel().getProjection());
                    this.panel.setManual_tr(false);
                } else if (this.panel.isNads_tr()) {
                    this.panel.setProjection(this.panel.getNadsTrPanel().getProjection());
                    this.panel.setTargetNad(this.panel.getNadsTrPanel().getNad());
                    this.panel.setNads_tr(false);
                } else {
                    this.panel.setSin_tr(true);
                    this.panel.setProjection(this.panel.getProjection());
                }
                authority2 = this.panel.getProjection().getCrsWkt().getAuthority()[0];
                name = this.panel.getProjection().getCrsWkt().getName();
                code = this.panel.getProjection().getCode();
                crsData = new CrsData(authority2, code, name);
                persistence = new RecentCRSsPersistence();
                persistence.addCrsData(crsData);
                DialogManager.closeJDialog(this.panel);
            }
            if (e.getSource() == this.panel.getCrsMainPanel().getNewCrsPanel().getJTable() && e.getClickCount() == 2) {
                if (this.panel.isEpsg_tr()) {
                    epsgParams2 = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
                    this.panel.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
                    this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
                    this.panel.setEpsg_tr(false);
                } else if (this.panel.isManual_tr()) {
                    this.panel.setProjection(this.panel.getManualTrPanel().getProjection());
                    this.panel.setManual_tr(false);
                } else if (this.panel.isNads_tr()) {
                    this.panel.setProjection(this.panel.getNadsTrPanel().getProjection());
                    this.panel.setTargetNad(this.panel.getNadsTrPanel().getNad());
                    this.panel.setNads_tr(false);
                } else {
                    this.panel.setSin_tr(true);
                    this.panel.setProjection(this.panel.getProjection());
                }
                authority2 = this.panel.getProjection().getCrsWkt().getAuthority()[0];
                name = this.panel.getProjection().getCrsWkt().getName();
                code = this.panel.getProjection().getCode();
                crsData = new CrsData(authority2, code, name);
                persistence = new RecentCRSsPersistence();
                persistence.addCrsData(crsData);
                DialogManager.closeJDialog(this.panel);
            }
        }
        if (e.getSource() == this.panel.getEpsgTrPanel().getJTable() && e.getClickCount() == 2) {
            epsgParams2 = new TransEPSG(this.panel.getEpsgTrPanel().getTransformation_code(), this.panel.getEpsgTrPanel().connect, this.panel.getEpsgTrPanel().inverseTranformation);
            this.panel.getEpsgTrPanel().setValues(epsgParams2.getParamValue());
            this.panel.setProjection(this.panel.getEpsgTrPanel().getProjection());
            this.panel.setEpsg_tr(false);
            authority = "EPSG";
            String name = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 1);
            int code = this.panel.getEpsgTrPanel().getTransformation_code();
            crsSource = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getSource());
            crsTarget2 = "EPSG:" + String.valueOf(this.panel.getEpsgTrPanel().getTarget());
            details2 = (String)this.panel.getEpsgTrPanel().sorter.getValueAt(this.panel.getEpsgTrPanel().selectedRowTable, 5);
            trData2 = new TrData(authority, code, name, crsSource, crsTarget2, details2);
            trPersistence2 = new RecentTrsPersistence();
            trPersistence2.addTrData(trData2);
            trPersistence2.setPersistent();
            authority = this.panel.getProjection().getCrsWkt().getAuthority()[0];
            name = this.panel.getProjection().getCrsWkt().getName();
            code = this.panel.getProjection().getCode();
            CrsData crsData = new CrsData(authority, code, name);
            RecentCRSsPersistence persistence = new RecentCRSsPersistence();
            persistence.addCrsData(crsData);
            persistence.setPersistent();
            DialogManager.closeJDialog(this.panel);
        }
        if (e.getSource() == this.panel.getRecentsTrPanel().getJTable() && e.getClickCount() == 2) {
            String[] transf = ((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0)).split(":");
            if (transf[0].equals("USR")) {
                this.panel.getRecentsTrPanel().setParamsManual((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4));
                this.panel.setProjection(this.panel.getRecentsTrPanel().getProjection());
            }
            if (transf[0].equals("EPSG")) {
                epsgParams = new TransEPSG(this.panel.getRecentsTrPanel().getTrCode(), this.panel.getEpsgTrPanel().connect, this.panel.getRecentsTrPanel().getInverseTransformation());
                this.panel.getRecentsTrPanel().setParamsEPGS(epsgParams.getParamValue());
                this.panel.setProjection(this.panel.getRecentsTrPanel().getProjection());
            }
            if (transf[0].equals("NADGR")) {
                this.panel.setProjection(this.panel.getRecentsTrPanel().getProjectionNad((String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4)));
            }
            this.panel.setRecents_tr(false);
            authCode = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 0);
            String authority4 = authCode.split(":")[0];
            String name = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 1);
            int code = Integer.parseInt(authCode.split(":")[1]);
            crsSource2 = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 2);
            crsTarget = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 3);
            details = (String)this.panel.getRecentsTrPanel().sorter.getValueAt(this.panel.getRecentsTrPanel().selectedRowTable, 4);
            trData = new TrData(authority4, code, name, crsSource2, crsTarget, details);
            trPersistence = new RecentTrsPersistence();
            trPersistence.addTrData(trData);
            trPersistence.setPersistent();
            authority4 = this.panel.getProjection().getCrsWkt().getAuthority()[0];
            name = this.panel.getProjection().getCrsWkt().getName();
            code = this.panel.getProjection().getCode();
            CrsData crsData = new CrsData(authority4, code, name);
            RecentCRSsPersistence persistence = new RecentCRSsPersistence();
            persistence.addCrsData(crsData);
            persistence.setPersistent();
            DialogManager.closeJDialog(this.panel);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!this.panel.getManualTrPanel().getStatus()) {
            this.panel.getJButtonAccept().setEnabled(false);
        } else {
            this.panel.getJButtonAccept().setEnabled(true);
        }
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }
}

