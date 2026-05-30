/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.StringUtils
 */
package org.saig.jump.plugin.simbology;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Dimension;
import org.apache.commons.lang.StringUtils;
import org.saig.core.gui.swing.sldeditor.style.StyleDialog;
import org.saig.core.styling.Rule;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleImpl;

public abstract class AbstractSLDEditorPlugIn
extends AbstractPlugIn {
    private Style newStyle;
    private Style oldStyle;

    protected abstract Layer getLayer(PlugInContext var1);

    protected abstract Rule getRule(PlugInContext var1);

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        final Layer layer = this.getLayer(context);
        Rule rule = this.getRule(context);
        boolean wasOkPressed = this.openEditor(context, rule, layer);
        if (!wasOkPressed) {
            return false;
        }
        UndoableCommand changeStyleCommand = new UndoableCommand(this.getName()){

            @Override
            public void execute() {
                layer.setModelStyle(AbstractSLDEditorPlugIn.this.newStyle);
                context.getLayerManager().fireLayerChanged(layer, LayerEventType.APPEARANCE_CHANGED);
            }

            @Override
            public void unexecute() {
                layer.setModelStyle(AbstractSLDEditorPlugIn.this.oldStyle);
                context.getLayerManager().fireLayerChanged(layer, LayerEventType.APPEARANCE_CHANGED);
            }
        };
        this.execute(changeStyleCommand, context);
        return true;
    }

    public boolean openEditor(PlugInContext context, Rule rule, Layer layer) throws Exception {
        if (layer == null) {
            return false;
        }
        Style currentStyle = layer.getModelStyle();
        Style cloneStyle = (Style)((StyleImpl)currentStyle).clone();
        this.oldStyle = (Style)((StyleImpl)currentStyle).clone();
        StyleDialog sd = StyleDialog.createDialog(context.getWorkbenchFrame(), layer.getFeatureCollectionWrapper().getUltimateWrappee(), cloneStyle);
        sd.setMapContext(context);
        if (rule != null) {
            sd.setSelectedRule(rule);
        }
        sd.setMinimumSize(new Dimension(700, 700));
        sd.setMaximumSize(new Dimension(800, 700));
        sd.setSize(new Dimension(700, 700));
        GUIUtil.centreOnScreen(sd);
        sd.setVisible(true);
        boolean okPressed = false;
        if (sd.wasOkPressed()) {
            okPressed = true;
            this.newStyle = sd.getStyle();
            if (StringUtils.isEmpty((String)this.newStyle.getName())) {
                this.newStyle.setName(layer.getName());
                this.newStyle.setTitle(layer.getName());
            }
        }
        return okPressed;
    }
}

