/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.help;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.images.DesktopIconLoader;
import es.kosmo.desktop.utils.DesktopUtils;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public abstract class AbstractOpenWebSitePlugIn
extends AbstractPlugIn {
    public static final Icon ICON = DesktopIconLoader.icon("help.png");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        if (this.isValid(this.getURL())) {
            DesktopUtils.browse(this.getURL());
        } else {
            JUMPWorkbench.getFrameInstance().warnUser(I18N.getMessage("es.kosmo.desktop.plugins.help.AbstractOpenWebSitePlugIn.The-URL-{0}-is-not-valid", new Object[]{this.getURL()}));
        }
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public abstract URL getURL() throws MalformedURLException;

    protected boolean isValid(URL url) {
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return super.getCheck();
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
    }
}

