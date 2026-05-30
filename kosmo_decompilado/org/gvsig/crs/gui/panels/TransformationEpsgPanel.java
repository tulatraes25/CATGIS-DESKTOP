/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui.panels;

import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.gui.TableSorter;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.Query;
import org.gvsig.crs.persistence.RecentTrsPersistence;
import org.gvsig.crs.persistence.TrData;
import org.saig.jump.lang.I18N;

public class TransformationEpsgPanel
extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(TransformationEpsgPanel.class);
    private static final long serialVersionUID = 1L;
    private IProjection firstProj;
    String[] transformations = new String[]{"9603", "9606", "9607", "9613", "9615", "9633"};
    private int transformation_code = -1;
    private String[] values;
    private String params = "+towgs84=";
    private JTable transformationTable;
    private JScrollPane jScrollPane = null;
    public boolean inverseTranformation = false;
    public CRSRepositoryConnection connect = new CRSRepositoryConnection();
    public int crs_target = -1;
    public DefaultTableModel dtm = null;
    private int crs_source_code;
    private String cadWKT = "";
    public int selectedRowTable = -1;
    boolean tra = false;
    int real_target;
    String authority_target = "";
    private JTextArea info;
    public TableSorter sorter = null;

    public TransformationEpsgPanel(String aut_target) {
        int target;
        this.connect.setConnectionEPSG();
        String[] authority_target = aut_target.split(":");
        this.setAuthorityTarget(authority_target[0]);
        this.real_target = target = Integer.parseInt(authority_target[1]);
        String sentence = "SELECT source_geogcrs_code, coord_ref_sys_kind FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + target;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        try {
            result.next();
            String kind = result.getString("coord_ref_sys_kind");
            if (kind.equals("projected")) {
                target = result.getInt("source_geogcrs_code");
            }
        }
        catch (SQLException e1) {
            LOGGER.error((Object)e1);
        }
        this.crs_target = target;
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new BorderLayout(1, 50));
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(I18N.getString("org.gvsig.crs.gui.panels.TransformationEpsgPanel.EPSG-transformation")), BorderFactory.createEmptyBorder(12, 2, 80, 2)), this.getBorder()));
        this.add((Component)this.getJScrollPane(), "Center");
    }

    private Component getInfo() {
        if (this.info == null) {
            this.info = new JTextArea();
            this.info.setLineWrap(true);
            this.info.setWrapStyleWord(true);
            this.info.setPreferredSize(new Dimension(400, 240));
            this.info.setEditable(false);
            this.info.append(this.getWKT());
        }
        this.info.setText(this.getWKT());
        return this.info;
    }

    public JTable getJTable() {
        if (this.transformationTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.CODE_KEY, CRSI18NConstants.NAME_KEY, CRSI18NConstants.TYPE_KEY, CRSI18NConstants.SOURCE_CRS_KEY, CRSI18NConstants.TARGET_CRS_KEY, CRSI18NConstants.DESCRIPTION_KEY};
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
                if (i == 0 || i == 3 || i == 4) {
                    column.setPreferredWidth(40);
                } else if (i == 2) {
                    column.setPreferredWidth(80);
                } else {
                    column.setPreferredWidth(160);
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

    private void callTransformation(int crsCode) {
        String sentence = "SELECT source_geogcrs_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + crsCode;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        int source = 0;
        try {
            result.next();
            source = result.getInt("source_geogcrs_code");
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        ResultSet result2 = null;
        ResultSet result3 = null;
        if (source != 0) {
            crsCode = source;
        }
        ArrayList<String> codecs = new ArrayList<String>();
        codecs.add(String.valueOf(crsCode));
        int j = 0;
        while (j < codecs.size()) {
            sentence = "SELECT coord_op_code, coord_op_name, coord_op_type, source_crs_code, target_crs_code, area_of_use_code, coord_op_method_code FROM epsg_coordoperation WHERE source_crs_code = " + (String)codecs.get(j) + "AND target_crs_code = " + this.crs_target;
            result = Query.select(sentence, this.connect.getConnection());
            try {
                while (result.next()) {
                    Object[] data = new Object[6];
                    data[0] = String.valueOf(result.getInt("coord_op_code"));
                    data[1] = result.getString("coord_op_name");
                    data[2] = result.getString("coord_op_type");
                    data[3] = String.valueOf(result.getInt("source_crs_code"));
                    data[4] = String.valueOf(result.getInt("target_crs_code"));
                    int aouc = Integer.parseInt(result.getString("area_of_use_code"));
                    sentence = "SELECT area_of_use FROM epsg_area WHERE area_code = " + aouc;
                    result2 = Query.select(sentence, this.connect.getConnection());
                    while (result2.next()) {
                        data[5] = result2.getString("area_of_use");
                    }
                    String coord_op_method = result.getString("coord_op_method_code");
                    sentence = "SELECT reverse_op FROM epsg_coordoperationmethod WHERE coord_op_method_code LIKE " + coord_op_method;
                    result3 = Query.select(sentence, this.connect.getConnection());
                    while (result3.next()) {
                        if (Integer.parseInt(result3.getString("reverse_op")) != 1) continue;
                        int i = 0;
                        while (i < this.transformations.length) {
                            if (coord_op_method.equals(this.transformations[i])) {
                                this.dtm.addRow(data);
                            }
                            ++i;
                        }
                    }
                }
            }
            catch (SQLException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
            ++j;
        }
    }

    private List<String> deleteItems(List<String> codecs) {
        ArrayList<String> cod = new ArrayList<String>();
        boolean equal = false;
        int i = 0;
        while (i < codecs.size()) {
            String c = codecs.get(i);
            int j = 0;
            while (j < cod.size()) {
                if (((String)cod.get(j)).equals(c)) {
                    equal = true;
                }
                ++j;
            }
            if (!equal) {
                cod.add(c);
            }
            equal = false;
            ++i;
        }
        return cod;
    }

    private void callInverseTransformation(int crsCode) {
        String sentence = "SELECT source_geogcrs_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + crsCode;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        int source = 0;
        try {
            result.next();
            source = result.getInt("source_geogcrs_code");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
        ResultSet result2 = null;
        ResultSet result3 = null;
        if (source != 0) {
            crsCode = source;
        }
        List<String> codecs = new ArrayList<String>();
        codecs.add(String.valueOf(crsCode));
        codecs = this.deleteItems(codecs);
        int j = 0;
        while (j < codecs.size()) {
            sentence = "SELECT coord_op_code, coord_op_name, coord_op_type, source_crs_code, target_crs_code, area_of_use_code, coord_op_method_code FROM epsg_coordoperation WHERE source_crs_code = " + codecs.get(j) + "AND target_crs_code = " + this.crs_target;
            result = Query.select(sentence, this.connect.getConnection());
            try {
                while (result.next()) {
                    Object[] data = new Object[6];
                    data[0] = String.valueOf(result.getInt("coord_op_code"));
                    data[1] = result.getString("coord_op_name");
                    data[2] = result.getString("coord_op_type");
                    data[4] = String.valueOf(result.getInt("source_crs_code"));
                    data[3] = String.valueOf(result.getInt("target_crs_code"));
                    int aouc = Integer.parseInt(result.getString("area_of_use_code"));
                    sentence = "SELECT area_of_use FROM epsg_area WHERE area_code = " + aouc;
                    result2 = Query.select(sentence, this.connect.getConnection());
                    while (result2.next()) {
                        data[5] = result2.getString("area_of_use");
                    }
                    String coord_op_method = result.getString("coord_op_method_code");
                    sentence = "SELECT reverse_op FROM epsg_coordoperationmethod WHERE coord_op_method_code LIKE " + coord_op_method;
                    result3 = Query.select(sentence, this.connect.getConnection());
                    while (result3.next()) {
                        if (Integer.parseInt(result3.getString("reverse_op")) != 1) continue;
                        int i = 0;
                        while (i < this.transformations.length) {
                            if (coord_op_method.equals(this.transformations[i])) {
                                this.dtm.addRow(data);
                            }
                            ++i;
                        }
                    }
                }
            }
            catch (SQLException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
            ++j;
        }
    }

    public void setValues(String[] val) {
        this.values = val;
    }

    public String[] getValues() {
        return this.values;
    }

    public void setTrasformation_code(int t_cod) {
        this.transformation_code = t_cod;
    }

    public int getTransformation_code() {
        return this.transformation_code;
    }

    public ICrs getProjection() {
        this.params = String.valueOf(this.params) + this.values[0];
        int i = 1;
        while (i < this.values.length) {
            this.params = String.valueOf(this.params) + "," + this.values[i];
            ++i;
        }
        try {
            ICrs crs = CrsRepositoryManager.getInstance().getCRS("EPSG:" + this.crs_source_code);
            crs.setTransformationParams(this.params, null);
            return crs;
        }
        catch (CrsException e) {
            LOGGER.error((Object)"", (Throwable)e);
            return null;
        }
    }

    public void setProjection(IProjection proj) {
        this.firstProj = proj;
    }

    public void setWKT(String cad) {
        this.cadWKT = cad;
        this.getInfo();
    }

    public String getWKT() {
        return this.cadWKT;
    }

    public void setSource(String fuente, int code) {
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionEPSG();
        this.inverseTranformation = false;
        this.crs_source_code = code;
        int numRow = this.dtm.getRowCount();
        while (numRow != 0) {
            this.dtm.removeRow(--numRow);
        }
        if (fuente.equals("EPSG") && this.getAuthorityTarget().equals("EPSG")) {
            this.callTransformation(this.crs_source_code);
            int new_target = this.crs_target;
            int base_target = code;
            String sentence = "SELECT source_geogcrs_code, coord_ref_sys_kind FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + code;
            ResultSet result = Query.select(sentence, this.connect.getConnection());
            try {
                result.next();
                String kind = result.getString("coord_ref_sys_kind");
                if (kind.equals("projected")) {
                    base_target = result.getInt("source_geogcrs_code");
                }
            }
            catch (SQLException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
            this.crs_target = base_target;
            this.crs_source_code = new_target;
            this.callInverseTransformation(this.crs_source_code);
            this.crs_target = new_target;
            this.crs_source_code = code;
            int numr = this.dtm.getRowCount();
            if (numr > 0) {
                this.getJTable().setRowSelectionInterval(0, 0);
            }
        }
    }

    public void setSourceCompuesta(String aut) {
        int code;
        this.connect = new CRSRepositoryConnection();
        this.connect.setConnectionEPSG();
        this.inverseTranformation = false;
        String[] authority = aut.split(":");
        this.crs_source_code = code = Integer.parseInt(authority[1]);
        int numRow = this.dtm.getRowCount();
        while (numRow != 0) {
            this.dtm.removeRow(--numRow);
        }
        if (authority[0].equals("EPSG")) {
            this.callTransformationCompuesta(this.crs_source_code);
            int new_target = this.crs_target;
            int base_target = code;
            String sentence = "SELECT source_geogcrs_code, coord_ref_sys_kind FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + code;
            ResultSet result = Query.select(sentence, this.connect.getConnection());
            try {
                result.next();
                String kind = result.getString("coord_ref_sys_kind");
                if (kind.equals("projected")) {
                    base_target = result.getInt("source_geogcrs_code");
                }
            }
            catch (SQLException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
            this.crs_target = base_target;
            this.crs_source_code = new_target;
            this.callInverseTransformation(this.crs_source_code);
            this.crs_target = new_target;
            this.crs_source_code = code;
            int numr = this.dtm.getRowCount();
            if (numr > 0) {
                this.getJTable().setRowSelectionInterval(0, 0);
            }
        }
    }

    private void callTransformationCompuesta(int crsCode) {
        int numRow = this.dtm.getRowCount();
        while (numRow != 0) {
            this.dtm.removeRow(--numRow);
        }
        String sentence = "SELECT source_geogcrs_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + crsCode;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        int source = 0;
        try {
            result.next();
            source = result.getInt("source_geogcrs_code");
        }
        catch (SQLException e1) {
            LOGGER.error((Object)"", (Throwable)e1);
        }
        ResultSet result2 = null;
        ResultSet result3 = null;
        if (source != 0) {
            crsCode = source;
        }
        ArrayList<String> codecs = new ArrayList<String>();
        codecs.add(String.valueOf(crsCode));
        int j = 0;
        while (j < codecs.size()) {
            sentence = "SELECT coord_op_code, coord_op_name, coord_op_type, source_crs_code, target_crs_code, area_of_use_code, coord_op_method_code FROM epsg_coordoperation WHERE source_crs_code = " + (String)codecs.get(j) + " OR target_crs_code = " + (String)codecs.get(j);
            result = Query.select(sentence, this.connect.getConnection());
            try {
                while (result.next()) {
                    Object[] data = new Object[6];
                    data[0] = String.valueOf(result.getInt("coord_op_code"));
                    data[1] = result.getString("coord_op_name");
                    data[2] = result.getString("coord_op_type");
                    data[3] = String.valueOf(result.getInt("source_crs_code"));
                    data[4] = String.valueOf(result.getInt("target_crs_code"));
                    int aouc = Integer.parseInt(result.getString("area_of_use_code"));
                    sentence = "SELECT area_of_use FROM epsg_area WHERE area_code = " + aouc;
                    result2 = Query.select(sentence, this.connect.getConnection());
                    while (result2.next()) {
                        data[5] = result2.getString("area_of_use");
                    }
                    String coord_op_method = result.getString("coord_op_method_code");
                    sentence = "SELECT reverse_op FROM epsg_coordoperationmethod WHERE coord_op_method_code LIKE " + coord_op_method;
                    result3 = Query.select(sentence, this.connect.getConnection());
                    while (result3.next()) {
                        if (Integer.parseInt(result3.getString("reverse_op")) != 1) continue;
                        int i = 0;
                        while (i < this.transformations.length) {
                            if (coord_op_method.equals(this.transformations[i])) {
                                this.dtm.addRow(data);
                            }
                            ++i;
                        }
                    }
                }
            }
            catch (SQLException e1) {
                LOGGER.error((Object)"", (Throwable)e1);
            }
            ++j;
        }
    }

    public int getSource() {
        return this.crs_source_code;
    }

    public int getTarget() {
        return this.real_target;
    }

    public void setAuthorityTarget(String aut_target) {
        this.authority_target = aut_target;
    }

    public String getAuthorityTarget() {
        return this.authority_target;
    }

    public void fillData(String details) {
        RecentTrsPersistence trPersistence = new RecentTrsPersistence();
        TrData[] crsTrDataArray = trPersistence.getArrayOfTrData();
        int iRow = crsTrDataArray.length - 1;
        block0: while (iRow >= 0) {
            if (details.equals(String.valueOf(crsTrDataArray[iRow].getAuthority()) + ":" + crsTrDataArray[iRow].getCode() + " <--> " + crsTrDataArray[iRow].getDetails()) && crsTrDataArray[iRow].getAuthority().equals("EPSG")) {
                String code = String.valueOf(crsTrDataArray[iRow].getCode());
                int i = 0;
                while (i < this.getJTable().getRowCount()) {
                    if (code.equals((String)this.getJTable().getValueAt(i, 0))) {
                        this.getJTable().setRowSelectionInterval(i, i);
                        break block0;
                    }
                    ++i;
                }
                break;
            }
            --iRow;
        }
    }

    public void resetData() {
        if (this.dtm.getRowCount() > 0) {
            this.getJTable().setRowSelectionInterval(0, 0);
        }
    }

    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(I18N.getString("org.gvsig.crs.gui.panels.TransformationEpsgPanel.EPSG-transformation"));
        return m_viewinfo;
    }
}

