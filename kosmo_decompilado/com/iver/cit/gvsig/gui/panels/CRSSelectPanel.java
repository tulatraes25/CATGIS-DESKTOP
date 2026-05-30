/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 *  org.gvsig.gui.beans.swing.JButton
 */
package com.iver.cit.gvsig.gui.panels;

import com.iver.cit.gvsig.gui.panels.ProjChooserPanel;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.gui.beans.swing.JButton;

public abstract class CRSSelectPanel
extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(CRSSelectPanel.class);
    private static Class<?> panelClass = ProjChooserPanel.class;
    private boolean transPanelActive = false;
    protected ActionListener actionListener = null;

    public static void registerPanelClass(Class<?> panelClass) {
        CRSSelectPanel.panelClass = panelClass;
    }

    public static CRSSelectPanel getPanel(IProjection proj) {
        CRSSelectPanel panel = null;
        Class[] args = new Class[]{IProjection.class};
        Object[] params = new Object[]{proj};
        try {
            panel = (CRSSelectPanel)panelClass.getConstructor(args).newInstance(params);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        return panel;
    }

    public CRSSelectPanel(IProjection proj) {
    }

    public abstract JButton getJBtnChangeProj();

    public abstract JLabel getJLabel();

    public abstract IProjection getCurProj();

    public abstract boolean isOkPressed();

    public void addActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public boolean isTransPanelActive() {
        return this.transPanelActive;
    }

    public void setTransPanelActive(boolean transPanelActive) {
        this.transPanelActive = transPanelActive;
    }
}

