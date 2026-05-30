/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui.panels;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsFactory;
import org.gvsig.crs.ICrs;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.DialogManager;
import org.saig.jump.lang.I18N;

public class InfoCRSPanel
extends JPanel
implements IWindow,
ActionListener {
    private static final long serialVersionUID = 1L;
    private JPanel panelLabels;
    private JTable jTable;
    public DefaultTableModel dtm = null;
    private JScrollPane jScrollPane1 = null;
    private JButton jButtonOk;
    private ICrs proj;
    private JLabel jLabelProjcs;
    private JLabel jLabelGeogcs;
    private JLabel jLabelDatum;
    private JLabel jLabelSpheroid;
    private JLabel jLabelPrimem;
    private JLabel jLabelProjection;
    private JLabel jLabelUnits;
    private JLabel jLabelUnits_p;
    private JLabel jLabelProjcsdinamic;
    private JLabel jLabelGeogcsdinamic;
    private JLabel jLabelDatumdinamic;
    private JLabel jLabelSpheroiddinamic;
    private JLabel jLabelPrimemdinamic;
    private JLabel jLabelProjectiondinamic;
    private JLabel jLabelUnitsdinamic;
    private JLabel jLabelUnits_pdinamic;

    public InfoCRSPanel(ICrs p) {
        super(new GridBagLayout(), false);
        this.proj = p;
        this.initialize();
    }

    public InfoCRSPanel(String fuente, int codigo) {
        super(new GridBagLayout(), false);
        try {
            this.proj = new CrsFactory().getCRS(String.valueOf(fuente) + ":" + codigo);
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
        this.initialize();
    }

    public InfoCRSPanel(String fuente, int codigo, String trans) {
        super(new GridBagLayout(), false);
        try {
            this.proj = new CrsFactory().getCRS(String.valueOf(fuente) + ":" + codigo);
        }
        catch (CrsException e) {
            e.printStackTrace();
        }
        this.initialize();
    }

    private void initialize() {
        FormUtils.addRowInGBL(this, 0, 0, this.getPanel());
        FormUtils.addFiller(this, 1, 0);
    }

    private JPanel getPanel() {
        if (this.panelLabels == null) {
            this.panelLabels = new JPanel(new GridBagLayout());
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 0, 0, this.getJLabelProjcs(), (JComponent)this.getJLabelProjcsDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 1, 0, this.getJLabelGeogcs(), (JComponent)this.getJLabelGeogcsDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 2, 0, this.getJLabelDatum(), (JComponent)this.getJLabelDatumDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 3, 0, this.getJLabelSpheroid(), (JComponent)this.getJLabelSpheroidDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 4, 0, this.getJLabelPrimen(), (JComponent)this.getJLabelPrimenDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 5, 0, this.getJLabelUnits(), (JComponent)this.getJLabelUnitsDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 6, 0, this.getJLabelProjection(), (JComponent)this.getJLabelProjectionDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 7, 0, this.getJLabelUnits_p(), (JComponent)this.getJLabelUnits_pDinamic(), true);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 8, 0, (JComponent)this.getJScrollPane1(), true, false);
            FormUtils.addRowInGBL((JComponent)this.panelLabels, 9, 0, (JComponent)this.createOKcancelPanel(), true, false);
            FormUtils.addFiller(this.panelLabels, 10, 0);
        }
        return this.panelLabels;
    }

    private OKCancelPanel createOKcancelPanel() {
        OKCancelPanel okCancelPanel = new OKCancelPanel();
        okCancelPanel.setCancelVisible(false);
        okCancelPanel.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent arg0) {
                DialogManager.closeJDialog(InfoCRSPanel.this);
            }
        });
        return okCancelPanel;
    }

    private JButton getJButtonOk() {
        if (this.jButtonOk == null) {
            this.jButtonOk = new JButton();
            this.jButtonOk.setText(CRSI18NConstants.ACCEPT_KEY);
            this.jButtonOk.setMnemonic('O');
            this.jButtonOk.setToolTipText(CRSI18NConstants.ACCEPT_KEY);
            this.jButtonOk.addActionListener(this);
        }
        return this.jButtonOk;
    }

    private JLabel getJLabelProjcs() {
        if (this.jLabelProjcs == null) {
            this.jLabelProjcs = new JLabel();
            this.jLabelProjcs.setPreferredSize(new Dimension(75, 20));
            this.jLabelProjcs.setFont(new Font("Projcs:", 1, 10));
            this.jLabelProjcs.setText("Projcs:");
        }
        return this.jLabelProjcs;
    }

    private JLabel getJLabelProjcsDinamic() {
        if (this.jLabelProjcsdinamic == null) {
            this.jLabelProjcsdinamic = new JLabel();
            this.jLabelProjcsdinamic.setPreferredSize(new Dimension(150, 20));
            this.jLabelProjcsdinamic.setFont(new Font("", 0, 10));
            this.jLabelProjcsdinamic.setText(this.proj.getCrsWkt().getProjcs());
        }
        return this.jLabelProjcsdinamic;
    }

    private JLabel getJLabelGeogcs() {
        if (this.jLabelGeogcs == null) {
            this.jLabelGeogcs = new JLabel();
            this.jLabelGeogcs.setPreferredSize(new Dimension(75, 20));
            this.jLabelGeogcs.setFont(new Font("Geogcs:", 1, 10));
            this.jLabelGeogcs.setText("Geogcs:");
        }
        return this.jLabelGeogcs;
    }

    private JLabel getJLabelGeogcsDinamic() {
        if (this.jLabelGeogcsdinamic == null) {
            this.jLabelGeogcsdinamic = new JLabel();
            this.jLabelGeogcsdinamic.setPreferredSize(new Dimension(150, 20));
            this.jLabelGeogcsdinamic.setFont(new Font("", 0, 10));
            this.jLabelGeogcsdinamic.setText(this.proj.getCrsWkt().getGeogcs());
        }
        return this.jLabelGeogcsdinamic;
    }

    private JLabel getJLabelDatum() {
        if (this.jLabelDatum == null) {
            this.jLabelDatum = new JLabel();
            this.jLabelDatum.setPreferredSize(new Dimension(75, 20));
            this.jLabelDatum.setFont(new Font("Datum:", 1, 10));
            this.jLabelDatum.setText("Datum:");
        }
        return this.jLabelDatum;
    }

    private JLabel getJLabelDatumDinamic() {
        if (this.jLabelDatumdinamic == null) {
            this.jLabelDatumdinamic = new JLabel();
            this.jLabelDatumdinamic.setPreferredSize(new Dimension(150, 20));
            this.jLabelDatumdinamic.setFont(new Font("", 0, 10));
            this.jLabelDatumdinamic.setText(this.proj.getCrsWkt().getDatumName());
        }
        return this.jLabelDatumdinamic;
    }

    private JLabel getJLabelSpheroid() {
        if (this.jLabelSpheroid == null) {
            this.jLabelSpheroid = new JLabel();
            this.jLabelSpheroid.setPreferredSize(new Dimension(75, 20));
            this.jLabelSpheroid.setFont(new Font("Spheroid", 1, 10));
            this.jLabelSpheroid.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.InfoCRSPanel.spheroid")) + ":");
        }
        return this.jLabelSpheroid;
    }

    private JLabel getJLabelSpheroidDinamic() {
        if (this.jLabelSpheroiddinamic == null) {
            String[] sphe = this.proj.getCrsWkt().getSpheroid();
            this.jLabelSpheroiddinamic = new JLabel();
            this.jLabelSpheroiddinamic.setPreferredSize(new Dimension(200, 20));
            this.jLabelSpheroiddinamic.setFont(new Font("", 0, 10));
            this.jLabelSpheroiddinamic.setText(String.valueOf(sphe[0]) + " , " + sphe[1] + " , " + sphe[2]);
        }
        return this.jLabelSpheroiddinamic;
    }

    private JLabel getJLabelPrimen() {
        if (this.jLabelPrimem == null) {
            this.jLabelPrimem = new JLabel();
            this.jLabelPrimem.setPreferredSize(new Dimension(75, 20));
            this.jLabelPrimem.setFont(new Font("Primen:", 1, 10));
            this.jLabelPrimem.setText("Primen:");
        }
        return this.jLabelPrimem;
    }

    private JLabel getJLabelPrimenDinamic() {
        if (this.jLabelPrimemdinamic == null) {
            String[] pri = this.proj.getCrsWkt().getPrimen();
            this.jLabelPrimemdinamic = new JLabel();
            this.jLabelPrimemdinamic.setPreferredSize(new Dimension(150, 20));
            this.jLabelPrimemdinamic.setFont(new Font("", 0, 10));
            this.jLabelPrimemdinamic.setText(String.valueOf(pri[0]) + " , " + pri[1]);
        }
        return this.jLabelPrimemdinamic;
    }

    private JLabel getJLabelProjection() {
        if (this.jLabelProjection == null) {
            this.jLabelProjection = new JLabel();
            this.jLabelProjection.setPreferredSize(new Dimension(75, 20));
            this.jLabelProjection.setFont(new Font("Projection", 1, 10));
            this.jLabelProjection.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.InfoCRSPanel.projection")) + ":");
        }
        return this.jLabelProjection;
    }

    private JLabel getJLabelProjectionDinamic() {
        if (this.jLabelProjectiondinamic == null) {
            this.jLabelProjectiondinamic = new JLabel();
            this.jLabelProjectiondinamic.setPreferredSize(new Dimension(150, 20));
            this.jLabelProjectiondinamic.setFont(new Font("", 0, 10));
            this.jLabelProjectiondinamic.setText(this.proj.getCrsWkt().getProjection());
        }
        return this.jLabelProjectiondinamic;
    }

    private JLabel getJLabelUnits() {
        if (this.jLabelUnits == null) {
            this.jLabelUnits = new JLabel();
            this.jLabelUnits.setPreferredSize(new Dimension(75, 20));
            this.jLabelUnits.setFont(new Font("Units", 1, 10));
            this.jLabelUnits.setText(String.valueOf(I18N.getString("org.gvsig.crs.gui.panels.InfoCRSPanel.units")) + ":");
        }
        return this.jLabelUnits;
    }

    private JLabel getJLabelUnitsDinamic() {
        if (this.jLabelUnitsdinamic == null) {
            String[] units = this.proj.getCrsWkt().getUnit();
            this.jLabelUnitsdinamic = new JLabel();
            this.jLabelUnitsdinamic.setPreferredSize(new Dimension(200, 20));
            this.jLabelUnitsdinamic.setFont(new Font("", 0, 10));
            this.jLabelUnitsdinamic.setText(String.valueOf(units[0]) + " , " + units[1]);
        }
        return this.jLabelUnitsdinamic;
    }

    private JLabel getJLabelUnits_p() {
        if (this.jLabelUnits_p == null) {
            this.jLabelUnits_p = new JLabel();
            this.jLabelUnits_p.setPreferredSize(new Dimension(75, 20));
            this.jLabelUnits_p.setFont(new Font("", 1, 10));
            this.jLabelUnits_p.setText("Unidades_p:");
        }
        return this.jLabelUnits_p;
    }

    private JLabel getJLabelUnits_pDinamic() {
        if (this.jLabelUnits_pdinamic == null) {
            String[] uni_p = this.proj.getCrsWkt().getUnit_p();
            this.jLabelUnits_pdinamic = new JLabel();
            this.jLabelUnits_pdinamic.setPreferredSize(new Dimension(150, 20));
            this.jLabelUnits_pdinamic.setFont(new Font("", 0, 10));
            this.jLabelUnits_pdinamic.setText(String.valueOf(uni_p[0]) + " , " + uni_p[1]);
        }
        return this.jLabelUnits_pdinamic;
    }

    private JScrollPane getJScrollPane1() {
        if (this.jScrollPane1 == null) {
            this.jScrollPane1 = new JScrollPane();
            this.jScrollPane1.setPreferredSize(new Dimension(400, 150));
            this.jScrollPane1.setViewportView(this.getJTable());
        }
        return this.jScrollPane1;
    }

    private JTable getJTable() {
        if (this.jTable == null) {
            Object[][] data;
            Object[] columnNames = new String[]{CRSI18NConstants.NAME_KEY, CRSI18NConstants.VALUE_KEY};
            String[] param_n = this.proj.getCrsWkt().getParam_name();
            String[] param_v = this.proj.getCrsWkt().getParam_value();
            if (param_v != null) {
                data = new Object[param_v.length][2];
                int i = 0;
                while (i < 2) {
                    int j = 0;
                    while (j < param_n.length) {
                        data[j][i] = i == 0 ? param_n[j] : param_v[j];
                        ++j;
                    }
                    ++i;
                }
            } else {
                data = new Object[][]{{"", ""}};
            }
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
            this.jTable = new JTable(this.dtm);
            this.jTable.setCellSelectionEnabled(false);
            this.jTable.setRowSelectionAllowed(true);
            this.jTable.setColumnSelectionAllowed(false);
            this.jTable.setSelectionMode(0);
        }
        return this.jTable;
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(this.proj.getCrsWkt().getName());
        return m_viewinfo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.getJButtonOk()) {
            DialogManager.closeJDialog(this);
        }
    }
}

