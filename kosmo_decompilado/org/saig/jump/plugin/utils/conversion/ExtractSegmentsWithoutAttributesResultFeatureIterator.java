/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.operation.linemerge.LineMerger
 */
package org.saig.jump.plugin.utils.conversion;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
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
import java.util.Iterator;
import java.util.List;

public class ExtractSegmentsWithoutAttributesResultFeatureIterator
extends AbstractResultsFeatureIterator {
    protected FeatureCollection sourceFC;
    protected FeatureSchema resultSchema;
    protected String resultSchemaPkName;
    protected AttributeType resultSchemaPkAttrType;
    protected int currentFeatId;
    protected Collection<LineString> sourceLinestringList;
    protected Iterator<LineString> sourceLinestringIterator;
    protected boolean mergeResults;
    protected boolean allSegmentsSelected;
    protected boolean allSegmentsOneTimeSelected;

    public ExtractSegmentsWithoutAttributesResultFeatureIterator(FeatureCollection fc, FeatureSchema schema, boolean mergeResults, boolean allSegmentsSelected, boolean allSegmentsOneTimeSelected) {
        this.sourceFC = fc;
        this.resultSchema = schema;
        this.resultSchemaPkName = this.resultSchema.getPrimaryKeyName();
        this.resultSchemaPkAttrType = this.resultSchema.getPrimaryKey().getType();
        this.currentFeatId = 0;
        this.mergeResults = mergeResults;
        this.allSegmentsSelected = allSegmentsSelected;
        this.allSegmentsOneTimeSelected = allSegmentsOneTimeSelected;
    }

    @Override
    protected Feature generateNextFeature() throws Exception {
        if (!this.sourceLinestringIterator.hasNext()) {
            return null;
        }
        BasicFeature resultFeat = new BasicFeature(this.resultSchema);
        resultFeat.setAttribute(this.resultSchemaPkName, FeatureUtil.getGoodAttribute(this.resultSchemaPkAttrType, this.currentFeatId++));
        resultFeat.setGeometry((Geometry)this.sourceLinestringIterator.next());
        return resultFeat;
    }

    @Override
    public int size() throws Exception {
        return -1;
    }

    @Override
    public void reset() {
        this.sourceLinestringIterator = this.sourceLinestringList.iterator();
        this.currentFeatId = 0;
    }

    @Override
    public void close() {
        this.nextFeat = null;
        this.sourceLinestringIterator = null;
        this.sourceLinestringList = null;
    }

    @Override
    public void close(boolean isCancel) {
        this.nextFeat = null;
        this.sourceLinestringIterator = null;
        this.sourceLinestringList = null;
    }

    protected List<LineString> toLineStrings(Collection<LineSegment> segments) {
        ArrayList<LineString> lineStringList = new ArrayList<LineString>();
        for (LineSegment seg : segments) {
            LineString ls = LineSegmentUtil.asGeometry(this.geomFact, seg);
            lineStringList.add(ls);
        }
        return lineStringList;
    }

    protected List<LineString> toMergedLineStrings(Collection<LineSegment> segments) {
        LineMerger lineMerger = new LineMerger();
        for (LineSegment seg : segments) {
            lineMerger.add((Geometry)LineSegmentUtil.asGeometry(this.geomFact, seg));
        }
        return (List)lineMerger.getMergedLineStrings();
    }

    @Override
    protected void initialize() throws Exception {
        SegmentsExtracter extracter = new SegmentsExtracter();
        extracter.add(this.sourceFC);
        Collection<Object> uniqueFSList = new ArrayList();
        uniqueFSList = this.allSegmentsSelected ? extracter.getAllSegments() : (this.allSegmentsOneTimeSelected ? extracter.getSegments() : extracter.getSegments(1, 1));
        this.sourceLinestringList = this.mergeResults ? this.toMergedLineStrings(uniqueFSList) : this.toLineStrings(uniqueFSList);
        this.sourceLinestringIterator = this.sourceLinestringList.iterator();
    }
}

