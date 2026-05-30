/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.CoordinateFilter
 *  com.vividsolutions.jts.geom.Geometry
 */
package com.vividsolutions.jump.coordsys;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.coordsys.CoordinateSystem;
import com.vividsolutions.jump.coordsys.Geographic;
import com.vividsolutions.jump.coordsys.Planar;

public class Reprojector {
    private static Reprojector instance = new Reprojector();

    private Reprojector() {
    }

    public static Reprojector instance() {
        return instance;
    }

    public boolean wouldChangeValues(CoordinateSystem source, CoordinateSystem destination) {
        if (source == CoordinateSystem.UNSPECIFIED) {
            return false;
        }
        if (destination == CoordinateSystem.UNSPECIFIED) {
            return false;
        }
        return source != destination;
    }

    public void reproject(Coordinate coordinate, CoordinateSystem source, CoordinateSystem destination) {
        if (!this.wouldChangeValues(source, destination)) {
            return;
        }
        Planar result = destination.getProjection().asPlanar(source.getProjection().asGeographic(new Planar(coordinate.x, coordinate.y), new Geographic()), new Planar());
        coordinate.x = result.x;
        coordinate.y = result.y;
    }

    public void reproject(Geometry geometry, final CoordinateSystem source, final CoordinateSystem destination) {
        if (!this.wouldChangeValues(source, destination)) {
            return;
        }
        geometry.apply(new CoordinateFilter(){

            public void filter(Coordinate coord) {
                Reprojector.this.reproject(coord, source, destination);
            }
        });
        geometry.setSRID(destination.getEPSGCode());
        geometry.geometryChanged();
    }
}

