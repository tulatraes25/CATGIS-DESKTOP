/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui.panels;

import com.iver.cit.gvsig.gui.TableSorter;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.gui.panels.NewCRSPanel;
import org.gvsig.crs.persistence.CrsData;
import org.gvsig.crs.persistence.RecentCRSsPersistence;
import org.saig.core.util.DialogManager;
import org.saig.jump.lang.I18N;

public class CrsRecentsPanel
extends JPanel
implements ActionListener {
    private static final Logger LOGGER = Logger.getLogger(CrsRecentsPanel.class);
    private static final long serialVersionUID = 1L;
    public JTable jTable = null;
    private JButton infoCrs = null;
    private JScrollPane jScrollPane = null;
    public DefaultTableModel dtm = null;
    public TableSorter sorter = null;
    private CrsData[] crsDataArray = null;
    public int selectedRowTable = -1;
    private String authority = null;
    private int codeCRS = -1;
    private ICrs crs = null;

    public CrsRecentsPanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridLayout(1, 2));
        this.setLayout(new FlowLayout(0, 10, 10));
        this.add((Component)this.getJLabel(), null);
        this.add((Component)this.getJScrollPane(), null);
    }

    private JLabel getJLabel() {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(500, 30));
        label.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.CrsRecentsPanel.last-used-coordinate-reference-systems")) + ":");
        return label;
    }

    private JScrollPane getJScrollPane() {
        if (this.jScrollPane == null) {
            this.jScrollPane = new JScrollPane();
            this.jScrollPane.setPreferredSize(new Dimension(500, 150));
            this.jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(CRSI18NConstants.CRS_KEY), BorderFactory.createEmptyBorder(5, 5, 5, 5)), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
    }

    public JTable getJTable() {
        if (this.jTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.SOURCE_KEY, CRSI18NConstants.CODE_KEY, CRSI18NConstants.NAME_KEY};
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
            while (i < 3) {
                column = this.jTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(60);
                } else if (i == 2) {
                    column.setPreferredWidth(275);
                } else {
                    column.setPreferredWidth(160);
                }
                ++i;
            }
        }
        return this.jTable;
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

    public void loadRecents() {
        RecentCRSsPersistence persistence = new RecentCRSsPersistence();
        this.crsDataArray = persistence.getArrayOfCrsData();
        int iRow = this.crsDataArray.length - 1;
        while (iRow >= 0) {
            Object[] row = new Object[]{this.crsDataArray[iRow].getAuthority(), Integer.toString(this.crsDataArray[iRow].getCode()), this.crsDataArray[iRow].getName()};
            this.dtm.addRow(row);
            --iRow;
        }
        int numr = this.dtm.getRowCount();
        if (numr != 0) {
            this.getJTable().setRowSelectionInterval(0, 0);
        }
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
            this.crs = CrsRepositoryManager.getInstance().getCRS(this.getAuthority());
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    public String getAuthority() {
        return this.authority;
    }

    public void setAuthority(String aut) {
        this.authority = aut;
    }

    public boolean isUSR(int code) {
        NewCRSPanel usr = new NewCRSPanel();
        return usr.isInBD(code);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getInfoCrs()) {
            String[] aut = this.getAuthority().split(":");
            InfoCRSPanel info = new InfoCRSPanel(aut[0], this.getCodeCRS());
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }
}

