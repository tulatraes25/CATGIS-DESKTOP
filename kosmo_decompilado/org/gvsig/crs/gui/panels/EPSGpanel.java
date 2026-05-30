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
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.ogr.Epsg2wkt;
import org.gvsig.crs.ogr.GetCRSepsg;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class EPSGpanel
extends JPanel
implements KeyListener,
ActionListener {
    private static final Logger LOGGER = Logger.getLogger(EPSGpanel.class);
    private static final long serialVersionUID = 1L;
    String cadWKT = "";
    String cad_valida = "";
    public String key;
    public String crs_kind = null;
    String[] soported_crs = new String[]{"projected", "geographic 2D", "geographic 3D", "engineering", "vertical", "compound", "geocentric"};
    String[] not_soported_crs = new String[0];
    int iteracion = 0;
    int transf = 0;
    int source_cod = 0;
    int method_code = 0;
    int datum_code = 0;
    int projection_conv_code = 0;
    public int epsg_code = 0;
    public int selectedRowTable = -1;
    private int codeCRS = -1;
    int[] valid_method_code = new int[]{9800, 9801, 9802, 9803, 9804, 9805, 9806, 9807, 9808, 9809, 9810, 9811, 9812, 9813, 9814, 9815, 9816, 9817, 9602, 9659, 9818, 9819, 9820, 9822, 9827};
    boolean tecla_valida = false;
    boolean source_yn = false;
    private JRadioButton codeRadioButton = null;
    private JRadioButton nameRadioButton = null;
    private JRadioButton areaRadioButton = null;
    private ButtonGroup optGroup;
    private JPanel groupRadioButton = null;
    public CRSRepositoryConnection connect = null;
    public JPanel EPSGpanel = null;
    private JLabel lblCriterio;
    private JButton infoCrs = null;
    private JButton searchButton = null;
    private JTextField searchTextField = null;
    public JTable jTable = null;
    private JScrollPane jScrollPane = null;
    public DefaultTableModel dtm = null;
    public TableSorter sorter = null;
    private int projectionCode = -1;

    public EPSGpanel() {
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
        this.connect.setConnectionEPSG();
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

    private JRadioButton getAreaRadioButton() {
        if (this.areaRadioButton == null) {
            this.areaRadioButton = new JRadioButton(CRSI18NConstants.BY_AREA_KEY);
            this.areaRadioButton.addActionListener(this);
        }
        return this.areaRadioButton;
    }

    private JPanel getGroupRadioButton() {
        if (this.groupRadioButton == null) {
            this.groupRadioButton = new JPanel();
            this.groupRadioButton.setLayout(new GridLayout(1, 4));
            this.groupRadioButton.add(this.getLblCriterio());
            this.groupRadioButton.add(this.getCodeRadioButton());
            this.groupRadioButton.add(this.getNameRadioButton());
            this.groupRadioButton.add(this.getAreaRadioButton());
            this.getOptGroup();
        }
        return this.groupRadioButton;
    }

    private JLabel getLblCriterio() {
        this.lblCriterio = new JLabel(String.valueOf(CRSI18NConstants.SEARCH_CRITERION_KEY) + ":");
        return this.lblCriterio;
    }

    private void searchButton() {
        boolean not_valid = false;
        boolean not_numeric = false;
        this.searchTextField.setBackground(Color.white);
        if (this.searchTextField.getText().equals("")) {
            this.searchTextField.setBackground(new Color(255, 204, 204));
            DialogFactory.showWarningDialog(this, CRSI18NConstants.MUST_FILL_NAME_KEY, CRSI18NConstants.WARNING_KEY);
        } else {
            String sentence;
            String key3;
            int numRow = this.dtm.getRowCount();
            while (numRow != 0) {
                this.dtm.removeRow(--numRow);
            }
            if (this.codeRadioButton.isSelected() && this.searchTextField.getText().length() != this.searchTextField.getText().replaceAll("[^0-9]", "").length()) {
                not_numeric = true;
            }
            ResultSet result = null;
            ResultSet result2 = null;
            int bus = 2;
            if (this.codeRadioButton.isSelected() && !not_numeric) {
                bus = 0;
                this.key = this.searchTextField.getText();
                int code = Integer.parseInt(this.key);
                String sentence2 = "SELECT coord_ref_sys_code, coord_ref_sys_name, coord_ref_sys_kind, area_of_use_code, source_geogcrs_code, projection_conv_code  FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + code;
                result = Query.select(sentence2, this.connect.getConnection());
            } else if (this.nameRadioButton.isSelected()) {
                bus = 0;
                this.key = this.searchTextField.getText();
                this.key = this.key.toLowerCase();
                String key2 = this.key.substring(0, 1);
                key3 = this.key.substring(1, this.key.length());
                key2 = key2.toUpperCase();
                sentence = "SELECT coord_ref_sys_code, coord_ref_sys_name, coord_ref_sys_kind, area_of_use_code, source_geogcrs_code, projection_conv_code FROM epsg_coordinatereferencesystem WHERE (coord_ref_sys_name LIKE '%" + this.key + "%') OR (coord_ref_sys_name LIKE '%" + this.key.toUpperCase() + "%') " + "OR (coord_ref_sys_name LIKE '%" + key2 + key3 + "%')";
                result = Query.select(sentence, this.connect.getConnection());
            } else if (this.areaRadioButton.isSelected()) {
                bus = 1;
                this.key = this.searchTextField.getText();
                this.key = this.key.toLowerCase();
                String key2 = this.key.substring(0, 1);
                key3 = this.key.substring(1, this.key.length());
                key2 = key2.toUpperCase();
                sentence = "SELECT area_name, area_of_use, area_code FROM epsg_area WHERE (area_name LIKE '%" + this.key + "%') OR (area_of_use LIKE '%" + this.key + "%') " + "OR (area_name LIKE '%" + this.key.toUpperCase() + "%') OR (area_of_use LIKE '%" + this.key.toUpperCase() + "%') " + "OR (area_name LIKE '%" + key2 + key3 + "%') OR (area_of_use LIKE '%" + key2 + key3 + "%') ";
                result = Query.select(sentence, this.connect.getConnection());
            }
            if (bus == 0) {
                try {
                    while (result.next()) {
                        Object[] data = new Object[5];
                        data[0] = String.valueOf(result.getInt("coord_ref_sys_code"));
                        data[1] = result.getString("coord_ref_sys_name");
                        this.crs_kind = result.getString("coord_ref_sys_kind");
                        data[2] = this.crs_kind;
                        this.projection_conv_code = result.getInt("projection_conv_code");
                        this.setProjectionCode(this.projection_conv_code);
                        int area_of_use_code = Integer.parseInt(result.getString("area_of_use_code"));
                        sentence = "SELECT area_name, area_of_use FROM epsg_area WHERE area_code = " + area_of_use_code;
                        result2 = Query.select(sentence, this.connect.getConnection());
                        while (result2.next()) {
                            data[3] = result2.getString("area_name");
                            data[4] = result2.getString("area_of_use");
                        }
                        if (data[0] == null || !this.valid(this.crs_kind)) continue;
                        this.dtm.addRow(data);
                    }
                }
                catch (SQLException e1) {
                    LOGGER.error((Object)e1);
                }
            } else if (bus == 1) {
                try {
                    while (result.next()) {
                        Object[] data = new Object[5];
                        data[3] = result.getString("area_name");
                        data[4] = result.getString("area_of_use");
                        int area_of_use_code = Integer.parseInt(result.getString("area_code"));
                        sentence = "SELECT coord_ref_sys_code, coord_ref_sys_name, coord_ref_sys_kind, source_geogcrs_code, projection_conv_code FROM epsg_coordinatereferencesystem WHERE area_of_use_code = " + area_of_use_code;
                        result2 = Query.select(sentence, this.connect.getConnection());
                        while (result2.next()) {
                            data[0] = String.valueOf(result2.getInt("coord_ref_sys_code"));
                            data[1] = result2.getString("coord_ref_sys_name");
                            data[2] = result2.getString("coord_ref_sys_kind");
                            this.crs_kind = (String)data[2];
                            this.projection_conv_code = result2.getInt("projection_conv_code");
                            this.setProjectionCode(this.projection_conv_code);
                        }
                        if (data[0] != null && this.valid(this.crs_kind)) {
                            this.dtm.addRow(data);
                        }
                        if (!this.notValid(this.crs_kind)) continue;
                        not_valid = true;
                    }
                }
                catch (SQLException e1) {
                    LOGGER.error((Object)e1);
                }
            }
            int numr = this.dtm.getRowCount();
            if (not_valid) {
                DialogFactory.showWarningDialog(this, CRSI18NConstants.UNSUPPORTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
                not_valid = false;
            } else if (not_numeric) {
                DialogFactory.showWarningDialog(this, CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY, CRSI18NConstants.WARNING_KEY);
                this.searchTextField.setText("");
            } else if (numr == 0) {
                DialogFactory.showWarningDialog(this, CRSI18NConstants.NO_RESULTS_HAVE_BEEN_FOUND_KEY, CRSI18NConstants.WARNING_KEY);
            } else {
                this.getJTable().setRowSelectionInterval(0, 0);
            }
        }
    }

    private boolean notValid(String kind) {
        int i = 0;
        while (i < this.not_soported_crs.length) {
            if (kind.equals(this.not_soported_crs[i])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    private boolean valid(String kind) {
        int i = 0;
        while (i < this.soported_crs.length) {
            if (kind.equals(this.soported_crs[i])) {
                return true;
            }
            ++i;
        }
        return false;
    }

    private boolean validCRS(int projection_conv_code2) {
        if (projection_conv_code2 == 0) {
            return true;
        }
        String sentence = "SELECT coord_op_method_code FROM epsg_coordoperation WHERE coord_op_code = " + this.projection_conv_code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            while (result.next()) {
                this.method_code = result.getInt("coord_op_method_code");
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)e);
        }
        int i = 0;
        while (i < this.valid_method_code.length) {
            if (this.method_code == this.valid_method_code[i]) {
                return true;
            }
            ++i;
        }
        return false;
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
            this.searchTextField.setPreferredSize(new Dimension(350, 20));
            this.searchTextField.addKeyListener(this);
        }
        return this.searchTextField;
    }

    public JTable getJTable() {
        if (this.jTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.CODE_KEY, CRSI18NConstants.NAME_KEY, CRSI18NConstants.TYPE_KEY, CRSI18NConstants.AREA_KEY, CRSI18NConstants.DESCRIPTION_KEY};
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
            this.jTable.setAutoResizeMode(0);
            this.jTable.setCellSelectionEnabled(false);
            this.jTable.setRowSelectionAllowed(true);
            this.jTable.setColumnSelectionAllowed(false);
            this.jTable.setSelectionMode(0);
            TableColumn column = null;
            int i = 0;
            while (i < columnNames.length) {
                column = this.jTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(50);
                } else if (i == 2) {
                    column.setPreferredWidth(80);
                } else if (i == 4) {
                    column.setPreferredWidth(300);
                } else {
                    column.setPreferredWidth(140);
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
            this.jScrollPane = new JScrollPane(this.getJTable(), 20, 32);
            this.jScrollPane.setPreferredSize(new Dimension(500, 150));
            this.jScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
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
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS("EPSG:" + this.epsg_code);
            return crs;
        }
        catch (CrsException e) {
            LOGGER.error((Object)e);
            return null;
        }
    }

    public void setWKT() {
        Epsg2wkt wkt = null;
        this.epsg_code = this.getCodeCRS();
        if (this.epsg_code != -1) {
            String sentence = "SELECT source_geogcrs_code, projection_conv_code, coord_ref_sys_kind, datum_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + this.epsg_code;
            ResultSet result = Query.select(sentence, this.connect.getConnection());
            try {
                result.next();
                this.source_cod = result.getInt("source_geogcrs_code");
                this.projection_conv_code = result.getInt("projection_conv_code");
                this.crs_kind = result.getString("coord_ref_sys_kind");
                this.datum_code = result.getInt("datum_code");
            }
            catch (SQLException e1) {
                LOGGER.error((Object)e1);
            }
            this.source_yn = this.datum_code != 0 ? true : this.source_cod == 0;
            GetCRSepsg ep = new GetCRSepsg(this.epsg_code, this.source_yn, this.source_cod, this.projection_conv_code, this.connect);
            ep.Getepsgdata();
            if (this.crs_kind.equals("geographic 2D") || this.crs_kind.equals("geographic 3D")) {
                wkt = new Epsg2wkt(ep, "geog");
            } else if (this.crs_kind.equals("projected")) {
                wkt = new Epsg2wkt(ep, "proj");
            } else if (this.crs_kind.equals("compound")) {
                wkt = new Epsg2wkt(ep, "comp");
            } else if (this.crs_kind.equals("geocentric")) {
                wkt = new Epsg2wkt(ep, "geoc");
            }
        } else {
            DialogFactory.showWarningDialog(this, CRSI18NConstants.NO_SELECTED_CRS_KEY, CRSI18NConstants.WARNING_KEY);
        }
        this.cadWKT = wkt.getWKT();
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public void setProjection(IProjection crs) {
    }

    public void loadViewCRS(int code) {
        this.connection();
        String sentence = "SELECT coord_ref_sys_code, coord_ref_sys_name, coord_ref_sys_kind, area_of_use_code, source_geogcrs_code, projection_conv_code  FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            result.next();
            Object[] data = new Object[5];
            data[0] = String.valueOf(result.getInt("coord_ref_sys_code"));
            data[1] = result.getString("coord_ref_sys_name");
            this.crs_kind = result.getString("coord_ref_sys_kind");
            data[2] = this.crs_kind;
            this.projection_conv_code = result.getInt("projection_conv_code");
            int area_of_use_code = Integer.parseInt(result.getString("area_of_use_code"));
            sentence = "SELECT area_name, area_of_use FROM epsg_area WHERE area_code = " + area_of_use_code;
            ResultSet result2 = Query.select(sentence, this.connect.getConnection());
            result2.next();
            data[3] = result2.getString("area_name");
            data[4] = result2.getString("area_of_use");
            this.dtm.addRow(data);
        }
        catch (SQLException e1) {
            LOGGER.error((Object)e1);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == this.getSearchTextField() && e.getKeyCode() == 10) {
            this.searchButton();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getCodeRadioButton()) {
            this.searchTextField.setText("");
        }
        if (e.getSource() == this.getNameRadioButton()) {
            this.searchTextField.setText("");
        }
        if (e.getSource() == this.getAreaRadioButton()) {
            this.searchTextField.setText("");
        }
        if (e.getSource() == this.getSearchButton()) {
            this.searchTextField.setBackground(Color.white);
            if (StringUtils.isEmpty((String)this.searchTextField.getText())) {
                this.searchTextField.setBackground(new Color(255, 204, 204));
                DialogFactory.showWarningDialog(this, CRSI18NConstants.MUST_FILL_NAME_KEY, CRSI18NConstants.WARNING_KEY);
            } else {
                this.searchButton();
            }
        }
        if (e.getSource() == this.getInfoCrs()) {
            InfoCRSPanel info = new InfoCRSPanel("EPSG", this.getCodeCRS());
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }

    public ButtonGroup getOptGroup() {
        if (this.optGroup == null) {
            this.optGroup = new ButtonGroup();
            this.optGroup.add(this.getCodeRadioButton());
            this.optGroup.add(this.getNameRadioButton());
            this.optGroup.add(this.getAreaRadioButton());
        }
        return this.optGroup;
    }

    public void setProjectionCode(int projCode) {
        String sentence = "SELECT coord_op_method_code FROM epsg_coordoperation WHERE coord_op_code = " + projCode;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            while (result.next()) {
                this.projectionCode = result.getInt("coord_op_method_code");
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)e);
        }
    }

    public int getProjectionCode(String code) {
        String sentence = "SELECT projection_conv_code  FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        int projCode = 0;
        try {
            while (result.next()) {
                projCode = result.getInt("projection_conv_code");
            }
        }
        catch (SQLException e1) {
            LOGGER.error((Object)e1);
        }
        sentence = "SELECT coord_op_method_code FROM epsg_coordoperation WHERE coord_op_code = " + projCode;
        result = Query.select(sentence, this.connect.getConnection());
        try {
            while (result.next()) {
                this.projectionCode = result.getInt("coord_op_method_code");
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)e);
        }
        return this.projectionCode;
    }
}

