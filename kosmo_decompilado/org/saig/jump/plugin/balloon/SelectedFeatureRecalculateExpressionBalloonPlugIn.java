/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.jump.plugin.balloon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.TextBalloonLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.saig.core.model.globes.TextBalloon;
import org.saig.jump.lang.I18N;

public class SelectedFeatureRecalculateExpressionBalloonPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.balloon.SelectedFeatureRecalculateExpressionBalloonPlugIn.Recalculate-balloons-texts-from-the-selected-features");
    public static final Icon ICON = IconLoader.icon("refreshCursor.png");
    public static final String LAST_EXPRESION_KEY = String.valueOf(SelectedFeatureRecalculateExpressionBalloonPlugIn.class.toString()) + " - LAST_EXPRESION";

    @Override
    public boolean execute(PlugInContext picontext) throws Exception {
        TextBalloonLayer editableLayer = this.getSelectedTextBalloonLayer();
        if (editableLayer == null) {
            return false;
        }
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        Collection<Feature> col = selectionManager.getFeaturesWithSelectedItems();
        for (Feature feat : col) {
            Object nt;
            Geometry geom = feat.getGeometry();
            Coordinate c = geom.getCentroid().getCoordinate();
            TextBalloon tb = this.getCloseBalloon(c, editableLayer);
            if (tb == null || !(feat instanceof BasicFeature) || (nt = ((BasicFeature)feat).getExpression(tb.getExpr())) == null) continue;
            tb.setText(nt.toString());
        }
        editableLayer.fireAppearanceChanged();
        return true;
    }

    private TextBalloon getCloseBalloon(Coordinate c, TextBalloonLayer editableLayer) {
        List<TextBalloon> balloons = editableLayer.getBalloons();
        TextBalloon nearest = null;
        double d = Double.MAX_VALUE;
        for (TextBalloon tb : balloons) {
            if (nearest != null && !(d > c.distance(tb.getBalloonEnd()))) continue;
            nearest = tb;
            d = c.distance(tb.getBalloonEnd());
        }
        if (d < 0.05) {
            return nearest;
        }
        return null;
    }

    private String getLastExpression() {
        return (String)PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(LAST_EXPRESION_KEY, "");
    }

    private void setLastExpression(String expr) {
        PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).put(LAST_EXPRESION_KEY, expr);
    }

    protected TextBalloonLayer getSelectedTextBalloonLayer() {
        Layerable[] layerables = JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel().getSelectedLayers();
        if (layerables.length < 1 || !(layerables[0] instanceof TextBalloonLayer)) {
            return null;
        }
        return (TextBalloonLayer)layerables[0];
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return SelectedFeatureRecalculateExpressionBalloonPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory cf = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck solucion = new MultiEnableCheck();
        solucion.add(cf.createTaskWindowMustBeActiveCheck());
        solucion.add(cf.createWindowWithLayerManagerMustBeActiveCheck());
        solucion.add(cf.createSelectedLayerMustBeBalloonLayerCheck());
        return solucion;
    }
}

