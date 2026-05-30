/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.finder;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import org.saig.jump.widgets.finder.ConfigFinderController;
import org.saig.jump.widgets.finder.ConfigFinderJDialog;

public class FinderWindowManager {
    protected static ConfigFinderController configFinderController;
    protected static ConfigFinderJDialog configFinderJDialog;

    public static ConfigFinderController getConfigFinderController() {
        if (configFinderController == null) {
            FinderWindowManager.createConfigFinderDialog();
        }
        return configFinderController;
    }

    public static ConfigFinderJDialog getConFinderJDialog() {
        if (configFinderJDialog == null) {
            FinderWindowManager.createConfigFinderDialog();
        }
        configFinderJDialog.setLocationRelativeTo(JUMPWorkbench.getFrameInstance());
        return configFinderJDialog;
    }

    private static void createConfigFinderDialog() {
        configFinderJDialog = new ConfigFinderJDialog(JUMPWorkbench.getFrameInstance(), true);
        configFinderController = new ConfigFinderController(configFinderJDialog);
        configFinderJDialog.setModal(true);
    }
}

