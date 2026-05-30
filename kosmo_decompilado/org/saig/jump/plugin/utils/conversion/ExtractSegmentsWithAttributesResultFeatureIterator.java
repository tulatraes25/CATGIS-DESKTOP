/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.geom.LineSegmentUtil;
import com.vividsolutions.jump.workbench.ui.plugin.edit.SegmentsExtracter;
import es.kosmo.core.dao.datasource.memory.AbstractResultsFeatureIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.saig.core.model.feature.FeatureIterator;

public class ExtractSegmentsWithAttributesResultFeatureIterator
extends AbstractResultsFeatureIterator {
    protected FeatureCollection sourceFC;
    protected FeatureSchema resultSchema;
    protected String resultSchemaPkName;
    protected AttributeType resultSchemaPkAttrType;
    protected FeatureIterator itFeatures;
    protected Feature currentSourceFeat;
    protected String sourcePkAttrName;
    protected int currentFeatId;
    protected List<LineString> linestringList;

    public ExtractSegmentsWithAttributesResultFeatureIterator(FeatureCollection fc, FeatureSchema schema, String idOrigAttrName) {
        this.sourceFC = fc;
        this.resultSchema = schema;
        this.resultSchemaPkName = this.resultSchema.getPrimaryKeyName();
        this.resultSchemaPkAttrType = this.resultSchema.getPrimaryKey().getType();
        this.sourcePkAttrName = idOrigAttrName;
        this.itFeatures = this.sourceFC.iterator();
        this.currentFeatId = 0;
    }

    @Override
    protected Feature generateNextFeature() throws Exception {
        while (this.currentSourceFeat == null && this.itFeatures.hasNext()) {
            this.currentSourceFeat = this.itFeatures.next();
            if (this.currentSourceFeat.getGeometry() == null) {
                this.currentSourceFeat = null;
            }
            SegmentsExtracter extracter = new SegmentsExtracter();
            extracter.add(this.currentSourceFeat.getGeometry());
            Collection<LineSegment> uniqueFSList = extracter.getAllSegments();
            this.linestringList = this.toLineStrings(uniqueFSList);
            if (!CollectionUtils.isEmpty(this.linestringList)) continue;
            this.currentSourceFeat = null;
        }
        if (this.currentSourceFeat == null) {
            return null;
        }
        BasicFeature resultFeat = new BasicFeature(this.resultSchema);
        FeatureUtil.copyAttributes(this.currentSourceFeat, resultFeat);
        resultFeat.setAttribute(this.resultSchemaPkName, FeatureUtil.getGoodAttribute(this.resultSchemaPkAttrType, this.currentFeatId++));
        resultFeat.setAttribute(this.sourcePkAttrName, this.currentSourceFeat.getPrimaryKey());
        resultFeat.setGeometry((Geometry)this.linestringList.get(0));
        this.linestringList.remove(0);
        if (CollectionUtils.isEmpty(this.linestringList)) {
            this.currentSourceFeat = null;
            this.linestringList = null;
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

    protected List<LineString> toLineStrings(Collection<LineSegment> segments) {
        ArrayList<LineString> lineStringList = new ArrayList<LineString>();
        for (LineSegment seg : segments) {
            LineString ls = LineSegmentUtil.asGeometry(this.geomFact, seg);
            lineStringList.add(ls);
        }
        return lineStringList;
    }
}

