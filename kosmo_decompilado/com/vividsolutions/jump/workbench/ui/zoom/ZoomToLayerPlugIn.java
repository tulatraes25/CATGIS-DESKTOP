/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.zoom;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.Arrays;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigZoomPanel;

public class ZoomToLayerPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.zoom.ZoomToLayerPlugIn.name");
    public static final Icon ICON = IconLoader.icon("ZoomToLayer.gif");
    private static final Logger LOGGER = Logger.getLogger(ZoomToLayerPlugIn.class);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        context.getLayerViewPanel().getViewport().zoom(EnvelopeUtil.bufferByFraction(this.envelopeOfSelectedLayers(context), ConfigZoomPanel.getExtentFraction()));
        return true;
    }

    private Envelope envelopeOfSelectedLayers(PlugInContext context) {
        Envelope envelope = new Envelope();
        for (Layerable layerable : Arrays.asList(context.getLayerNamePanel().getSelectedLayers())) {
            Envelope envelope2 = null;
            if (layerable instanceof Layer) {
                Layer layer = (Layer)layerable;
                int size = 0;
                try {
                    size = layer.getUltimateFeatureCollectionWrapper().size();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                if (layer.getFeatureCollectionWrapper() != null && size > 0) {
                    envelope2 = layer.getTransformedEnvelope();
                }
            } else if (layerable instanceof WMSLayer) {
                envelope2 = ((WMSLayer)layerable).getFullEnvelope();
            } else if (layerable instanceof TextBalloonLayer) {
                envelope2 = ((TextBalloonLayer)layerable).getFullEnvelope();
            }
            if (envelope2 == null || envelope2.isNull()) continue;
            envelope.expandToInclude(envelope2);
        }
        return envelope;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createSelectedLayerMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustHaveFeaturesCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                if (component.getClass().equals(JMenuItem.class)) {
                    ((JMenuItem)component).setText(String.valueOf(NAME) + StringUtil.s(workbenchContext.getLayerNamePanel().getSelectedLayers().length));
                } else {
                    ((JButton)component).setToolTipText(String.valueOf(NAME) + StringUtil.s(workbenchContext.getLayerNamePanel().getSelectedLayers().length));
                }
                return null;
            }
        });
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return ZoomToLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

