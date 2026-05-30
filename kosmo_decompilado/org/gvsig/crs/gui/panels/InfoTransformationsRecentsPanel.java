/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.gui.panels;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.gui.TableSorter;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.Query;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.DialogManager;
import org.saig.jump.lang.I18N;

public class InfoTransformationsRecentsPanel
extends JPanel
implements IWindow,
ActionListener {
    private static final Logger LOGGER = Logger.getLogger(InfoTransformationsRecentsPanel.class);
    private static final long serialVersionUID = 1L;
    private JTable jTable;
    public DefaultTableModel dtm = null;
    private JScrollPane jScrollPane1 = null;
    private JPanel jPanelbuttons;
    private JButton jButtonOk;
    public TableSorter sorter = null;
    String[] data = null;

    public InfoTransformationsRecentsPanel(String[] data) {
        this.data = data;
        this.initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        FormUtils.addRowInGBL(this, 0, 0, this.getJScrollPane1());
        FormUtils.addRowInGBL(this, 1, 0, this.getJPanelButtons());
        FormUtils.addFiller(this, 2, 0);
    }

    private JPanel getJPanelButtons() {
        if (this.jPanelbuttons == null) {
            this.jPanelbuttons = new JPanel();
            this.jPanelbuttons.setLayout(new FlowLayout(2));
            this.jPanelbuttons.setPreferredSize(new Dimension(400, 50));
            this.jPanelbuttons.add((Component)this.getJButtonOk(), null);
        }
        return this.jPanelbuttons;
    }

    private JButton getJButtonOk() {
        if (this.jButtonOk == null) {
            this.jButtonOk = new JButton();
            this.jButtonOk.setText(CRSI18NConstants.ACCEPT_KEY);
            this.jButtonOk.setPreferredSize(new Dimension(100, 25));
            this.jButtonOk.setMnemonic('O');
            this.jButtonOk.setToolTipText(CRSI18NConstants.ACCEPT_KEY);
            this.jButtonOk.addActionListener(this);
        }
        return this.jButtonOk;
    }

    private JScrollPane getJScrollPane1() {
        if (this.jScrollPane1 == null) {
            this.jScrollPane1 = new JScrollPane();
            this.jScrollPane1.setPreferredSize(new Dimension(400, 150));
            this.jScrollPane1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(CRSI18NConstants.TRANSFORMATION_INFO_KEY), BorderFactory.createEmptyBorder(5, 5, 5, 5)), this.jScrollPane1.getBorder()));
            this.jScrollPane1.setViewportView(this.getJTable());
        }
        return this.jScrollPane1;
    }

    private JTable getJTable() {
        if (this.jTable == null) {
            Object[] columnNames = new String[]{CRSI18NConstants.NAME_KEY, CRSI18NConstants.VALUE_KEY};
            Object[][] datos = this.obtainData();
            this.dtm = new DefaultTableModel(datos, columnNames){
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            this.sorter = new TableSorter(this.dtm);
            this.jTable = new JTable(this.sorter);
            this.jTable.setCellSelectionEnabled(false);
            this.jTable.setRowSelectionAllowed(true);
            this.jTable.setColumnSelectionAllowed(false);
            this.jTable.setSelectionMode(0);
        }
        return this.jTable;
    }

    private String[][] obtainData() {
        String[][] valid = null;
        String[] transformation = this.data[0].split(":");
        if (transformation[0].equals("EPSG")) {
            valid = new String[5][2];
            valid[0][0] = CRSI18NConstants.SOURCE_CRS_KEY;
            valid[0][1] = this.data[2];
            valid[1][0] = CRSI18NConstants.TARGET_CRS_KEY;
            valid[1][1] = this.data[3];
            CRSRepositoryConnection conn = new CRSRepositoryConnection();
            conn.setConnectionEPSG();
            String sentence = "SELECT area_of_use_code FROM epsg_coordoperation WHERE coord_op_code = " + transformation[1];
            ResultSet result = Query.select(sentence, conn.getConnection());
            try {
                result.next();
                valid[2][0] = I18N.getString("org.gvsig.crs.gui.panels.InfoTransformationsRecentsPanel.transformation-code");
                valid[2][1] = transformation[1];
                sentence = "SELECT area_of_use FROM epsg_area WHERE area_code = " + Integer.parseInt(result.getString("area_of_use_code"));
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            valid[3][0] = I18N.getString("org.gvsig.crs.gui.panels.InfoTransformationsRecentsPanel.transformation-name");
            valid[3][1] = this.data[1];
            valid[4][0] = CRSI18NConstants.DETAILS_KEY;
            result = Query.select(sentence, conn.getConnection());
            try {
                result.next();
                valid[4][1] = result.getString("area_of_use");
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        } else if (transformation[0].equals("USR")) {
            valid = new String[3][2];
            valid[0][0] = CRSI18NConstants.SOURCE_CRS_KEY;
            valid[0][1] = this.data[2];
            valid[1][0] = CRSI18NConstants.TARGET_CRS_KEY;
            valid[1][1] = this.data[3];
            valid[2][0] = CRSI18NConstants.DETAILS_KEY;
            valid[2][1] = this.data[4];
        } else {
            valid = new String[4][2];
            String[] partes = this.data[4].split("\\(");
            String nadFile = partes[0];
            String codigoNad = partes[1].substring(0, partes[1].length() - 1);
            valid[0][0] = CRSI18NConstants.SOURCE_CRS_KEY;
            valid[0][1] = this.data[2];
            valid[1][0] = CRSI18NConstants.TARGET_CRS_KEY;
            valid[1][1] = this.data[3];
            valid[2][0] = I18N.getString("org.gvsig.crs.gui.panels.InfoTransformationsRecentsPanel.grids-file");
            valid[2][1] = nadFile;
            valid[3][0] = I18N.getString("org.gvsig.crs.gui.panels.InfoTransformationsRecentsPanel.calculated-in");
            valid[3][1] = codigoNad;
        }
        return valid;
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(CRSI18NConstants.TRANSFORMATION_INFO_KEY);
        return m_viewinfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getJButtonOk()) {
            DialogManager.closeJDialog(this);
        }
    }
}

