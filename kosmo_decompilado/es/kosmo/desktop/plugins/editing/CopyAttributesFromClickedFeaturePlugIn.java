/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 */
package es.kosmo.desktop.plugins.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.AbstractCursorTool;
import com.vividsolutions.jump.workbench.ui.cursortool.CursorTool;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.tools.editing.SelectFeatureToCopyAttributesFromTool;
import java.awt.event.KeyListener;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class CopyAttributesFromClickedFeaturePlugIn
extends AbstractPlugIn
implements AbstractCursorTool.Listener {
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.editing.CopyAttributesFromClickedFeaturePlugIn.Copy-attributes-from-the-clicked-feature");
    public static final Icon ICON = IconLoader.icon("copyAttributesFromClicked.png");
    protected KeyListener keyListener;
    protected SelectFeatureToCopyAttributesFromTool selectTool;
    protected CursorTool selectedCursorTool;
    protected Geometry sourceGeom;
    protected String editableLayerName;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        this.selectTool = new SelectFeatureToCopyAttributesFromTool();
        this.selectTool.add(this);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        SelectionManager manager = context.getLayerViewPanel().getSelectionManager();
        Feature selectedSourceGeomFeat = manager.getFeaturesWithSelectedItems().iterator().next();
        this.sourceGeom = selectedSourceGeomFeat.getGeometry();
        this.editableLayerName = context.getLayerManager().getEditableLayers().iterator().next().getName();
        this.selectedCursorTool = context.getLayerViewPanel().getCurrentCursorTool();
        this.selectTool.setEditableLayerName(this.editableLayerName);
        this.selectTool.setSourceGeom(this.sourceGeom);
        context.getLayerViewPanel().setCurrentCursorTool(this.selectTool);
        return true;
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
        return CopyAttributesFromClickedFeaturePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1));
        solucion.add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
        return solucion;
    }

    @Override
    public void gestureFinished() {
        this.selectTool.clearCoordinates();
        this.selectTool.cancelGesture();
    }
}

