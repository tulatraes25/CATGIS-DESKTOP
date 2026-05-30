/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.editing;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.desktop.images.DesktopIconLoader;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.KosmoDesktopUtils;
import org.saig.jump.widgets.util.DialogFactory;

public class DiscardChangesPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.editing.DiscardChangesPlugIn.Discard-changes");
    public static final Icon ICON = DesktopIconLoader.icon("cancel.png");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        int response;
        Layer editableLayer = KosmoDesktopUtils.getEditableLayer();
        if (editableLayer != null && (response = DialogFactory.showYesNoDialog(JUMPWorkbench.getFrameInstance(), I18N.getString("es.kosmo.desktop.plugins.editing.DiscardChangesPlugIn.Are-you-sure-to-discard-the-changes"), NAME)) == 0) {
            editableLayer.getFeatureCollectionWrapper().rollBack();
            editableLayer.fireLayerChanged(LayerEventType.COMMITED);
            editableLayer.setFeatureCollectionModified(false);
            KosmoDesktopUtils.discardSelection();
        }
        return true;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheck check = new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Layer layer = null;
                Collection<Layer> layers = null;
                if (workbenchContext == null || workbenchContext.getLayerNamePanel() == null) {
                    return I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.at-least-one-layer-must-be-modified");
                }
                layers = workbenchContext.getLayerManager().getEditableLayers();
                if (layers != null && layers.size() > 0) {
                    layer = layers.iterator().next();
                }
                if (layer == null || layer != null && !layer.isFeatureCollectionModified() || !layer.isEditable()) {
                    return I18N.getString("org.saig.jump.plugin.editing.CommitPlugIn.at-least-one-layer-must-be-modified");
                }
                return null;
            }
        };
        return new MultiEnableCheck().add(check);
    }
}

