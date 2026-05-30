/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.CoordinateSequenceFilter
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.util.AffineTransformation
 *  org.apache.log4j.Logger
 */
package es.kosmo.desktop.plugins.conversion;

import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.AffineTransformation;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import es.kosmo.desktop.widgets.conversion.AffineTransformationDialog;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;

public class AffineTransformationResultsFeatureIterator
extends AbstractResultsFeatureIterator {
    private static final Logger LOGGER = Logger.getLogger(AffineTransformationResultsFeatureIterator.class);
    protected FeatureCollection sourceFC;
    protected FeatureSchema resultSchema;
    protected FeatureIterator itFeatures;
    protected Feature currentSourceFeat;
    protected AffineTransformation affineTransformation;
    private AffineTransformationDialog affineDialog;
    private PlugInContext plugInContext;

    public AffineTransformationResultsFeatureIterator(AffineTransformationDialog dialog, PlugInContext context, FeatureSchema schema, AffineTransformation trans) {
        this.resultSchema = schema;
        this.affineDialog = dialog;
        this.plugInContext = context;
        try {
            this.itFeatures = this.affineDialog.getFeaturesToProcess(this.plugInContext);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.affineTransformation = trans;
    }

    @Override
    protected Feature generateNextFeature() throws Exception {
        while (this.currentSourceFeat == null && this.itFeatures.hasNext()) {
            this.currentSourceFeat = this.itFeatures.next();
            if (this.currentSourceFeat.getGeometry() != null) continue;
            this.currentSourceFeat = null;
        }
        if (this.currentSourceFeat == null) {
            return null;
        }
        BasicFeature resultFeat = new BasicFeature(this.resultSchema);
        FeatureUtil.copyAttributes(this.currentSourceFeat, resultFeat);
        Geometry g = this.currentSourceFeat.getGeometry();
        Geometry g2 = (Geometry)g.clone();
        g2.apply((CoordinateSequenceFilter)this.affineTransformation);
        resultFeat.setGeometry(g2);
        this.currentSourceFeat = null;
        return resultFeat;
    }

    @Override
    public int size() throws Exception {
        return -1;
    }

    @Override
    public void reset() {
        if (this.itFeatures != null) {
            this.itFeatures.close();
        }
        try {
            this.itFeatures = this.affineDialog.getFeaturesToProcess(this.plugInContext);
        }
        catch (Exception e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        this.currentSourceFeat = null;
    }

    @Override
    public void close() {
        this.nextFeat = null;
        if (this.itFeatures != null) {
            this.itFeatures.close();
        }
    }

    @Override
    public void close(boolean isCancel) {
        this.nextFeat = null;
        if (this.itFeatures != null) {
            this.itFeatures.close();
        }
    }
}

