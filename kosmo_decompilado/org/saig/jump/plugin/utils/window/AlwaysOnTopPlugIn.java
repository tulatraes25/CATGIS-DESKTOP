/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils.window;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import org.saig.jump.lang.I18N;

public class AlwaysOnTopPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.utils.window.AlwaysOnTopPlugIn.always-on-top");
    public static final Icon ICON_ENABLED = IconLoader.icon("GreenPinPushedIn.gif");
    public static final Icon ICON_DISABLED = IconLoader.icon("RedPinPushedIn.gif");
    public static final int ALWAYS_ON_TOP_LEVEL = 100;
    private boolean alwaysOnTop = false;
    private int originalLayer = 0;
    private JInternalFrame frameParent;

    public AlwaysOnTopPlugIn(JInternalFrame frame) {
        this.frameParent = frame;
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getWorkbenchFrame().getDesktopPane();
        this.originalLayer = JDesktopPane.getLayer(this.frameParent);
        this.alwaysOnTop = this.originalLayer == 100;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        boolean bl = this.alwaysOnTop = !this.alwaysOnTop;
        if (this.alwaysOnTop) {
            context.getWorkbenchFrame().getDesktopPane().setLayer(this.frameParent, 100);
        } else {
            context.getWorkbenchFrame().getDesktopPane().setLayer(this.frameParent, this.originalLayer);
        }
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        if (this.alwaysOnTop) {
            return ICON_ENABLED;
        }
        return ICON_DISABLED;
    }
}

