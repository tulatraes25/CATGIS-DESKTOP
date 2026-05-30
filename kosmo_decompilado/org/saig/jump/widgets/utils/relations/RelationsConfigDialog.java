/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.utils.relations;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.saig.core.model.data.Table;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.utils.relations.RelationsConfigPanel;

public class RelationsConfigDialog
extends JDialog {
    private static final long serialVersionUID = 1L;

    public RelationsConfigDialog(JFrame parent, boolean modal, WorkbenchContext context, LayerManager layerManager, Layer layer) {
        super((Frame)parent, modal);
        RelationsConfigPanel mainPanel = new RelationsConfigPanel(context, layerManager, layer);
        this.initialize(mainPanel, layer.getName());
    }

    public RelationsConfigDialog(JFrame parent, boolean modal, WorkbenchContext context, LayerManager layerManager, Table table) {
        super((Frame)parent, modal);
        RelationsConfigPanel mainPanel = new RelationsConfigPanel(context, layerManager, table);
        this.setContentPane(mainPanel);
        this.initialize(mainPanel, table.getName());
    }

    private void initialize(JPanel mainPanel, String name) {
        this.setContentPane(mainPanel);
        this.setTitle(String.valueOf(I18N.getString("org.saig.core.model.relations.widgets.RelationsConfigDialog.configure-relations")) + ": " + name);
        this.setSize(520, 460);
        GUIUtil.centreOnScreen(this);
        this.setVisible(true);
    }
}

