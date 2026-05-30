/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.query;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.query.LayerQueryWizardDialog;

public class QueryWizardPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.query.QueryWizardPlugIn.name");
    public static final Icon ICON = IconLoader.icon("query_wiz.gif");
    private LayerQueryWizardDialog dialog;

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return QueryWizardPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.dialog = new LayerQueryWizardDialog(context.getWorkbenchFrame(), false, context);
        context.getWorkbenchContext().getLayerViewPanel().getLayerManager().addLayerListener(this.dialog);
        this.dialog.setTitle(NAME);
        this.dialog.setVisible(true);
        return true;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersMustBeActiveCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersMustNotBeRasterCheck(1));
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        return solucion;
    }
}

