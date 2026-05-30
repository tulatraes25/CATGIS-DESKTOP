/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang.ArrayUtils
 */
package org.saig.jump.widgets.query;

import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.apache.commons.lang.ArrayUtils;
import org.saig.core.filter.Filter;
import org.saig.core.styling.RuleImpl;
import org.saig.jump.plugin.query.QueryWizardPlugIn;
import org.saig.jump.widgets.query.IQueryWizardDialog;
import org.saig.jump.widgets.query.LayerQueryWizardPanel;

public class LayerQueryWizardDialog
extends JDialog
implements LayerListener,
IQueryWizardDialog {
    private static final long serialVersionUID = 1L;
    private LayerQueryWizardPanel layerQueryWizardPanel;
    private PlugInContext context;

    public LayerQueryWizardDialog(JDialog parent, boolean modal, PlugInContext context) {
        super((Dialog)parent, modal);
        this.setTitle(QueryWizardPlugIn.NAME);
        this.context = context;
        LayerNamePanel layerNamePanel = context.getWorkbenchContext().getLayerNamePanel();
        Object[] selectedLayers = layerNamePanel.getSelectedLayers();
        Layer layer = null;
        if (!ArrayUtils.isEmpty((Object[])selectedLayers)) {
            layer = (Layer)selectedLayers[0];
        } else {
            Collection<RuleImpl> selectedRules = layerNamePanel.selectedNodes(RuleImpl.class);
            if (selectedRules.size() > 0) {
                RuleImpl rule = selectedRules.iterator().next();
                layer = ((TreeLayerNamePanel)layerNamePanel).findParentLayer(rule);
            }
        }
        this.layerQueryWizardPanel = new LayerQueryWizardPanel(layer.getFeatureSchema(), layer, false, context);
        this.layerQueryWizardPanel.initialize();
        this.setContentPane(this.layerQueryWizardPanel);
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    public LayerQueryWizardDialog(JFrame parent, boolean modal, PlugInContext context) {
        super((Frame)parent, modal);
        this.setTitle(QueryWizardPlugIn.NAME);
        this.context = context;
        Layer layer = null;
        LayerNamePanel layerNamePanel = context.getWorkbenchContext().getLayerNamePanel();
        Object[] selectedLayers = layerNamePanel.getSelectedLayers();
        if (!ArrayUtils.isEmpty((Object[])selectedLayers)) {
            layer = (Layer)selectedLayers[0];
        } else {
            Collection<RuleImpl> selectedRules = layerNamePanel.selectedNodes(RuleImpl.class);
            if (selectedRules.size() > 0) {
                RuleImpl rule = selectedRules.iterator().next();
                layer = ((TreeLayerNamePanel)layerNamePanel).findParentLayer(rule);
            }
        }
        this.layerQueryWizardPanel = new LayerQueryWizardPanel(layer != null ? layer.getFeatureSchema() : null, layer, false, context);
        this.layerQueryWizardPanel.initialize();
        this.setContentPane(this.layerQueryWizardPanel);
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                LayerQueryWizardDialog.this.removeLayerListener();
            }
        });
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    public LayerQueryWizardDialog(JFrame parent, boolean modal, PlugInContext context, Layer layer) {
        super((Frame)parent, modal);
        this.setTitle(QueryWizardPlugIn.NAME);
        this.context = context;
        this.layerQueryWizardPanel = new LayerQueryWizardPanel(layer.getFeatureSchema(), layer, false, context);
        this.layerQueryWizardPanel.initialize();
        this.setContentPane(this.layerQueryWizardPanel);
        this.pack();
        GUIUtil.centreOnWindow(this);
    }

    protected void removeLayerListener() {
        if (this.context != null && this.context.getWorkbenchContext().getLayerViewPanel() != null && this.context.getWorkbenchContext().getLayerViewPanel().getLayerManager() != null) {
            this.context.getWorkbenchContext().getLayerViewPanel().getLayerManager().removeLayerListener(this);
            this.layerQueryWizardPanel.closeInfoFrame();
        }
    }

    @Override
    public void setFilter(Filter filter) {
        this.layerQueryWizardPanel.setFilter(filter);
    }

    @Override
    public Filter getFilter() {
        return this.layerQueryWizardPanel.getFilter();
    }

    @Override
    public boolean exitOk() {
        return this.layerQueryWizardPanel.exitOk();
    }

    @Override
    public String getRawText() {
        if (this.layerQueryWizardPanel.getFilter() != null) {
            return this.layerQueryWizardPanel.getRawText();
        }
        return "";
    }

    @Override
    public void categoryChanged(CategoryEvent e) {
    }

    @Override
    public void featuresChanged(FeatureEvent e) {
    }

    @Override
    public void layerChanged(LayerEvent e) {
        if (e.getType() == LayerEventType.ADDED || e.getType() == LayerEventType.REMOVED) {
            this.layerQueryWizardPanel.layerChangedNotification();
        }
    }
}

