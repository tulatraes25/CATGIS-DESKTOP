/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.print;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.TaskFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;
import org.saig.core.model.layout.PrintLayoutManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.print.PrintLayoutFrame;

public class PrintLayoutPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.print.PrintLayoutPlugIn.name");
    public static final Icon ICON = IconLoader.icon("printer.png");

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(checkFactory.createTaskWindowMustBeActiveCheck()).add(checkFactory.createAngleOfTheActiveViewMustBe(0.0));
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        PrintLayoutFrame frame = new PrintLayoutFrame((TaskFrame)context.getActiveInternalFrame(), context.getWorkbenchFrame());
        PrintLayoutManager printLayoutManager = context.getWorkbenchContext().getPrintLayoutManager();
        printLayoutManager.addLayout(frame);
        frame.setVisible(true);
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }
}

