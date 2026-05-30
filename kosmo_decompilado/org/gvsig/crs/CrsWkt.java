/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.units.ConversionException
 *  javax.units.Unit
 *  org.geotools.referencing.CRS
 *  org.geotools.referencing.crs.AbstractSingleCRS
 *  org.geotools.referencing.crs.DefaultGeographicCRS
 *  org.geotools.referencing.crs.DefaultProjectedCRS
 *  org.geotools.referencing.datum.DefaultEllipsoid
 *  org.geotools.referencing.datum.DefaultGeodeticDatum
 *  org.geotools.referencing.datum.DefaultPrimeMeridian
 *  org.opengis.referencing.FactoryException
 *  org.opengis.referencing.NoSuchAuthorityCodeException
 *  org.opengis.referencing.crs.CoordinateReferenceSystem
 *  org.opengis.referencing.datum.PrimeMeridian
 */
package org.gvsig.crs;

import java.util.ArrayList;
import javax.units.ConversionException;
import javax.units.Unit;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.AbstractSingleCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PrimeMeridian;

public class CrsWkt {
    private String wkt;
    private String projcs = "";
    private String geogcs = "";
    private String datum = "";
    private String[] spheroid = new String[]{"", "", ""};
    private String[] primem = new String[]{"", ""};
    private String[] unit = new String[]{"", ""};
    private String[] unit_p = new String[]{"", ""};
    private String projection = "";
    private String[] param_name;
    private String[] param_value;
    private int contador = 0;
    private String[] authority = new String[]{"", ""};
    int divider = 10000;
    private static final double EPS = 1.0E-8;

    public CrsWkt(String wkt_spaces) {
        String wkt = "";
        int i = 0;
        while (i < wkt_spaces.length()) {
            String aux = "" + wkt_spaces.charAt(i);
            wkt = !aux.equals(" ") ? String.valueOf(wkt) + aux : String.valueOf(wkt);
            ++i;
        }
        if (wkt.length() > 15) {
            this.fromWKT(wkt, false);
        } else {
            this.fromCode(wkt);
        }
    }

    public CrsWkt(CoordinateReferenceSystem crsGT) {
        DefaultGeodeticDatum d;
        String[] val;
        DefaultProjectedCRS sour;
        AbstractSingleCRS crs = (AbstractSingleCRS)crsGT;
        String authority = crs.getName().toString().split(":")[0];
        this.setAuthority(((AbstractSingleCRS)crsGT).getIdentifiers().iterator().next().toString());
        this.setWkt(crsGT.toWKT());
        if (crsGT instanceof DefaultProjectedCRS) {
            sour = (DefaultProjectedCRS)crsGT;
            this.setProjcs(sour.getName().toString().split(":")[1]);
            val = sour.getBaseCRS().getName().toString().split(":");
            if (val.length < 2) {
                this.setGeogcs(sour.getBaseCRS().getName().toString().split(":")[0]);
            } else {
                this.setGeogcs(sour.getBaseCRS().getName().toString().split(":")[1]);
            }
            d = (DefaultGeodeticDatum)sour.getDatum();
            val = d.getName().toString().split(":");
            if (val.length < 2) {
                this.setDatumName(d.getName().toString().split(":")[0]);
            } else {
                this.setDatumName(d.getName().toString().split(":")[1]);
            }
            this.setSpheroid((DefaultEllipsoid)d.getEllipsoid());
            this.setPrimen(d.getPrimeMeridian());
            val = sour.getConversionFromBase().getMethod().getName().toString().split(":");
            if (val.length < 2) {
                this.setProjection(sour.getConversionFromBase().getMethod().getName().toString().split(":")[0]);
            } else {
                this.setProjection(sour.getConversionFromBase().getMethod().getName().toString().split(":")[1]);
            }
            this.param_name = new String[sour.getConversionFromBase().getParameterValues().values().size()];
            this.param_value = new String[sour.getConversionFromBase().getParameterValues().values().size()];
            int i = 0;
            while (i < sour.getConversionFromBase().getParameterValues().values().size()) {
                String str = sour.getConversionFromBase().getParameterValues().values().get(i).toString();
                Unit u = sour.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).getUnit();
                double value = sour.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).doubleValue();
                value = this.convert(value, u.toString());
                this.param_name[i] = str.split("=")[0];
                this.param_value[i] = String.valueOf(value);
                ++i;
            }
        }
        if (crsGT instanceof DefaultGeographicCRS) {
            sour = (DefaultGeographicCRS)crsGT;
            val = sour.getName().toString().split(":");
            if (val.length < 2) {
                this.setGeogcs(sour.getName().toString().split(":")[0]);
            } else {
                this.setGeogcs(sour.getName().toString().split(":")[1]);
            }
            d = (DefaultGeodeticDatum)sour.getDatum();
            val = d.getName().toString().split(":");
            if (val.length < 2) {
                this.setDatumName(d.getName().toString().split(":")[0]);
            } else {
                this.setDatumName(d.getName().toString().split(":")[1]);
            }
            this.setSpheroid((DefaultEllipsoid)d.getEllipsoid());
            this.setPrimen(d.getPrimeMeridian());
        }
    }

    /*
     * Exception decompiling
     */
    private void fromWKT(String wkt, boolean isProj) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [42[DOLOOP]], but top level block is 45[DOLOOP]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public String getProjection() {
        return this.projection;
    }

    public String getProjcs() {
        return this.projcs;
    }

    public String getGeogcs() {
        return this.geogcs;
    }

    public String getDatumName() {
        return this.datum;
    }

    public String[] getSpheroid() {
        return this.spheroid;
    }

    public String[] getPrimen() {
        return this.primem;
    }

    public String getName() {
        if (this.projcs == "") {
            return this.geogcs;
        }
        return this.projcs;
    }

    public String[] getUnit() {
        return this.unit;
    }

    public String[] getUnit_p() {
        return this.unit_p;
    }

    public String[] getParam_name() {
        return this.param_name;
    }

    public String[] getParam_value() {
        return this.param_value;
    }

    public String[] getAuthority() {
        return this.authority;
    }

    private void fromCode(String code) {
        this.setAuthority(code);
        try {
            DefaultGeodeticDatum d;
            String[] val;
            DefaultProjectedCRS sour;
            CoordinateReferenceSystem source = CRS.decode((String)code);
            this.setWkt(source.toWKT());
            if (source instanceof DefaultProjectedCRS) {
                sour = (DefaultProjectedCRS)source;
                this.setProjcs(sour.getName().toString().split(":")[1]);
                val = sour.getBaseCRS().getName().toString().split(":");
                if (val.length < 2) {
                    this.setGeogcs(sour.getBaseCRS().getName().toString().split(":")[0]);
                } else {
                    this.setGeogcs(sour.getBaseCRS().getName().toString().split(":")[1]);
                }
                d = (DefaultGeodeticDatum)sour.getDatum();
                val = d.getName().toString().split(":");
                if (val.length < 2) {
                    this.setDatumName(d.getName().toString().split(":")[0]);
                } else {
                    this.setDatumName(d.getName().toString().split(":")[1]);
                }
                this.setSpheroid((DefaultEllipsoid)d.getEllipsoid());
                this.setPrimen(d.getPrimeMeridian());
                val = sour.getConversionFromBase().getMethod().getName().toString().split(":");
                if (val.length < 2) {
                    this.setProjection(sour.getConversionFromBase().getMethod().getName().toString().split(":")[0]);
                } else {
                    this.setProjection(sour.getConversionFromBase().getMethod().getName().toString().split(":")[1]);
                }
                this.param_name = new String[sour.getConversionFromBase().getParameterValues().values().size()];
                this.param_value = new String[sour.getConversionFromBase().getParameterValues().values().size()];
                int i = 0;
                while (i < sour.getConversionFromBase().getParameterValues().values().size()) {
                    String str = sour.getConversionFromBase().getParameterValues().values().get(i).toString();
                    Unit u = sour.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).getUnit();
                    double value = sour.getConversionFromBase().getParameterValues().parameter(str.split("=")[0]).doubleValue();
                    value = this.convert(value, u.toString());
                    this.param_name[i] = str.split("=")[0];
                    this.param_value[i] = String.valueOf(value);
                    ++i;
                }
            }
            if (source instanceof DefaultGeographicCRS) {
                sour = (DefaultGeographicCRS)source;
                val = sour.getName().toString().split(":");
                if (val.length < 2) {
                    this.setGeogcs(sour.getName().toString().split(":")[0]);
                } else {
                    this.setGeogcs(sour.getName().toString().split(":")[1]);
                }
                d = (DefaultGeodeticDatum)sour.getDatum();
                val = d.getName().toString().split(":");
                if (val.length < 2) {
                    this.setDatumName(d.getName().toString().split(":")[0]);
                } else {
                    this.setDatumName(d.getName().toString().split(":")[1]);
                }
                this.setSpheroid((DefaultEllipsoid)d.getEllipsoid());
                this.setPrimen(d.getPrimeMeridian());
            }
        }
        catch (NoSuchAuthorityCodeException e) {
            e.printStackTrace();
        }
        catch (FactoryException e) {
            e.printStackTrace();
        }
    }

    private void parseWKT(String data) {
        WKT wkt = new WKT(data);
    }

    public void setProjcs(String proj) {
        this.projcs = proj;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public void setGeogcs(String geo) {
        this.geogcs = geo;
    }

    public void setDatumName(String dat) {
        this.datum = dat;
    }

    public void setSpheroid(DefaultEllipsoid ellips) {
        Unit u = ellips.getAxisUnit();
        double semi_major = this.convert(ellips.getSemiMajorAxis(), u.toString());
        double inv_f = this.convert(ellips.getInverseFlattening(), u.toString());
        String[] val = ellips.getName().toString().split(":");
        this.spheroid[0] = val.length < 2 ? ellips.getName().toString().split(":")[0] : ellips.getName().toString().split(":")[1];
        this.spheroid[1] = String.valueOf(semi_major);
        this.spheroid[2] = String.valueOf(inv_f);
    }

    public void setPrimen(PrimeMeridian prim) {
        DefaultPrimeMeridian pm = (DefaultPrimeMeridian)prim;
        Unit u = pm.getAngularUnit();
        double value = this.convert(pm.getGreenwichLongitude(), u.toString());
        String[] val = pm.getName().toString().split(":");
        this.primem[0] = val.length < 2 ? pm.getName().toString().split(":")[0] : pm.getName().toString().split(":")[1];
        this.primem[1] = String.valueOf(value);
    }

    public void setAuthority(String aut) {
        this.authority = aut.split(":");
    }

    public void setWkt(String wkt) {
        this.wkt = wkt;
    }

    public String getWkt() {
        return this.wkt;
    }

    public double convert(double value, String measure) throws ConversionException {
        if (measure.equals("D.MS")) {
            int deg = (int)((value *= (double)this.divider) / 10000.0);
            int min = (int)((value -= (double)(10000 * deg)) / 100.0);
            value -= (double)(100 * min);
            if (min <= -60 || min >= 60) {
                if ((double)Math.abs(Math.abs(min) - 100) <= 1.0E-8) {
                    deg = min >= 0 ? ++deg : --deg;
                    min = 0;
                } else {
                    throw new ConversionException("Invalid minutes: " + min);
                }
            }
            if (value <= -60.0 || value >= 60.0) {
                if (Math.abs(Math.abs(value) - 100.0) <= 1.0E-8) {
                    min = value >= 0.0 ? ++min : --min;
                    value = 0.0;
                } else {
                    throw new ConversionException("Invalid secondes: " + value);
                }
            }
            value = (value / 60.0 + (double)min) / 60.0 + (double)deg;
            return value;
        }
        if (measure.equals("grad") || measure.equals("grade")) {
            return value * 180.0 / 200.0;
        }
        if (measure.equals("\u00b0")) {
            return value;
        }
        if (measure.equals("DMS")) {
            return value;
        }
        if (measure.equals("m")) {
            return value;
        }
        if (measure.startsWith("[m*")) {
            return value * Double.parseDouble(measure.substring(3, measure.length() - 1));
        }
        if (measure.equals("")) {
            return value;
        }
        if (measure.equalsIgnoreCase("ft") || measure.equalsIgnoreCase("foot") || measure.equalsIgnoreCase("feet")) {
            return value * 0.3048 / 1.0;
        }
        throw new ConversionException("Conversion no contemplada: " + measure);
    }

    public static class WKT {
        String data;
        static int cnt = 0;

        public WKT(String data) {
            this.data = data;
            Param param = this.parseParam(0);
        }

        private Param parseParam(int pos) {
            Param param = null;
            int l = this.data.length();
            int i = pos;
            while (i < l) {
                String name;
                int abre;
                int cierra;
                int nextParam = this.data.indexOf(",", i);
                if (nextParam == i) {
                    nextParam = this.data.indexOf(",", ++i);
                }
                if ((cierra = this.data.indexOf("]", i)) < (abre = this.data.indexOf("[", i))) {
                    WKT.pinta(" =>");
                    param.pos = cierra;
                    return param;
                }
                if (param == null) {
                    if (abre <= 0) continue;
                    String key = this.data.substring(i, this.data.indexOf("[", i));
                    WKT.pinta(String.valueOf(key) + " <= ");
                    i = abre + 1;
                    param = new Param(key);
                    continue;
                }
                if (this.data.substring(i).startsWith("\"")) {
                    name = this.data.substring(i + 1, this.data.indexOf("\"", i + 1));
                    i += name.length() + 2;
                    WKT.pinta("|" + name + "|,");
                    param.addValue(name);
                    continue;
                }
                if (nextParam < abre) {
                    name = this.data.substring(i, this.data.indexOf(",", i));
                    i += name.length();
                    WKT.pinta(String.valueOf(name) + ",");
                    param.addValue(name);
                    continue;
                }
                Param p = this.parseParam(i);
                i = p.pos + 1;
            }
            return param;
        }

        public static void pinta(String str) {
            if (++cnt > 60) {
                System.exit(1);
            }
            System.out.println(str);
        }

        public class Param {
            String key;
            ArrayList values = new ArrayList();
            ArrayList params = new ArrayList();
            public int pos = 0;

            public Param(String key) {
                this.key = key;
            }

            public void addValue(String name) {
                this.values.add(name);
            }

            public void addParam(Param p) {
                this.params.add(p);
            }
        }
    }
}

