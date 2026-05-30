/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.CoordUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.cursortool.editing.MoveSelectedItemsTool;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.saig.core.model.relations.topology.TopologyRelationException;
import org.saig.jump.lang.I18N;

public class MoveSelectedItemsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    protected Coordinate modelDestination;
    protected Coordinate modelSource;

    public MoveSelectedItemsPlugIn(Coordinate modelDestination, Coordinate modelSource) {
        this.modelDestination = modelDestination;
        this.modelSource = modelSource;
    }

    @Override
    public void run(final TaskMonitor monitor, PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Coordinate displacement = CoordUtil.subtract(this.modelDestination, this.modelSource);
        final Layer editableLayer = this.getLayers(context).iterator().next();
        final SelectionManager selectionManager = context.getLayerViewPanel().getSelectionManager();
        final Collection<Feature> featuresWithSelectedItems = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
        monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.MoveSelectedItemsPlugIn.moving-{0}-features-from-the-layer-{1}", new Object[]{featuresWithSelectedItems.size(), editableLayer.getName()}));
        final ArrayList<Feature> featsToMove = new ArrayList<Feature>();
        Iterator<Feature> j = featuresWithSelectedItems.iterator();
        while (j.hasNext()) {
            Feature featToMove = j.next().clone(true);
            featToMove.setGeometry(this.move(featToMove.getGeometry(), displacement));
            featsToMove.add(featToMove);
        }
        this.execute(new UndoableCommand(String.valueOf(this.getName()) + " - " + I18N.getMessage("org.saig.jump.plugin.editing.MoveSelectedItemsPlugIn.{0}-features", new Object[]{featuresWithSelectedItems.size()})){

            @Override
            public void execute() throws Exception {
                monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.MoveSelectedItemsPlugIn.moving-{0}-features-from-the-layer-{1}", new Object[]{featuresWithSelectedItems.size(), editableLayer.getName()}));
                selectionManager.unselectItems(editableLayer);
                try {
                    editableLayer.getFeatureCollectionWrapper().updateAll(featsToMove);
                    editableLayer.getLayerManager().fireGeometryModified(featsToMove, editableLayer, featuresWithSelectedItems);
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featsToMove);
                }
                catch (TopologyRelationException e) {
                    selectionManager.getFeatureSelection().selectItems(editableLayer, featuresWithSelectedItems);
                    JUMPWorkbench.getFrameInstance().warnUser(e.getMessage());
                }
            }

            @Override
            public void unexecute() throws Exception {
                monitor.report(I18N.getMessage("org.saig.jump.plugin.editing.MoveSelectedItemsPlugIn.moving-{0}-features-from-the-layer-{1}", new Object[]{featuresWithSelectedItems.size(), editableLayer.getName()}));
                selectionManager.unselectItems(editableLayer);
                editableLayer.getFeatureCollectionWrapper().updateAll(featuresWithSelectedItems);
                editableLayer.getLayerManager().fireGeometryModified(featuresWithSelectedItems, editableLayer, featsToMove);
                selectionManager.getFeatureSelection().selectItems(editableLayer, featuresWithSelectedItems);
            }
        }, context);
    }

    protected Geometry move(Geometry geometry, final Coordinate displacement) {
        geometry.apply(new CoordinateFilter(){

            public void filter(Coordinate coordinate) {
                double z = coordinate.z;
                coordinate.setCoordinate(CoordUtil.add(coordinate, displacement));
                coordinate.z = z;
            }
        });
        geometry.geometryChanged();
        return geometry;
    }

    @Override
    public String getName() {
        return MoveSelectedItemsTool.NAME;
    }

    protected Collection<Layer> getLayers(PlugInContext context) {
        return context.getLayerViewPanel().getLayerManager().getEditableLayers();
    }
}

