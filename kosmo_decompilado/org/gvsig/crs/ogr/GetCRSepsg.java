/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.gvsig.crs.ogr;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import org.gvsig.crs.CRSRepositoryConnection;
import org.gvsig.crs.Query;
import org.saig.jump.lang.I18N;

public class GetCRSepsg {
    private static final Logger LOGGER = Logger.getLogger(GetCRSepsg.class);
    int epsg_code;
    boolean crs_source;
    int source_code;
    int projection_code;
    int coord_op_code;
    int datum_code;
    int ellipsoid_code;
    int prime_meridian_code;
    String PROJCS = null;
    String GEOGCS = null;
    String DATUM = null;
    String[] SPHEROID = null;
    String[] PRIMEM = null;
    String UNIT_A = "Decimal Degree";
    String UNIT_B = "Meter";
    String PROJECTION = null;
    String[] param_name = null;
    String[] param_value = null;
    String[] AUTHORITY = null;
    CRSRepositoryConnection connect;

    public GetCRSepsg() {
    }

    public GetCRSepsg(int code, boolean source_yn, int source_cod, int coord_op_cod, CRSRepositoryConnection conn) {
        this.epsg_code = code;
        this.crs_source = source_yn;
        this.source_code = source_cod;
        this.coord_op_code = coord_op_cod;
        this.connect = conn;
        this.SPHEROID = new String[3];
        this.PRIMEM = new String[2];
        this.AUTHORITY = new String[2];
    }

    public void Getepsgdata() {
        ResultSet result;
        String sentence;
        if (this.crs_source) {
            sentence = "SELECT coord_ref_sys_name, datum_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + this.epsg_code;
            result = Query.select(sentence, this.connect.getConnection());
            try {
                while (result.next()) {
                    this.GEOGCS = result.getString("coord_ref_sys_name");
                    this.datum_code = Integer.parseInt(result.getString("datum_code"));
                }
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        } else {
            sentence = "SELECT coord_ref_sys_name FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + this.epsg_code;
            result = Query.select(sentence, this.connect.getConnection());
            String sentence2 = "SELECT coord_ref_sys_name, datum_code FROM epsg_coordinatereferencesystem WHERE coord_ref_sys_code = " + this.source_code;
            ResultSet result2 = Query.select(sentence2, this.connect.getConnection());
            try {
                while (result.next()) {
                    this.PROJCS = result.getString("coord_ref_sys_name");
                }
                while (result2.next()) {
                    this.datum_code = Integer.parseInt(result2.getString("datum_code"));
                    this.GEOGCS = result2.getString("coord_ref_sys_name");
                }
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
        }
        sentence = "SELECT datum_name, ellipsoid_code, prime_meridian_code FROM epsg_datum WHERE datum_code = " + this.datum_code;
        result = Query.select(sentence, this.connect.getConnection());
        try {
            while (result.next()) {
                this.DATUM = result.getString("datum_name");
                this.ellipsoid_code = Integer.parseInt(result.getString("ellipsoid_code"));
                this.prime_meridian_code = Integer.parseInt(result.getString("prime_meridian_code"));
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        sentence = "SELECT ellipsoid_name, semi_major_axis, inv_flattening, uom_code, semi_minor_axis, ellipsoid_shape FROM epsg_ellipsoid WHERE ellipsoid_code = " + this.ellipsoid_code;
        result = Query.select(sentence, this.connect.getConnection());
        this.SPHEROID = this.getEllipsoid(result);
        sentence = "SELECT prime_meridian_name, greenwich_longitude, uom_code FROM epsg_primemeridian WHERE prime_meridian_code = " + this.prime_meridian_code;
        result = Query.select(sentence, this.connect.getConnection());
        this.PRIMEM = this.getPrimeMeridian(result);
        this.AUTHORITY = this.getAuthority(this.epsg_code);
        if (!this.crs_source) {
            sentence = "SELECT coord_op_method_code FROM epsg_coordoperation WHERE coord_op_code = " + this.coord_op_code;
            result = Query.select(sentence, this.connect.getConnection());
            try {
                while (result.next()) {
                    this.projection_code = result.getInt("coord_op_method_code");
                }
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            sentence = "SELECT coord_op_method_name FROM epsg_coordoperationmethod WHERE coord_op_method_code = " + this.projection_code;
            result = Query.select(sentence, this.connect.getConnection());
            try {
                while (result.next()) {
                    this.PROJECTION = result.getString("coord_op_method_name");
                }
            }
            catch (SQLException e) {
                LOGGER.error((Object)"", (Throwable)e);
            }
            this.parameters(this.projection_code);
        }
    }

    private String[] getEllipsoid(ResultSet result) {
        String[] spheroid = new String[3];
        double semi_major_axis = 0.0;
        double semi_minor_axis = 0.0;
        double inv_flattening = 0.0;
        int uom_code = 0;
        int ellipsoid_shape = 0;
        try {
            while (result.next()) {
                spheroid[0] = result.getString("ellipsoid_name");
                semi_major_axis = result.getDouble("semi_major_axis");
                semi_minor_axis = result.getDouble("semi_minor_axis");
                inv_flattening = result.getDouble("inv_flattening");
                uom_code = result.getInt("uom_code");
                ellipsoid_shape = result.getInt("ellipsoid_shape");
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        String sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE uom_code = " + uom_code;
        ResultSet result2 = Query.select(sentence, this.connect.getConnection());
        double factor_b = 0.0;
        double factor_c = 0.0;
        try {
            while (result2.next()) {
                factor_b = result2.getDouble("factor_b");
                factor_c = result2.getDouble("factor_c");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (factor_b != 0.0 && factor_c != 0.0) {
            semi_major_axis = semi_major_axis * factor_b / factor_c;
            if (semi_minor_axis != 0.0) {
                semi_minor_axis = semi_minor_axis * factor_b / factor_c;
            }
        }
        if (inv_flattening == 0.0) {
            inv_flattening = ellipsoid_shape == 0 ? 0.0 : semi_major_axis / (semi_major_axis - semi_minor_axis);
        }
        spheroid[1] = "" + semi_major_axis;
        spheroid[2] = "" + inv_flattening;
        return spheroid;
    }

    private String[] getPrimeMeridian(ResultSet result) {
        String[] primem = new String[2];
        double greenwich_longitude = 0.0;
        try {
            while (result.next()) {
                primem[0] = result.getString("prime_meridian_name");
                greenwich_longitude = result.getDouble("greenwich_longitude");
                int co = Integer.parseInt(result.getString("uom_code"));
                ResultSet result2 = null;
                if (co == 9110) continue;
                String sentence = "SELECT factor_b, factor_c FROM epsg_unitofmeasure WHERE uom_code = " + co;
                result2 = Query.select(sentence, this.connect.getConnection());
                while (result.next()) {
                    greenwich_longitude = greenwich_longitude * result2.getDouble("factor_b") / result2.getDouble("factor_c") * 57.29577951308232;
                }
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        primem[1] = "" + greenwich_longitude;
        return primem;
    }

    private void parameters(int proj) {
        String sentence = "SELECT COUNT(*) FROM epsg_coordoperationparamusage WHERE coord_op_method_code = " + proj;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        int count = 0;
        try {
            result.next();
            count = result.getInt(1);
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        this.param_name = new String[count];
        this.param_value = new String[count];
        sentence = "SELECT parameter_code FROM epsg_coordoperationparamusage WHERE coord_op_method_code = " + proj + " " + "ORDER BY sort_order ASC";
        result = Query.select(sentence, this.connect.getConnection());
        int i = 0;
        try {
            while (result.next()) {
                int cod = result.getInt("parameter_code");
                sentence = "SELECT parameter_name FROM epsg_coordoperationparam WHERE parameter_code = " + cod;
                ResultSet result2 = Query.select(sentence, this.connect.getConnection());
                result2.next();
                this.param_name[i] = result2.getString("parameter_name");
                sentence = "SELECT parameter_value, uom_code FROM epsg_coordoperationparamvalue WHERE parameter_code = " + cod + " AND coord_op_code = " + this.coord_op_code;
                ResultSet result3 = Query.select(sentence, this.connect.getConnection());
                result3.next();
                double param_val = result3.getDouble("parameter_value");
                int uom_code = result3.getInt("uom_code");
                sentence = "SELECT factor_b, factor_c, unit_of_meas_type FROM epsg_unitofmeasure WHERE uom_code = " + uom_code;
                ResultSet result4 = Query.select(sentence, this.connect.getConnection());
                double factor_b = 0.0;
                double factor_c = 0.0;
                result4.next();
                String type = result4.getString("unit_of_meas_type");
                factor_b = result4.getDouble("factor_b");
                factor_c = result4.getDouble("factor_c");
                if (factor_b != 0.0 && factor_c != 0.0 && !type.equals("angle")) {
                    param_val = param_val * factor_b / factor_c;
                } else if (factor_b != 0.0 && factor_c != 0.0 && type.equals("angle")) {
                    param_val = param_val * factor_b / factor_c * 57.29577951308232;
                } else if (uom_code == 9110) {
                    param_val = this.especialDegree(param_val);
                    param_val = Math.toDegrees(param_val);
                } else {
                    LOGGER.warn((Object)I18N.getString("org.gvsig.crs.ogr.GetCRSepsg.invalid-measure-code"));
                }
                this.param_value[i] = "" + param_val;
                ++i;
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
    }

    private double especialDegree(double val) {
        int signo = 1;
        if (val < 0.0) {
            signo = -1;
            val = Math.abs(val);
        }
        double grad = Math.floor(val);
        val = (val - grad) * 100.0;
        double min = Math.floor(val);
        double sec = (val - min) * 100.0;
        val = (grad + min / 60.0 + sec / 3600.0) * (Math.PI / 180) * (double)signo;
        return val;
    }

    private String[] getAuthority(int epsg_cod) {
        String[] aut = new String[]{"EPSG", "" + epsg_cod};
        return aut;
    }

    public String getPROJCS() {
        return this.PROJCS;
    }

    public String getGEOGCS() {
        return this.GEOGCS;
    }

    public String getDATUM() {
        return this.DATUM;
    }

    public String[] getSPHEROID() {
        return this.SPHEROID;
    }

    public String[] getPRIMEM() {
        return this.PRIMEM;
    }

    public String getUNIT_A() {
        return this.UNIT_A;
    }

    public String getPROJECTION() {
        return this.PROJECTION;
    }

    public String[] getParam_name() {
        return this.param_name;
    }

    public String[] getParam_value() {
        return this.param_value;
    }

    public String getUNIT_B() {
        return this.UNIT_B;
    }

    public String[] getAUTHORITY() {
        return this.AUTHORITY;
    }
}

