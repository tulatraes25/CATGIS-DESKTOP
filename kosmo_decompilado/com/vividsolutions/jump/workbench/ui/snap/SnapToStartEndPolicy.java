/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryFactory
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 */
package com.vividsolutions.jump.workbench.ui.snap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import com.vividsolutions.jump.workbench.ui.snap.SnapOptionsPanel;
import com.vividsolutions.jump.workbench.ui.snap.SnapPolicy;
import com.vividsolutions.jump.workbench.ui.snap.VisiblePointsAndLinesCache;
import java.awt.Color;
import org.saig.jump.lang.I18N;

public class SnapToStartEndPolicy
implements SnapPolicy {
    public static final String NAME = I18N.getString("com.vividsolutions.jump.workbench.ui.snap.SnapToStartEndPolicy.Snap-to-element-starts-and-ends");
    public static final String ENABLED_KEY = String.valueOf(SnapToStartEndPolicy.class.getName()) + " - ENABLED";
    private Color color;
    private GeometryFactory factory = new GeometryFactory();
    private Blackboard blackboard;

    public SnapToStartEndPolicy(Blackboard blackboard) {
        this.blackboard = blackboard;
        this.color = SnapOptionsPanel.getSnapToStartEndColor();
    }

    public SnapToStartEndPolicy() {
        this(new Blackboard());
        this.blackboard.put(ENABLED_KEY, true);
    }

    @Override
    public Coordinate snap(LayerViewPanel panel, Coordinate originalCoordinate, Geometry toSpecificGeometry) {
        if (!PersistentBlackboardPlugIn.get(this.blackboard).get(ENABLED_KEY, false)) {
            return null;
        }
        Object value = PersistentBlackboardPlugIn.get(this.blackboard).get("SNAP_BUFFER");
        double units = 10.0;
        if (value != null) {
            units = (Double)value;
        }
        Geometry bufferedTransformedCursorLocation = this.factory.createPoint(originalCoordinate).buffer(units);
        Coordinate closest = null;
        double distance = Double.MAX_VALUE;
        LineString ls = null;
        MultiLineString mls = null;
        if (toSpecificGeometry != null) {
            Geometry candidate = VisiblePointsAndLinesCache.toPointsAndLines(toSpecificGeometry);
            if (candidate instanceof LineString) {
                ls = (LineString)candidate;
                Coordinate auxCord = ls.getStartPoint().getCoordinate();
                double disAux = auxCord.distance(originalCoordinate);
                if (disAux < units && distance > disAux) {
                    distance = disAux;
                    closest = auxCord;
                }
                if ((disAux = (auxCord = ls.getEndPoint().getCoordinate()).distance(originalCoordinate)) < units && distance > disAux) {
                    distance = disAux;
                    closest = auxCord;
                }
            } else if (candidate instanceof MultiLineString) {
                mls = (MultiLineString)candidate;
                int k = 0;
                while (k < mls.getNumGeometries()) {
                    ls = (LineString)mls.getGeometryN(k);
                    Coordinate auxCord = ls.getStartPoint().getCoordinate();
                    double disAux = auxCord.distance(originalCoordinate);
                    if (disAux < units && distance > disAux) {
                        distance = disAux;
                        closest = auxCord;
                    }
                    if ((disAux = (auxCord = ls.getEndPoint().getCoordinate()).distance(originalCoordinate)) < units && distance > disAux) {
                        distance = disAux;
                        closest = auxCord;
                    }
                    ++k;
                }
            }
        } else {
            for (Geometry candidate : VisiblePointsAndLinesCache.instance(panel).getTree().query(bufferedTransformedCursorLocation.getEnvelopeInternal())) {
                double disAux;
                Coordinate auxCord;
                if (candidate instanceof LineString) {
                    ls = (LineString)candidate;
                    auxCord = ls.getStartPoint().getCoordinate();
                    disAux = auxCord.distance(originalCoordinate);
                    if (disAux < units && distance > disAux) {
                        distance = disAux;
                        closest = auxCord;
                    }
                    if (!((disAux = (auxCord = ls.getEndPoint().getCoordinate()).distance(originalCoordinate)) < units) || !(distance > disAux)) continue;
                    distance = disAux;
                    closest = auxCord;
                    continue;
                }
                if (!(candidate instanceof MultiLineString)) continue;
                mls = (MultiLineString)candidate;
                int k = 0;
                while (k < mls.getNumGeometries()) {
                    ls = (LineString)mls.getGeometryN(k);
                    auxCord = ls.getStartPoint().getCoordinate();
                    disAux = auxCord.distance(originalCoordinate);
                    if (disAux < units && distance > disAux) {
                        distance = disAux;
                        closest = auxCord;
                    }
                    if ((disAux = (auxCord = ls.getEndPoint().getCoordinate()).distance(originalCoordinate)) < units && distance > disAux) {
                        distance = disAux;
                        closest = auxCord;
                    }
                    ++k;
                }
            }
        }
        this.color = SnapOptionsPanel.getSnapToStartEndColor();
        return closest;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

