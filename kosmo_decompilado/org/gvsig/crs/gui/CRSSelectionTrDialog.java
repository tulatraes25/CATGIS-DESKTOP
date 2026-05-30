/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.gui;

import java.awt.Component;
import javax.swing.JPanel;
import org.gvsig.crs.CrsWkt;
import org.gvsig.crs.ICrs;
import org.gvsig.crs.gui.CRSMainTrPanel;

public class CRSSelectionTrDialog
extends JPanel {
    private static final long serialVersionUID = 1L;
    protected JPanel contentPane = null;
    protected ICrs curProj = null;
    protected int targetProjCode;
    protected CrsWkt crsWktTarget;

    public CRSSelectionTrDialog(ICrs proj, int targetCode, CrsWkt wktTarget) {
        super(false);
        this.curProj = proj;
        this.targetProjCode = targetCode;
        this.crsWktTarget = wktTarget;
        this.inicializate();
    }

    public void inicializate() {
        this.add((Component)this.getContentPanel(), null);
    }

    public CRSMainTrPanel getProjPanel() {
        return (CRSMainTrPanel)this.getContentPanel();
    }

    public JPanel getContentPanel() {
        if (this.contentPane == null) {
            this.contentPane = new CRSMainTrPanel(this.targetProjCode, this.crsWktTarget, this.curProj);
        }
        return this.contentPane;
    }
}

