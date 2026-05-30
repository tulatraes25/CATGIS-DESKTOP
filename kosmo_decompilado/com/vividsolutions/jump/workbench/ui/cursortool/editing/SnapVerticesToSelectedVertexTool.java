/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DrawRectangleFenceTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexClickTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class SnapVerticesToSelectedVertexTool
extends QuasimodeTool {
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexTool.name");
    private static final Cursor SHIFT_DOWN_CURSOR = AbstractCursorTool.createCursor(IconLoader.icon("SnapVerticesTogetherCursor3.gif").getImage());
    private static final Cursor SHIFT_NOT_DOWN_CURSOR = AbstractCursorTool.createCursor(IconLoader.icon("SnapVerticesTogetherCursor4.gif").getImage());

    @Override
    public String getName() {
        return NAME;
    }

    public SnapVerticesToSelectedVertexTool(EnableCheckFactory checkFactory) {
        super(new DrawRectangleFenceTool(){

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (!this.check(new EnableCheck(){

                    @Override
                    public String check(JComponent component) {
                        return !e.isShiftDown() ? I18N.getString("workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexTool.shift-click-the-vertex-to-snap-to") : null;
                    }
                })) {
                    return;
                }
                super.mouseClicked(e);
            }

            @Override
            public Cursor getCursor() {
                return SHIFT_NOT_DOWN_CURSOR;
            }
        });
        this.add(new QuasimodeTool.ModifierKeySpec(false, true, false), new SnapVerticesToSelectedVertexClickTool(checkFactory){

            @Override
            public Cursor getCursor() {
                return SHIFT_DOWN_CURSOR;
            }
        });
    }

    @Override
    public Icon getIcon() {
        return IconLoader.icon("SnapVerticesTogether.gif");
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext, CursorTool tool) {
        MultiEnableCheck solucion = new MultiEnableCheck(tool);
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2}));
        solucion.add(checkFactory.createSelectedItemsLayersMustBeEditableCheck());
        return solucion;
    }
}

