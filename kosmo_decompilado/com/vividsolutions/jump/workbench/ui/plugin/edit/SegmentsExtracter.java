/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineSegment
 */
package com.vividsolutions.jump.workbench.ui.plugin.edit;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.DummyTaskMonitor;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.util.CoordinateArrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class SegmentsExtracter {
    private Map<LineSegment, SegmentCount> segmentMap = new TreeMap<LineSegment, SegmentCount>();
    private boolean countZeroLengthSegments = true;
    private TaskMonitor monitor;
    private Geometry fence = null;

    public SegmentsExtracter() {
        this.monitor = new DummyTaskMonitor();
    }

    public SegmentsExtracter(TaskMonitor monitor) {
        this.monitor = monitor;
    }

    /*
     * Handled impossible loop by duplicating code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void add(FeatureCollection fc) throws Exception {
        this.monitor.allowCancellationRequests();
        int totalFeatures = fc.size();
        int j = 0;
        FeatureIterator it = null;
        try {
            block7: {
                block6: {
                    it = fc.iterator();
                    if (!true) break block6;
                    if (this.monitor.isCancelRequested()) return;
                    if (!it.hasNext()) break block7;
                }
                do {
                    Feature feature = it.next();
                    this.monitor.report(++j, totalFeatures, I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.edit.SegmentsExtracter.processed-elements"));
                    this.add(feature);
                    if (this.monitor.isCancelRequested()) return;
                } while (it.hasNext());
            }
            return;
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
    }

    public void add(Feature f) {
        this.add(f.getGeometry());
    }

    public void add(Geometry g) {
        if (this.fence != null && !g.intersects(this.fence)) {
            return;
        }
        List<Coordinate[]> coordArrays = CoordinateArrays.toCoordinateArrays(g, true);
        for (Coordinate[] coord : coordArrays) {
            int j = 0;
            while (j < coord.length - 1) {
                this.add(coord[j], coord[j + 1]);
                ++j;
            }
        }
    }

    public void add(Coordinate p0, Coordinate p1) {
        boolean isZeroLength = p0.equals((Object)p1);
        if (!this.countZeroLengthSegments && isZeroLength) {
            return;
        }
        LineSegment lineseg = new LineSegment(p0, p1);
        lineseg.normalize();
        SegmentCount count = this.segmentMap.get(lineseg);
        if (count == null) {
            this.segmentMap.put(lineseg, new SegmentCount(1));
        } else {
            count.increment();
        }
    }

    public Collection<LineSegment> getSegments() {
        return this.segmentMap.keySet();
    }

    public Collection<LineSegment> getSegments(int minOccurs, int maxOccurs) {
        ArrayList<LineSegment> segmentList = new ArrayList<LineSegment>();
        for (Map.Entry<LineSegment, SegmentCount> entry : this.segmentMap.entrySet()) {
            LineSegment ls = entry.getKey();
            int count = entry.getValue().getCount();
            if (count < minOccurs || count > maxOccurs) continue;
            segmentList.add(ls);
        }
        return segmentList;
    }

    public Collection<LineSegment> getAllSegments() {
        ArrayList<LineSegment> segmentList = new ArrayList<LineSegment>();
        for (Map.Entry<LineSegment, SegmentCount> entry : this.segmentMap.entrySet()) {
            LineSegment ls = entry.getKey();
            int count = entry.getValue().getCount();
            segmentList.add(ls);
            int i = 1;
            while (i < count) {
                segmentList.add(new LineSegment(new Coordinate(ls.p0), new Coordinate(ls.p1)));
                ++i;
            }
        }
        return segmentList;
    }

    public class SegmentCount {
        private int count = 0;

        public SegmentCount(int value) {
            this.count = value;
        }

        public int getCount() {
            return this.count;
        }

        public void increment() {
            ++this.count;
        }
    }
}

