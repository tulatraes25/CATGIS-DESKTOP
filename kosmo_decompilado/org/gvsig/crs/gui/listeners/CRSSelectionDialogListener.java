/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.listeners;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.CRSMainPanel;
import org.gvsig.crs.gui.CRSSelectionDialog;
import org.gvsig.crs.persistence.CrsData;
import org.gvsig.crs.persistence.RecentCRSsPersistence;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class CRSSelectionDialogListener
implements ActionListener,
ListSelectionListener,
ItemListener,
MouseListener {
    CRSSelectionDialog dialog = null;

    public CRSSelectionDialogListener(CRSSelectionDialog d) {
        this.dialog = d;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.dialog.getCrsMainPanel().getJButtonAccept()) {
            this.dialog.setLastProj(this.dialog.getProjection());
            this.dialog.setDataSource(this.dialog.getCrsMainPanel().getDataSource());
            if (this.dialog.getLastProj() != null) {
                String authority = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getAuthority()[0];
                String name = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getName();
                int code = ((ICrs)this.dialog.getLastProj()).getCode();
                CrsData crsData = new CrsData(authority, code, name);
                RecentCRSsPersistence persistence = new RecentCRSsPersistence(RecentCRSsPersistence.pluginClassInstance);
                persistence.addCrsData(crsData);
                persistence.setPersistent();
                DialogManager.closeJDialog(this.dialog);
                this.dialog.setOkPressed(true);
            } else {
                this.dialog.setOkPressed(false);
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getJButtonCancel()) {
            this.dialog.setProjection(this.dialog.getLastProj());
            DialogManager.closeJDialog(this.dialog);
            this.dialog.setOkPressed(false);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        CardLayout cl = (CardLayout)this.dialog.getCrsMainPanel().getJPanelMain().getLayout();
        String op = (String)e.getItem();
        if (op.equals("EPSG")) {
            this.dialog.getCrsMainPanel().getEpsgPanel().connection();
            if (this.dialog.getCrsMainPanel().getEpsgPanel().getJTable().getSelectedRowCount() > 0) {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
            } else {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
            }
        } else if (op.equals("IAU2000")) {
            this.dialog.getCrsMainPanel().getIauPanel().connection();
            if (this.dialog.getCrsMainPanel().getIauPanel().getJTable().getSelectedRowCount() > 0) {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
            } else {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
            }
        } else if (op.equals(CRSMainPanel.RECENTS_KEY)) {
            if (this.dialog.getCrsMainPanel().getRecentsPanel2().getJTable().getSelectedRowCount() > 0) {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
            } else {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
            }
        } else if (op.equals("ESRI")) {
            this.dialog.getCrsMainPanel().getEsriPanel().connection();
            if (this.dialog.getCrsMainPanel().getEsriPanel().getJTable().getSelectedRowCount() > 0) {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
            } else {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
            }
        } else if (op.equals(CRSMainPanel.NEW_CRS_KEY)) {
            this.dialog.getCrsMainPanel().getNewCrsPanel().connection();
            if (this.dialog.getCrsMainPanel().getNewCrsPanel().getJTable().getSelectedRowCount() > 0) {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
                if (this.dialog.getCrsMainPanel().getNewCrsPanel().getSearchTextField().getText().equals("")) {
                    this.dialog.getCrsMainPanel().getNewCrsPanel().initializeTable();
                }
            } else {
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
            }
        }
        cl.show(this.dialog.getCrsMainPanel().getJPanelMain(), (String)e.getItem());
        this.dialog.getCrsMainPanel().setDataSource((String)e.getItem());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        int i;
        String crs_kind;
        CrsWkt crs;
        String[] not_soported;
        ListSelectionModel lsm;
        if (e.getSource() == this.dialog.getCrsMainPanel().getRecentsPanel2().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                this.dialog.getCrsMainPanel().getRecentsPanel2().selectedRowTable = -1;
                this.dialog.getCrsMainPanel().getRecentsPanel2().setCodeCRS(-1);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                this.dialog.getCrsMainPanel().getRecentsPanel2().getInfoCrs().setEnabled(false);
                this.dialog.setCode(0);
            } else {
                this.dialog.getCrsMainPanel().getRecentsPanel2().initCrs();
                this.dialog.setCode(this.dialog.getCrsMainPanel().getRecentsPanel2().getCodeCRS());
                this.dialog.getCrsMainPanel().getRecentsPanel2().getInfoCrs().setEnabled(true);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getIauPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            not_soported = new String[]{"Oblique_Cylindrical_Equal_Area"};
            boolean soported = true;
            if (lsm.isSelectionEmpty()) {
                this.dialog.getCrsMainPanel().getIauPanel().selectedRowTable = -1;
                this.dialog.getCrsMainPanel().getIauPanel().setCodeCRS(-1);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                this.dialog.setCode(0);
            } else {
                this.dialog.getCrsMainPanel().getIauPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.dialog.getCrsMainPanel().getIauPanel().setCodeCRS(Integer.parseInt((String)this.dialog.getCrsMainPanel().getIauPanel().sorter.getValueAt(this.dialog.getCrsMainPanel().getIauPanel().selectedRowTable, 0)));
                this.dialog.getCrsMainPanel().getIauPanel().setWKT();
                crs = new CrsWkt(this.dialog.getCrsMainPanel().getIauPanel().getWKT());
                crs_kind = crs.getProjection();
                i = 0;
                while (i < not_soported.length) {
                    if (crs_kind.equals(not_soported[i])) {
                        soported = false;
                    }
                    ++i;
                }
                if (soported) {
                    this.dialog.setCode(this.dialog.getCrsMainPanel().getIauPanel().getCodeCRS());
                    this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
                } else {
                    this.showUnsupportedCRSDialog();
                    this.dialog.getCrsMainPanel().getIauPanel().setCodeCRS(-1);
                    this.dialog.getCrsMainPanel().getIauPanel().setCodeCRS(0);
                    this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                }
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getEpsgPanel().getJTable().getSelectionModel()) {
            String[] not_soported2 = new String[]{"engineering", "vertical", "compound", "geocentric"};
            boolean soported = true;
            ListSelectionModel lsm2 = (ListSelectionModel)e.getSource();
            if (lsm2.isSelectionEmpty()) {
                this.dialog.getCrsMainPanel().getEpsgPanel().selectedRowTable = -1;
                this.dialog.getCrsMainPanel().getEpsgPanel().setCodeCRS(-1);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                this.dialog.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(false);
                this.dialog.setCode(0);
            } else {
                this.dialog.getCrsMainPanel().getEpsgPanel().selectedRowTable = lsm2.getMinSelectionIndex();
                String crs_kind2 = (String)this.dialog.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.dialog.getCrsMainPanel().getEpsgPanel().selectedRowTable, 2);
                int i2 = 0;
                while (i2 < not_soported2.length) {
                    if (crs_kind2.equals(not_soported2[i2])) {
                        soported = false;
                    }
                    ++i2;
                }
                if (soported) {
                    this.dialog.getCrsMainPanel().getEpsgPanel().setCodeCRS(Integer.parseInt((String)this.dialog.getCrsMainPanel().getEpsgPanel().sorter.getValueAt(this.dialog.getCrsMainPanel().getEpsgPanel().selectedRowTable, 0)));
                    this.dialog.getCrsMainPanel().getEpsgPanel().setWKT();
                    this.dialog.setCode(this.dialog.getCrsMainPanel().getEpsgPanel().epsg_code);
                    this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
                    this.dialog.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(true);
                } else {
                    this.showUnsupportedCRSDialog();
                    this.dialog.getCrsMainPanel().getEpsgPanel().setCodeCRS(-1);
                    this.dialog.setCode(0);
                    this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                    this.dialog.getCrsMainPanel().getEpsgPanel().getInfoCrs().setEnabled(false);
                }
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getEsriPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            not_soported = new String[]{"Oblique_Cylindrical_Equal_Area"};
            boolean soported = true;
            if (lsm.isSelectionEmpty()) {
                this.dialog.getCrsMainPanel().getEsriPanel().selectedRowTable = -1;
                this.dialog.getCrsMainPanel().getEsriPanel().setCodeCRS(-1);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                this.dialog.setCode(0);
            } else {
                this.dialog.getCrsMainPanel().getEsriPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.dialog.getCrsMainPanel().getEsriPanel().setCodeCRS(Integer.parseInt((String)this.dialog.getCrsMainPanel().getEsriPanel().sorter.getValueAt(this.dialog.getCrsMainPanel().getEsriPanel().selectedRowTable, 0)));
                this.dialog.getCrsMainPanel().getEsriPanel().setWKT();
                crs = new CrsWkt(this.dialog.getCrsMainPanel().getEsriPanel().getWKT());
                crs_kind = crs.getProjection();
                i = 0;
                while (i < not_soported.length) {
                    if (crs_kind.equals(not_soported[i])) {
                        soported = false;
                    }
                    ++i;
                }
                if (soported) {
                    this.dialog.setCode(this.dialog.getCrsMainPanel().getEsriPanel().getCodeCRS());
                    this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
                } else {
                    this.showUnsupportedCRSDialog();
                    this.dialog.getCrsMainPanel().getEsriPanel().setCodeCRS(-1);
                    this.dialog.getCrsMainPanel().getEsriPanel().setCodeCRS(0);
                    this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                }
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getNewCrsPanel().getJTable().getSelectionModel()) {
            lsm = (ListSelectionModel)e.getSource();
            if (lsm.isSelectionEmpty()) {
                this.dialog.getCrsMainPanel().getNewCrsPanel().selectedRowTable = -1;
                this.dialog.getCrsMainPanel().getNewCrsPanel().setCodeCRS(-1);
                this.dialog.getCrsMainPanel().getNewCrsPanel().getInfoCrs().setEnabled(false);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(false);
                this.dialog.getCrsMainPanel().getNewCrsPanel().getBtnEliminar().setEnabled(false);
                this.dialog.getCrsMainPanel().getNewCrsPanel().getBtnEditar().setEnabled(false);
                this.dialog.setCode(0);
            } else {
                this.dialog.getCrsMainPanel().getNewCrsPanel().selectedRowTable = lsm.getMinSelectionIndex();
                this.dialog.getCrsMainPanel().getNewCrsPanel().setCodeCRS(Integer.parseInt((String)this.dialog.getCrsMainPanel().getNewCrsPanel().sorter.getValueAt(this.dialog.getCrsMainPanel().getNewCrsPanel().selectedRowTable, 0)));
                this.dialog.getCrsMainPanel().getNewCrsPanel().setWKT();
                this.dialog.getCrsMainPanel().getNewCrsPanel().getInfoCrs().setEnabled(true);
                this.dialog.getCrsMainPanel().getJButtonAccept().setEnabled(true);
                this.dialog.getCrsMainPanel().getNewCrsPanel().getBtnEliminar().setEnabled(true);
                this.dialog.getCrsMainPanel().getNewCrsPanel().getBtnEditar().setEnabled(true);
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        RecentCRSsPersistence persistence;
        CrsData crsData;
        int code;
        String name;
        String authority;
        if (e.getSource() == this.dialog.getCrsMainPanel().getRecentsPanel2().getJTable() && e.getClickCount() == 2) {
            this.dialog.setLastProj(this.dialog.getProjection());
            this.dialog.setDataSource(this.dialog.getCrsMainPanel().getDataSource());
            if (this.dialog.getLastProj() != null) {
                authority = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getAuthority()[0];
                name = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getName();
                code = ((ICrs)this.dialog.getLastProj()).getCode();
                crsData = new CrsData(authority, code, name);
                persistence = new RecentCRSsPersistence(RecentCRSsPersistence.pluginClassInstance);
                persistence.addCrsData(crsData);
                persistence.setPersistent();
                DialogManager.closeJDialog(this.dialog);
                this.dialog.setOkPressed(true);
            } else {
                this.dialog.setOkPressed(false);
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getEpsgPanel().getJTable() && e.getClickCount() == 2) {
            this.dialog.setLastProj(this.dialog.getProjection());
            this.dialog.setDataSource(this.dialog.getCrsMainPanel().getDataSource());
            if (this.dialog.getLastProj() != null) {
                authority = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getAuthority()[0];
                name = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getName();
                code = ((ICrs)this.dialog.getLastProj()).getCode();
                crsData = new CrsData(authority, code, name);
                persistence = new RecentCRSsPersistence(RecentCRSsPersistence.pluginClassInstance);
                persistence.addCrsData(crsData);
                persistence.setPersistent();
                DialogManager.closeJDialog(this.dialog);
                this.dialog.setOkPressed(true);
            } else {
                this.dialog.setOkPressed(false);
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getIauPanel().getJTable() && e.getClickCount() == 2) {
            this.dialog.setLastProj(this.dialog.getProjection());
            this.dialog.setDataSource(this.dialog.getCrsMainPanel().getDataSource());
            if (this.dialog.getLastProj() != null) {
                authority = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getAuthority()[0];
                name = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getName();
                code = ((ICrs)this.dialog.getLastProj()).getCode();
                crsData = new CrsData(authority, code, name);
                persistence = new RecentCRSsPersistence(RecentCRSsPersistence.pluginClassInstance);
                persistence.addCrsData(crsData);
                persistence.setPersistent();
                DialogManager.closeJDialog(this.dialog);
                this.dialog.setOkPressed(true);
            } else {
                this.dialog.setOkPressed(false);
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getEsriPanel().getJTable() && e.getClickCount() == 2) {
            this.dialog.setLastProj(this.dialog.getProjection());
            this.dialog.setDataSource(this.dialog.getCrsMainPanel().getDataSource());
            if (this.dialog.getLastProj() != null) {
                authority = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getAuthority()[0];
                name = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getName();
                code = ((ICrs)this.dialog.getLastProj()).getCode();
                crsData = new CrsData(authority, code, name);
                persistence = new RecentCRSsPersistence(RecentCRSsPersistence.pluginClassInstance);
                persistence.addCrsData(crsData);
                persistence.setPersistent();
                DialogManager.closeJDialog(this.dialog);
                this.dialog.setOkPressed(true);
            } else {
                this.dialog.setOkPressed(false);
            }
        }
        if (e.getSource() == this.dialog.getCrsMainPanel().getNewCrsPanel().getJTable() && e.getClickCount() == 2) {
            ICrs crs = (ICrs)this.dialog.getProjection();
            try {
                crs.getProj4String();
            }
            catch (CrsException e1) {
                DialogFactory.showWarningDialog(this.dialog, e1.getMessage(), CRSI18NConstants.WARNING_KEY);
                return;
            }
            this.dialog.setLastProj(this.dialog.getProjection());
            this.dialog.setDataSource(this.dialog.getCrsMainPanel().getDataSource());
            if (this.dialog.getLastProj() != null) {
                String authority2 = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getAuthority()[0];
                String name2 = ((ICrs)this.dialog.getLastProj()).getCrsWkt().getName();
                int code2 = ((ICrs)this.dialog.getLastProj()).getCode();
                CrsData crsData2 = new CrsData(authority2, code2, name2);
                RecentCRSsPersistence persistence2 = new RecentCRSsPersistence();
                persistence2.addCrsData(crsData2);
                DialogManager.closeJDialog(this.dialog);
                this.dialog.setOkPressed(true);
            } else {
                this.dialog.setOkPressed(false);
            }
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

    private void showUnsupportedCRSDialog() {
        DialogFactory.showWarningDialog(this.dialog, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
    }
}

