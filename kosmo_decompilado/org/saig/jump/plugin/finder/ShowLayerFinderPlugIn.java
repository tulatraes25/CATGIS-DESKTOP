/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.finder;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.finder.FinderJDialog;
import org.saig.jump.widgets.finder.ILayerFinderDialog;
import org.saig.jump.widgets.finder.ManualFinderJDialog;

public class ShowLayerFinderPlugIn
extends AbstractPlugIn {
    public static String NAME = I18N.getString("org.saig.jump.plugin.finder.ShowLayerFinderPlugIn.Activate-locator");
    public static Icon ICON = IconLoader.icon("finder.png");
    public static int MAX_NORMAL = 50000;
    private String layerName;
    private String dialogTitle;
    private boolean modal;
    private ILayerFinderDialog dialog;

    public ShowLayerFinderPlugIn() {
        this(null);
    }

    public ShowLayerFinderPlugIn(String selectedLayerName) {
        this(selectedLayerName, true);
    }

    public ShowLayerFinderPlugIn(String selectedLayerName, boolean modal) {
        this.layerName = selectedLayerName;
        this.modal = modal;
    }

    protected Layer getSelectedLayer() {
        if (this.layerName != null) {
            return this.getFinderLayer(this.layerName);
        }
        Layerable[] layers = JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel().getSelectedLayers();
        if (layers.length > 0) {
            return (Layer)layers[0];
        }
        return null;
    }

    public Map<String, Object> getSelectedFilter() {
        return this.dialog == null ? new HashMap() : this.dialog.getSelectedFilter();
    }

    public Layer getFinderLayer(String layerName) {
        Layer layer = JUMPWorkbench.getLayer(layerName);
        if (layer == null) {
            return JUMPWorkbench.getHiddenLayer(layerName);
        }
        return layer;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer layer = this.getSelectedLayer();
        if (layer != null) {
            if (layer.isDataBaseDataSource() || layer.getFeatureCollectionWrapper().size() < MAX_NORMAL) {
                FinderJDialog dialog = new FinderJDialog(layer, this.modal);
                dialog.pack();
                if (this.dialogTitle != null) {
                    dialog.setTitle(this.dialogTitle);
                }
                GUIUtil.centreOnWindow(dialog);
                dialog.setVisible(true);
                this.dialog = dialog;
            } else {
                ManualFinderJDialog dialog = new ManualFinderJDialog(layer, this.modal);
                dialog.pack();
                if (this.dialogTitle != null) {
                    dialog.setTitle(this.dialogTitle);
                }
                GUIUtil.centreOnWindow(dialog);
                dialog.setVisible(true);
                this.dialog = dialog;
            }
        }
        return true;
    }

    @Override
    public EnableCheck getCheck() {
        if (this.layerName == null) {
            return ShowLayerFinderPlugIn.createEnableCheck();
        }
        return null;
    }

    public static EnableCheck createEnableCheck() {
        EnableCheckFactory factory = new EnableCheckFactory(JUMPWorkbench.getFrameInstance().getContext());
        MultiEnableCheck enableCheck = new MultiEnableCheck().add(factory.createExactlyNLayersMustBeSelectedCheck(1)).add(factory.createAtLeastNLayerablesMustBeSelectedCheck(1, Layer.class)).add(factory.createSelectedLayersHasFinderDefined());
        return enableCheck;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void setDialogTitle(String title) {
        this.dialogTitle = title;
    }

    public String getDialogTitle() {
        return this.dialogTitle;
    }
}

