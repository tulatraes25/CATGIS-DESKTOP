/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  javax.measure.quantity.Length
 *  javax.measure.unit.Unit
 */
package es.kosmo.desktop.plugins.measure;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.task.TaskMonitorDialog;
import java.util.List;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.KosmoDesktopUtils;
import org.saig.jump.widgets.util.DialogFactory;

public class MeasureLinesPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final String NAME = I18N.getString(MeasureLinesPlugIn.class, "Measure-selected-lines");
    public static final Icon ICON = IconLoader.icon("linesLength.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return super.execute(context);
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        ((TaskMonitorDialog)monitor).setVisible(false);
        List<Feature> selectedFeatures = KosmoDesktopUtils.getSelectedFeatures();
        double acum = 0.0;
        for (Feature feat : selectedFeatures) {
            Geometry geom = feat.getGeometry();
            acum += geom.getLength();
        }
        DialogFactory.showInformationDialog(JUMPWorkbench.getFrameInstance(), this.getInfoMessage(acum), I18N.getString("es.kosmo.desktop.plugins.meassure.MeasureLinesPlugIn.Line-length"));
    }

    protected String getInfoMessage(double length) {
        int numFeats = KosmoDesktopUtils.getSelectedFeatures().size();
        LayerViewPanel lvp = JUMPWorkbench.getFrameInstance().getContext().getLayerViewPanel();
        int numLayers = lvp.getSelectionManager().getLayersWithSelectedItems().size();
        Unit<Length> userLengthUnit = lvp.getUserLengthUnit();
        String infoMsg = "<html>";
        infoMsg = numFeats == 1 ? String.valueOf(infoMsg) + I18N.getString(MeasureLinesPlugIn.class, "one-line-in-one-layer") : (numLayers == 1 ? String.valueOf(infoMsg) + I18N.getMessage(MeasureLinesPlugIn.class, "{0}-lines-in-one-layer", new Object[]{numFeats}) : String.valueOf(infoMsg) + I18N.getMessage(MeasureLinesPlugIn.class, "{0}-lines-in-{1}-layers", new Object[]{numFeats, numLayers}));
        infoMsg = String.valueOf(infoMsg) + "<br/>" + I18N.getMessage(MeasureLinesPlugIn.class, "Total-length-{0}-{1}", new Object[]{this.format(length), userLengthUnit}) + "</html>";
        return infoMsg;
    }

    protected String format(double acum) {
        acum *= 100000.0;
        acum = Math.rint(acum);
        return String.valueOf(acum /= 100000.0);
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getWorkbenchFrame().getToolBar().addPlugIn(this.getIcon(), this, MeasureLinesPlugIn.createEnableCheck(context.getWorkbenchContext()), context.getWorkbenchContext());
    }

    @Override
    public void finish(PlugInContext context) {
        context.getWorkbenchFrame().getToolBar().removePlugIn(this);
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return MeasureLinesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        int[] lines = new int[]{3, 2};
        int[] nArray = new int[9];
        nArray[0] = 9;
        nArray[1] = 10;
        nArray[3] = 11;
        nArray[4] = 15;
        nArray[5] = 8;
        nArray[6] = 4;
        nArray[7] = 1;
        nArray[8] = 5;
        int[] noLines = nArray;
        EnableCheck[] checks = new EnableCheck[]{cf.createTaskWindowMustBeActiveCheck(), cf.createAtLeastNFeaturesMustBeSelectedCheck(lines, noLines, 1)};
        return new MultiEnableCheck(checks);
    }
}

