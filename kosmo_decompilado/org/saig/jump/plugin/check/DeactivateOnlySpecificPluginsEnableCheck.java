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

public class DeactivateOnlySpecificPluginsEnableCheck
implements EnableCheck {
    private static final String DEFAULT_CHECK_MESSAGE = I18N.getString("org.saig.jump.plugin.check.DeactivateOnlySpecificPluginsEnableCheck.Disabled");
    private static String customMessage;
    private static DeactivateOnlySpecificPluginsEnableCheck instance;
    private static Set<String> deactivatedPlugins;

    private DeactivateOnlySpecificPluginsEnableCheck() {
        deactivatedPlugins = new HashSet<String>();
    }

    public static DeactivateOnlySpecificPluginsEnableCheck getInstance() {
        if (instance == null) {
            instance = new DeactivateOnlySpecificPluginsEnableCheck();
        }
        return instance;
    }

    @Override
    public String check(JComponent component) {
        if (this.isDeactive(component.getName())) {
            if (StringUtils.isNotEmpty((String)customMessage)) {
                return customMessage;
            }
            return DEFAULT_CHECK_MESSAGE;
        }
        return null;
    }

    private boolean isDeactive(String name) {
        if (deactivatedPlugins.contains(name)) {
            return true;
        }
        if (StringUtils.endsWith((String)name, (String)"...")) {
            return deactivatedPlugins.contains(StringUtils.removeEnd((String)name, (String)"..."));
        }
        return false;
    }

    public void setCustomMessage(String message) {
        customMessage = message;
    }

    public void deactivatePlugIn(String name) {
        deactivatedPlugins.add(name);
    }

    public void activatePlugIn(String name) {
        deactivatedPlugins.remove(name);
    }

    public void clearDeactivePlugins() {
        deactivatedPlugins.clear();
    }
}

