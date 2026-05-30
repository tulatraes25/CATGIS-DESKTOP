/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.cursortool.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.DelegatingTool;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectFeaturesTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectLineStringsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SelectPartsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.SplitLineStringTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DeleteVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawLineStringTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawPointTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawPolygonTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.DrawRectangleTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.InsertVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveSelectedItemsTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesToSelectedVertexTool;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.SnapVerticesTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.ClearSelectionPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.DeleteSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.OptionsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.RedoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.UndoPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CopySelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CutSelectedItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxDialog;
import com.vividsolutions.jump.workbench.ui.toolbox.ToolboxPlugIn;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;
import es.kosmo.desktop.core.plugins.ToolInstanceManager;
import es.kosmo.desktop.plugins.editing.CopyAttributesFromClickedFeaturePlugIn;
import es.kosmo.desktop.plugins.editing.CopyGeometryToClipboardPlugIn;
import es.kosmo.desktop.plugins.editing.DiscardChangesPlugIn;
import es.kosmo.desktop.plugins.editing.PasteGeometryPlugIn;
import es.kosmo.desktop.tools.editing.RemoveSectionInLineTool;
import java.awt.Component;
import java.awt.GridBagLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.editing.CommitPlugIn;
import org.saig.jump.plugin.editing.CopySelectedItemsToEditableLayerPlugIn;
import org.saig.jump.plugin.editing.ExplodeSelectedFeaturesPlugIn;
import org.saig.jump.plugin.editing.FeatureUnionPlugIn;
import org.saig.jump.plugin.editing.ReverseLinestringPlugIn;
import org.saig.jump.plugin.editing.SplitPolygonsPlugIn;
import org.saig.jump.tools.editing.AddAreaTool;
import org.saig.jump.tools.editing.ContinueLineStringTool;
import org.saig.jump.tools.editing.CreateHoleCursorTool;
import org.saig.jump.tools.editing.GenerateAdjacentPolygonTool;
import org.saig.jump.tools.editing.RemoveAreaTool;
import org.saig.jump.tools.editing.SelectEditingFeaturesTool;

public class EditingPlugIn
extends ToolboxPlugIn {
    public static final String KEY = EditingPlugIn.class.getName();
    private static final Logger LOGGER = Logger.getLogger(EditingPlugIn.class);
    public static final String NAME = I18N.getString("workbench.ui.cursortool.editing.EditingPlugIn.name");
    private static final Icon ICON = IconLoader.icon("EditingToolbox.gif");

    @Override
    public void initialize(PlugInContext context) throws Exception {
        JUMPWorkbench.getBlackboard().put(KEY, this);
    }

    @Override
    protected void initializeToolbox(ToolboxDialog toolbox) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(toolbox.getContext());
        SelectEditingFeaturesTool selectFeatureTool = new SelectEditingFeaturesTool();
        toolbox.add(new QuasimodeTool(selectFeatureTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), SelectEditingFeaturesTool.createEnableCheck(toolbox.getContext(), selectFeatureTool));
        SelectPartsTool selectPartTool = new SelectPartsTool();
        toolbox.add(new QuasimodeTool(selectPartTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), SelectFeaturesTool.createEnableCheck(toolbox.getContext(), selectPartTool));
        SelectLineStringsTool selectLineStringTool = new SelectLineStringsTool();
        toolbox.add(new QuasimodeTool(selectLineStringTool).add(new QuasimodeTool.ModifierKeySpec(true, false, false), null), SelectFeaturesTool.createEnableCheck(toolbox.getContext(), selectLineStringTool));
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)ZoomToSelectedItemsPlugIn.ICON, 20), new ZoomToSelectedItemsPlugIn(), ZoomToSelectedItemsPlugIn.createEnableCheck(toolbox.getContext()), toolbox.getContext());
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)ClearSelectionPlugIn.ICON, 20), new ClearSelectionPlugIn(), ClearSelectionPlugIn.createEnableCheck(toolbox.getContext()), toolbox.getContext());
        toolbox.getToolBar().addSeparator();
        CutSelectedItemsPlugIn cutSelectedItemsPlugIn = new CutSelectedItemsPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)cutSelectedItemsPlugIn.getIcon(), 20), cutSelectedItemsPlugIn, cutSelectedItemsPlugIn.getCheck(), toolbox.getContext());
        CopySelectedItemsPlugIn copySelectedItemsPlugIn = new CopySelectedItemsPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)copySelectedItemsPlugIn.getIcon(), 20), copySelectedItemsPlugIn, copySelectedItemsPlugIn.getCheck(), toolbox.getContext());
        CopySelectedItemsToEditableLayerPlugIn copySelectedItemsToEditableLayerPlugIn = new CopySelectedItemsToEditableLayerPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)copySelectedItemsToEditableLayerPlugIn.getIcon(), 20), copySelectedItemsToEditableLayerPlugIn, copySelectedItemsToEditableLayerPlugIn.getCheck(), toolbox.getContext());
        PasteItemsPlugIn pasteItemsPlugIn = new PasteItemsPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)pasteItemsPlugIn.getIcon(), 20), pasteItemsPlugIn, pasteItemsPlugIn.getCheck(), toolbox.getContext());
        CopyGeometryToClipboardPlugIn copyGeometryToClipboardPlugIn = new CopyGeometryToClipboardPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)copyGeometryToClipboardPlugIn.getIcon(), 20), copyGeometryToClipboardPlugIn, copyGeometryToClipboardPlugIn.getCheck(), toolbox.getContext());
        CopyAttributesFromClickedFeaturePlugIn copyAttributesFromClickedFeaturePlugIn = new CopyAttributesFromClickedFeaturePlugIn();
        try {
            copyAttributesFromClickedFeaturePlugIn.initialize(toolbox.getContext().createPlugInContext());
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)copyAttributesFromClickedFeaturePlugIn.getIcon(), 20), copyAttributesFromClickedFeaturePlugIn, copyAttributesFromClickedFeaturePlugIn.getCheck(), toolbox.getContext());
        PasteGeometryPlugIn pasteGeometryPlugIn = new PasteGeometryPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)pasteGeometryPlugIn.getIcon(), 20), pasteGeometryPlugIn, pasteGeometryPlugIn.getCheck(), toolbox.getContext());
        DeleteSelectedItemsPlugIn deleteSelectedItemsPlugIn = new DeleteSelectedItemsPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)deleteSelectedItemsPlugIn.getIcon(), 20), deleteSelectedItemsPlugIn, deleteSelectedItemsPlugIn.getCheck(), toolbox.getContext());
        toolbox.getToolBar().addSeparator();
        UndoPlugIn undoPlugIn = new UndoPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)undoPlugIn.getIcon(), 20), undoPlugIn, undoPlugIn.getCheck(), toolbox.getContext());
        RedoPlugIn redoPlugIn = new RedoPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)redoPlugIn.getIcon(), 20), redoPlugIn, redoPlugIn.getCheck(), toolbox.getContext());
        toolbox.getToolBar().setDefaultCursorTool(selectFeatureTool);
        toolbox.getToolBar().setDefaultCursorTool(selectFeatureTool);
        toolbox.addToolBar();
        MoveSelectedItemsTool moveSelectedItems = new MoveSelectedItemsTool(checkFactory);
        toolbox.add(moveSelectedItems, MoveSelectedItemsTool.createEnableCheck(toolbox.getContext(), moveSelectedItems));
        FeatureUnionPlugIn featureUnionPlugIn = new FeatureUnionPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)featureUnionPlugIn.getIcon(), 20), featureUnionPlugIn, featureUnionPlugIn.getCheck(), toolbox.getContext());
        ExplodeSelectedFeaturesPlugIn explodeSelectedFeaturesPlugIn = new ExplodeSelectedFeaturesPlugIn();
        toolbox.addPlugIn(explodeSelectedFeaturesPlugIn);
        toolbox.getToolBar().addSeparator();
        CursorTool drawPointTool = DrawPointTool.create(toolbox.getContext());
        toolbox.add(drawPointTool, DrawPointTool.createEnableCheck(toolbox.getContext(), drawPointTool));
        InsertVertexTool insertVertexTool = new InsertVertexTool(checkFactory);
        toolbox.add(insertVertexTool, InsertVertexTool.createEnableCheck(toolbox.getContext(), insertVertexTool));
        DeleteVertexTool deleteVertexTool = new DeleteVertexTool(checkFactory);
        toolbox.add(deleteVertexTool, DeleteVertexTool.createEnableCheck(toolbox.getContext(), deleteVertexTool));
        MoveVertexTool moveVertexTool = new MoveVertexTool(checkFactory);
        toolbox.add(moveVertexTool, MoveVertexTool.createEnableCheck(toolbox.getContext(), moveVertexTool));
        SnapVerticesTool snapVertexTool = new SnapVerticesTool(checkFactory);
        toolbox.add(snapVertexTool, SnapVerticesTool.createEnableCheck(toolbox.getContext(), snapVertexTool));
        toolbox.add(new SnapVerticesToSelectedVertexTool(checkFactory), SnapVerticesToSelectedVertexTool.createEnableCheck(toolbox.getContext(), null));
        toolbox.getToolBar().addSeparator();
        DelegatingTool delLineTool = (DelegatingTool)DrawLineStringTool.create(toolbox.getContext());
        DrawLineStringTool drawLineStringTool = (DrawLineStringTool)delLineTool.getDelegate();
        toolbox.add(delLineTool, DrawLineStringTool.createEnableCheck(toolbox.getContext(), drawLineStringTool));
        ToolInstanceManager.instance().registerCursorTool(drawLineStringTool);
        DelegatingTool delContinueTool = (DelegatingTool)ContinueLineStringTool.create(toolbox.getContext());
        ContinueLineStringTool continueLineStringTool = (ContinueLineStringTool)delContinueTool.getDelegate();
        toolbox.add(continueLineStringTool, ContinueLineStringTool.createEnableCheck(toolbox.getContext(), continueLineStringTool));
        SplitLineStringTool splitLineStringTool = new SplitLineStringTool();
        toolbox.add(splitLineStringTool, SplitLineStringTool.createEnableCheck(toolbox.getContext(), splitLineStringTool));
        RemoveSectionInLineTool removeSectionInLineTool = new RemoveSectionInLineTool(checkFactory);
        toolbox.add(removeSectionInLineTool, RemoveSectionInLineTool.createEnableCheck(toolbox.getContext(), removeSectionInLineTool));
        ReverseLinestringPlugIn reverseLinestringPlugIn = new ReverseLinestringPlugIn();
        toolbox.addPlugIn(GUIUtil.resize((ImageIcon)reverseLinestringPlugIn.getIcon(), 20), reverseLinestringPlugIn, reverseLinestringPlugIn.getCheck(), toolbox.getContext());
        toolbox.getToolBar().setDefaultCursorTool(selectFeatureTool);
        toolbox.getToolBar().setDefaultCursorTool(selectFeatureTool);
        toolbox.addToolBar();
        CursorTool drawRectangleTool = DrawRectangleTool.create(toolbox.getContext());
        toolbox.add(drawRectangleTool, DrawPolygonTool.createEnableCheck(toolbox.getContext(), drawRectangleTool));
        DelegatingTool delPolygonTool = (DelegatingTool)DrawPolygonTool.create(toolbox.getContext());
        DrawPolygonTool drawPolygonTool = (DrawPolygonTool)delPolygonTool.getDelegate();
        toolbox.add(delPolygonTool, DrawPolygonTool.createEnableCheck(toolbox.getContext(), drawPolygonTool));
        AddAreaTool addAreaTool = new AddAreaTool();
        toolbox.add(addAreaTool, AddAreaTool.createEnableCheck(toolbox.getContext(), addAreaTool));
        RemoveAreaTool removeAreaTool = new RemoveAreaTool();
        toolbox.add(removeAreaTool, RemoveAreaTool.createEnableCheck(toolbox.getContext(), removeAreaTool));
        SplitPolygonsPlugIn splitPolygonsPlugIn = new SplitPolygonsPlugIn();
        toolbox.addPlugIn(splitPolygonsPlugIn);
        CreateHoleCursorTool createHoleTool = new CreateHoleCursorTool();
        toolbox.add(createHoleTool, CreateHoleCursorTool.createEnableCheck(toolbox.getContext(), createHoleTool));
        DelegatingTool delGenerateAdjacentPolygonTool = (DelegatingTool)GenerateAdjacentPolygonTool.create(JUMPWorkbench.getFrameInstance().getContext());
        GenerateAdjacentPolygonTool generateAdjacentPolygonTool = (GenerateAdjacentPolygonTool)delGenerateAdjacentPolygonTool.getDelegate();
        toolbox.add(delGenerateAdjacentPolygonTool, GenerateAdjacentPolygonTool.createEnableCheck(toolbox.getContext(), generateAdjacentPolygonTool));
        toolbox.getToolBar().setDefaultCursorTool(selectFeatureTool);
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        JButton commitButton = toolbox.addPlugIn(null, new CommitPlugIn(), CommitPlugIn.createEnableCheck(toolbox.getContext()), toolbox.getContext());
        commitButton.setIcon(CommitPlugIn.ICON);
        JButton discardChangesButton = toolbox.addPlugIn(null, new DiscardChangesPlugIn(), CommitPlugIn.createEnableCheck(toolbox.getContext()), toolbox.getContext());
        discardChangesButton.setIcon(DiscardChangesPlugIn.ICON);
        JButton optionsButton = toolbox.addPlugIn(null, new OptionsPlugIn(), null, toolbox.getContext());
        optionsButton.setText(String.valueOf(optionsButton.getText()) + "...");
        optionsButton.setIcon(OptionsPlugIn.ICON);
        FormUtils.addRowInGBL((JComponent)bottomPanel, 0, 0, (JComponent)optionsButton, false, true);
        FormUtils.addRowInGBL((JComponent)bottomPanel, 0, 1, (JComponent)discardChangesButton, false, true);
        FormUtils.addRowInGBL((JComponent)bottomPanel, 0, 2, (JComponent)commitButton, false, true);
        toolbox.getCenterPanel().add((Component)bottomPanel, "South");
        toolbox.setInitialLocation(new GUIUtil.Location(20, true, 20, false));
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return EditingPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

