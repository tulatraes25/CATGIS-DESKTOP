/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.saig.jump.lang.I18N;

public class DefineBalloonEditingScalePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.balloon.DefineBalloonEditingScalePlugIn.Redefine-the-balloon-editing-scale");
    public static final Icon ICON = IconLoader.icon("Ruler.gif");
    public static final String EDITING_SCALE_KEY = String.valueOf(DefineBalloonEditingScalePlugIn.class.toString()) + " - EDITING-ESCALE";
    public static final int DEFAULT_EDITING_SCALE_VALUE = 5000;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext picontext) throws Exception {
        int lastEscale = this.getLastEditingScale();
        String number = JOptionPane.showInputDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.balloon.DefineBalloonEditingScalePlugIn.Introduce-the-ballon-definition-scale"), String.valueOf(lastEscale));
        if (number == null) {
            return false;
        }
        Integer escale = null;
        try {
            escale = new Integer(number);
            this.setLastEditingScale(escale);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("org.saig.jump.plugin.balloon.DefineBalloonEditingScalePlugIn.You-must-insert-a-number"));
            return false;
        }
        return true;
    }

    private int getLastEditingScale() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(EDITING_SCALE_KEY, 5000);
    }

    private void setLastEditingScale(int scale) {
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(EDITING_SCALE_KEY, scale);
    }

    @Override
    public EnableCheck getCheck() {
        return DefineBalloonEditingScalePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck solucion = new MultiEnableCheck();
        solucion.add(cf.createTaskWindowMustBeActiveCheck());
        solucion.add(cf.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(cf.createSelectedLayerMustBeBalloonLayerCheck());
        return solucion;
    }
}

