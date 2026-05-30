/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.event.KeyListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.editing.ExtendTwoLinesToIntersect;

public class ExtendTwoLinesPlugIn
extends AbstractPlugIn {
    protected KeyListener keyListener;
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.ExtendTwoLinesPlugIn.extend-two-lines-until-their-intersection");
    public static final ImageIcon ICON = IconLoader.icon("blank.GIF");
    protected ExtendTwoLinesToIntersect slt = new ExtendTwoLinesToIntersect();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) {
        this.reportNothingToUndoYet(context);
        context.getLayerViewPanel().setCurrentCursorTool(this.slt);
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        return ExtendTwoLinesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                return null;
            }
        });
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(new int[]{3, 2}, new int[]{9}, 2));
        return solucion;
    }
}

