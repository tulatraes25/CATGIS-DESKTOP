/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.zoom;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.NClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Cursor;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class PanToClickTool
extends NClickTool {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.zoom.PanToClickTool.name");
    public static final Icon ICON = IconLoader.icon("QuickSnap.gif");

    @Override
    public Cursor getCursor() {
        return PanToClickTool.createCursor(IconLoader.icon("SnapVerticesTogetherCursor3.gif").getImage());
    }

    public PanToClickTool() {
        super(1, false);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustExistCheck(1)).add(checkFactory.createAngleOfTheActiveViewMustBe(0.0));
    }

    @Override
    protected void gestureFinished() throws Exception {
        this.getPanel().getViewport().zoomToViewPoint(this.getPanel().getLastClickedPoint(), 1.0);
    }
}

