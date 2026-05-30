/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import org.saig.core.model.feature.FeatureIterator;

public class GetPointsFromLinesResultsFeatureIterator
extends AbstractResultsFeatureIterator {
    protected FeatureCollection sourceFC;
    protected FeatureSchema resultSchema;
    protected String resultSchemaPkName;
    protected AttributeType resultSchemaPkAttrType;
    protected String sourcePKFieldName;
    protected FeatureIterator itFeatures;
    protected Feature currentSourceFeat;
    protected int currentFeatVertexPosition;
    protected Coordinate[] coords;
    protected String orderAttributeName;
    protected int currentFeatId;

    public GetPointsFromLinesResultsFeatureIterator(FeatureCollection fc, FeatureSchema schema, String pkAttrName, String orderAttrName) {
        this.sourceFC = fc;
        this.resultSchema = schema;
        this.resultSchemaPkName = this.resultSchema.getPrimaryKeyName();
        this.resultSchemaPkAttrType = this.resultSchema.getPrimaryKey().getType();
        this.sourcePKFieldName = pkAttrName;
        this.orderAttributeName = orderAttrName;
        this.itFeatures = this.sourceFC.iterator();
        this.currentFeatId = 0;
    }

    @Override
    protected Feature generateNextFeature() throws Exception {
        while (this.currentSourceFeat == null && this.itFeatures.hasNext()) {
            this.currentSourceFeat = this.itFeatures.next();
            if (this.currentSourceFeat.getGeometry() != null) {
                this.currentFeatVertexPosition = 0;
                this.coords = this.currentSourceFeat.getGeometry().getCoordinates();
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
        resultFeat.setAttribute(this.sourcePKFieldName, this.currentSourceFeat.getPrimaryKey());
        resultFeat.setAttribute(this.orderAttributeName, (Object)new Integer(this.currentFeatVertexPosition));
        resultFeat.setGeometry((Geometry)this.geomFact.createPoint(this.coords[this.currentFeatVertexPosition]));
        if (++this.currentFeatVertexPosition == this.coords.length) {
            this.currentSourceFeat = null;
            this.coords = null;
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
        this.currentFeatVertexPosition = 0;
        this.coords = null;
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

