/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import es.kosmo.core.crs.CrsRepositoryManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CRSI18NConstants;
import org.gvsig.crs.CrsException;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.CRSMainPanel;
import org.gvsig.crs.gui.listeners.CRSMainTrPanelListener;
import org.gvsig.crs.gui.panels.TransformationCapaPanel;
import org.gvsig.crs.gui.panels.TransformationEpsgPanel;
import org.gvsig.crs.gui.panels.TransformationManualPanel;
import org.gvsig.crs.gui.panels.TransformationNadgridsPanel;
import org.gvsig.crs.gui.panels.TransformationRecentsPanel;
import org.gvsig.crs.gui.panels.TransformationVistaPanel;
import org.saig.jump.lang.I18N;

public class CRSMainTrPanel
extends JPanel
implements IWindow {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CRSMainTrPanel.class);
    boolean tra = false;
    boolean inAnApplet = true;
    private boolean cancelYes = false;
    private JButton jButtonCancel = null;
    private JButton jButtonNext = null;
    private JButton jButtonAccept = null;
    private JButton jButtonBefore = null;
    private JPanel jPanelButtons = null;
    private JLabel jLabelTrans = null;
    boolean targetNad = false;
    String crs_target = null;
    CrsWkt crsWkt_target = null;
    int transformation_code = 0;
    CRSMainPanel viewPan = null;
    private JPanel jPanelMain = null;
    private TransformationManualPanel manualTrPanel = null;
    private boolean manual_tr = false;
    private TransformationEpsgPanel epsgTrPanel = null;
    private boolean epsg_tr = false;
    private TransformationNadgridsPanel nadsTrPanel = null;
    private boolean nads_tr = false;
    private TransformationRecentsPanel recentsTrPanel = null;
    private boolean recents_tr = false;
    private TransformationVistaPanel vistaTrPanel = null;
    private boolean vista_tr = false;
    private TransformationCapaPanel capaTrPanel = null;
    private boolean capa_tr = false;
    private boolean compuesta_tr = false;
    private boolean sin_tr = false;
    private JPanel jPanelCombo = null;
    private JComboBox jComboOptions = null;
    private IProjection crsfirst;
    private CRSMainPanel crsMainPanel;
    private String newSelection;
    String dataSource = "";
    ICrs curProj = null;

    public CRSMainTrPanel(int target, CrsWkt crsWkttarget, ICrs proj) {
        this.curProj = proj;
        this.viewPan = this.crsMainPanel = new CRSMainPanel(target, this.curProj);
        this.crs_target = String.valueOf(crsWkttarget.getAuthority()[0]) + ":" + crsWkttarget.getAuthority()[1];
        this.setCrsWkt_target(crsWkttarget);
        this.manualTrPanel = new TransformationManualPanel();
        this.epsgTrPanel = new TransformationEpsgPanel(this.crs_target);
        this.nadsTrPanel = new TransformationNadgridsPanel(true);
        this.recentsTrPanel = new TransformationRecentsPanel();
        this.capaTrPanel = new TransformationCapaPanel(this.crs_target, this);
        this.vistaTrPanel = new TransformationVistaPanel(this.crs_target, this);
        this.jPanelMain = new JPanel();
        this.jPanelMain.setLayout(new CardLayout());
        this.jPanelMain.add("primero", this.viewPanel());
        this.jPanelMain.add("manual", this.manualTrPanel);
        this.jPanelMain.add("epsg", this.epsgTrPanel);
        this.jPanelMain.add("nad", this.nadsTrPanel);
        this.jPanelMain.add("recents", this.recentsTrPanel);
        this.jPanelMain.add("vista", this.vistaTrPanel);
        this.jPanelMain.add("capa", this.capaTrPanel);
        this.setLayout(new BorderLayout());
        this.add((Component)this.jPanelMain, "Center");
        this.add((Component)this.getButtons(), "South");
        this.setListeners();
        this.setDataSource(this.crsMainPanel.getDataSource());
        this.crsMainPanel.getCrsAndTransformationRecentsPanel().loadRecents(crsWkttarget, proj);
    }

    private JPanel viewPanel() {
        JPanel integro = new JPanel();
        integro.setLayout(new BorderLayout());
        integro.setBorder(BorderFactory.createTitledBorder(CRSI18NConstants.TRANSFORMATIONS_KEY));
        integro.add((Component)this.viewPan.capa(), "Center");
        integro.add((Component)this.getCombopanel(), "South");
        return integro;
    }

    public JPanel getCombopanel() {
        if (this.jPanelCombo == null) {
            this.jPanelCombo = new JPanel();
            this.jPanelCombo.setLayout(new FlowLayout(0, 10, 10));
            this.jPanelCombo.add(this.getJLabelTrans());
            this.jPanelCombo.add(this.getJComboOptions());
        }
        return this.jPanelCombo;
    }

    private JLabel getJLabelTrans() {
        if (this.jLabelTrans == null) {
            this.jLabelTrans = new JLabel();
            this.jLabelTrans.setPreferredSize(new Dimension(180, 25));
            this.jLabelTrans.setText(I18N.getString("org.gvsig.crs.gui.CRSMainTrPanel.select-transformation"));
        }
        return this.jLabelTrans;
    }

    public JComboBox getJComboOptions() {
        if (this.jComboOptions == null) {
            String[] selection = new String[]{CRSI18NConstants.WITHOUT_TRANSFORMATION_KEY, CRSI18NConstants.RECENT_TRANSFORMATIONS_KEY, CRSI18NConstants.EPSG_TRANSFORMATION_KEY, CRSI18NConstants.CUSTOM_TRANSFORMATION_KEY, CRSI18NConstants.GRIDS_KEY};
            this.jComboOptions = new JComboBox<String>(selection);
            this.jComboOptions.setPreferredSize(new Dimension(180, 25));
            this.jComboOptions.setEnabled(false);
            this.jComboOptions.setEditable(false);
            this.jComboOptions.setSelectedIndex(0);
            this.newSelection = (String)this.jComboOptions.getSelectedItem();
        }
        return this.jComboOptions;
    }

    private JPanel getButtons() {
        if (this.jPanelButtons == null) {
            this.jPanelButtons = new JPanel();
            this.jPanelButtons.setLayout(new FlowLayout(2, 10, 10));
            this.jPanelButtons.add(this.getJButtonCancel());
            this.jPanelButtons.add(this.getJButtonBefore());
            this.jPanelButtons.add(this.getJButtonNext());
            this.jPanelButtons.add(this.getJButtonAccept());
        }
        return this.jPanelButtons;
    }

    public JButton getJButtonBefore() {
        if (this.jButtonBefore == null) {
            this.jButtonBefore = new JButton(CRSI18NConstants.PREVIOUS_KEY);
            this.jButtonBefore.setMnemonic('B');
            this.jButtonBefore.setPreferredSize(new Dimension(100, 25));
            this.jButtonBefore.setEnabled(false);
        }
        return this.jButtonBefore;
    }

    public JButton getJButtonAccept() {
        if (this.jButtonAccept == null) {
            this.jButtonAccept = new JButton(CRSI18NConstants.FINISH_KEY);
            this.jButtonAccept.setMnemonic('O');
            this.jButtonAccept.setVisible(true);
            this.jButtonAccept.setEnabled(false);
            this.jButtonAccept.setPreferredSize(new Dimension(100, 25));
        }
        return this.jButtonAccept;
    }

    public JButton getJButtonNext() {
        if (this.jButtonNext == null) {
            this.jButtonNext = new JButton(CRSI18NConstants.NEXT_KEY);
            this.jButtonNext.setMnemonic('S');
            this.jButtonNext.setVisible(true);
            this.jButtonNext.setPreferredSize(new Dimension(100, 25));
        }
        return this.jButtonNext;
    }

    public JButton getJButtonCancel() {
        if (this.jButtonCancel == null) {
            this.jButtonCancel = new JButton(CRSI18NConstants.CANCEL_KEY);
            this.jButtonCancel.setMnemonic('C');
            this.jButtonCancel.setPreferredSize(new Dimension(100, 25));
        }
        return this.jButtonCancel;
    }

    public void setTargetNad(boolean tarNad) {
        this.targetNad = tarNad;
    }

    public boolean getTargetNad() {
        return this.targetNad;
    }

    public ICrs getProjection() {
        if (this.sin_tr) {
            ICrs crs;
            block9: {
                try {
                    if (this.getDataSource().equals("EPSG")) {
                        crs = CrsRepositoryManager.getInstance().getCRS("EPSG:" + this.crsMainPanel.getEpsgPanel().getCodeCRS());
                        break block9;
                    }
                    if (this.getDataSource().equals("IAU2000")) {
                        crs = CrsRepositoryManager.getInstance().getCRS("IAU2000:" + this.crsMainPanel.getIauPanel().getCodeCRS());
                        break block9;
                    }
                    if (this.getDataSource().equals(CRSI18NConstants.RECENTS_KEY)) {
                        crs = CrsRepositoryManager.getInstance().getCRS(this.crsMainPanel.getCrsAndTransformationRecentsPanel().getAuthority());
                        break block9;
                    }
                    if (this.getDataSource().equals("ESRI")) {
                        crs = CrsRepositoryManager.getInstance().getCRS("ESRI:" + this.crsMainPanel.getEsriPanel().getCodeCRS());
                        break block9;
                    }
                    if (this.getDataSource().equals(CRSI18NConstants.USER_CRS_KEY)) {
                        crs = CrsRepositoryManager.getInstance().getCRS("USR:" + this.crsMainPanel.getNewCrsPanel().getCodeCRS());
                        break block9;
                    }
                    this.sin_tr = false;
                    return null;
                }
                catch (CrsException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            this.sin_tr = false;
            return crs;
        }
        return (ICrs)this.crsfirst;
    }

    public void setProjection(IProjection proj) {
        this.crsfirst = proj;
    }

    private void setListeners() {
        CRSMainTrPanelListener listener = new CRSMainTrPanelListener(this);
        this.jButtonAccept.addActionListener(listener);
        this.jButtonBefore.addActionListener(listener);
        this.jButtonCancel.addActionListener(listener);
        this.jButtonNext.addActionListener(listener);
        this.crsMainPanel.getJComboOptions().addItemListener(listener);
        this.getJComboOptions().addActionListener(listener);
        this.getRecentsTrPanel().getJButtonInfo().addActionListener(listener);
        ListSelectionModel rowSM = this.crsMainPanel.getEpsgPanel().getJTable().getSelectionModel();
        rowSM.addListSelectionListener(listener);
        ListSelectionModel rowSMiau = this.crsMainPanel.getIauPanel().getJTable().getSelectionModel();
        rowSMiau.addListSelectionListener(listener);
        ListSelectionModel rowSMrecents = this.crsMainPanel.getCrsAndTransformationRecentsPanel().getJTable().getSelectionModel();
        rowSMrecents.addListSelectionListener(listener);
        ListSelectionModel rowSMesri = this.crsMainPanel.getEsriPanel().getJTable().getSelectionModel();
        rowSMesri.addListSelectionListener(listener);
        ListSelectionModel rowSMusr = this.crsMainPanel.getNewCrsPanel().getJTable().getSelectionModel();
        rowSMusr.addListSelectionListener(listener);
        this.crsMainPanel.getEsriPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getEpsgPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getCrsAndTransformationRecentsPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getIauPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getNewCrsPanel().getJTable().addMouseListener(listener);
        this.getEpsgTrPanel().getJTable().addMouseListener(listener);
        this.getRecentsTrPanel().getJTable().addMouseListener(listener);
        ListSelectionModel rowSMEpsgTr = this.getEpsgTrPanel().getJTable().getSelectionModel();
        rowSMEpsgTr.addListSelectionListener(listener);
        ListSelectionModel rowSMRecentsTr = this.getRecentsTrPanel().getJTable().getSelectionModel();
        rowSMRecentsTr.addListSelectionListener(listener);
        this.getNadsTrPanel().getJComboNadFile().addItemListener(listener);
        this.getManualTrPanel().getTx_Translation().addKeyListener(listener);
        this.getManualTrPanel().getTy_Translation().addKeyListener(listener);
        this.getManualTrPanel().getTz_Translation().addKeyListener(listener);
        this.getManualTrPanel().getTx_Rotation().addKeyListener(listener);
        this.getManualTrPanel().getTy_Rotation().addKeyListener(listener);
        this.getManualTrPanel().getTz_Rotation().addKeyListener(listener);
        this.getManualTrPanel().getTscale().addKeyListener(listener);
    }

    public void setDataSource(String sour) {
        this.dataSource = sour;
    }

    public String getDataSource() {
        return this.crsMainPanel.getDataSource();
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(I18N.getString("org.gvsig.crs.gui.CRSMainTrPanel.crs-view"));
        return m_viewinfo;
    }

    public CRSMainPanel getCrsMainPanel() {
        return this.crsMainPanel;
    }

    public TransformationEpsgPanel getEpsgTrPanel() {
        return this.epsgTrPanel;
    }

    public TransformationManualPanel getManualTrPanel() {
        return this.manualTrPanel;
    }

    public TransformationNadgridsPanel getNadsTrPanel() {
        return this.nadsTrPanel;
    }

    public TransformationRecentsPanel getRecentsTrPanel() {
        return this.recentsTrPanel;
    }

    public boolean isEpsg_tr() {
        return this.epsg_tr;
    }

    public boolean isManual_tr() {
        return this.manual_tr;
    }

    public boolean isNads_tr() {
        return this.nads_tr;
    }

    public boolean isRecents_tr() {
        return this.recents_tr;
    }

    public boolean isSin_tr() {
        return this.sin_tr;
    }

    public void setEpsg_tr(boolean epsg_tr) {
        this.epsg_tr = epsg_tr;
    }

    public void setManual_tr(boolean manual_tr) {
        this.manual_tr = manual_tr;
    }

    public void setNads_tr(boolean nads_tr) {
        this.nads_tr = nads_tr;
    }

    public void setRecents_tr(boolean recents_tr) {
        this.recents_tr = recents_tr;
    }

    public void setSin_tr(boolean sin_tr) {
        this.sin_tr = sin_tr;
    }

    public boolean isCancelYes() {
        return this.cancelYes;
    }

    public void setCancelYes(boolean cancelYes) {
        this.cancelYes = cancelYes;
    }

    public IProjection getCrsfirst() {
        return this.crsfirst;
    }

    public JPanel getJPanelMain() {
        return this.jPanelMain;
    }

    public String getNewSelection() {
        return this.newSelection;
    }

    public void setNewSelection(String newSelection) {
        this.newSelection = newSelection;
    }

    public CrsWkt getCrsWkt_target() {
        return this.crsWkt_target;
    }

    public void setCrsWkt_target(CrsWkt crsWkt_target) {
        this.crsWkt_target = crsWkt_target;
    }

    public TransformationVistaPanel getVistaTrPanel() {
        return this.vistaTrPanel;
    }

    public TransformationCapaPanel getCapaTrPanel() {
        return this.capaTrPanel;
    }

    public boolean isVista_tr() {
        return this.vista_tr;
    }

    public boolean isCapa_tr() {
        return this.capa_tr;
    }

    public boolean isCompuesta_tr() {
        return this.compuesta_tr;
    }

    public void setCapa_tr(boolean capa_tr) {
        this.capa_tr = capa_tr;
    }

    public void setCompuesta_tr(boolean compuesta_tr) {
        this.compuesta_tr = compuesta_tr;
    }

    public void setVista_tr(boolean vista_tr) {
        this.vista_tr = vista_tr;
    }

    public Object getWindowProfile() {
        return 8;
    }
}

