/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.coordsys.impl;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.coordsys.CoordinateSystem;
import com.vividsolutions.jump.coordsys.Radius;
import com.vividsolutions.jump.coordsys.Spheroid;
import com.vividsolutions.jump.coordsys.impl.Albers;
import com.vividsolutions.jump.coordsys.impl.LatLong;
import com.vividsolutions.jump.coordsys.impl.UniversalTransverseMercator;

public class PredefinedCoordinateSystems {
    public static final CoordinateSystem BC_ALBERS_NAD_83 = new CoordinateSystem("BC Albers", 42102, new Albers(){
        {
            this.setSpheroid(new Spheroid(new Radius(0)));
            this.setParameters(-126.0, 50.0, 58.5, 45.0, 1000000.0, 0.0);
        }
    });
    public static final CoordinateSystem GEOGRAPHICS_WGS_84 = new CoordinateSystem("Geographics", 4326, new LatLong());
    public static final CoordinateSystem UTM_07N_WGS_84 = PredefinedCoordinateSystems.createUTMNorth(7);
    public static final CoordinateSystem UTM_08N_WGS_84 = PredefinedCoordinateSystems.createUTMNorth(8);
    public static final CoordinateSystem UTM_09N_WGS_84 = PredefinedCoordinateSystems.createUTMNorth(9);
    public static final CoordinateSystem UTM_10N_WGS_84 = PredefinedCoordinateSystems.createUTMNorth(10);
    public static final CoordinateSystem UTM_11N_WGS_84 = PredefinedCoordinateSystems.createUTMNorth(11);
    public static final CoordinateSystem UTM_30N_ED50 = PredefinedCoordinateSystems.createED50(30);
    public static final CoordinateSystem UTM_29N_ED50 = PredefinedCoordinateSystems.createED50(29);
    public static final CoordinateSystem UTM_31N_ED50 = PredefinedCoordinateSystems.createED50(31);

    private PredefinedCoordinateSystems() {
    }

    public static CoordinateSystem createUTMNorth(int zone) {
        Assert.isTrue((1 <= zone && zone <= 60 ? 1 : 0) != 0);
        return new CoordinateSystem("UTM " + (zone < 10 ? "0" : "") + zone + "N", 32600 + zone, new UniversalTransverseMercator(zone){
            {
                this.setSpheroid(new Spheroid(new Radius(0)));
                this.setParameters(n);
            }
        });
    }

    public static CoordinateSystem createED50(int zone) {
        Assert.isTrue((1 <= zone && zone <= 60 ? 1 : 0) != 0);
        return new CoordinateSystem("UTM " + (zone < 10 ? "0" : "") + zone + "N ED50", 23000 + zone, new UniversalTransverseMercator(zone){
            {
                this.setSpheroid(new Spheroid(new Radius(3)));
                this.setParameters(n);
            }
        });
    }

    public static CoordinateSystem getCoordinateSystem(int epsgCode) {
        CoordinateSystem cs = null;
        if (epsgCode == GEOGRAPHICS_WGS_84.getEPSGCode()) {
            cs = GEOGRAPHICS_WGS_84;
        } else if (epsgCode == BC_ALBERS_NAD_83.getEPSGCode()) {
            cs = BC_ALBERS_NAD_83;
        } else if (epsgCode == UTM_07N_WGS_84.getEPSGCode()) {
            cs = UTM_07N_WGS_84;
        } else if (epsgCode == UTM_08N_WGS_84.getEPSGCode()) {
            cs = UTM_08N_WGS_84;
        } else if (epsgCode == UTM_09N_WGS_84.getEPSGCode()) {
            cs = UTM_09N_WGS_84;
        } else if (epsgCode == UTM_10N_WGS_84.getEPSGCode()) {
            cs = UTM_10N_WGS_84;
        } else if (epsgCode == UTM_11N_WGS_84.getEPSGCode()) {
            cs = UTM_11N_WGS_84;
        } else if (epsgCode == UTM_29N_ED50.getEPSGCode()) {
            cs = UTM_29N_ED50;
        } else if (epsgCode == UTM_30N_ED50.getEPSGCode()) {
            cs = UTM_30N_ED50;
        } else if (epsgCode == UTM_31N_ED50.getEPSGCode()) {
            cs = UTM_31N_ED50;
        }
        return cs;
    }
}

