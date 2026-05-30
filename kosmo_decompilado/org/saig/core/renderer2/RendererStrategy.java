/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer2;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import org.saig.core.dao.datasource.AbstractDataSource;
import org.saig.core.dao.datasource.dbdatasource.AbstractJDBCDataSource;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.core.model.feature.FeatureCollectionOnDemand;
import org.saig.core.renderer2.CadRenderer;
import org.saig.core.renderer2.DefaultRenderer;
import org.saig.core.renderer2.IRenderer;
import org.saig.core.renderer2.RasterRenderer;
import org.saig.core.renderer2.ShapeRenderer;

public class RendererStrategy {
    public static synchronized IRenderer getRenderer(Layerable layerToRenderer, double factor) {
        IRenderer renderer = null;
        if (layerToRenderer instanceof Layer) {
            Layer layer = (Layer)layerToRenderer;
            FeatureCollection fc = layer.getUltimateFeatureCollectionWrapper();
            if (layer.isCadLayer()) {
                renderer = new CadRenderer(factor);
            } else if (layer.isRaster()) {
                renderer = new RasterRenderer();
            } else if (fc instanceof FeatureDataset) {
                renderer = new DefaultRenderer(factor);
            } else if (fc instanceof FeatureCollectionOnDemand) {
                FeatureCollectionOnDemand fcd = (FeatureCollectionOnDemand)fc;
                AbstractDataSource ds = fcd.getDataAccesor();
                if (ds instanceof AbstractJDBCDataSource) {
                    renderer = new DefaultRenderer(factor);
                } else if (ds instanceof ShapeFileDataSource) {
                    renderer = new ShapeRenderer(factor);
                }
            }
        }
        if (renderer == null) {
            renderer = new DefaultRenderer(factor);
        }
        return renderer;
    }
}

