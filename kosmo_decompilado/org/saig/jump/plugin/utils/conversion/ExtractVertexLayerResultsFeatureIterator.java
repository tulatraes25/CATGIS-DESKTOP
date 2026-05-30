/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import org.saig.core.model.feature.FeatureIterator;

public class ExtractVertexLayerResultsFeatureIterator
extends AbstractResultsFeatureIterator {
    protected FeatureCollection sourceFC;
    protected FeatureSchema resultSchema;
    protected String resultSchemaPkName;
    protected AttributeType resultSchemaPkAttrType;
    protected FeatureIterator itFeatures;
    protected Feature currentSourceFeat;
    protected int currentFeatGeometryPosition;
    protected boolean currentVertexIsStartPoint;
    protected LineString currentLineString;
    protected int currentFeatId;

    public ExtractVertexLayerResultsFeatureIterator(FeatureCollection fc, FeatureSchema schema) {
        this.sourceFC = fc;
        this.resultSchema = schema;
        this.resultSchemaPkName = this.resultSchema.getPrimaryKeyName();
        this.resultSchemaPkAttrType = this.resultSchema.getPrimaryKey().getType();
        this.itFeatures = this.sourceFC.iterator();
        this.currentFeatId = 0;
    }

    @Override
    protected Feature generateNextFeature() throws Exception {
        while (this.currentSourceFeat == null && this.itFeatures.hasNext()) {
            this.currentSourceFeat = this.itFeatures.next();
            if (this.currentSourceFeat.getGeometry() != null) {
                this.currentVertexIsStartPoint = true;
                this.currentFeatGeometryPosition = 0;
                this.currentLineString = (LineString)this.currentSourceFeat.getGeometry().getGeometryN(this.currentFeatGeometryPosition);
                continue;
            }
            this.currentSourceFeat = null;
        }
        if (this.currentSourceFeat == null) {
            return null;
        }
        BasicFeature resultFeat = new BasicFeature(this.resultSchema);
        FeatureUtil.copyAttributes(this.currentSourceFeat, resultFeat);
        resultFeat.setAttribute(this.resultSchemaPkName, FeatureUtil.getGoodAttribute(this.resultSchemaPkAttrType, this.currentFeatId++));
        resultFeat.setGeometry((Geometry)(this.currentVertexIsStartPoint ? this.currentLineString.getStartPoint() : this.currentLineString.getEndPoint()));
        if (!this.currentVertexIsStartPoint) {
            if (++this.currentFeatGeometryPosition == this.currentSourceFeat.getGeometry().getNumGeometries()) {
                this.currentSourceFeat = null;
                this.currentLineString = null;
                this.currentVertexIsStartPoint = true;
            } else {
                this.currentLineString = (LineString)this.currentSourceFeat.getGeometry().getGeometryN(this.currentFeatGeometryPosition);
                this.currentVertexIsStartPoint = true;
            }
        } else {
            this.currentVertexIsStartPoint = false;
        }
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
        this.itFeatures = this.sourceFC.iterator();
        this.currentSourceFeat = null;
        this.currentLineString = null;
        this.currentVertexIsStartPoint = true;
        this.currentFeatId = 0;
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

