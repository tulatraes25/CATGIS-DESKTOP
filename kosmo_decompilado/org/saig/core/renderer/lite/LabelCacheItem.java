/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 */
package org.saig.core.renderer.lite;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.saig.core.renderer.lite.LineToLabel;
import org.saig.core.renderer.lite.LiteShape2;
import org.saig.core.renderer.style.TextStyle2D;
import org.saig.core.styling.TextSymbolizer;

public class LabelCacheItem
implements Comparable<LabelCacheItem> {
    protected TextStyle2D textStyle;
    protected List<Geometry> geoms = new ArrayList<Geometry>();
    protected Geometry originalShape;
    protected double priority = 0.0;
    protected int spaceAround = 0;
    protected String label;
    protected Set<String> layerIds = new HashSet<String>();
    protected LineToLabel lineToLabel;
    private static long staticId = 0L;
    protected long id;
    protected int repeat = 0;
    protected boolean followLineEnabled;
    protected double maxAngleDelta;
    protected int autoWrap;
    protected TextSymbolizer.GraphicResize graphicsResize = TextSymbolizer.GraphicResize.NONE;
    protected int[] graphicMargin = null;
    protected boolean forceLeftToRightEnabled;
    protected int maxDisplacement = 0;
    protected boolean conflictResolutionEnabled;
    protected boolean removeGroupOverlaps;
    protected boolean labelAllGroup;
    protected boolean allowOverruns;
    protected int minGroupDistance;
    protected TextSymbolizer.PolygonAlignOptions polygonAlign;
    protected double goodnessOfFit;

    public LabelCacheItem(String layerId, TextStyle2D textStyle, LiteShape2 shape, LiteShape2 originalShape, String label) {
        this.textStyle = textStyle;
        this.geoms.add(shape.getGeometry());
        this.label = label;
        this.layerIds.add(layerId);
        this.originalShape = originalShape != null ? originalShape.getGeometry() : null;
        this.id = staticId++;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String l) {
        this.label = l;
    }

    public int getSpaceAround() {
        return this.spaceAround;
    }

    public void setSpaceAround(int space) {
        this.spaceAround = space;
    }

    public double getPriority() {
        return this.priority;
    }

    public void setPriority(double d) {
        this.priority = d;
    }

    public Set<String> getLayerIds() {
        return Collections.synchronizedSet(this.layerIds);
    }

    public List<Geometry> getGeoms() {
        return this.geoms;
    }

    public TextStyle2D getTextStyle() {
        return this.textStyle;
    }

    public boolean equals(Object arg0) {
        if (arg0 instanceof String) {
            String label = (String)arg0;
            return label.equals(this.textStyle.getLabel());
        }
        if (arg0 instanceof LabelCacheItem) {
            LabelCacheItem item = (LabelCacheItem)arg0;
            return this.textStyle.getLabel().equals(item.getTextStyle().getLabel());
        }
        if (arg0 instanceof TextStyle2D) {
            TextStyle2D text = (TextStyle2D)arg0;
            return this.textStyle.getLabel().equals(text.getLabel());
        }
        return false;
    }

    public int hashCode() {
        return this.textStyle.getLabel().hashCode();
    }

    public Geometry getGeometry() {
        return this.geoms.get(0);
    }

    @Override
    public int compareTo(LabelCacheItem o) {
        LabelCacheItem other = o;
        return Double.compare(this.getPriority(), other.getPriority());
    }

    public Geometry getOriginalShape() {
        Geometry originalGeom = this.originalShape;
        if (this.originalShape != null && !this.originalShape.isValid()) {
            originalGeom = new GeometryFactory().createPoint(originalGeom.getCoordinate());
        }
        return originalGeom;
    }

    public void setOriginalShape(Geometry originalShape) {
        this.originalShape = originalShape;
    }

    public long getId() {
        return this.id;
    }

    public LineToLabel getLineToLabel() {
        return this.lineToLabel;
    }

    public void setLineToLabel(LineToLabel lineToLabel) {
        this.lineToLabel = lineToLabel;
    }

    public int getRepeat() {
        return this.repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public boolean isFollowLineEnabled() {
        return this.followLineEnabled;
    }

    public void setFollowLineEnabled(boolean followLineEnabled) {
        this.followLineEnabled = followLineEnabled;
    }

    public double getMaxAngleDelta() {
        return this.maxAngleDelta;
    }

    public void setMaxAngleDelta(double maxAngleDelta) {
        this.maxAngleDelta = maxAngleDelta;
    }

    public int getAutoWrap() {
        return this.autoWrap;
    }

    public void setAutoWrap(int autoWrap) {
        this.autoWrap = autoWrap;
    }

    public TextSymbolizer.GraphicResize getGraphicsResize() {
        return this.graphicsResize;
    }

    public void setGraphicsResize(TextSymbolizer.GraphicResize graphicsResize) {
        this.graphicsResize = graphicsResize;
    }

    public int[] getGraphicMargin() {
        return this.graphicMargin;
    }

    public void setGraphicMargin(int[] graphicMargin) {
        this.graphicMargin = graphicMargin;
    }

    public boolean isForceLeftToRightEnabled() {
        return this.forceLeftToRightEnabled;
    }

    public void setForceLeftToRightEnabled(boolean forceLeftToRight) {
        this.forceLeftToRightEnabled = forceLeftToRight;
    }

    public int getMaxDisplacement() {
        return this.maxDisplacement;
    }

    public void setMaxDisplacement(int maxDisplacement) {
        this.maxDisplacement = maxDisplacement;
    }

    public boolean isConflictResolutionEnabled() {
        return this.conflictResolutionEnabled;
    }

    public void setConflictResolutionEnabled(boolean conflictResolutionEnabled) {
        this.conflictResolutionEnabled = conflictResolutionEnabled;
    }

    public boolean removeGroupOverlaps() {
        return this.removeGroupOverlaps;
    }

    public void setRemoveGroupOverlaps(boolean removeGroupOverlaps) {
        this.removeGroupOverlaps = removeGroupOverlaps;
    }

    public boolean labelAllGroup() {
        return this.labelAllGroup;
    }

    public void setLabelAllGroup(boolean labelAllGroup) {
        this.labelAllGroup = labelAllGroup;
    }

    public boolean allowOverruns() {
        return this.allowOverruns;
    }

    public void setAllowOverruns(boolean allowOverruns) {
        this.allowOverruns = allowOverruns;
    }

    public int getMinGroupDistance() {
        return this.minGroupDistance;
    }

    public void setMinGroupDistance(int minGroupDistance) {
        this.minGroupDistance = minGroupDistance;
    }

    public void setPolygonAlign(TextSymbolizer.PolygonAlignOptions polygonAlign) {
        this.polygonAlign = polygonAlign;
    }

    public TextSymbolizer.PolygonAlignOptions getPolygonAlign() {
        return this.polygonAlign;
    }

    public double getGoodnessOfFit() {
        return this.goodnessOfFit;
    }

    public void setGoodnessOfFit(double goodnessOfFit) {
        this.goodnessOfFit = goodnessOfFit;
    }
}

