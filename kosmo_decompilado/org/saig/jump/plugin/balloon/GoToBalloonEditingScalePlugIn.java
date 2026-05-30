/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.Toolkit;
import java.awt.geom.NoninvertibleTransformException;
import javax.swing.Icon;
import org.saig.core.util.ScaleManager;
import org.saig.jump.lang.I18N;
import org.saig.jump.plugin.balloon.DefineBalloonEditingScalePlugIn;

public class GoToBalloonEditingScalePlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.balloon.GoToBalloonEditingScalePlugIn.Go-to-the-balloon-editing-scale");
    public static final Icon ICON = IconLoader.icon("ZoomToLayer.gif");

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        int lastScale = this.getLastEditingEscale();
        double dpi = Toolkit.getDefaultToolkit().getScreenResolution();
        LayerViewPanel layerViewPanel = context.getLayerViewPanel();
        Envelope currentEnvelope = layerViewPanel.getViewport().getEnvelopeInModelCoordinates();
        double value = (double)lastScale / context.getLayerViewPanel().getFactor();
        double layerViewPanelWidth = layerViewPanel.getWidth();
        double newWidth = value * layerViewPanelWidth / (dpi / 2.54 * 100.0);
        Envelope smEnvelope = ScaleManager.getInstance().generateNewEnvelopeValue(currentEnvelope, layerViewPanel.getWidth(), layerViewPanel.getHeight(), value, context.getTask().getProjection(), layerViewPanel.getMapLengthUnit());
        try {
            layerViewPanel.getViewport().zoom(smEnvelope);
        }
        catch (NoninvertibleTransformException noninvertibleTransformException) {
            // empty catch block
        }
        return true;
    }

    private int getLastEditingEscale() {
        return PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(DefineBalloonEditingScalePlugIn.EDITING_SCALE_KEY, 5000);
    }

    @Override
    public EnableCheck getCheck() {
        return GoToBalloonEditingScalePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck solucion = new MultiEnableCheck();
        solucion.add(checkFactory.createTaskWindowMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(checkFactory.createSelectedLayerMustBeBalloonLayerCheck());
        return solucion;
    }
}

