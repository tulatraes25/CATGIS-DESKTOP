/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import java.util.ArrayList;
import org.gvsig.crs.ogr.GetCRSepsg;

public class Epsg2wkt {
    String cadWKT = "";
    String[] paramsEPSG = new String[]{"Angle from Rectified to Skew Grid", "Azimuth of initial line", "Easting at false origin", "Easting at projection centre", "False easting", "False northing", "Latitude of false origin", "Latitude of 1st standard parallel", "Latitude of natural origin", "Latitude of origin", "Latitude of projection centre", "Latitude of 2nd standard parallel", "Longitude of false origin", "Longitude of natural origin", "Longitude of origin", "Longitude of projection centre", "Northing at false origin", "Northing at projection centre", "Scale factor at natural origin", "Scale factor on initial line"};
    String[] paramsWkt = new String[]{"rectified_grid_angle", "azimuth", "false_easting", "false_easting", "false_easting", "false_northing", "latitude_of_origin", "standard_parallel_1", "latitude_of_origin", "latitude_of_origin", "latitude_of_center", "standard_parallel_2", "central_meridian", "central_meridian", "central_meridian", "longitude_of_center", "false_northing", "false_northing", "scale_factor", "scale_factor"};
    String[] projectionsEPSG = new String[]{"Lambert Conic Conformal (1SP)", "Lambert Conic Conformal (2SP)", "Lambert Conic Conformal (2SP) Belgium", "American Polyconic", "Krovak Oblique Conic Conformal", "Albers Equal Area"};
    String[] projectionsWkt = new String[]{"Lambert Conformal Conic 1SP", "Lambert Conformal Conic 2SP", "Lambert Conformal Conic 2SP Belgium", "Polyconic", "Krovak", "Albers Conic Equal Area"};

    public Epsg2wkt(GetCRSepsg epsg, String kind) {
        if (kind.equals("proj")) {
            String[] spheroid = epsg.getSPHEROID();
            String[] primem = epsg.getPRIMEM();
            String[] param_name = epsg.getParam_name();
            String[] param_value = epsg.getParam_value();
            String[] authority = epsg.getAUTHORITY();
            this.cadWKT = "PROJCS[\"" + epsg.getPROJCS() + "\", GEOGCS[\"" + epsg.getGEOGCS() + "\", DATUM[\"" + epsg.getDATUM() + "\", SPHEROID[\"" + spheroid[0] + "\", " + spheroid[1] + ", " + spheroid[2] + "]], " + "PRIMEM[\"" + primem[0] + "\", " + primem[1] + "], UNIT[\"" + epsg.getUNIT_A() + "\", " + Math.PI / 180 + "]], PROJECTION[\"" + this.getNameProjectionWkt(epsg.getPROJECTION()) + "\"], ";
            int i = 0;
            while (i < param_name.length) {
                param_name[i] = this.getParametersWkt(param_name[i]);
                this.cadWKT = String.valueOf(this.cadWKT) + "PARAMETER[\"" + param_name[i] + "\", " + param_value[i] + "], ";
                ++i;
            }
            this.cadWKT = String.valueOf(this.cadWKT) + "UNIT[\"" + epsg.getUNIT_B() + "\", 1.0], ";
            this.cadWKT = String.valueOf(this.cadWKT) + "AUTHORITY[\"" + authority[0] + "\", " + authority[1] + "]]";
        } else if (kind.equals("geog")) {
            String[] spheroid = epsg.getSPHEROID();
            String[] primem = epsg.getPRIMEM();
            String[] authority = epsg.getAUTHORITY();
            this.cadWKT = "GEOGCS[\"" + epsg.getGEOGCS() + "\", DATUM[\"" + epsg.getDATUM() + "\", SPHEROID[\"" + spheroid[0] + "\", " + spheroid[1] + ", " + spheroid[2] + "]], " + "PRIMEM[\"" + primem[0] + "\", " + primem[1] + "], UNIT[\"" + epsg.getUNIT_A() + "\", " + Math.PI / 180 + "], ";
            this.cadWKT = String.valueOf(this.cadWKT) + "AUTHORITY[\"" + authority[0] + "\", " + authority[1] + "]]";
        } else if (kind.equals("geoc")) {
            String[] spheroid = epsg.getSPHEROID();
            String[] primem = epsg.getPRIMEM();
            this.cadWKT = "GEOCCS[\"" + epsg.getGEOGCS() + "\", DATUM[\"" + epsg.getDATUM() + "\", SPHEROID[\"" + spheroid[0] + "\", " + spheroid[1] + ", " + spheroid[2] + "]], " + "PRIMEM[\"" + primem[0] + "\", " + primem[1] + "], UNIT[\"" + epsg.getUNIT_A() + "\", " + Math.PI / 180 + "]]";
        } else {
            kind.equals("comp");
        }
    }

    public Epsg2wkt(String localcs, String local_datum, String unit, ArrayList axis) {
        this.cadWKT = "LOCAL_CS[" + localcs + ", LOCAL_DATUM[" + local_datum + "], UNIT[" + unit + "]";
        int i = 0;
        while (i < axis.size()) {
            this.cadWKT = String.valueOf(this.cadWKT) + ", AXIS[" + axis.get(i) + "]";
            ++i;
        }
        this.cadWKT = String.valueOf(this.cadWKT) + "]";
    }

    public String getWKT() {
        return this.cadWKT;
    }

    private String getNameProjectionWkt(String projection) {
        String proj = null;
        int i = 0;
        while (i < this.projectionsEPSG.length) {
            if (this.projectionsEPSG[i].equals(projection)) {
                projection = this.projectionsWkt[i];
            }
            ++i;
        }
        proj = projection.replaceAll(" ", "_");
        return proj;
    }

    private String getParametersWkt(String param) {
        int j = 0;
        while (j < this.paramsEPSG.length) {
            if (param.equals(this.paramsEPSG[j])) {
                param = this.paramsWkt[j];
            }
            ++j;
        }
        return param.replaceAll(" ", "_");
    }
}

