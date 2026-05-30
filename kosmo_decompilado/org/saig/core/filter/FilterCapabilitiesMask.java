/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.filter;

import java.util.HashMap;
import java.util.Map;
import org.saig.core.filter.Filter;
import org.saig.core.filter.FilterCapabilities;

public class FilterCapabilitiesMask
extends FilterCapabilities {
    public static final int NO_OP = 0;
    public static final int BBOX = 1;
    public static final int EQUALS = 2;
    public static final int DISJOINT = 4;
    public static final int INTERSECT = 8;
    public static final int TOUCHES = 16;
    public static final int CROSSES = 32;
    public static final int WITHIN = 64;
    public static final int CONTAINS = 128;
    public static final int OVERLAPS = 256;
    public static final int BEYOND = 512;
    public static final int DWITHIN = 1024;
    public static final int LOGICAL = 2048;
    public static final int SIMPLE_COMPARISONS = 4096;
    public static final int LIKE = 8192;
    public static final int BETWEEN = 16384;
    public static final int NULL_CHECK = 32768;
    public static final int SIMPLE_ARITHMETIC = 65536;
    public static final int FUNCTIONS = 131072;
    private static Map<String, Integer> smap = FilterCapabilitiesMask.loadSMap();
    private static Map<String, Integer> cmap = FilterCapabilitiesMask.loadCMap();
    private int ops = 0;

    private static Map<String, Integer> loadSMap() {
        smap = new HashMap<String, Integer>();
        smap.put("", new Integer(0));
        smap.put("BBOX", new Integer(1));
        smap.put("Equals", new Integer(2));
        smap.put("Disjoint", new Integer(4));
        smap.put("Intersect", new Integer(8));
        smap.put("Touches", new Integer(16));
        smap.put("Crosses", new Integer(32));
        smap.put("Within", new Integer(64));
        smap.put("Contains", new Integer(128));
        smap.put("Overlaps", new Integer(256));
        smap.put("Beyond", new Integer(512));
        smap.put("DWithin", new Integer(1024));
        return smap;
    }

    private static Map<String, Integer> loadCMap() {
        cmap = new HashMap<String, Integer>();
        cmap.put("", new Integer(0));
        cmap.put("Logical", new Integer(2048));
        cmap.put("Simple_Comparisons", new Integer(4096));
        cmap.put("Like", new Integer(8192));
        cmap.put("Between", new Integer(16384));
        cmap.put("NullCheck", new Integer(32768));
        cmap.put("Simple_Arithmetic", new Integer(65536));
        cmap.put("Functions", new Integer(131072));
        return cmap;
    }

    public static int findOperation(String s) {
        if (smap.containsKey(s)) {
            return smap.get(s);
        }
        if (cmap.containsKey(s)) {
            return cmap.get(s);
        }
        return 0;
    }

    public static String writeSpatialOperation(int i) {
        switch (i) {
            case 1: {
                return "BBOX";
            }
            case 2: {
                return "Equals";
            }
            case 4: {
                return "Disjoint";
            }
            case 8: {
                return "Intersect";
            }
            case 16: {
                return "Touches";
            }
            case 32: {
                return "Crosses";
            }
            case 64: {
                return "Within";
            }
            case 128: {
                return "Contains";
            }
            case 256: {
                return "Overlaps";
            }
            case 512: {
                return "Beyond";
            }
            case 1024: {
                return "DWithin";
            }
        }
        return "";
    }

    public static String writeScalarOperation(int i) {
        switch (i) {
            case 2048: {
                return "Logical";
            }
            case 4096: {
                return "Simple_Comparisons";
            }
            case 8192: {
                return "Like";
            }
            case 16384: {
                return "Between";
            }
            case 32768: {
                return "NullCheck";
            }
            case 65536: {
                return "Simple_Arithmetic";
            }
            case 131072: {
                return "Functions";
            }
        }
        return "";
    }

    @Override
    public void addType(short type) {
        this.ops |= type;
    }

    @Override
    public boolean fullySupports(Filter filter) {
        return super.fullySupports(filter);
    }

    @Override
    public boolean supports(Filter filter) {
        return super.supports(filter);
    }

    @Override
    public boolean supports(short type) {
        return (this.ops & type) == type;
    }

    public int getScalarOps() {
        return this.ops;
    }

    public int getSpatialOps() {
        return this.ops;
    }
}

