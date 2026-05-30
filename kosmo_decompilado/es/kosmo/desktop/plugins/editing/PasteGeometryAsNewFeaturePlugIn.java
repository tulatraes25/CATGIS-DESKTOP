/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.io.WKTReader
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.editing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.plugins.editing.GeometryTransferable;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;

public class PasteGeometryAsNewFeaturePlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(PasteGeometryAsNewFeaturePlugIn.class);
    public static final String NAME = I18N.getString("es.kosmo.desktop.plugins.conversion.PasteGeometryAsNewFeaturePlugIn.Copy-geometry-as-a-new-feature");
    public static final Icon ICON = IconLoader.icon("pasteGeom.png");
    private WKTReader reader = new WKTReader();

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        final Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        Transferable transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
        Geometry geom = transferable.isDataFlavorSupported(GeometryTransferable.WKT_FLAVOR) ? this.reader.read((Reader)new StringReader((String)transferable.getTransferData(GeometryTransferable.WKT_FLAVOR))) : this.reader.read((Reader)new StringReader((String)transferable.getTransferData(DataFlavor.stringFlavor)));
        boolean ok = PasteGeometryAsNewFeaturePlugIn.conformGeometryType(geom, editableLayer.getFeatureSchema().getGeometryType());
        if (!ok) {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("es.kosmo.desktop.plugins.editing.PasteGeometryPlugIn.The-geometry-type-stored-at-the-clipboard-{0}-is-not-compatible-with-the-editable-layer-one-{1}", new Object[]{geom.getGeometryType(), GUITranslationsUtils.getGeometryName(editableLayer.getGeometryType())}));
            return false;
        }
        final SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        final Collection<Feature> selectedFeatures = selectionManager.getFeaturesWithSelectedItems();
        BasicFeature newFeat = new BasicFeature(editableLayer.getFeatureSchema());
        newFeat.setGeometry(geom);
        final ArrayList<BasicFeature> featsToAdd = new ArrayList<BasicFeature>();
        featsToAdd.add(newFeat);
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() throws Exception {
                try {
                    selectionManager.unselectItems(editableLayer);
                    editableLayer.getFeatureCollectionWrapper().addAll(featsToAdd);
                    editableLayer.getLayerManager().fireFeaturesChanged(featsToAdd, FeatureEventType.ADDED, editableLayer);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsToAdd);
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(editableLayer, selectedFeatures);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                selectionManager.unselectItems(editableLayer);
                editableLayer.getFeatureCollectionWrapper().removeAll(featsToAdd);
                editableLayer.getLayerManager().fireFeaturesChanged(featsToAdd, FeatureEventType.DELETED, editableLayer);
                selectionManager.getFeatureSelection().selectItems(editableLayer, selectedFeatures);
            }
        }, context);
        context.getWorkbenchFrame().warnUser(I18N.getString("es.kosmo.desktop.plugins.editing.PasteGeometryPlugIn.The-geometry-has-been-copied"));
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
        return PasteGeometryAsNewFeaturePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    private static boolean conformGeometryType(Geometry geom, int geometryType) {
        boolean conform = false;
        if (geom != null) {
            switch (geometryType) {
                case 1: 
                case 8: {
                    conform = geom instanceof Point || geom instanceof MultiPoint;
                    break;
                }
                case 2: 
                case 3: {
                    conform = geom instanceof LineString || geom instanceof MultiLineString;
                    break;
                }
                case 4: 
                case 5: {
                    conform = geom instanceof Polygon || geom instanceof MultiPolygon;
                }
            }
        }
        return conform;
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createWindowWithLayerViewPanelMustBeActiveCheck());
        solucion.add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck());
        solucion.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        solucion.add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Transferable transferable;
                try {
                    transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    return I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.wrong-clipboard-content");
                }
                if (transferable == null) {
                    return I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.clipboard-must-not-be-empty");
                }
                if (transferable.isDataFlavorSupported(GeometryTransferable.WKT_FLAVOR)) {
                    return null;
                }
                try {
                    if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor) && this.isWKT((String)transferable.getTransferData(DataFlavor.stringFlavor))) {
                        return null;
                    }
                }
                catch (Exception e) {
                    workbenchContext.getErrorHandler().handleThrowable(e);
                }
                return I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.clipboard-must-contain-geometries-or-WKT");
            }

            private boolean isWKT(String s) {
                try {
                    new WKTReader().read((Reader)new StringReader(s));
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            }
        });
        return solucion;
    }
}

