/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.io.WKTWriter
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.WorkbenchException;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.EditTransaction;
import com.vividsolutions.jump.workbench.ui.EnterWKTDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.WKTPlugIn;
import java.util.Arrays;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class EditSelectedFeaturePlugIn
extends WKTPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.EditSelectedFeaturePlugIn.name");
    public static final Icon ICON = IconLoader.icon("blank.png");
    private Feature feature;

    @Override
    protected Layer layer(PlugInContext context) {
        return context.getLayerViewPanel().getSelectionManager().getLayersWithSelectedItems().iterator().next();
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createExactlyNFeaturesMustHaveSelectedItemsCheck(1));
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return EditSelectedFeaturePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return this.execute(context, context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems().iterator().next(), true);
    }

    public boolean execute(PlugInContext context, Feature feature, boolean editable) throws Exception {
        this.feature = feature;
        this.reportNothingToUndoYet(context);
        return super.execute(context);
    }

    @Override
    protected void apply(String wkt, PlugInContext context) throws Exception {
        if (!this.layer(context).isEditable()) {
            return;
        }
        super.apply(wkt, context);
    }

    @Override
    protected void apply(FeatureCollection c, PlugInContext context) throws Exception {
        if (c.size() != 1) {
            throw new WorkbenchException(String.valueOf(I18N.getString("workbench.ui.plugin.EditSelectedFeaturePlugIn.expected-one-feature-but-found")) + " " + c.size());
        }
        EditTransaction transaction = new EditTransaction(Arrays.asList(this.feature), this.getName(), this.layer, this.isRollingBackInvalidEdits(), false, context.getWorkbenchFrame());
        transaction.setGeometry(0, c.getFeaturesSamples(1).get(0).getGeometry());
        transaction.commit();
    }

    @Override
    protected EnterWKTDialog createDialog(PlugInContext context) {
        EnterWKTDialog d = super.createDialog(context);
        String pk = "";
        pk = this.feature.getPrimaryKey() != null ? this.feature.getPrimaryKey().toString() : String.valueOf(pk) + this.feature.getID();
        d.setTitle(String.valueOf(this.layer(context).isEditable() ? String.valueOf(I18N.getString("workbench.ui.plugin.EditSelectedFeaturePlugIn.edit")) + " " : "") + I18N.getString("workbench.ui.plugin.EditSelectedFeaturePlugIn.feature") + " " + pk + " " + I18N.getString("workbench.ui.plugin.EditSelectedFeaturePlugIn.in") + " " + this.layer + (this.layer(context).isEditable() ? "" : " (" + I18N.getString("workbench.ui.plugin.EditSelectedFeaturePlugIn.layer-is-uneditable") + ")"));
        d.setEditable(this.layer(context).isEditable());
        WKTWriter writer = new WKTWriter(3);
        if (this.feature.getGeometry() != null) {
            d.setText(writer.write(this.feature.getGeometry()));
        }
        return d;
    }
}

