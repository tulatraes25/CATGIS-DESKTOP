/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.model.data.Table;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.editing.EditSelectedFeatureAttributesFrame;

public class EditSelectedFeatureAttributesPlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.editing.EditSelectedFeatureAttributesPlugIn.Smart-edit")) + "...";
    public static final Icon ICON = IconLoader.icon("editFeatureAttributes.png");
    private EditSelectedFeatureAttributesFrame editFrame;
    public static final String FEATURE_ATTRIBUTES_TABLE = "feature_values";
    public static final String FEATURE_DESCRIPTIONS_TABLE = "feature_fields";
    public static final String FEATURE_FIELDS_FEATURE_CODE = "FEATURE_CODE";
    public static final String FEATURE_VALUES_FEATURE_CODE = "FEATURE_CODE";
    public static final String FEATURE_FIELDS_ATT_NAME = "ATT_NAME";
    public static final String FEATURE_FIELDS_ATT_CODE = "ATT_CODE";
    public static final String FEATURE_FIELDS_MULTIPLE = "MULTIPLE";
    public static final String FEATURE_FIELDS_DEFAULT_VALUE = "DEFAULT_VALUE";
    public static final String FEATURE_VALUES_ATT_VALUE = "ATT_VALUE";
    public static final String FEATURE_VALUES_ATT_VALUE_NAME = "ATT_VALUE_NAME";
    public static final String FEATURE_VALUES_ATT_CODE = "ATT_CODE";

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
        return EditSelectedFeatureAttributesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1));
        check.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return check;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        if (this.editFrame == null) {
            this.editFrame = new EditSelectedFeatureAttributesFrame();
            context.getLayerViewPanel().addListener(this.editFrame);
            this.editFrame.loadAttributeMaps(this.getAttributeTables());
            context.getWorkbenchFrame().addInternalFrame(this.editFrame);
            GUIUtil.centreOnWindow(this.editFrame);
        } else {
            this.editFrame.setVisible(true);
            this.editFrame.toFront();
        }
        Layer editableLayer = context.getLayerViewPanel().getLayerManager().getEditableLayers().iterator().next();
        this.editFrame.loadEditableLayerAttributes(editableLayer);
        Feature selectedFeature = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer).iterator().next();
        this.editFrame.loadSelectedFeature(selectedFeature);
        context.getWorkbenchFrame().getDesktopPane().setLayer(this.editFrame, 120);
        return true;
    }

    @Override
    public void finish(PlugInContext context) {
        if (this.editFrame != null) {
            context.getLayerViewPanel().removeListener(this.editFrame);
        }
    }

    private Table[] getAttributeTables() {
        Table[] tables = new Table[]{JUMPWorkbench.getTable(FEATURE_ATTRIBUTES_TABLE), JUMPWorkbench.getTable(FEATURE_DESCRIPTIONS_TABLE)};
        return tables;
    }
}

