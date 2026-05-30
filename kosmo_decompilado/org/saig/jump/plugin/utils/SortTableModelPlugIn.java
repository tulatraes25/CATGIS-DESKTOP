/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.AttributeTab;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelProxy;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class SortTableModelPlugIn
extends AbstractPlugIn {
    public static final Logger LOGGER = Logger.getLogger(SortTableModelPlugIn.class);
    public static final String NAME_ASC = I18N.getString("org.saig.jump.plugin.utils.SortTableModelPlugIn.sort-ascending");
    public static final String NAME_DSC = I18N.getString("org.saig.jump.plugin.utils.SortTableModelPlugIn.sort-descending");
    public static final Icon ICON_ASC = IconLoader.icon("sort_asc.gif");
    public static final Icon ICON_DSC = IconLoader.icon("sort_desc.gif");
    private boolean ascending = true;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        LayerNamePanelProxy panelProxy = (LayerNamePanelProxy)((Object)context.getActiveInternalFrame());
        AttributeTab attributeTab = (AttributeTab)panelProxy.getLayerNamePanel();
        attributeTab.explicitSort(this.ascending);
        return true;
    }

    public SortTableModelPlugIn(boolean ascending) {
        this.ascending = ascending;
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustNotBeRasterCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(AttributeTab.createNotGeometryRightClickEnableCheck(workbenchContext));
    }

    @Override
    public EnableCheck getCheck() {
        return SortTableModelPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public String getName() {
        if (this.ascending) {
            return NAME_ASC;
        }
        return NAME_DSC;
    }

    @Override
    public Icon getIcon() {
        if (this.ascending) {
            return ICON_ASC;
        }
        return ICON_DSC;
    }
}

