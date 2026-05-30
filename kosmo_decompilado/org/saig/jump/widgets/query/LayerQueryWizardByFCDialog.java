/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.saig.core.filter.Filter;
import org.saig.jump.plugin.query.QueryWizardPlugIn;
import org.saig.jump.widgets.query.IQueryWizardDialog;
import org.saig.jump.widgets.query.LayerQueryWizardByFCPanel;

public class LayerQueryWizardByFCDialog
extends JDialog
implements IQueryWizardDialog {
    private static final long serialVersionUID = 1L;
    private LayerQueryWizardByFCPanel layerQueryWizardPanel;

    public LayerQueryWizardByFCDialog(JFrame parent, boolean modal, FeatureCollection fc) {
        super((Frame)parent, modal);
        this.setTitle(QueryWizardPlugIn.NAME);
        this.layerQueryWizardPanel = new LayerQueryWizardByFCPanel(fc, JUMPWorkbench.getFrameInstance().getContext().createPlugInContext());
        this.setContentPane(this.layerQueryWizardPanel);
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    @Override
    public void setFilter(Filter filter) {
        this.layerQueryWizardPanel.setFilter(filter);
    }

    @Override
    public Filter getFilter() {
        return this.layerQueryWizardPanel.getFilter();
    }

    @Override
    public boolean exitOk() {
        return this.layerQueryWizardPanel.exitOk();
    }

    @Override
    public String getRawText() {
        if (this.layerQueryWizardPanel.getFilter() != null) {
            return this.layerQueryWizardPanel.getRawText();
        }
        return "";
    }
}

