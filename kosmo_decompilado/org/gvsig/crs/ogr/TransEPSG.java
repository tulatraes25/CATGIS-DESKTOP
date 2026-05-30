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

public class TransEPSG {
    private static final Logger LOGGER = Logger.getLogger(TransEPSG.class);
    int parameter_code = 0;
    int coord_op_method_code = 0;
    boolean inverseTransformation = false;
    public CRSRepositoryConnection connect;
    double[] param_value_double = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    String[] param_value = new String[]{"0", "0", "0", "0", "0", "0", "0"};
    String[] param_name = new String[]{"0", "0", "0", "0", "0", "0", "0"};

    public TransEPSG(int coord_op_code, CRSRepositoryConnection conn, boolean invTr) {
        this.inverseTransformation = invTr;
        this.connect = conn;
        int uom_code = 0;
        String sentence = "SELECT coord_op_method_code, parameter_code, parameter_value, uom_code FROM epsg_coordoperationparamvalue WHERE coord_op_code = " + coord_op_code;
        ResultSet result = Query.select(sentence, this.connect.getConnection());
        int i = 0;
        try {
            while (result.next()) {
                this.coord_op_method_code = result.getInt("coord_op_method_code");
                this.parameter_code = result.getInt("parameter_code");
                double param_val = result.getDouble("parameter_value");
                sentence = "SELECT parameter_name FROM epsg_coordoperationparam WHERE parameter_code = " + this.parameter_code;
                ResultSet result2 = Query.select(sentence, this.connect.getConnection());
                result2.next();
                this.param_name[i] = result2.getString("parameter_name");
                uom_code = result.getInt("uom_code");
                if (uom_code == 0) continue;
                sentence = "SELECT factor_b, factor_c, unit_of_meas_type FROM epsg_unitofmeasure WHERE uom_code = " + uom_code;
                ResultSet result3 = Query.select(sentence, this.connect.getConnection());
                double factor_b = 0.0;
                double factor_c = 0.0;
                result3.next();
                String type = result3.getString("unit_of_meas_type");
                factor_b = result3.getDouble("factor_b");
                factor_c = result3.getDouble("factor_c");
                if (uom_code != 9202) {
                    if (factor_b != 0.0 && factor_c != 0.0 && !type.equals("angle")) {
                        param_val = param_val * factor_b / factor_c;
                        if (type.equals("scale")) {
                            param_val = (param_val - 1.0) * 1000000.0;
                        }
                    } else if (factor_b != 0.0 && factor_c != 0.0 && type.equals("angle")) {
                        param_val = param_val * factor_b / factor_c * 57.29577951308232;
                        param_val *= 3600.0;
                    } else if (uom_code == 9110) {
                        param_val = this.especialDegree(param_val);
                        param_val = Math.toDegrees(param_val);
                        param_val *= 3600.0;
                    } else {
                        System.out.println(I18N.getString("org.gvsig.crs.ogr.GetTransepsg.invalid-measure-code"));
                    }
                }
                this.param_value_double[i] = param_val;
                ++i;
            }
            if (this.inverseTransformation) {
                int j = 0;
                while (j < this.param_value_double.length) {
                    this.param_value_double[j] = this.param_value_double[j] * -1.0;
                    ++j;
                }
            }
        }
        catch (SQLException e) {
            LOGGER.error((Object)"", (Throwable)e);
        }
        int k = 0;
        while (k < this.param_value_double.length) {
            this.param_value[k] = String.valueOf(this.param_value_double[k]);
            ++k;
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

    private float round(double f, int i) {
        double d = Math.pow(10.0, i);
        double aux = f * d;
        int auxi = (int)aux;
        float df = (float)auxi / (float)d;
        return df;
    }

    public String[] getParamName() {
        return this.param_name;
    }

    public String[] getParamValue() {
        return this.param_value;
    }
}

