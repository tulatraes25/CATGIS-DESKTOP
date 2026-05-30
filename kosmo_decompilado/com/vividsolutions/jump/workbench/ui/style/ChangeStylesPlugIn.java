/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.style;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorScheme;
import com.vividsolutions.jump.workbench.ui.renderer.style.ColorThemingStylePanel;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;
import com.vividsolutions.jump.workbench.ui.style.DecorationStylePanel;
import com.vividsolutions.jump.workbench.ui.style.LabelStylePanel;
import com.vividsolutions.jump.workbench.ui.style.RenderingStylePanel;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.ApplicationExitListener;

public class ChangeStylesPlugIn
extends AbstractPlugIn
implements ApplicationExitListener {
    private static final Logger LOGGER = Logger.getLogger(ChangeStylesPlugIn.class);
    public static final String NAME = I18N.getString("workbench.ui.style.ChangeStylesPlugIn.name");
    public static final Icon ICON = IconLoader.icon("Palette.gif");
    private static final String LAST_TAB_KEY = String.valueOf(ChangeStylesPlugIn.class.getName()) + " - LAST TAB";

    @Override
    public String getName() {
        return String.valueOf(NAME) + "...";
    }

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getWorkbenchFrame().getApplicationExitHandler().addExitListener(this);
    }

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        final Layer layer = (Layer)context.getSelectedLayer(0);
        MultiInputDialog dialog = new MultiInputDialog(context.getWorkbenchFrame(), NAME, true);
        dialog.setResizable(false);
        dialog.setInset(0);
        dialog.setSideBarImage(IconLoader.icon("toolImages/Symbology.gif"));
        dialog.setSideBarDescription(I18N.getString("workbench.ui.style.ChangeStylesPlugIn.you-can-use-this-dialog-to-change-color-line-width-and-other-visual-properties-of-a-layer"));
        ArrayList<StylePanel> stylePanels = new ArrayList<StylePanel>();
        context.getWorkbenchContext().getWorkbench();
        RenderingStylePanel renderingStylePanel = new RenderingStylePanel(JUMPWorkbench.getBlackboard(), layer);
        stylePanels.add(renderingStylePanel);
        if (!layer.isRaster()) {
            DecorationStylePanel decorationStylePanel = new DecorationStylePanel(layer, context.getWorkbenchFrame().getSwappableStyleClasses());
            decorationStylePanel.setPreferredSize(new Dimension(500, 400));
            if (layer.getFeatureCollectionWrapper().getFeatureSchema().getAttributeCount() > 1) {
                ColorThemingStylePanel colorThemingStylePanel = new ColorThemingStylePanel(layer, context.getWorkbenchContext(), renderingStylePanel);
                colorThemingStylePanel.setPreferredSize(new Dimension(500, 400));
                stylePanels.add(colorThemingStylePanel);
                GUIUtil.sync(renderingStylePanel.getTransparencySlider(), colorThemingStylePanel.getTransparencySlider());
                GUIUtil.sync(renderingStylePanel.getSynchronizeCheckBox(), colorThemingStylePanel.getSynchronizeCheckBox());
            } else {
                stylePanels.add(new DummyColorThemingStylePanel());
            }
            stylePanels.add(new LabelStylePanel(layer, context.getLayerViewPanel(), dialog, context.getErrorHandler()));
            stylePanels.add(decorationStylePanel);
        }
        JTabbedPane tabbedPane = new JTabbedPane();
        for (final StylePanel stylePanel : stylePanels) {
            tabbedPane.add((Component)((Object)stylePanel), stylePanel.getTitle());
            dialog.addEnableChecks(stylePanel.getTitle(), Arrays.asList(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return stylePanel.validateInput();
                }
            }));
        }
        dialog.addRow(tabbedPane);
        context.getWorkbenchContext().getWorkbench();
        tabbedPane.setSelectedComponent(this.find(stylePanels, (String)JUMPWorkbench.getBlackboard().get(LAST_TAB_KEY, ((StylePanel)stylePanels.iterator().next()).getTitle())));
        dialog.setVisible(true);
        context.getWorkbenchContext().getWorkbench();
        JUMPWorkbench.getBlackboard().put(LAST_TAB_KEY, ((StylePanel)((Object)tabbedPane.getSelectedComponent())).getTitle());
        if (dialog.wasOKPressed()) {
            final List oldStyles = (List)layer.cloneStyles();
            final ArrayList<Style> stylesToAdd = new ArrayList<Style>();
            for (StylePanel stylePanel : stylePanels) {
                stylesToAdd.add(stylePanel.updateStyles());
            }
            final List newStyles = (List)layer.cloneStyles();
            this.execute(new UndoableCommand(this.getName()){

                @Override
                public void execute() {
                    layer.setStyles(newStyles);
                    layer.addStyles(stylesToAdd);
                    context.getLayerManager().fireLayerChanged(layer, LayerEventType.APPEARANCE_CHANGED);
                }

                @Override
                public void unexecute() {
                    layer.setStyles(oldStyles);
                    layer.addStyles(oldStyles);
                    context.getLayerManager().fireLayerChanged(layer, LayerEventType.APPEARANCE_CHANGED);
                }
            }, context);
            return true;
        }
        return false;
    }

    private Component find(Collection<StylePanel> stylePanels, String title) {
        for (StylePanel stylePanel : stylePanels) {
            if (!stylePanel.getTitle().equals(title)) continue;
            return (Component)((Object)stylePanel);
        }
        return (Component)((Object)stylePanels.iterator().next());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createSelectedLayerIsNotCAD());
    }

    @Override
    public EnableCheck getCheck() {
        return ChangeStylesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean exitingApplication() {
        LOGGER.info((Object)(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.style.ChangeStylesPlugIn.saving-color-schemes-to-the-xml-file")) + "..."));
        ColorScheme.saveCustomColorSchemes();
        return true;
    }

    private class DummyColorThemingStylePanel
    extends JPanel
    implements StylePanel {
        public DummyColorThemingStylePanel() {
            super(new GridBagLayout());
            this.add(new JLabel(I18N.getString("workbench.ui.style.ChangeStylesPlugIn.this-layer-has-no-attributes")));
        }

        @Override
        public String getTitle() {
            return ColorThemingStylePanel.TITLE;
        }

        @Override
        public Style updateStyles() {
            return null;
        }

        @Override
        public String validateInput() {
            return null;
        }
    }
}

