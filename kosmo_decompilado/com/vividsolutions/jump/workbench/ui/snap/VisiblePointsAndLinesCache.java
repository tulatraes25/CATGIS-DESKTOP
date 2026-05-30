/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.index.strtree.STRtree
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 *  org.cresques.cts.ICoordTrans
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.ViewportListener;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapLayersOptionsPanel;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.IShapeGeometry;
import org.saig.core.dao.datasource.filedatasource.shape.geometry.ShapeGeometryConverter;
import org.saig.core.model.feature.FeatureIterator;

public class VisiblePointsAndLinesCache {
    private static final String PANEL_PROPERTY_KEY = "VISIBLE_POINTS_AND_LINES_CACHE";
    private static final Logger LOGGER = Logger.getLogger(VisiblePointsAndLinesCache.class);
    private static GeometryFactory factory = new GeometryFactory();
    private LayerViewPanel panel;
    private STRtree tree = null;
    private LayerListener layerListener = new LayerListener(){

        @Override
        public void layerChanged(LayerEvent e) {
            VisiblePointsAndLinesCache.this.invalidate();
        }

        @Override
        public void featuresChanged(FeatureEvent e) {
        }

        @Override
        public void categoryChanged(CategoryEvent e) {
        }
    };
    private ViewportListener viewportListener = new ViewportListener(){

        @Override
        public void zoomChanged(Envelope modelEnvelope) {
            VisiblePointsAndLinesCache.this.invalidate();
        }
    };

    public static VisiblePointsAndLinesCache instance(LayerViewPanel panel) {
        VisiblePointsAndLinesCache cache = (VisiblePointsAndLinesCache)panel.getBlackboard().get(PANEL_PROPERTY_KEY);
        if (cache == null) {
            cache = new VisiblePointsAndLinesCache(panel);
        }
        return cache;
    }

    private VisiblePointsAndLinesCache(LayerViewPanel panel) {
        this.panel = panel;
        panel.getViewport().addListener(this.viewportListener);
        panel.getLayerManager().addLayerListener(this.layerListener);
        panel.getBlackboard().put(PANEL_PROPERTY_KEY, this);
    }

    public void invalidate() {
        this.tree = null;
    }

    public STRtree getTree() {
        if (this.tree == null) {
            Envelope viewportEnvelope = this.panel.getViewport().getEnvelopeInModelCoordinates();
            this.tree = new STRtree();
            boolean snapToSpecificLayers = PersistentBlackboardPlugIn.get(JUMPWorkbench.getFrameInstance().getContext()).get(SnapLayersOptionsPanel.SNAP_TO_SPECIFIC_LAYERS_KEY, false);
            ArrayList layersToSnapList = null;
            Object value = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(SnapLayersOptionsPanel.SPECIFIC_LAYERS_TO_SNAP_KEY);
            layersToSnapList = value == null || !(value instanceof ArrayList) ? new ArrayList() : (ArrayList)value;
            Iterator<Layer> i = this.panel.getLayerManager().iterator();
            while (i.hasNext()) {
                Layer layer = i.next();
                if (!layer.isVisible() || !layer.isEnabled() || layer.isRaster() || snapToSpecificLayers && !layersToSnapList.contains(layer.getName())) continue;
                ICoordTrans coordTrans = null;
                coordTrans = layer.getCoordTrans() != null ? layer.getCoordTrans() : null;
                FeatureIterator itFeats = null;
                try {
                    try {
                        itFeats = layer.getFeatureCollectionWrapper().queryIterator(viewportEnvelope);
                        while (itFeats.hasNext()) {
                            Geometry geometry;
                            Feature feature = itFeats.next();
                            if (feature == null || (geometry = feature.getGeometry()) == null) continue;
                            if (coordTrans != null) {
                                IShapeGeometry pathGeom = ShapeGeometryConverter.jts_to_igeometry(geometry);
                                pathGeom.reProject(coordTrans);
                                geometry = ShapeGeometryConverter.java2d_to_jts(pathGeom.getShp());
                            }
                            this.tree.insert(geometry.getEnvelopeInternal(), (Object)VisiblePointsAndLinesCache.toPointsAndLines(geometry));
                        }
                    }
                    catch (Exception e) {
                        LOGGER.error((Object)"", (Throwable)e);
                        if (itFeats == null) continue;
                        itFeats.close();
                        continue;
                    }
                }
                catch (Throwable throwable) {
                    if (itFeats != null) {
                        itFeats.close();
                    }
                    throw throwable;
                }
                if (itFeats == null) continue;
                itFeats.close();
            }
        }
        return this.tree;
    }

    public static Geometry toPointsAndLines(Geometry g) {
        if (g.getDimension() <= 1) {
            return g;
        }
        if (g instanceof GeometryCollection) {
            GeometryCollection oldCollection = (GeometryCollection)g;
            ArrayList<Geometry> newCollection = new ArrayList<Geometry>();
            int i = 0;
            while (i < oldCollection.getNumGeometries()) {
                newCollection.add(VisiblePointsAndLinesCache.toPointsAndLines(oldCollection.getGeometryN(i)));
                ++i;
            }
            Geometry[] geoms = new Geometry[newCollection.size()];
            newCollection.toArray(geoms);
            return factory.createGeometryCollection(geoms);
        }
        Assert.isTrue((boolean)(g instanceof Polygon));
        return ((Polygon)g).getBoundary();
    }
}

