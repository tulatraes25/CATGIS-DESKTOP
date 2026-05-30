/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin.skin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.OptionsDialog;
import com.vividsolutions.jump.workbench.ui.plugin.skin.LookAndFeelProxy;
import com.vividsolutions.jump.workbench.ui.plugin.skin.SkinOptionsPanel;
import java.util.ArrayList;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.saig.jump.lang.I18N;

public class InstallSkinsPlugIn
extends AbstractPlugIn {
    private LookAndFeelProxy createProxy(final String name, final String lookAndFeelClassName) {
        return new LookAndFeelProxy(){

            @Override
            public LookAndFeel getLookAndFeel() {
                try {
                    return (LookAndFeel)Class.forName(lookAndFeelClassName).newInstance();
                }
                catch (InstantiationException e) {
                    Assert.shouldNeverReachHere((String)e.toString());
                }
                catch (IllegalAccessException e) {
                    Assert.shouldNeverReachHere((String)e.toString());
                }
                catch (ClassNotFoundException e) {
                    Assert.shouldNeverReachHere((String)e.toString());
                }
                return null;
            }

            public String toString() {
                return name;
            }
        };
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        ArrayList<LookAndFeelProxy> skins = new ArrayList<LookAndFeelProxy>();
        skins.add(this.createProxy("Default", UIManager.getSystemLookAndFeelClassName()));
        skins.add(this.createProxy("Metal", UIManager.getCrossPlatformLookAndFeelClassName()));
        skins.add(this.createProxy("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"));
        skins.add(this.createProxy("Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel"));
        context.getWorkbenchContext().getWorkbench();
        JUMPWorkbench.getBlackboard().put(SkinOptionsPanel.SKINS_KEY, skins);
        OptionsDialog optionsDialog = OptionsDialog.instance(context.getWorkbenchContext().getWorkbench());
        String string = I18N.getString("workbench.ui.plugin.skin.InstallSkinsPlugIn.skins");
        context.getWorkbenchContext().getWorkbench();
        optionsDialog.addTab(string, new SkinOptionsPanel(JUMPWorkbench.getBlackboard(), context.getWorkbenchFrame()));
    }
}

