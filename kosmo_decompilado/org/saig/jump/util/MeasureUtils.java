/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  javax.measure.converter.UnitConverter
 *  javax.measure.quantity.Length
 *  javax.measure.unit.NonSI
 *  javax.measure.unit.SI
 *  javax.measure.unit.Unit
 *  org.cresques.cts.GeoCalc
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.util;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.Point2D;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.cresques.cts.GeoCalc;
import org.cresques.cts.IProjection;
import org.saig.core.util.UnitsManager;

public class MeasureUtils {
    public static double transformLenght(double value, Unit<Length> from, Unit<Length> to) {
        UnitConverter converterTo = from.getConverterTo(to);
        return converterTo.convert(value);
    }

    public static double getLengthTransformFactor(Unit<Length> from, Unit<Length> to) {
        UnitConverter converterTo = from.getConverterTo(to);
        return converterTo.convert(1.0);
    }

    public static Unit<Length> getLenghtUnitByName(String unitName) {
        return AceptedLengthUnits.getUnitByName(unitName);
    }

    public static String convertFromMapUnitToUserUnit(double mapDistance, Unit<Length> mapUnit, Unit<Length> userUnit) {
        return UnitsManager.convertDistanceValueToString(mapDistance, mapUnit, userUnit);
    }

    public static double distanceWorld(Point2D pt1, Point2D pt2, IProjection proj) {
        double dist = -1.0;
        dist = pt1.distance(pt2);
        if (proj != null && !proj.isProjected()) {
            dist = new GeoCalc(proj).distanceVincenty(proj.toGeo(pt1), proj.toGeo(pt2));
        }
        return dist;
    }

    public static double distanceWorld(Coordinate[] coordinates, IProjection proj) {
        double totalDistance = 0.0;
        int i = 1;
        while (i < coordinates.length) {
            Point2D.Double point1 = new Point2D.Double(coordinates[i - 1].x, coordinates[i - 1].y);
            Point2D.Double point2 = new Point2D.Double(coordinates[i].x, coordinates[i].y);
            totalDistance += MeasureUtils.distanceWorld(point1, point2, proj);
            ++i;
        }
        return totalDistance;
    }

    public static enum AceptedLengthUnits {
        METER((Unit<Length>)SI.METER),
        KILOMETER((Unit<Length>)SI.KILOMETER),
        MILIMETER((Unit<Length>)SI.MILLIMETER),
        CENTIMETER((Unit<Length>)SI.CENTIMETER),
        INCH((Unit<Length>)NonSI.INCH),
        FOOT((Unit<Length>)NonSI.FOOT),
        MILE((Unit<Length>)NonSI.MILE);

        private Unit<Length> unit;

        private AceptedLengthUnits(Unit<Length> unit) {
            this.unit = unit;
        }

        public Unit<Length> getUnit() {
            return this.unit;
        }

        public static AceptedLengthUnits getUnit(String unitName) {
            AceptedLengthUnits[] aceptedLengthUnitsArray = AceptedLengthUnits.values();
            int n = aceptedLengthUnitsArray.length;
            int n2 = 0;
            while (n2 < n) {
                AceptedLengthUnits unit = aceptedLengthUnitsArray[n2];
                if (unit.getUnit().toString().equals(unitName)) {
                    return unit;
                }
                ++n2;
            }
            return null;
        }

        protected static Unit<Length> getUnitByName(String unitName) {
            return AceptedLengthUnits.getUnit(unitName).getUnit();
        }
    }
}

