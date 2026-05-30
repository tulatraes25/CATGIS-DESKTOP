/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.swing.JTipOfTheDay$ShowOnStartupChoice
 */
package es.kosmo.desktop.plugins.utils;

import com.l2fprod.common.swing.JTipOfTheDay;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.widgets.utils.BasicTipOfTheDay;
import javax.swing.Icon;
import javax.swing.JDialog;
import org.saig.jump.lang.I18N;

public class TipOfTheDayPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.tipoftheday.TipOfTheDayPlugIn.show-user-tips");
    public static final Icon ICON = IconLoader.icon("Bulb.gif");
    private BasicTipOfTheDay tipOfTheDay;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.tipOfTheDay = new BasicTipOfTheDay();
        JTipOfTheDay.ShowOnStartupChoice store = new JTipOfTheDay.ShowOnStartupChoice(){

            public boolean isShowingOnStartup() {
                return TipOfTheDayPlugIn.this.tipOfTheDay.isShowOnStartup();
            }

            public void setShowingOnStartup(boolean showOnStartup) {
                TipOfTheDayPlugIn.this.tipOfTheDay.setShowOnStartup(showOnStartup);
            }
        };
        JDialog dialog = this.tipOfTheDay.buildDialog(context.getWorkbenchFrame(), store);
        dialog.setVisible(true);
        dialog.dispose();
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return TipOfTheDayPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        return new MultiEnableCheck();
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

