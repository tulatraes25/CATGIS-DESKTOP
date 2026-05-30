/*
 * Decompiled with CFR 0.152.
 */
package org.gvsig.crs.ogr;

import org.gvsig.crs.CrsWkt;

public class Esri2wkt {
    CrsWkt datos = null;
    String cadWKT = "";
    String[] param_name = null;
    String[] param_value = null;

    public Esri2wkt(String wkt) {
        this.datos = new CrsWkt(wkt);
        this.param_name = this.datos.getParam_name();
        this.param_value = this.datos.getParam_value();
    }

    public String getWkt() {
        String proj = this.getNameProjectionWkt(this.datos.getProjection());
        this.cadWKT = "PROJCS[\"" + this.datos.getProjcs() + "\", GEOGCS[\"" + this.datos.getGeogcs() + "\", DATUM[\"" + this.datos.getDatumName() + "\", SPHEROID[\"" + this.datos.getSpheroid()[0] + "\", " + this.datos.getSpheroid()[1] + ", " + this.datos.getSpheroid()[2] + "]], " + "PRIMEM[\"" + this.datos.getPrimen()[0] + "\", " + this.datos.getPrimen()[1] + "], UNIT[\"" + this.datos.getUnit()[0] + "\", " + Math.PI / 180 + "]], PROJECTION[\"" + proj + "\"], ";
        int i = 0;
        while (i < this.param_name.length) {
            this.cadWKT = String.valueOf(this.cadWKT) + "PARAMETER[\"" + this.param_name[i] + "\", " + this.param_value[i] + "], ";
            ++i;
        }
        this.cadWKT = String.valueOf(this.cadWKT) + "UNIT[\"" + this.datos.getUnit_p()[0] + "\", 1.0], ";
        this.cadWKT = String.valueOf(this.cadWKT) + "AUTHORITY[\"" + this.datos.getAuthority()[0] + "\", " + this.datos.getAuthority()[1] + "]]";
        return this.cadWKT;
    }

    private String getNameProjectionWkt(String projection) {
        if (projection.equals("Lambert_Conformal_Conic")) {
            projection = "Lambert_Conformal_Conic_1SP";
            int i = 0;
            while (i < this.param_name.length) {
                if (this.param_name[i].equals("Standard_Parallel_2")) {
                    projection = "Lambert_Conformal_Conic_2SP";
                }
                ++i;
            }
        }
        return projection;
    }
}

