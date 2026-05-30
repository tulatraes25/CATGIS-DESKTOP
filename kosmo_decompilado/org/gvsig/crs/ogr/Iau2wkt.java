/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import org.gvsig.crs.CrsWkt;

public class Iau2wkt {
    String[] projectionsIAU = new String[]{"Albers"};
    String[] projectionsGDAL = new String[]{"Albers_Conic_Equal_Area"};
    CrsWkt datos = null;
    String cadWKT = "";
    String[] param_name = null;
    String[] param_value = null;

    public Iau2wkt(String wkt) {
        this.datos = new CrsWkt(wkt);
        this.param_name = this.datos.getParam_name();
        this.param_value = this.datos.getParam_value();
    }

    public String getWkt() {
        String proj = this.getNameProjectionWkt(this.datos.getProjection());
        this.cadWKT = "PROJCS[\"" + this.datos.getProjcs() + "\", GEOGCS[\"" + this.datos.getGeogcs() + "\", DATUM[\"" + this.datos.getDatumName() + "\", SPHEROID[\"" + this.datos.getSpheroid()[0] + "\", " + this.datos.getSpheroid()[1] + ", " + this.datos.getSpheroid()[2] + "]], " + "PRIMEM[\"" + this.datos.getPrimen()[0] + "\", " + this.datos.getPrimen()[1] + "], UNIT[\"" + this.datos.getUnit()[0] + "\", " + Math.PI / 180 + "]], PROJECTION[\"" + proj + "\"], ";
        int i = 0;
        while (i < this.param_name.length) {
            if (proj.equals("Mercator_1SP")) {
                if (this.param_name[i].equals("Standard_Parallel_1")) {
                    this.cadWKT = this.param_value[i].startsWith("-") ? String.valueOf(this.cadWKT) + "PARAMETER[\"" + this.param_name[i] + "\", " + this.param_value[i].substring(1, this.param_value[i].length()) + "], " : String.valueOf(this.cadWKT) + "PARAMETER[\"" + this.param_name[i] + "\", " + this.param_value[i] + "], ";
                } else if (!this.param_name[i].equals("Standard_Parallel_2")) {
                    this.cadWKT = String.valueOf(this.cadWKT) + "PARAMETER[\"" + this.param_name[i] + "\", " + this.param_value[i] + "], ";
                }
            } else {
                this.cadWKT = String.valueOf(this.cadWKT) + "PARAMETER[\"" + this.param_name[i] + "\", " + this.param_value[i] + "], ";
            }
            ++i;
        }
        this.cadWKT = String.valueOf(this.cadWKT) + "UNIT[\"" + this.datos.getUnit_p()[0] + "\", 1.0], ";
        this.cadWKT = String.valueOf(this.cadWKT) + "AUTHORITY[\"" + this.datos.getAuthority()[0] + "\", " + this.datos.getAuthority()[1] + "]]";
        return this.cadWKT;
    }

    private String getNameProjectionWkt(String projection) {
        String proj = null;
        int i = 0;
        while (i < this.projectionsIAU.length) {
            if (projection.equals(this.projectionsIAU[i])) {
                projection = this.projectionsGDAL[i];
            }
            ++i;
        }
        if (projection.equals("Lambert_Conformal_Conic")) {
            projection = "Lambert_Conformal_Conic_1SP";
            String standardParallel1 = "";
            String standardParallel2 = "";
            int i2 = 0;
            while (i2 < this.param_name.length) {
                if (this.param_name[i2].equals("Standard_Parallel_1")) {
                    standardParallel1 = this.param_value[i2];
                }
                if (this.param_name[i2].equals("Standard_Parallel_2")) {
                    standardParallel2 = this.param_value[i2];
                    projection = "Lambert_Conformal_Conic_2SP";
                }
                ++i2;
            }
            if (standardParallel1.startsWith("-") && standardParallel2.equals(standardParallel1.substring(1, standardParallel1.length()))) {
                projection = "Mercator_1SP";
            }
        }
        if (projection.equals("Mercator")) {
            projection = "Mercator_1SP";
            i = 0;
            while (i < this.param_name.length) {
                if (this.param_name[i].equals("Standard_Parallel_2")) {
                    projection = "Mercator_2SP";
                }
                ++i;
            }
        }
        proj = projection.replaceAll(" ", "_");
        return proj;
    }
}

