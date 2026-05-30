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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.ogr.Esri2wkt;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class ESRIpanel
extends JPanel
implements ActionListener,
KeyListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ESRIpanel.class);
    private JRadioButton codeRadioButton = null;
    private JRadioButton nameRadioButton = null;
    private JPanel groupRadioButton = null;
    public TableSorter sorter = null;
    public JTable jTable = null;
    private JScrollPane jScrollPane = null;
    private JButton searchButton = null;
    private JButton infoCrs = null;
    private JTextField searchTextField = null;
    public DefaultTableModel dtm = null;
    private int codeCRS = -1;
    public String key;
    String cadWKT;
    public int selectedRowTable = -1;
    public CRSRepositoryConnection connect = null;

    public ESRIpanel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        JPanel pNorth = new JPanel();
        pNorth.setLayout(new GridLayout(2, 1));
        pNorth.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        pNorth.add(this.getGroupRadioButton());
        JPanel pInNorth = new JPanel();
        pInNorth.setLayout(new FlowLayout(0, 10, 1));
        pInNorth.add(this.getSearchButton());
        pInNorth.add(this.getSearchTextField());
        pNorth.add(pInNorth);
        this.add((Component)pNorth, "North");
        this.add((Component)this.getJScrollPane(), "Center");
        JPanel pSouth = new JPanel(new FlowLayout(2, 10, 3));
        pSouth.add(this.getInfoCrs());
        this.add((Component)pSouth, "South");
    }

    public void connection() {
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionEsri();
    }

    private JRadioButton getCodeRadioButton() {
        if (this.codeRadioButton == null) {
            this.codeRadioButton = new JRadioButton(CRSI18NConstants.BY_CODE_KEY);
            this.codeRadioButton.setSelected(true);
            this.codeRadioButton.addActionListener(this);
        }
        return this.codeRadioButton;
    }

    private JRadioButton getNameRadioButton() {
        if (this.nameRadioButton == null) {
            this.nameRadioButton = new JRadioButton(CRSI18NConstants.BY_NAME_KEY);
            this.nameRadioButton.addActionListener(this);
        }
        return this.nameRadioButton;
    }

    private JPanel getGroupRadioButton() {
        if (this.groupRadioButton == null) {
            this.groupRadioButton = new JPanel();
            this.groupRadioButton.setLayout(new GridLayout(1, 0));
            this.groupRadioButton.setPreferredSize(new Dimension(500, 30));
            this.groupRadioButton.add(this.getLabel());
            this.groupRadioButton.add(this.getCodeRadioButton());
            this.groupRadioButton.add(this.getNameRadioButton());
        }
        return this.groupRadioButton;
    }

    private JLabel getLabel() {
        JLabel criterio = new JLabel();
        criterio.setPreferredSize(new Dimension(100, 20));
        criterio.setText(String.valueOf(CRSI18NConstants.SEARCH_CRITERION_KEY) + ": ");
        return criterio;
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

    private JButton getSearchButton() {
        if (this.searchButton == null) {
            this.searchButton = new JButton();
            this.searchButton.setPreferredSize(new Dimension(75, 20));
            this.searchButton.setText(CRSI18NConstants.SEARCH_KEY);
            this.searchButton.setMnemonic('S');
            this.searchButton.setToolTipText(CRSI18NConstants.SEARCH_CRS_BY_SELECTED_CRITERION_KEY);
            this.searchButton.addActionListener(this);
        }
        return this.searchButton;
    }

    private JTextField getSearchTextField() {
        if (this.searchTextField == null) {
            this.searchTextField = new JTextField();
            this.searchTextField.setPreferredSize(new Dimension(300, 20));
            this.searchTextField.addKeyListener(this);
        }
        return this.searchTextField;
    }

    public JTable getJTable() {
        if (this.jTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.CODE_KEY, CRSI18NConstants.NAME_KEY, CRSI18NConstants.PROJECTED_KEY, CRSI18NConstants.DATUM_KEY};
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
            while (i < 4) {
                column = this.jTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(80);
                } else if (i == 2) {
                    column.setPreferredWidth(50);
                } else {
                    column.setPreferredWidth(175);
                }
                ++i;
            }
        }
        return this.jTable;
    }

    public void setCodeCRS(int code) {
        this.codeCRS = code;
    }

    public int getCodeCRS() {
        return this.codeCRS;
    }

    private JScrollPane getJScrollPane() {
        if (this.jScrollPane == null) {
            this.jScrollPane = new JScrollPane();
            this.jScrollPane.setPreferredSize(new Dimension(500, 150));
            this.jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("ESRI"), BorderFactory.createEmptyBorder(5, 5, 5, 5)), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == this.getSearchTextField() && e.getKeyCode() == 10) {
            this.searchButton();
        }
    }

    private void searchButton() {
        this.searchTextField.setBackground(Color.white);
        boolean not_numeric = false;
        if (this.searchTextField.getText().equals("")) {
            this.searchTextField.setBackground(new Color(255, 204, 204));
            DialogFactory.showWarningDialog(this, CRSI18NConstants.MUST_FILL_NAME_KEY, CRSI18NConstants.WARNING_KEY);
        } else {
            int numRow = this.dtm.getRowCount();
            while (numRow != 0) {
                this.dtm.removeRow(--numRow);
            }
            if (this.codeRadioButton.isSelected() && this.searchTextField.getText().length() != this.searchTextField.getText().replaceAll("[^0-9]", "").length()) {
                not_numeric = true;
            }
            ResultSet result = null;
            if (this.codeRadioButton.isSelected() && !not_numeric) {
                this.key = this.searchTextField.getText();
                int code = Integer.parseInt(this.key);
                String sentence = "SELECT esri_code, esri_wkt, esri_proj, esri_geog, esri_datum FROM ESRI WHERE esri_code = " + code;
                result = Query.select(sentence, this.connect.getConnection());
                Object[] data = new Object[4];
                try {
                    while (result.next()) {
                        data[0] = result.getString("esri_code");
                        data[1] = result.getString("esri_wkt");
                        String proj = result.getString("esri_proj");
                        if (!proj.equals("")) {
                            data[1] = proj;
                            data[2] = CRSI18NConstants.YES_KEY;
                        } else {
                            data[1] = result.getString("esri_geog");
                            data[2] = CRSI18NConstants.NO_KEY;
                        }
                        data[3] = result.getString("esri_datum");
                        this.dtm.addRow(data);
                    }
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
            } else if (this.nameRadioButton.isSelected()) {
                this.key = this.searchTextField.getText();
                this.key = this.key.toLowerCase();
                String key2 = this.key.substring(0, 1);
                String key3 = this.key.substring(1, this.key.length());
                key2 = key2.toUpperCase();
                String sentence = "SELECT esri_code, esri_wkt, esri_proj, esri_geog, esri_datum FROM ESRI WHERE (esri_proj LIKE '%" + this.key + "%') OR (esri_proj LIKE '%" + this.key.toUpperCase() + "%') " + "OR (esri_proj LIKE '%" + key2 + key3 + "%') OR " + "(esri_geog LIKE '%" + this.key + "%') OR (esri_geog LIKE '%" + this.key.toUpperCase() + "%') " + "OR (esri_geog LIKE '%" + key2 + key3 + "%')";
                result = Query.select(sentence, this.connect.getConnection());
                Object[] data = new Object[4];
                try {
                    while (result.next()) {
                        data[0] = result.getString("esri_code");
                        data[1] = result.getString("esri_wkt");
                        String proj = result.getString("esri_proj");
                        if (!proj.equals("")) {
                            data[1] = proj;
                            data[2] = CRSI18NConstants.YES_KEY;
                        } else {
                            data[1] = result.getString("esri_geog");
                            data[2] = CRSI18NConstants.NO_KEY;
                        }
                        data[3] = result.getString("esri_datum");
                        this.dtm.addRow(data);
                    }
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            int numr = this.dtm.getRowCount();
            if (not_numeric) {
                DialogFactory.showWarningDialog(this, CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY, CRSI18NConstants.WARNING_KEY);
                this.searchTextField.setText("");
            } else if (numr == 0) {
                DialogFactory.showWarningDialog(this, CRSI18NConstants.NO_RESULTS_HAVE_BEEN_FOUND_KEY, CRSI18NConstants.WARNING_KEY);
            } else {
                this.getJTable().setRowSelectionInterval(0, 0);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getSearchButton()) {
            this.searchTextField.setBackground(Color.white);
            if (this.searchTextField.getText().equals("")) {
                this.searchTextField.setBackground(new Color(255, 204, 204));
                DialogFactory.showWarningDialog(this, CRSI18NConstants.MUST_FILL_NAME_KEY, CRSI18NConstants.WARNING_KEY);
            } else {
                this.searchButton();
            }
        }
        if (e.getSource() == this.getCodeRadioButton()) {
            this.searchTextField.setText("");
            this.codeRadioButton.setSelected(true);
            this.nameRadioButton.setSelected(false);
        }
        if (e.getSource() == this.getNameRadioButton()) {
            this.searchTextField.setText("");
            this.nameRadioButton.setSelected(true);
            this.codeRadioButton.setSelected(false);
        }
        if (e.getSource() == this.getInfoCrs()) {
            InfoCRSPanel info = new InfoCRSPanel("ESRI", this.getCodeCRS());
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }

    public ICrs getProjection() {
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS("ESRI:" + this.getCodeCRS());
            return crs;
        }
        catch (CrsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setWKT() {
        int code = this.getCodeCRS();
        String sentence = "SELECT esri_wkt FROM ESRI WHERE esri_code = " + code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            result.next();
            this.cadWKT = result.getString("esri_wkt");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        this.cadWKT = String.valueOf(this.cadWKT.substring(0, this.cadWKT.length() - 1)) + ", AUTHORITY[\"ESRI\"," + this.getCodeCRS() + "]]";
        if (this.cadWKT.charAt(0) == 'P') {
            Esri2wkt wk = new Esri2wkt(this.cadWKT);
            this.cadWKT = wk.getWkt();
        }
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public void setProjection(IProjection crs) {
    }
}

