/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.plugin.check;

import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import org.apache.commons.lang.StringUtils;
import org.saig.jump.lang.I18N;

public class ActivateOnlySpecificPluginsEnableCheck
implements EnableCheck {
    private static final String DEFAULT_CHECK_MESSAGE = I18N.getString("org.saig.jump.plugin.check.ActivateOnlySpecificPluginsEnableCheck.Disabled");
    private static String customMessage;
    private static ActivateOnlySpecificPluginsEnableCheck instance;
    private static Set<String> activatedPlugins;

    private ActivateOnlySpecificPluginsEnableCheck() {
        activatedPlugins = new HashSet<String>();
    }

    public static ActivateOnlySpecificPluginsEnableCheck getInstance() {
        if (instance == null) {
            instance = new ActivateOnlySpecificPluginsEnableCheck();
        }
        return instance;
    }

    @Override
    public String check(JComponent component) {
        if (this.isActive(component.getName())) {
            return null;
        }
        if (StringUtils.isNotEmpty((String)customMessage)) {
            return customMessage;
        }
        return DEFAULT_CHECK_MESSAGE;
    }

    private boolean isActive(String name) {
        if (activatedPlugins.contains(name)) {
            return true;
        }
        if (StringUtils.endsWith((String)name, (String)"...")) {
            return activatedPlugins.contains(StringUtils.removeEnd((String)name, (String)"..."));
        }
        return false;
    }

    public void setCustomMessage(String message) {
        customMessage = message;
    }

    public void activatePlugIn(String name) {
        activatedPlugins.add(name);
    }

    public void deactivatePlugIn(String name) {
        activatedPlugins.remove(name);
    }

    public void clearActivePlugins() {
        activatedPlugins.clear();
    }
}

