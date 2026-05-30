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
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.saig.core.model.globes.TextBalloon;
import org.saig.jump.lang.I18N;
import org.saig.jump.tools.balloon.AddNewBalloonTool;

public class SelectedFeaturePropertiesBalloonPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.balloon.SelectedFeaturePropertiesBalloonPlugIn.Create-balloons-from-the-selected-features");
    public static final Icon ICON = IconLoader.icon("SnapVerticesTogether2.gif");
    public static final String LAST_EXPRESION_KEY = String.valueOf(SelectedFeaturePropertiesBalloonPlugIn.class.toString()) + " - LAST_EXPRESION";

    @Override
    public boolean execute(PlugInContext picontext) throws Exception {
        final TextBalloonLayer editableLayer = this.getSelectedTextBalloonLayer();
        if (editableLayer == null) {
            return false;
        }
        final ArrayList<TextBalloon> textballoons = new ArrayList<TextBalloon>();
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        Collection<Feature> col = selectionManager.getFeaturesWithSelectedItems();
        String texto = JOptionPane.showInputDialog(I18N.getString("org.saig.jump.plugin.balloon.SelectedFeaturePropertiesBalloonPlugIn.Write-expression"), (Object)this.getLastExpression());
        if (texto == null) {
            return false;
        }
        this.setLastExpression(texto);
        for (Feature feat : col) {
            Object o;
            TextBalloon tb = new TextBalloon();
            Geometry geom = feat.getGeometry();
            Coordinate c = geom.getCentroid().getCoordinate();
            String generatedText = texto;
            generatedText = feat instanceof BasicFeature ? ((o = ((BasicFeature)feat).getExpression(texto)) == texto ? "" : o.toString()) : texto;
            AddNewBalloonTool.fillNewBalloon(tb, c, generatedText);
            tb.setExpr(texto);
            textballoons.add(tb);
        }
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                for (TextBalloon tb : textballoons) {
                    editableLayer.addBalloon(tb);
                }
                editableLayer.fireAppearanceChanged();
            }

            @Override
            public void unexecute() throws Exception {
                for (TextBalloon tb : textballoons) {
                    editableLayer.removeBalloon(tb);
                }
                editableLayer.fireAppearanceChanged();
            }
        }, picontext);
        return true;
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
        return SelectedFeaturePropertiesBalloonPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
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

