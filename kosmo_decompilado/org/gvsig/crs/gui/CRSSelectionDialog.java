/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.cresques.cts.IProjection;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.CRSMainPanel;
import org.gvsig.crs.gui.listeners.CRSSelectionDialogListener;
import org.gvsig.crs.persistence.CrsData;
import org.gvsig.crs.persistence.RecentCRSsPersistence;
import org.saig.jump.lang.I18N;

public class CRSSelectionDialog
extends JPanel
implements IWindow {
    private static final long serialVersionUID = 1L;
    JPanel contentPane = null;
    private CRSMainPanel crsMainPanel = null;
    private IProjection lastProj = null;
    int code = 0;
    String dataSource = "";
    private boolean okPressed = false;

    public CRSSelectionDialog(ICrs curProj) {
        this.lastProj = curProj;
        this.initialize();
    }

    public void initialize() {
        this.crsMainPanel = new CRSMainPanel((ICrs)this.lastProj);
        this.add((Component)this.getContentPanel(), null);
        this.setListeners();
    }

    public void initRecents(ICrs proj) {
        CrsData crsData = new CrsData(proj.getCrsWkt().getAuthority()[0], proj.getCode(), proj.getCrsWkt().getName());
        RecentCRSsPersistence persistence = new RecentCRSsPersistence();
        persistence.addCrsData(crsData);
        this.crsMainPanel.getRecentsPanel2().loadRecents();
    }

    public boolean isOkPressed() {
        return this.okPressed;
    }

    public CRSMainPanel getProjPanel() {
        return (CRSMainPanel)this.getContentPanel();
    }

    public JPanel getContentPanel() {
        if (this.contentPane == null) {
            this.contentPane = this.crsMainPanel;
        }
        return this.contentPane;
    }

    public void setListeners() {
        CRSSelectionDialogListener listener = new CRSSelectionDialogListener(this);
        ListSelectionModel rowSM = this.crsMainPanel.getEpsgPanel().getJTable().getSelectionModel();
        rowSM.addListSelectionListener(listener);
        ListSelectionModel rowSMiau = this.crsMainPanel.getIauPanel().getJTable().getSelectionModel();
        rowSMiau.addListSelectionListener(listener);
        ListSelectionModel rowSMrecents = this.crsMainPanel.getRecentsPanel2().getJTable().getSelectionModel();
        rowSMrecents.addListSelectionListener(listener);
        ListSelectionModel rowSMesri = this.crsMainPanel.getEsriPanel().getJTable().getSelectionModel();
        rowSMesri.addListSelectionListener(listener);
        ListSelectionModel rowSMusr = this.crsMainPanel.getNewCrsPanel().getJTable().getSelectionModel();
        rowSMusr.addListSelectionListener(listener);
        this.crsMainPanel.getJComboOptions().addItemListener(listener);
        this.crsMainPanel.getJButtonAccept().addActionListener(listener);
        this.crsMainPanel.getJButtonCancel().addActionListener(listener);
        this.crsMainPanel.getEsriPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getEpsgPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getRecentsPanel2().getJTable().addMouseListener(listener);
        this.crsMainPanel.getIauPanel().getJTable().addMouseListener(listener);
        this.crsMainPanel.getNewCrsPanel().getJTable().addMouseListener(listener);
    }

    public void setCode(int cod) {
        this.code = cod;
    }

    public int getCode() {
        return this.code;
    }

    public IProjection getProjection() {
        return this.getProjPanel().getProjection();
    }

    public void setProjection(IProjection proj) {
        this.lastProj = proj;
        this.getProjPanel().setProjection(proj);
    }

    public String getProjectionAbrev() {
        return this.getProjPanel().getProjection().getAbrev();
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
        m_viewinfo.setTitle(I18N.getString("org.gvsig.crs.gui.CRSSelectionDialog.new-coordinate-reference-system"));
        return m_viewinfo;
    }

    public IProjection getLastProj() {
        return this.lastProj;
    }

    public void setLastProj(IProjection lastProj) {
        this.lastProj = lastProj;
    }

    public void setOkPressed(boolean okPressed) {
        this.okPressed = okPressed;
    }

    public CRSMainPanel getCrsMainPanel() {
        return this.crsMainPanel;
    }
}

