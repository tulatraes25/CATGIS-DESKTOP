/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.panels.CrsAndTransformationRecentsPanel;
import org.gvsig.crs.gui.panels.CrsRecentsPanel;
import org.gvsig.crs.gui.panels.EPSGpanel;
import org.gvsig.crs.gui.panels.ESRIpanel;
import org.gvsig.crs.gui.panels.IAU2000panel;
import org.gvsig.crs.gui.panels.NewCRSPanel;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.core.util.DialogManager;
import org.saig.jump.lang.I18N;

public class CRSMainPanel
extends JPanel
implements IWindow {
    private static final long serialVersionUID = 1L;
    private JComboBox jComboOptions = null;
    private JPanel Combopanel = null;
    private JPanel USGSpanel = null;
    public static final String RECENTS_KEY = I18N.getString("org.gvsig.crs.gui.CRSMainPanel.recents");
    public static final String EPSG_KEY = "EPSG";
    public static final String USGS_KEY = "USGS";
    public static final String ESRI_KEY = "ESRI";
    public static final String IAU2000_KEY = "IAU2000";
    public static final String NEW_CRS_KEY = I18N.getString("org.gvsig.crs.gui.CRSMainPanel.new-coordinate-reference-system");
    String[] selection = new String[]{RECENTS_KEY, "EPSG", "IAU2000", NEW_CRS_KEY};
    boolean inAnApplet = true;
    public CrsRecentsPanel crsRecentsPanel = null;
    public CrsAndTransformationRecentsPanel crsAndTransformationRecentsPanel = null;
    public EPSGpanel epsgPanel = null;
    public ESRIpanel esriPanel = null;
    public IAU2000panel iauPanel = null;
    public NewCRSPanel newCrsPanel = null;
    private JPanel jPanelMain = null;
    private JPanel jPanelButtons;
    private JButton jButtonAccept;
    private JButton jButonCancel;
    String dataSource = "";
    private ICrs viewCrs;

    public CRSMainPanel(ICrs crs) {
        this.viewCrs = crs;
        this.setLayout(new GridBagLayout());
        this.crsRecentsPanel = new CrsRecentsPanel();
        this.epsgPanel = new EPSGpanel();
        this.esriPanel = new ESRIpanel();
        this.iauPanel = new IAU2000panel();
        this.newCrsPanel = new NewCRSPanel(this.viewCrs);
        FormUtils.addRowInGBL(this, 0, 0, this.vista());
        FormUtils.addRowInGBL(this, 1, 0, this.getJPanelButtons());
        FormUtils.addFiller(this, 2, 0);
        this.setDataSource(this.selection[0]);
    }

    public CRSMainPanel(int target, ICrs crs) {
        this.viewCrs = crs;
        this.crsAndTransformationRecentsPanel = new CrsAndTransformationRecentsPanel();
        this.epsgPanel = new EPSGpanel();
        this.esriPanel = new ESRIpanel();
        this.iauPanel = new IAU2000panel();
        this.newCrsPanel = new NewCRSPanel(this.viewCrs);
        this.setDataSource(this.selection[0]);
    }

    public JPanel capa() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(550, 320));
        p.setLayout(new GridLayout(0, 1));
        p.setLayout(new FlowLayout(0, 10, 10));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(I18N.getString("org.gvsig.crs.gui.CRSMainPanel.select-layer-coordinate-reference-system")), BorderFactory.createEmptyBorder(1, 1, 1, 1)), p.getBorder()));
        p.add(this.getCombopanel());
        p.add(this.getJPanelLayerMain());
        return p;
    }

    public JPanel vista() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(550, 320));
        p.setLayout(new GridLayout(0, 1));
        p.setLayout(new FlowLayout(0, 10, 10));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(I18N.getString("org.gvsig.crs.gui.CRSMainPanel.select-view-coordinate-reference-system")), BorderFactory.createEmptyBorder(2, 2, 2, 2)), p.getBorder()));
        p.add(this.getCombopanel());
        p.add(this.getJPanelMain());
        return p;
    }

    public JPanel getJPanelMain() {
        if (this.jPanelMain == null) {
            this.jPanelMain = new JPanel();
            this.jPanelMain.setLayout(new CardLayout());
            this.jPanelMain.setPreferredSize(new Dimension(525, 230));
            this.jPanelMain.add(RECENTS_KEY, this.crsRecentsPanel);
            this.jPanelMain.add(EPSG_KEY, this.epsgPanel);
            this.jPanelMain.add(ESRI_KEY, this.esriPanel);
            this.jPanelMain.add(USGS_KEY, this.getJPanelUSGS());
            this.jPanelMain.add(IAU2000_KEY, this.iauPanel);
            this.jPanelMain.add(NEW_CRS_KEY, this.newCrsPanel);
        }
        return this.jPanelMain;
    }

    public JPanel getJPanelLayerMain() {
        if (this.jPanelMain == null) {
            this.jPanelMain = new JPanel();
            this.jPanelMain.setLayout(new CardLayout());
            this.jPanelMain.setPreferredSize(new Dimension(525, 230));
            this.jPanelMain.add(RECENTS_KEY, this.crsAndTransformationRecentsPanel);
            this.jPanelMain.add(EPSG_KEY, this.epsgPanel);
            this.jPanelMain.add(ESRI_KEY, this.esriPanel);
            this.jPanelMain.add(USGS_KEY, this.getJPanelUSGS());
            this.jPanelMain.add(IAU2000_KEY, this.iauPanel);
            this.jPanelMain.add(NEW_CRS_KEY, this.newCrsPanel);
        }
        return this.jPanelMain;
    }

    public JPanel getCombopanel() {
        if (this.Combopanel == null) {
            this.Combopanel = new JPanel();
            this.Combopanel.setPreferredSize(new Dimension(525, 30));
            this.Combopanel.add(this.getJLabelTipo());
            this.Combopanel.add(this.getJComboOptions());
        }
        return this.Combopanel;
    }

    private JLabel getJLabelTipo() {
        JLabel jLabelTipo = new JLabel();
        jLabelTipo.setPreferredSize(new Dimension(50, 25));
        jLabelTipo.setText(String.valueOf(CRSI18NConstants.TYPE_KEY) + ":");
        return jLabelTipo;
    }

    public JComboBox getJComboOptions() {
        if (this.jComboOptions == null) {
            this.jComboOptions = new JComboBox<String>(this.selection);
            this.jComboOptions.setPreferredSize(new Dimension(100, 25));
            this.jComboOptions.setEditable(false);
            this.jComboOptions.setSelectedIndex(0);
        }
        return this.jComboOptions;
    }

    public JPanel getJPanelUSGS() {
        if (this.USGSpanel == null) {
            this.USGSpanel = new JPanel();
            this.USGSpanel.setLayout(new GridLayout(3, 4));
            this.USGSpanel.setLayout(new FlowLayout(0, 10, 10));
            this.USGSpanel.setPreferredSize(new Dimension(525, 400));
        }
        return this.USGSpanel;
    }

    private JPanel getJPanelButtons() {
        if (this.jPanelButtons == null) {
            this.jPanelButtons = new JPanel();
            this.jPanelButtons.setLayout(new FlowLayout(2));
            this.jPanelButtons.setPreferredSize(new Dimension(525, 50));
            this.jPanelButtons.add((Component)this.getJButtonCancel(), null);
            this.jPanelButtons.add((Component)this.getJButtonAccept(), null);
        }
        return this.jPanelButtons;
    }

    public JButton getJButtonCancel() {
        if (this.jButonCancel == null) {
            this.jButonCancel = new JButton();
            this.jButonCancel.setText(CRSI18NConstants.CANCEL_KEY);
            this.jButonCancel.setPreferredSize(new Dimension(100, 25));
            this.jButonCancel.setMnemonic('C');
            this.jButonCancel.setToolTipText(CRSI18NConstants.CANCEL_KEY);
        }
        return this.jButonCancel;
    }

    public void cancelButton_actionPerformed(ActionEvent e) {
        DialogManager.closeJDialog(this);
    }

    public JButton getJButtonAccept() {
        if (this.jButtonAccept == null) {
            this.jButtonAccept = new JButton();
            this.jButtonAccept.setText(CRSI18NConstants.ACCEPT_KEY);
            this.jButtonAccept.setPreferredSize(new Dimension(100, 25));
            this.jButtonAccept.setEnabled(false);
            this.jButtonAccept.setMnemonic('A');
            this.jButtonAccept.setToolTipText(CRSI18NConstants.ACCEPT_KEY);
        }
        return this.jButtonAccept;
    }

    public ICrs getProjection() {
        if (this.getDataSource().equals(EPSG_KEY)) {
            return this.epsgPanel.getProjection();
        }
        if (this.getDataSource().equals(IAU2000_KEY)) {
            return this.iauPanel.getProjection();
        }
        if (this.getDataSource().equals(RECENTS_KEY)) {
            return this.crsRecentsPanel.getProjection();
        }
        if (this.getDataSource().equals(ESRI_KEY)) {
            return this.esriPanel.getProjection();
        }
        if (this.getDataSource().equals(NEW_CRS_KEY)) {
            return this.newCrsPanel.getProjection();
        }
        return null;
    }

    public void setProjection(IProjection crs) {
    }

    public void setDataSource(String sour) {
        this.dataSource = sour;
    }

    public String getDataSource() {
        return this.dataSource;
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(I18N.getString("org.gvsig.crs.gui.CRSMainPanel.select-the-spatial-reference-system"));
        return m_viewinfo;
    }

    public EPSGpanel getEpsgPanel() {
        return this.epsgPanel;
    }

    public ESRIpanel getEsriPanel() {
        return this.esriPanel;
    }

    public IAU2000panel getIauPanel() {
        return this.iauPanel;
    }

    public CrsRecentsPanel getRecentsPanel2() {
        return this.crsRecentsPanel;
    }

    public CrsAndTransformationRecentsPanel getCrsAndTransformationRecentsPanel() {
        return this.crsAndTransformationRecentsPanel;
    }

    public NewCRSPanel getNewCrsPanel() {
        return this.newCrsPanel;
    }
}

