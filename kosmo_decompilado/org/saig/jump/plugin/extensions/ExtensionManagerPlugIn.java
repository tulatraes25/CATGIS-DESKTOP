/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.MapUtils
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.extensions;

import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.PlugInManager;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import es.kosmo.desktop.images.DesktopIconLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.extensions.ExtensionDialog;

public class ExtensionManagerPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger((String)"org.saig.jump.plugin.extensions.ExtensionManagerPlugIn");
    public static final String NAME = I18N.getString("org.saig.jump.plugin.extensions.ExtensionManagerPlugIn.name");
    public static final Icon ICON = GUIUtil.resize(DesktopIconLoader.icon("package.png"), 20);
    public static final String LOADED_EXTENSIONS_KEY = ExtensionManagerPlugIn.class + " - LOADED_EXTENSIONS";
    private ExtensionDialog dialog;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.dialog = new ExtensionDialog(context.getWorkbenchFrame(), true);
        this.dialog.pack();
        GUIUtil.centreOnWindow(this.dialog);
        this.dialog.initializeData(context.getWorkbenchContext().getWorkbench().getPlugInManager());
        this.dialog.setVisible(true);
        return this.dialog.wasOkPressed();
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
        return ExtensionManagerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        return new MultiEnableCheck();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        monitor.report(String.valueOf(I18N.getString("org.saig.jump.plugin.extensions.ExtensionManagerPlugIn.Updating-the-extensions")) + "...");
        List<Extension> extensions = this.dialog.getExtensions();
        Map<String, Boolean> extensionActiveMap = this.dialog.getExtensionActiveMap();
        int cont = 1;
        int numExts = extensions.size();
        for (Extension currentExt : extensions) {
            if (currentExt.isActive() && extensionActiveMap.get(currentExt.getName()) == null) {
                monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.widgets.extensions.ExtensionDialog.activating-the-extension-{0}-{1}", new Object[]{currentExt.getName(), currentExt.getVersion()})) + "...");
                monitor.report(cont, numExts, I18N.getString("org.saig.jump.plugin.extensions.ExtensionManagerPlugIn.Extensions-processed"));
                try {
                    currentExt.install(context);
                }
                catch (Exception e1) {
                    LOGGER.error((Object)I18N.getMessage("org.saig.jump.widgets.extensions.ExtensionDialog.error-loading-the-extension-{0}-{1}", new Object[]{currentExt.getName(), currentExt.getVersion()}), (Throwable)e1);
                    LOGGER.error((Object)"", (Throwable)e1);
                }
            }
            if (!currentExt.isActive() && extensionActiveMap.get(currentExt.getName()) != null) {
                monitor.report(String.valueOf(I18N.getMessage("org.saig.jump.widgets.extensions.ExtensionDialog.deactivating-the-extension-{0}-{1}", new Object[]{currentExt.getName(), currentExt.getVersion()})) + "...");
                monitor.report(cont, numExts, I18N.getString("org.saig.jump.plugin.extensions.ExtensionManagerPlugIn.Extensions-processed"));
                try {
                    currentExt.uninstall(context);
                }
                catch (Exception e1) {
                    LOGGER.error((Object)I18N.getMessage("org.saig.jump.widgets.extensions.ExtensionDialog.error-deactivating-the-extension-{0}", new Object[]{e1}));
                    LOGGER.error((Object)"", (Throwable)e1);
                }
            }
            ++cont;
        }
        if (this.dialog.isMarkAsDefault()) {
            HashMap<String, String> loadedExtensions = new HashMap<String, String>();
            for (Extension currentExt : extensions) {
                if (!currentExt.isActive()) continue;
                loadedExtensions.put(currentExt.getClass().getName(), currentExt.getClass().getName());
            }
            PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).put(LOADED_EXTENSIONS_KEY, loadedExtensions);
        }
    }

    public void loadDefaultExtensions(WorkbenchContext context) {
        Map loadedExtensions = (Map)PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(LOADED_EXTENSIONS_KEY);
        ArrayList<Exception> exceptionsList = new ArrayList<Exception>();
        if (MapUtils.isNotEmpty((Map)loadedExtensions)) {
            PlugInManager plugInManager = context.getWorkbench().getPlugInManager();
            for (String currentExtName : loadedExtensions.keySet()) {
                try {
                    plugInManager.loadExtensionByName(currentExtName);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    exceptionsList.add(e);
                }
            }
        }
    }
}

