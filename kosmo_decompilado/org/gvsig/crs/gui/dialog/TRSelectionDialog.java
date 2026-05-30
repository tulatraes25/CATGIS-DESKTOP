/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.gvsig.crs.gui.dialog;

import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import org.cresques.cts.IProjection;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.CRSSelectionTrDialog;
import org.saig.jump.lang.I18N;

public class TRSelectionDialog
extends CRSSelectionTrDialog
implements IWindow {
    private static final long serialVersionUID = 1L;
    protected boolean okPressed = false;
    protected IProjection lastProj = null;
    protected boolean targetNad = false;

    public TRSelectionDialog(ICrs proj, int targetCode, CrsWkt wktTarget) {
        super(proj, targetCode, wktTarget);
        this.init();
    }

    private void init() {
        this.setBounds(0, 0, 600, 500);
        this.setLayout(new GridLayout(0, 1));
        this.setLayout(new FlowLayout(1, 10, 10));
    }

    public boolean isOkPressed() {
        return this.okPressed;
    }

    public void setOkPressed(boolean okPressed) {
        this.okPressed = okPressed;
    }

    public void setTargetNad(boolean tarNad) {
        this.targetNad = tarNad;
    }

    public boolean getTargetNad() {
        return this.targetNad;
    }

    public IProjection getProjection() {
        return this.getProjPanel().getProjection();
    }

    public void setProjection(IProjection proj) {
        this.lastProj = proj;
        this.getProjPanel().setProjection(proj);
    }

    public String getProjectionName() {
        return this.getProjPanel().getProjection().getCrsWkt().getName();
    }

    @Override
    public WindowInfo getWindowInfo() {
        WindowInfo m_viewinfo = new WindowInfo(8);
        m_viewinfo.setTitle(I18N.getString("org.gvsig.crs.gui.dialog.TRSelectionDialog.select-the-spatial-reference-system"));
        return m_viewinfo;
    }
}

