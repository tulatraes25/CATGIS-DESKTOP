/*
 * Decompiled with CFR 0.152.
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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.gui.panels.InfoCRSPanel;
import org.gvsig.crs.gui.panels.wizard.MainPanel;
import org.gvsig.crs.persistence.CrsData;
import org.gvsig.crs.persistence.RecentCRSsPersistence;
import org.saig.core.util.DialogManager;
import org.saig.jump.widgets.util.DialogFactory;

public class NewCRSPanel
extends JPanel
implements ActionListener,
KeyListener {
    private static final long serialVersionUID = 1L;
    private JRadioButton codeRadioButton = null;
    private JRadioButton nameRadioButton = null;
    private JLabel lblCriterio = null;
    private JButton searchButton = null;
    private JTextField searchTextField = null;
    private JButton infoCrs;
    private JButton btnNuevo;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JScrollPane jScrollPane = null;
    private JTable jTable;
    public TableSorter sorter = null;
    public DefaultTableModel dtm = null;
    public String key;
    public CRSRepositoryConnection connect = null;
    public int selectedRowTable = -1;
    String cadWkt = "";
    private int codeCRS = -1;
    private ICrs currentCrs;

    public NewCRSPanel() {
    }

    public NewCRSPanel(ICrs crs) {
        this.currentCrs = crs;
        this.initialize();
        this.setListener();
        this.habilitarJbuttons(false);
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setBorder(new EmptyBorder(1, 1, 1, 1));
        JPanel radio = new JPanel();
        radio.setLayout(new GridLayout(1, 4, 10, 0));
        radio.add(this.getLblCriterio());
        radio.add(this.getCodeRadioButton());
        radio.add(this.getNameRadioButton());
        this.agruparRadioButtons();
        JPanel busqueda = new JPanel();
        busqueda.setLayout(new FlowLayout(0, 10, 1));
        busqueda.add(this.getSearchButton());
        busqueda.add(this.getSearchTextField());
        JPanel pNorth = new JPanel();
        pNorth.setLayout(new GridLayout(2, 1));
        pNorth.add(radio);
        pNorth.add(busqueda);
        this.add((Component)pNorth, "North");
        JPanel pInSouth = new JPanel();
        pInSouth.setLayout(new FlowLayout(0, 10, 0));
        pInSouth.add(this.getInfoCrs());
        pInSouth.add(this.getBtnNuevo());
        pInSouth.add(this.getBtnEditar());
        pInSouth.add(this.getBtnEliminar());
        JPanel pCenter = new JPanel();
        pCenter.setLayout(new BorderLayout());
        pCenter.add((Component)this.getJScrollPane(), "Center");
        pCenter.add((Component)pInSouth, "South");
        this.add((Component)pCenter, "Center");
    }

    public void connection() {
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionUsr();
    }

    public JButton getInfoCrs() {
        if (this.infoCrs == null) {
            this.infoCrs = new JButton();
            this.infoCrs.setPreferredSize(new Dimension(85, 23));
            this.infoCrs.setText(CRSI18NConstants.CRS_INFO_KEY);
            this.infoCrs.setMnemonic('I');
            this.infoCrs.setToolTipText(CRSI18NConstants.CRS_INFO_KEY);
        }
        return this.infoCrs;
    }

    private JScrollPane getJScrollPane() {
        if (this.jScrollPane == null) {
            this.jScrollPane = new JScrollPane(this.getJTable(), 20, 32);
            this.jScrollPane.setPreferredSize(new Dimension(500, 150));
            this.jScrollPane.setBorder(new CompoundBorder(new EmptyBorder(3, 10, 3, 10), this.jScrollPane.getBorder()));
            this.jScrollPane.setViewportView(this.getJTable());
        }
        return this.jScrollPane;
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
            this.initializeTable();
        }
        return this.jTable;
    }

    public void initializeTable() {
        int numRow = this.dtm.getRowCount();
        while (numRow != 0) {
            this.dtm.removeRow(--numRow);
        }
        String sentence = "SELECT usr_code, usr_wkt, usr_proj, usr_geog, usr_datum FROM USR ORDER BY usr_code ASC";
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionUsr();
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            this.connect.shutdown();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        Object[] data = new Object[4];
        try {
            while (result.next()) {
                data[0] = result.getString("usr_code");
                data[1] = result.getString("usr_wkt");
                String proj = result.getString("usr_proj");
                if (!proj.equals("")) {
                    data[1] = proj;
                    data[2] = CRSI18NConstants.YES_KEY;
                } else {
                    data[1] = result.getString("usr_geog");
                    data[2] = CRSI18NConstants.NO_KEY;
                }
                data[3] = result.getString("usr_datum");
                this.dtm.addRow(data);
            }
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        int numr = this.dtm.getRowCount();
        if (numr > 0) {
            this.getJTable().setRowSelectionInterval(0, 0);
        }
    }

    public JRadioButton getCodeRadioButton() {
        if (this.codeRadioButton == null) {
            this.codeRadioButton = new JRadioButton();
            this.codeRadioButton.setText(CRSI18NConstants.BY_CODE_KEY);
            this.codeRadioButton.setSelected(true);
            this.codeRadioButton.addActionListener(this);
        }
        return this.codeRadioButton;
    }

    public JRadioButton getNameRadioButton() {
        if (this.nameRadioButton == null) {
            this.nameRadioButton = new JRadioButton();
            this.nameRadioButton.setText(CRSI18NConstants.BY_NAME_KEY);
            this.nameRadioButton.addActionListener(this);
        }
        return this.nameRadioButton;
    }

    private void agruparRadioButtons() {
        ButtonGroup group = new ButtonGroup();
        group.add(this.getCodeRadioButton());
        group.add(this.getNameRadioButton());
    }

    public JLabel getLblCriterio() {
        this.lblCriterio = new JLabel();
        this.lblCriterio.setText(String.valueOf(CRSI18NConstants.SEARCH_CRITERION_KEY) + ":");
        return this.lblCriterio;
    }

    public JButton getSearchButton() {
        if (this.searchButton == null) {
            this.searchButton = new JButton();
            this.searchButton.setPreferredSize(new Dimension(75, 20));
            this.searchButton.setText(CRSI18NConstants.SEARCH_KEY);
            this.searchButton.setMnemonic('S');
            this.searchButton.setToolTipText(CRSI18NConstants.SEARCH_CRS_BY_SELECTED_CRITERION_KEY);
        }
        return this.searchButton;
    }

    public JTextField getSearchTextField() {
        if (this.searchTextField == null) {
            this.searchTextField = new JTextField();
            this.searchTextField.setPreferredSize(new Dimension(340, 20));
        }
        return this.searchTextField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(this.getCodeRadioButton())) {
            this.getSearchTextField().setText("");
        } else if (e.getSource().equals(this.getNameRadioButton())) {
            this.getSearchTextField().setText("");
        } else if (e.getSource().equals(this.getBtnEditar())) {
            ICrs crs = null;
            try {
                crs = CrsRepositoryManager.getInstance().getCRS("USR:" + this.getCodeCRS());
            }
            catch (CrsException e1) {
                e1.printStackTrace();
            }
            MainPanel wizard = new MainPanel(crs);
            wizard.setEditing(true);
            wizard.setEditingPanel();
            DialogManager.addJDialog(wizard, CRSI18NConstants.USER_CRS_KEY);
            this.getNewCrs(wizard.getNewCrsCode());
            String authority = "USR";
            String name = "";
            name = wizard.getPSistCoord().getTxtNombreProy().getText().equals("") ? wizard.getPDatum().getTxtNombreCrs().getText() : wizard.getPSistCoord().getTxtNombreProy().getText();
            int code = this.getCodeCRS();
            CrsData crsData = new CrsData(authority, code, name);
            RecentCRSsPersistence persistence = new RecentCRSsPersistence();
            persistence.addCrsData(crsData);
        } else if (e.getSource().equals(this.getBtnEliminar())) {
            int i = DialogFactory.showYesNoWarningDialog(this, CRSI18NConstants.DELETE_USR_CRS_KEY, CRSI18NConstants.WARNING_KEY);
            if (i == 0) {
                this.connect = new CRSRepositoryConnection();
                this.connect.setConnectionUsr();
                String sentence = "DELETE FROM USR WHERE usr_code =" + this.getCodeCRS();
                Query.select(sentence, this.connect.getConnection());
                try {
                    this.connect.shutdown();
                }
                catch (SQLException arg0) {
                    arg0.printStackTrace();
                }
                this.dtm.removeRow(this.getJTable().getSelectedRow());
            }
        } else if (e.getSource().equals(this.getBtnNuevo())) {
            MainPanel wizard = new MainPanel(this.currentCrs);
            if (wizard.getPCard().getSelectedIndex() == 0 && wizard.getPCrsUsr().getRbCrsExistente().isSelected()) {
                ICrs crs = wizard.getPCrsUsr().getCrs();
                if (crs != null) {
                    wizard.fillData(crs);
                } else {
                    wizard.fillData(wizard.getCrs());
                }
            } else if (wizard.getPCard().getSelectedIndex() == 0 && wizard.getPCrsUsr().getRbNuevoCrs().isSelected()) {
                wizard.cleanData();
            }
            DialogManager.addJDialog(wizard, CRSI18NConstants.USER_CRS_KEY);
            this.getNewCrs(wizard.getNewCrsCode());
        } else if (e.getSource().equals(this.getSearchButton())) {
            this.searchButton();
        } else if (e.getSource().equals(this.getInfoCrs())) {
            InfoCRSPanel info = new InfoCRSPanel("USR", this.getCodeCRS());
            DialogManager.addJDialog(info, CRSI18NConstants.CRS_INFO_KEY);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == this.getSearchTextField() && e.getKeyCode() == 10) {
            this.searchTextField.setBackground(Color.white);
            if (this.searchTextField.getText().equals("")) {
                this.initializeTable();
            } else {
                this.searchButton();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public JButton getBtnEditar() {
        if (this.btnEditar == null) {
            this.btnEditar = new JButton();
            this.btnEditar.setText(CRSI18NConstants.EDIT_KEY);
        }
        return this.btnEditar;
    }

    public JButton getBtnEliminar() {
        if (this.btnEliminar == null) {
            this.btnEliminar = new JButton();
            this.btnEliminar.setText(CRSI18NConstants.REMOVE_KEY);
        }
        return this.btnEliminar;
    }

    public JButton getBtnNuevo() {
        if (this.btnNuevo == null) {
            this.btnNuevo = new JButton();
            this.btnNuevo.setText(CRSI18NConstants.NEW_KEY);
        }
        return this.btnNuevo;
    }

    private void setListener() {
        this.getBtnEditar().addActionListener(this);
        this.getBtnEliminar().addActionListener(this);
        this.getBtnNuevo().addActionListener(this);
        this.getInfoCrs().addActionListener(this);
        this.getCodeRadioButton().addActionListener(this);
        this.getNameRadioButton().addActionListener(this);
        this.getSearchButton().addActionListener(this);
        this.getSearchTextField().addKeyListener(this);
    }

    private void habilitarJbuttons(boolean b) {
        this.getInfoCrs().setEnabled(b);
        this.getBtnEditar().setEnabled(b);
        this.getBtnEliminar().setEnabled(b);
    }

    private void searchButton() {
        int numr;
        this.searchTextField.setBackground(Color.white);
        if (this.codeRadioButton.isSelected() && this.searchTextField.getText().length() != this.searchTextField.getText().replaceAll("[^0-9]", "").length()) {
            DialogFactory.showWarningDialog(this, CRSI18NConstants.NUMERIC_FORMAT_MESSAGE_KEY, CRSI18NConstants.WARNING_KEY);
            this.searchTextField.setText("");
            return;
        }
        int numRow = this.dtm.getRowCount();
        while (numRow != 0) {
            this.dtm.removeRow(--numRow);
        }
        ResultSet result = null;
        if (this.searchTextField.getText().equals("")) {
            String sentence = "SELECT usr_code, usr_wkt, usr_proj, usr_geog, usr_datum FROM USR ORDER BY usr_code ASC";
            this.connect = new CRSRepositoryConnection();
            this.connect.setConnectionUsr();
            result = Query.select(sentence, this.connect.getConnection());
            try {
                this.connect.shutdown();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            Object[] data = new Object[4];
            try {
                while (result.next()) {
                    data[0] = result.getString("usr_code");
                    data[1] = result.getString("usr_wkt");
                    String proj = result.getString("usr_proj");
                    if (!proj.equals("")) {
                        data[1] = proj;
                        data[2] = CRSI18NConstants.YES_KEY;
                    } else {
                        data[1] = result.getString("usr_geog");
                        data[2] = CRSI18NConstants.NO_KEY;
                    }
                    data[3] = result.getString("usr_datum");
                    this.dtm.addRow(data);
                }
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else if (this.codeRadioButton.isSelected()) {
            this.key = this.searchTextField.getText();
            int code = Integer.parseInt(this.key);
            String sentence = "SELECT usr_code, usr_wkt, usr_proj, usr_geog, usr_datum FROM USR WHERE usr_code = " + code;
            this.connect = new CRSRepositoryConnection();
            this.connect.setConnectionUsr();
            result = Query.select(sentence, this.connect.getConnection());
            try {
                this.connect.shutdown();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            Object[] data = new Object[4];
            try {
                while (result.next()) {
                    data[0] = result.getString("usr_code");
                    data[1] = result.getString("usr_wkt");
                    String proj = result.getString("usr_proj");
                    if (!proj.equals("")) {
                        data[1] = proj;
                        data[2] = CRSI18NConstants.YES_KEY;
                    } else {
                        data[1] = result.getString("usr_geog");
                        data[2] = CRSI18NConstants.NO_KEY;
                    }
                    data[3] = result.getString("usr_datum");
                    this.dtm.addRow(data);
                }
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else if (this.nameRadioButton.isSelected()) {
            this.key = this.searchTextField.getText();
            String key2 = this.key.substring(0, 1);
            String key3 = this.key.substring(1, this.key.length());
            key2 = key2.toUpperCase();
            String sentence = "SELECT usr_code, usr_wkt, usr_proj, usr_geog, usr_datum FROM USR WHERE (usr_proj LIKE '%" + this.key + "%') OR (usr_proj LIKE '%" + this.key.toUpperCase() + "%') " + "OR (usr_proj LIKE '%" + key2 + key3 + "%') OR " + "(usr_geog LIKE '%" + this.key + "%') OR (usr_geog LIKE '%" + this.key.toUpperCase() + "%') " + "OR (usr_geog LIKE '%" + key2 + key3 + "%')";
            this.connect = new CRSRepositoryConnection();
            this.connect.setConnectionUsr();
            result = Query.select(sentence, this.connect.getConnection());
            try {
                this.connect.shutdown();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            Object[] data = new Object[4];
            try {
                while (result.next()) {
                    data[0] = result.getString("usr_code");
                    data[1] = result.getString("usr_wkt");
                    String proj = result.getString("usr_proj");
                    if (!proj.equals("")) {
                        data[1] = proj;
                        data[2] = CRSI18NConstants.YES_KEY;
                    } else {
                        data[1] = result.getString("usr_geog");
                        data[2] = CRSI18NConstants.NO_KEY;
                    }
                    data[3] = result.getString("usr_datum");
                    this.dtm.addRow(data);
                }
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        if ((numr = this.dtm.getRowCount()) == 0) {
            DialogFactory.showWarningDialog(this, CRSI18NConstants.NO_RESULTS_HAVE_BEEN_FOUND_KEY, CRSI18NConstants.WARNING_KEY);
        } else {
            this.getJTable().setRowSelectionInterval(0, 0);
        }
    }

    public void setCodeCRS(int code) {
        this.codeCRS = code;
    }

    public int getCodeCRS() {
        return this.codeCRS;
    }

    public void setWKT() {
        int code = this.getCodeCRS();
        String sentence = "SELECT usr_wkt FROM USR WHERE usr_code = " + code;
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionUsr();
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            this.connect.shutdown();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            result.next();
            this.cadWkt = result.getString("usr_wkt");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        this.cadWkt = String.valueOf(this.cadWkt.substring(0, this.cadWkt.length() - 1)) + ", AUTHORITY[\"USR\"," + this.getCodeCRS() + "]]";
    }

    public String getWKT() {
        return this.cadWkt;
    }

    public ICrs getProjection() {
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS("USR:" + this.getCodeCRS());
            return crs;
        }
        catch (CrsException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getNewCrs(int code) {
        if (code != -1) {
            int numRow = this.dtm.getRowCount();
            while (numRow != 0) {
                this.dtm.removeRow(--numRow);
            }
            String sentence = "SELECT usr_code, usr_wkt, usr_proj, usr_geog, usr_datum FROM USR WHERE usr_code = " + code;
            this.connect = new CRSRepositoryConnection();
            this.connect.setConnectionUsr();
            ResultSet result = Query.select(sentence, this.connect.getConnection());
            try {
                this.connect.shutdown();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            Object[] data = new Object[4];
            try {
                while (result.next()) {
                    data[0] = result.getString("usr_code");
                    data[1] = result.getString("usr_wkt");
                    String proj = result.getString("usr_proj");
                    if (!proj.equals("")) {
                        data[1] = proj;
                        data[2] = CRSI18NConstants.YES_KEY;
                    } else {
                        data[1] = result.getString("usr_geog");
                        data[2] = CRSI18NConstants.NO_KEY;
                    }
                    data[3] = result.getString("usr_datum");
                    this.dtm.addRow(data);
                }
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            int numr = this.dtm.getRowCount();
            if (numr == 0) {
                this.searchButton();
            } else {
                this.getJTable().setRowSelectionInterval(0, 0);
            }
        }
    }

    public boolean isInBD(int code) {
        String sentence = "SELECT usr_code FROM USR WHERE usr_code =" + code;
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionUsr();
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            this.connect.shutdown();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (result.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

