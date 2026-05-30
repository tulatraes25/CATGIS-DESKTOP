/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.gui.panels;

import com.iver.cit.gvsig.gui.TableSorter;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;
import org.saig.jump.lang.I18N;

public class TransformationRecentsPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TransformationRecentsPanel.class);
    int code = 0;
    private JTable transformationTable;
    private JScrollPane jScrollPane = null;
    public DefaultTableModel dtm = null;
    public TableSorter sorter = null;
    private JButton infoTr = null;
    public int selectedRowTable = -1;
    private TrData[] trDataArray;
    private String cadWKT = "";
    private String params = "";
    public CRSRepositoryConnection connect = null;
    boolean inverseTransformation = false;
    int trCode;
    boolean targetNad = false;
    String sourceAbrev = null;

    public TransformationRecentsPanel() {
        this.initialize();
    }

    private void initialize() {
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionEPSG();
        this.setLayout(new GridLayout(2, 1));
        this.setLayout(new FlowLayout(3, 5, 10));
        this.setPreferredSize(new Dimension(525, 100));
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(I18N.getString("org.gvsig.crs.gui.panels.TransformationRecentsPanel.recent-transformations")), BorderFactory.createEmptyBorder(2, 2, 2, 2)), this.getBorder()));
        this.add(this.getJButtonInfo());
        this.add(this.getJScrollPane());
    }

    public JTable getJTable() {
        if (this.transformationTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.TRANSFORMATIONS_KEY, CRSI18NConstants.NAME_KEY, CRSI18NConstants.SOURCE_CRS_KEY, CRSI18NConstants.TARGET_CRS_KEY, CRSI18NConstants.DETAILS_KEY};
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
            this.transformationTable = new JTable(this.sorter);
            this.sorter.setTableHeader(this.transformationTable.getTableHeader());
            this.transformationTable.setCellSelectionEnabled(false);
            this.transformationTable.setRowSelectionAllowed(true);
            this.transformationTable.setAutoResizeMode(0);
            this.transformationTable.setColumnSelectionAllowed(false);
            this.transformationTable.setSelectionMode(0);
            TableColumn column = null;
            int i = 0;
            while (i < columnNames.length) {
                column = this.transformationTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(80);
                } else if (i == 4) {
                    column.setPreferredWidth(200);
                } else {
                    column.setPreferredWidth(100);
                }
                ++i;
            }
        }
        return this.transformationTable;
    }

    private JScrollPane getJScrollPane() {
        if (this.jScrollPane == null) {
            this.jScrollPane = new JScrollPane();
            this.jScrollPane.setPreferredSize(new Dimension(525, 200));
            this.jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(CRSI18NConstants.TRANSFORMATIONS_KEY), BorderFactory.createEmptyBorder(5, 5, 5, 5)), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
    }

    public JButton getJButtonInfo() {
        if (this.infoTr == null) {
            this.infoTr = new JButton();
            this.infoTr.setPreferredSize(new Dimension(200, 20));
            this.infoTr.setText(CRSI18NConstants.TRANSFORMATION_INFO_KEY);
            this.infoTr.setMnemonic('I');
            this.infoTr.setEnabled(false);
        }
        return this.infoTr;
    }

    public ICrs getProjection() {
        ICrs crs = null;
        try {
            crs = CrsRepositoryManager.getInstance().getCRS(this.getCode(), this.getWKT(), this.getParams());
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return crs;
    }

    public ICrs getProjectionNad(String info) {
        String[] partes = info.split("\\(");
        String nadFile = partes[0];
        int codigoNad = Integer.parseInt(partes[1].substring(0, partes[1].length() - 1).split(":")[1]);
        String[] sourceAuthority = this.getSourceAbrev().split(":");
        if (this.getCode() == codigoNad) {
            try {
                this.setNad(false);
                ICrs crs = CrsRepositoryManager.getInstance().getCRS(String.valueOf(sourceAuthority[0]) + ":" + this.getCode());
                crs.setTransformationParams("+nadgrids=" + nadFile, null);
                return crs;
            }
            catch (CrsException e) {
                e.printStackTrace();
                return null;
            }
        }
        this.setNad(true);
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS(String.valueOf(sourceAuthority[0]) + ":" + this.getCode());
            crs.setTransformationParams(null, "+nadgrids=" + nadFile);
            return crs;
        }
        catch (CrsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setNad(boolean nadg) {
        this.targetNad = nadg;
    }

    public boolean getNad() {
        return this.targetNad;
    }

    public void setCode(int cod) {
        this.code = cod;
    }

    public int getCode() {
        return this.code;
    }

    public void setWKT(String cad) {
        this.cadWKT = cad;
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public void setParamsManual(String param) {
        this.params = "+towgs84=" + param.substring(1, param.length() - 1) + " ";
    }

    public void setParamsEPGS(String[] values) {
        this.params = "+towgs84=" + values[0];
        int i = 1;
        while (i < values.length) {
            this.params = String.valueOf(this.params) + "," + values[i];
            ++i;
        }
        this.params = String.valueOf(this.params) + " ";
    }

    public void setParamsNads(String nadfile) {
    }

    public String getParams() {
        return this.params;
    }

    public void setInverseTransformation(boolean inverse) {
        this.inverseTransformation = inverse;
    }

    public boolean getInverseTransformation() {
        return this.inverseTransformation;
    }

    public void setTrCode(int code) {
        this.trCode = code;
    }

    public int getTrCode() {
        return this.trCode;
    }

    public void loadRecents(String source, String target) {
        this.setSourceAbrev(source);
        int numRow = this.dtm.getRowCount();
        while (numRow != 0) {
            this.dtm.removeRow(--numRow);
        }
        RecentTrsPersistence persistence = new RecentTrsPersistence();
        this.trDataArray = persistence.getArrayOfTrData();
        int iRow = this.trDataArray.length - 1;
        while (iRow >= 0) {
            String crsSource = this.trDataArray[iRow].getCrsSource();
            String crsTarget = this.trDataArray[iRow].getCrsTarget();
            if (source.equals(crsSource) && target.equals(crsTarget)) {
                Object[] row = new Object[]{String.valueOf(this.trDataArray[iRow].getAuthority()) + ":" + this.trDataArray[iRow].getCode(), this.trDataArray[iRow].getName(), this.trDataArray[iRow].getCrsSource(), this.trDataArray[iRow].getCrsTarget(), this.trDataArray[iRow].getDetails()};
                this.dtm.addRow(row);
            }
            --iRow;
        }
        int numr = this.dtm.getRowCount();
        if (numr != 0) {
            this.getJTable().setRowSelectionInterval(0, 0);
        }
    }

    public void setSourceAbrev(String source) {
        this.sourceAbrev = source;
    }

    public String getSourceAbrev() {
        return this.sourceAbrev;
    }
}

