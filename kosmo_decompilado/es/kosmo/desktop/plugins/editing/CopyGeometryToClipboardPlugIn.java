/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.editing;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.DummyClipboardOwner;
import es.kosmo.desktop.plugins.editing.GeometryTransferable;
import java.awt.Toolkit;
import javax.swing.Icon;
import org.saig.jump.lang.I18N;

public class CopyGeometryToClipboardPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.editing.CopyGeometryToClipboardPlugIn.Copy-the-selected-feature-geometry-to-the-clipboard");
    public static final Icon ICON = IconLoader.icon("copyGeom.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Feature feat = context.getWorkbenchContext().getLayerViewPanel().getSelectionManager().getFeatureSelection().getFeaturesWithSelectedItems().iterator().next();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new GeometryTransferable(feat.getGeometry()), new DummyClipboardOwner());
        context.getWorkbenchFrame().warnUser(I18N.getString("es.kosmo.desktop.plugins.editing.CopyGeometryToClipboardPlugIn.Geometry-copied-to-the-clipboard"));
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
        return CopyGeometryToClipboardPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        solucion.add(checkFactory.createExactlyNFeaturesMustBeSelectedCheck(1));
        return solucion;
    }
}

