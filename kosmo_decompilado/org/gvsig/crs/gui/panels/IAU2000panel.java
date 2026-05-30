/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui.panels;

import com.iver.cit.gvsig.gui.TableSorter;
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.ogr.Iau2wkt;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class IAU2000panel
extends JPanel
implements KeyListener,
ActionListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(IAU2000panel.class);
    public JPanel IAU2000panel = null;
    private JRadioButton codeRadioButton = null;
    private JRadioButton nameRadioButton = null;
    private JPanel groupRadioButton = null;
    private JButton infoCrs = null;
    JLabel jLabel = null;
    private JLabel lblCriterio = null;
    JTextField crsTextFld = null;
    String cadWKT = "";
    int transf = 0;
    boolean source_yn = false;
    int source_cod = 0;
    int method_code = 0;
    int datum_code = 0;
    int projection_conv_code = 0;
    public String crs_kind = null;
    public TableSorter sorter = null;
    public CRSRepositoryConnection connect = null;
    private JButton searchButton = null;
    private JTextField searchTextField = null;
    public JTable jTable = null;
    private JScrollPane jScrollPane = null;
    public DefaultTableModel dtm = null;
    public String key;
    public int selectedRowTable = -1;
    private int codeCRS = -1;

    public IAU2000panel() {
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        JPanel pNorth = new JPanel();
        JPanel pInNorth = new JPanel();
        JPanel pSouth = new JPanel();
        pInNorth.setLayout(new FlowLayout(1, 10, 1));
        pInNorth.add(this.getSearchButton());
        pInNorth.add(this.getSearchTextField());
        pNorth.setLayout(new GridLayout(0, 1));
        pNorth.add(this.getGroupRadioButton());
        pNorth.add(pInNorth);
        pSouth.setLayout(new FlowLayout(2, 5, 5));
        pSouth.add(this.getInfoCrs());
        this.add((Component)pNorth, "North");
        this.add((Component)this.getJScrollPane(), "Center");
        this.add((Component)pSouth, "South");
    }

    public void connection() {
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionIAU2000();
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
            this.groupRadioButton.add(this.getLblCriterio());
            this.groupRadioButton.add(this.getCodeRadioButton());
            this.groupRadioButton.add(this.getNameRadioButton());
            ButtonGroup group = new ButtonGroup();
            group.add(this.getCodeRadioButton());
            group.add(this.getNameRadioButton());
        }
        return this.groupRadioButton;
    }

    private JLabel getLblCriterio() {
        if (this.lblCriterio == null) {
            this.lblCriterio = new JLabel();
            this.lblCriterio.setPreferredSize(new Dimension(100, 20));
            this.lblCriterio.setText(String.valueOf(CRSI18NConstants.SEARCH_CRITERION_KEY) + ": ");
        }
        return this.lblCriterio;
    }

    private void searchButton() {
        this.searchTextField.setBackground(Color.white);
        boolean not_numeric = false;
        if (StringUtils.isEmpty((String)this.searchTextField.getText())) {
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
                String sentence = "SELECT iau_code, iau_wkt, iau_proj, iau_geog, iau_datum FROM IAU2000 WHERE iau_code = " + code;
                result = Query.select(sentence, this.connect.getConnection());
                Object[] data = new Object[4];
                try {
                    while (result.next()) {
                        data[0] = result.getString("iau_code");
                        data[1] = result.getString("iau_wkt");
                        String proj = result.getString("iau_proj");
                        if (!proj.equals("")) {
                            data[1] = proj;
                            data[2] = CRSI18NConstants.YES_KEY;
                        } else {
                            data[1] = result.getString("iau_geog");
                            data[2] = CRSI18NConstants.NO_KEY;
                        }
                        data[3] = result.getString("iau_datum");
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
                String sentence = "SELECT iau_code, iau_wkt, iau_proj, iau_geog, iau_datum FROM IAU2000 WHERE (iau_proj LIKE '%" + this.key + "%') OR (iau_proj LIKE '%" + this.key.toUpperCase() + "%') " + "OR (iau_proj LIKE '%" + key2 + key3 + "%') OR " + "(iau_geog LIKE '%" + this.key + "%') OR (iau_geog LIKE '%" + this.key.toUpperCase() + "%') " + "OR (iau_geog LIKE '%" + key2 + key3 + "%')";
                result = Query.select(sentence, this.connect.getConnection());
                Object[] data = new Object[4];
                try {
                    while (result.next()) {
                        data[0] = result.getString("iau_code");
                        data[1] = result.getString("iau_wkt");
                        String proj = result.getString("iau_proj");
                        if (!proj.equals("")) {
                            data[1] = proj;
                            data[2] = CRSI18NConstants.YES_KEY;
                        } else {
                            data[1] = result.getString("iau_geog");
                            data[2] = CRSI18NConstants.NO_KEY;
                        }
                        data[3] = result.getString("iau_datum");
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
            while (i < columnNames.length) {
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
            this.jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
    }

    public ICrs getProjection() {
        try {
            String txt = this.getWKT();
            ICrs crs = new CrsFactory().getCRS("IAU2000:" + this.getCodeCRS());
            return crs;
        }
        catch (CrsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setWKT() {
        int code = this.getCodeCRS();
        String sentence = "SELECT iau_wkt FROM IAU2000 WHERE iau_code = " + code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            result.next();
            this.cadWKT = result.getString("iau_wkt");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        this.cadWKT = String.valueOf(this.cadWKT.substring(0, this.cadWKT.length() - 1)) + ", AUTHORITY[\"IAU2000\"," + this.getCodeCRS() + "]]";
        if (this.cadWKT.charAt(0) == 'P') {
            Iau2wkt wk = new Iau2wkt(this.cadWKT);
            this.cadWKT = wk.getWkt();
        }
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public void setProjection(IProjection crs) {
    }

    public void loadViewCRS(int code) {
        this.connection();
        String sentence = "SELECT iau_code, iau_wkt, iau_proj, iau_geog, iau_datum FROM IAU2000 WHERE iau_code = " + code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        Object[] data = new Object[4];
        try {
            while (result.next()) {
                data[0] = result.getString("iau_code");
                data[1] = result.getString("iau_wkt");
                String proj = result.getString("iau_proj");
                if (!proj.equals("")) {
                    data[1] = proj;
                    data[2] = CRSI18NConstants.YES_KEY;
                } else {
                    data[1] = result.getString("iau_geog");
                    data[2] = CRSI18NConstants.NO_KEY;
                }
                data[3] = result.getString("iau_datum");
                this.dtm.addRow(data);
            }
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == this.getSearchTextField() && e.getKeyCode() == 10) {
            this.searchTextField.setBackground(Color.white);
            if (StringUtils.isEmpty((String)this.searchTextField.getText())) {
                this.searchTextField.setBackground(new Color(255, 204, 204));
                DialogFactory.showWarningDialog(this, CRSI18NConstants.MUST_FILL_NAME_KEY, CRSI18NConstants.WARNING_KEY);
            } else {
                this.searchButton();
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
            if (StringUtils.isEmpty((String)this.searchTextField.getText())) {
                this.searchTextField.setBackground(new Color(255, 204, 204));
                DialogFactory.showWarningDialog(this, CRSI18NConstants.MUST_FILL_NAME_KEY, CRSI18NConstants.WARNING_KEY);
            } else {
                this.searchButton();
            }
        }
        if (e.getSource() == this.getCodeRadioButton()) {
            this.searchTextField.setText("");
        }
        if (e.getSource() == this.getNameRadioButton()) {
            this.searchTextField.setText("");
        }
        if (e.getSource() == this.getInfoCrs()) {
            InfoCRSPanel info = new InfoCRSPanel("IAU2000", this.getCodeCRS());
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }
}

