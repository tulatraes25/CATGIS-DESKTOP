/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui.panels;

import com.iver.cit.gvsig.gui.TableSorter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.ArrayUtils;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.gui.panels.NewCRSPanel;
import org.gvsig.crs.persistence.CrsData;
import org.gvsig.crs.persistence.RecentCRSsPersistence;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;
import org.saig.core.util.DialogManager;

public class CrsAndTransformationRecentsPanel
extends JPanel
implements ActionListener {
    private static final long serialVersionUID = 1L;
    public JTable jTable = null;
    private JScrollPane jScrollPane = null;
    private JButton infoCrs = null;
    public DefaultTableModel dtm = null;
    public TableSorter sorter = null;
    private CrsData[] crsDataArray = null;
    private TrData[] crsTrDataArray = null;
    public int selectedRowTable = -1;
    private String authority = null;
    private int codeCRS = -1;
    private ICrs crs = null;

    public CrsAndTransformationRecentsPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        JPanel p = new JPanel(new FlowLayout(0, 15, 15));
        p.add(this.getJLabel());
        this.add((Component)p, "North");
        this.add((Component)this.getJScrollPane(), "Center");
        JPanel pSouth = new JPanel(new FlowLayout(2, 5, 5));
        pSouth.add(this.getInfoCrs());
        this.add((Component)pSouth, "South");
    }

    private JLabel getJLabel() {
        JLabel label = new JLabel();
        label.setText(String.valueOf(CRSI18NConstants.LAST_USED_CRS_KEY) + ":");
        return label;
    }

    private JScrollPane getJScrollPane() {
        if (this.jScrollPane == null) {
            this.jScrollPane = new JScrollPane();
            this.jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
    }

    public JTable getJTable() {
        if (this.jTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.SOURCE_KEY, CRSI18NConstants.CODE_KEY, CRSI18NConstants.NAME_KEY, CRSI18NConstants.TRANSFORMATION_KEY};
            Object[][] data = new Object[][]{};
            this.dtm = new DefaultTableModel(data, columnNames){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    return this.getValueAt(0, column).getClass();
                }
            };
            this.sorter = new TableSorter(this.dtm);
            this.jTable = new JTable(this.sorter);
            this.sorter.setTableHeader(this.jTable.getTableHeader());
            this.jTable.setCellSelectionEnabled(false);
            this.jTable.setRowSelectionAllowed(true);
            this.jTable.setColumnSelectionAllowed(false);
            this.jTable.setSelectionMode(0);
            TableColumn column = null;
            int i = 0;
            while (i < columnNames.length) {
                column = this.jTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(60);
                } else if (i == 1) {
                    column.setPreferredWidth(60);
                } else {
                    column.setPreferredWidth(240);
                }
                ++i;
            }
        }
        return this.jTable;
    }

    public JButton getInfoCrs() {
        if (this.infoCrs == null) {
            this.infoCrs = new JButton();
            this.infoCrs.setPreferredSize(new Dimension(85, 20));
            this.infoCrs.setText(CRSI18NConstants.CRS_INFO_KEY);
            this.infoCrs.setMnemonic('I');
            this.infoCrs.setEnabled(false);
            this.infoCrs.setToolTipText(CRSI18NConstants.MORE_INFO_KEY);
            this.infoCrs.addActionListener(this);
        }
        return this.infoCrs;
    }

    public ICrs getProjection() {
        return this.crs;
    }

    public void setCodeCRS(int code) {
        this.codeCRS = code;
    }

    public int getCodeCRS() {
        return this.codeCRS;
    }

    public void setProjection(IProjection crs) {
    }

    /*
     * Unable to fully structure code
     */
    public void loadRecents(CrsWkt crsWkttarget, ICrs curCrs) {
        block29: {
            trPersistence = new RecentTrsPersistence();
            this.crsTrDataArray = trPersistence.getArrayOfTrData();
            persistence = new RecentCRSsPersistence();
            crsData = new CrsData(curCrs.getCrsWkt().getAuthority()[0], curCrs.getCode(), curCrs.getCrsWkt().getName());
            if (curCrs.getSourceTransformationParams() == null && curCrs.getTargetTransformationParams() == null) {
                persistence.addCrsData(crsData);
            }
            this.crsDataArray = persistence.getArrayOfCrsData();
            available = true;
            numRow = this.dtm.getRowCount();
            while (numRow != 0) {
                this.dtm.removeRow(--numRow);
            }
            initialCrsTr = this.crsTrDataArray.length - 1;
            initialCrs = this.crsDataArray.length - 1;
            if (!(curCrs.getSourceTransformationParams() == null && curCrs.getTargetTransformationParams() == null || ArrayUtils.isEmpty((Object[])this.crsTrDataArray))) {
                initialCrsTr = this.crsTrDataArray.length - 1;
                first = new Object[]{this.crsTrDataArray[initialCrsTr].getCrsSource().split(":")[0], this.crsTrDataArray[initialCrsTr].getCrsSource().split(":")[1], this.crsTrDataArray[initialCrsTr].getName(), String.valueOf(this.crsTrDataArray[initialCrsTr].getAuthority()) + ":" + this.crsTrDataArray[initialCrsTr].getCode() + " <--> " + this.crsTrDataArray[initialCrsTr].getDetails()};
                if (this.crsTrDataArray[initialCrsTr].getAuthority().equals("USR")) {
                    available = this.isUSR(this.crsTrDataArray[initialCrsTr].getCode());
                }
                if (available) {
                    this.dtm.addRow(first);
                    initialCrsTr = this.crsTrDataArray.length - 2;
                } else {
                    available = true;
                }
            } else {
                initialCrs = this.crsDataArray.length - 1;
                first = new Object[]{this.crsDataArray[initialCrs].getAuthority(), Integer.toString(this.crsDataArray[initialCrs].getCode()), this.crsDataArray[initialCrs].getName(), CRSI18NConstants.NO_TRANSFORMATION_KEY};
                if (this.crsDataArray[initialCrs].getAuthority().equals("USR")) {
                    available = this.isUSR(this.crsDataArray[initialCrs].getCode());
                }
                if (available) {
                    this.dtm.addRow(first);
                    initialCrs = this.crsDataArray.length - 2;
                } else {
                    available = true;
                }
            }
            iRowCrs = initialCrs;
            iRowTr = initialCrsTr;
            while (iRowCrs >= 0 && iRowTr >= 0) {
                if (this.crsDataArray[iRowCrs].getDate().after(this.crsTrDataArray[iRowTr].getDate())) {
                    if (this.crsDataArray[iRowCrs].getAuthority().equals("USR")) {
                        available = this.isUSR(this.crsDataArray[iRowCrs].getCode());
                    }
                    if (available) {
                        row = new Object[]{this.crsDataArray[iRowCrs].getAuthority(), Integer.toString(this.crsDataArray[iRowCrs].getCode()), this.crsDataArray[iRowCrs].getName(), CRSI18NConstants.NO_TRANSFORMATION_KEY};
                        this.dtm.addRow(row);
                    } else {
                        available = true;
                    }
                    --iRowCrs;
                    continue;
                }
                target = String.valueOf(crsWkttarget.getAuthority()[0]) + ":" + crsWkttarget.getAuthority()[1];
                crsSource = this.crsTrDataArray[iRowTr].getCrsSource();
                crsTarget = this.crsTrDataArray[iRowTr].getCrsTarget();
                if (target.equals(crsTarget)) {
                    if (this.crsTrDataArray[iRowTr].getAuthority().equals("USR")) {
                        available = this.isUSR(this.crsTrDataArray[iRowTr].getCode());
                    }
                    if (available) {
                        row = new Object[]{crsSource.split(":")[0], crsSource.split(":")[1], this.crsTrDataArray[iRowTr].getName(), String.valueOf(this.crsTrDataArray[iRowTr].getAuthority()) + ":" + this.crsTrDataArray[iRowTr].getCode() + " <--> " + this.crsTrDataArray[iRowTr].getDetails()};
                        this.dtm.addRow(row);
                    } else {
                        available = true;
                    }
                }
                --iRowTr;
            }
            if (iRowTr < 0) ** GOTO lbl85
            while (iRowTr >= 0) {
                target = String.valueOf(crsWkttarget.getAuthority()[0]) + ":" + crsWkttarget.getAuthority()[1];
                crsSource = this.crsTrDataArray[iRowTr].getCrsSource();
                crsTarget = this.crsTrDataArray[iRowTr].getCrsTarget();
                if (target.equals(crsTarget)) {
                    if (this.crsTrDataArray[iRowTr].getAuthority().equals("USR")) {
                        available = this.isUSR(this.crsTrDataArray[iRowTr].getCode());
                    }
                    if (available) {
                        row = new Object[]{crsSource.split(":")[0], crsSource.split(":")[1], this.crsTrDataArray[iRowTr].getName(), String.valueOf(this.crsTrDataArray[iRowTr].getAuthority()) + ":" + this.crsTrDataArray[iRowTr].getCode() + " <--> " + this.crsTrDataArray[iRowTr].getDetails()};
                        this.dtm.addRow(row);
                    } else {
                        available = true;
                    }
                }
                --iRowTr;
            }
            break block29;
lbl-1000:
            // 1 sources

            {
                if (this.crsDataArray[iRowCrs].getAuthority().equals("USR")) {
                    available = this.isUSR(this.crsDataArray[iRowCrs].getCode());
                }
                if (available) {
                    row = new Object[]{this.crsDataArray[iRowCrs].getAuthority(), Integer.toString(this.crsDataArray[iRowCrs].getCode()), this.crsDataArray[iRowCrs].getName(), CRSI18NConstants.NO_TRANSFORMATION_KEY};
                    this.dtm.addRow(row);
                } else {
                    available = true;
                }
                --iRowCrs;
lbl85:
                // 2 sources

                ** while (iRowCrs >= 0)
            }
        }
        numr = this.dtm.getRowCount();
        if (numr != 0) {
            this.getJTable().setRowSelectionInterval(0, 0);
        }
    }

    public boolean isUSR(int code) {
        NewCRSPanel usr = new NewCRSPanel();
        return usr.isInBD(code);
    }

    public ICrs getCrs() {
        return this.crs;
    }

    public void initCrs() {
        this.selectedRowTable = this.getJTable().getSelectedRow();
        Integer.parseInt((String)this.sorter.getValueAt(this.selectedRowTable, 1));
        this.setAuthority(String.valueOf((String)this.sorter.getValueAt(this.selectedRowTable, 0)) + ":" + (String)this.sorter.getValueAt(this.selectedRowTable, 1));
        this.codeCRS = Integer.parseInt((String)this.sorter.getValueAt(this.selectedRowTable, 1));
        try {
            this.crs = new CrsFactory().getCRS(this.getAuthority());
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
    }

    public String getAuthority() {
        return this.authority;
    }

    public void setAuthority(String aut) {
        this.authority = aut;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getInfoCrs()) {
            String[] aut = this.getAuthority().split(":");
            InfoCRSPanel info = new InfoCRSPanel(aut[0], this.getCodeCRS(), (String)this.getJTable().getValueAt(this.getJTable().getSelectedRow(), 3));
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }
}

