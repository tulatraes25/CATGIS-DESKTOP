/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.plugin.config;

import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.widgets.config.DatasourceOptionsPanel;
import java.net.Authenticator;
import java.util.Properties;
import javax.swing.Icon;
import org.apache.commons.lang.StringUtils;
import org.saig.core.model.data.dao.export.OpenOfficeLibLoader;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigDefaultViewOptionsPanel;
import org.saig.jump.widgets.config.ConfigDialog;
import org.saig.jump.widgets.config.ConfigNetworkPropertiesPanel;
import org.saig.jump.widgets.config.ConfigPathPanel;
import org.saig.jump.widgets.config.ConfigRenderOptionsPanel;
import org.saig.jump.widgets.config.ConfigTooltipPanel;
import org.saig.jump.widgets.config.ConfigViewDataPanel;
import org.saig.jump.widgets.config.ConfigZoomPanel;
import org.saig.jump.widgets.config.HTTPProxySettings;
import org.saig.jump.widgets.config.ProxyAuth;

public class ConfigPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.config.ConfigPlugIn.configuration");
    public static final Icon ICON = IconLoader.icon("Hammer.gif");
    private static ConfigDialog dialog;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        Blackboard blackboard = context.getWorkbenchContext().getBlackboard();
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigRenderOptionsPanel(blackboard), ConfigDialog.SCREEN_MAIN_CATEGORY_NAME, ConfigRenderOptionsPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigTooltipPanel(blackboard), ConfigDialog.SCREEN_MAIN_CATEGORY_NAME, ConfigTooltipPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigDefaultViewOptionsPanel(blackboard), ConfigDialog.ADVANCED_MAIN_CATEGORY_NAME, ConfigDefaultViewOptionsPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigViewDataPanel(blackboard), ConfigDialog.ADVANCED_MAIN_CATEGORY_NAME, ConfigViewDataPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new DatasourceOptionsPanel(blackboard), ConfigDialog.ADVANCED_MAIN_CATEGORY_NAME, DatasourceOptionsPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigPathPanel(blackboard), ConfigDialog.PATHS_MAIN_CATEGORY_NAME, ConfigPathPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigZoomPanel(blackboard), ConfigDialog.TOOLS_MAIN_CATEGORY_NAME, ConfigPathPanel.NAME);
        ConfigPlugIn.getDialog().addConfigPanel(new ConfigNetworkPropertiesPanel(blackboard), ConfigDialog.NET_MAIN_CATEGORY_NAME, ConfigNetworkPropertiesPanel.NAME);
        HTTPProxySettings settings = (HTTPProxySettings)PersistentBlackboardPlugIn.get(blackboard).get(ConfigNetworkPropertiesPanel.HTTP_PROXY_SETTINGS_KEY);
        Properties systemSettings = System.getProperties();
        if (settings != null) {
            systemSettings.put("http.proxySet", "true");
            systemSettings.put("http.proxyHost", settings.getHost());
            systemSettings.put("http.proxyPort", "" + settings.getPort());
            if (StringUtils.isNotEmpty((String)settings.getUserName())) {
                systemSettings.put("http.proxyUserName", settings.getUserName());
            }
            if (StringUtils.isNotEmpty((String)settings.getPassword())) {
                systemSettings.put("http.proxyPassword", settings.getPassword());
            }
            if (StringUtils.isNotEmpty((String)settings.getDirectConnectionTo())) {
                systemSettings.put("http.nonProxyHosts", settings.getDirectConnectionTo());
            }
            System.setProperties(systemSettings);
            if (StringUtils.isNotEmpty((String)settings.getUserName())) {
                Authenticator.setDefault(new ProxyAuth(settings.getUserName(), settings.getPassword()));
            } else {
                Authenticator.setDefault(new ProxyAuth("", ""));
            }
        }
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        ConfigPlugIn.getDialog().initializePanels();
        ConfigPlugIn.getDialog().pack();
        GUIUtil.centreOnWindow(ConfigPlugIn.getDialog());
        ConfigPlugIn.getDialog().setVisible(true);
        try {
            OpenOfficeLibLoader.loadLibs();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        return new MultiEnableCheck();
    }

    public static ConfigDialog getDialog() {
        if (dialog == null) {
            dialog = new ConfigDialog(JUMPWorkbench.getFrameInstance(), true);
        }
        return dialog;
    }
}

