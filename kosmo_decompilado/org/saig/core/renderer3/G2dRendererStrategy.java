/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer3;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.renderer3.DefaultRenderer;
import org.saig.core.renderer3.IG2dRenderer;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.renderer3.RasterRenderer;
import org.saig.core.renderer3.WMSLayerRenderer;

public class G2dRendererStrategy {
    public static synchronized IG2dRenderer getRenderer(Layerable layerToRenderer, double factor) {
        IRenderer renderer = null;
        if (layerToRenderer instanceof Layer) {
            Layer layer = (Layer)layerToRenderer;
            FeatureCollection fc = layer.getUltimateFeatureCollectionWrapper();
            if (layer.isRaster()) {
                renderer = new RasterRenderer();
            } else if (fc instanceof FeatureDataset) {
                renderer = new DefaultRenderer(factor);
            } else if (fc instanceof FeatureCollectionOnDemand) {
                FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)fc;
                AbstractDataSource ds = fcd.getDataAccesor();
                if (ds instanceof AbstractJDBCDataSource) {
                    renderer = new DefaultRenderer(factor);
                } else if (ds instanceof ShapeFileDataSource) {
                    renderer = new DefaultRenderer(factor);
                }
            }
        }
        if (layerToRenderer instanceof WMSLayer) {
            renderer = new WMSLayerRenderer();
        }
        if (renderer == null) {
            renderer = new DefaultRenderer(factor);
        }
        return renderer;
    }
}

