/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.datasource;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooserManager;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.config.ConfigPlugIn;
import org.saig.jump.plugin.datasource.CalcFileDataSource;
import org.saig.jump.widgets.config.ConfigLinuxOpenOfficePanel;
import org.saig.jump.widgets.config.ConfigWindowsAndMacosOpenOfficePanel;

public class CalcPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.datasource.CalcPlugIn.calc-files");
    public static final String ODS_EXTENSION = "ods";

    @Override
    public void initialize(PlugInContext context) {
        SaveFileDataSourceQueryChooser saveQueryChooser = new SaveFileDataSourceQueryChooser(CalcFileDataSource.class, NAME, new String[]{ODS_EXTENSION}, context.getWorkbenchContext());
        DataSourceQueryChooserManager dataSourceManager = DataSourceQueryChooserManager.get(JUMPWorkbench.getBlackboard());
        dataSourceManager.addSaveDataSourceQueryChooser(saveQueryChooser);
        String osname = System.getProperty("os.name");
        if (osname != null) {
            if (osname.startsWith("Windows") || osname.startsWith("Darwin")) {
                ConfigPlugIn.getDialog().addConfigPanel(new ConfigWindowsAndMacosOpenOfficePanel(context.getWorkbenchContext().getBlackboard()), I18N.getString("org.saig.jump.widgets.config.ConfigDialog.paths"), "Open Office");
            } else if (osname.startsWith("Linux")) {
                ConfigPlugIn.getDialog().addConfigPanel(new ConfigLinuxOpenOfficePanel(context.getWorkbenchContext().getBlackboard()), I18N.getString("org.saig.jump.widgets.config.ConfigDialog.paths"), "Open Office");
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}

