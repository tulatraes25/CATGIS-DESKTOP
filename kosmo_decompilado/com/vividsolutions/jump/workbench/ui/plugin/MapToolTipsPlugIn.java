/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.widgets.config.MapToolTipsConfigPanel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.widgets.config.ConfigDialog;

public class MapToolTipsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.MapToolTipsPlugIn.show-attributes");
    public static final String SHOW_AREA_AND_LENGTH_KEY = String.valueOf(MapToolTipsPlugIn.class.getName()) + " - SHOW AREA AND LENGTH";
    public static final String DEFAULT_TEMPLATE = "";
    private String template;
    private MapToolTipsConfigPanel configPanel;

    public MapToolTipsPlugIn() {
        this.template = DEFAULT_TEMPLATE;
    }

    public MapToolTipsPlugIn(String template) {
        this.template = template;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        Blackboard blackboard = context.getWorkbenchContext().getBlackboard();
        this.configPanel = new MapToolTipsConfigPanel(blackboard);
        ConfigPlugIn.getDialog().addConfigPanel(this.configPanel, ConfigDialog.TOOLS_MAIN_CATEGORY_NAME, MapToolTipsConfigPanel.NAME);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        context.getLayerViewPanel().setToolTipText(this.template);
        context.getLayerViewPanel().getToolTipWriter().setEnabled(!context.getLayerViewPanel().getToolTipWriter().isEnabled());
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return MapToolTipsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(workbenchContext.getLayerViewPanel().getToolTipWriter().isEnabled());
                return null;
            }
        });
    }

    @Override
    public void finish(PlugInContext context) {
        ConfigPlugIn.getDialog().removeConfigPanel(this.configPanel, ConfigDialog.TOOLS_MAIN_CATEGORY_NAME);
    }
}

