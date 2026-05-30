/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils.generalization;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class DouglasPeuckerSimplificationPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static Logger LOGGER = Logger.getLogger(DouglasPeuckerSimplificationPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.Douglas-Peucker-simplification");
    public static final Icon ICON = IconLoader.icon("blank.png");
    public static final Icon DIALOG_ICON = IconLoader.icon("toolImages/douglas_peucker.png");
    private static final String sidebarText = I18N.getString("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.generalize-the-selected-features");
    private static final String sItem = I18N.getString("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.Item");
    private static final String sSimplificationFinalized = I18N.getString("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.simplification-finalized");
    private static String T3 = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.Maximum-point-displacement-in-model-units")) + ":";
    protected double maxPDisp = 0.0;

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1)).add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(), this.getName(), true);
        this.setDialogValues(dialog, context);
        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (!dialog.wasOKPressed()) {
            return false;
        }
        this.getDialogValues(dialog);
        return true;
    }

    private void setDialogValues(MultiInputDialog dialog, PlugInContext context) {
        dialog.setSideBarDescription(sidebarText);
        dialog.addNumberSpinner(T3, 1.0, 0.0, 9999999.9, 0.1, new EnableCheck[]{dialog.createDoubleCheck(T3)}, null);
        dialog.setSideBarImage(DIALOG_ICON);
    }

    private void getDialogValues(MultiInputDialog dialog) {
        this.maxPDisp = dialog.getDouble(T3);
    }

    protected Layer layer(PlugInContext context) {
        return context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems().iterator().next();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.allowCancellationRequests();
        this.simplify(context, this.maxPDisp, monitor);
    }

    private boolean simplify(PlugInContext context, double maxDisp, TaskMonitor monitor) throws Exception {
        Collection<Feature> features = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems();
        EditTransaction transaction = new EditTransaction(features, this.getName(), this.layer(context), this.isRollingBackInvalidEdits(), false, context.getWorkbenchFrame());
        int count = 0;
        int noItems = features.size();
        Geometry resultgeom = null;
        Iterator<Feature> iter = features.iterator();
        while (!monitor.isCancelRequested() && iter.hasNext()) {
            Feature f = iter.next();
            resultgeom = DouglasPeuckerSimplifier.simplify((Geometry)f.getGeometry(), (double)Math.abs(maxDisp));
            monitor.report(String.valueOf(sItem) + ": " + ++count + " / " + noItems + " : " + sSimplificationFinalized);
            transaction.setGeometry(count - 1, resultgeom);
            if (count % 100 != 0) continue;
            monitor.report(count, noItems, I18N.getString("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.Processed-features"));
        }
        if (monitor.isCancelRequested()) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.Operation-{0}-aborted-by-user", new Object[]{this.getName()}));
            return false;
        }
        transaction.commit();
        context.getWorkbenchFrame().warnUser(I18N.getMessage("org.saig.jump.plugin.utils.generalization.DouglasPeuckerSimplificationPlugIn.Operation-{0}-finished", new Object[]{this.getName()}));
        return true;
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
        return DouglasPeuckerSimplificationPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

