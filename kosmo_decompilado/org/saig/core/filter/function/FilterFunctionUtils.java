/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.algorithm.MinimumBoundingCircle
 *  com.vividsolutions.jts.algorithm.MinimumDiameter
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.io.WKTReader
 */
package org.saig.core.filter.function;

import com.vividsolutions.jts.algorithm.MinimumBoundingCircle;
import com.vividsolutions.jts.algorithm.MinimumDiameter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FilterFunctionUtils {
    public static Geometry geomFromWKT(String wkt) {
        WKTReader wktreader = new WKTReader();
        try {
            return wktreader.read(wkt);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("bad wkt");
        }
    }

    public static String toWKT(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.toString();
    }

    public static boolean contains(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.contains(arg1);
    }

    public static boolean isEmpty(Geometry arg0) {
        if (arg0 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.isEmpty();
    }

    public static double geomLength(Geometry arg0) {
        if (arg0 == null) {
            return 0.0;
        }
        Geometry _this = arg0;
        return _this.getLength();
    }

    public static boolean intersects(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.intersects(arg1);
    }

    public static boolean isValid(Geometry arg0) {
        if (arg0 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.isValid();
    }

    public static String geometryType(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.getGeometryType();
    }

    public static int numPoints(Geometry arg0) {
        if (arg0 == null) {
            return 0;
        }
        Geometry _this = arg0;
        return _this.getNumPoints();
    }

    public static boolean isSimple(Geometry arg0) {
        if (arg0 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.isSimple();
    }

    public static double distance(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return -1.0;
        }
        Geometry _this = arg0;
        return _this.distance(arg1);
    }

    public static boolean isWithinDistance(Geometry arg0, Geometry arg1, Double arg2) {
        if (arg0 == null || arg1 == null || arg2 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.isWithinDistance(arg1, arg2.doubleValue());
    }

    public static double area(Geometry arg0) {
        if (arg0 == null) {
            return -1.0;
        }
        Geometry _this = arg0;
        return _this.getArea();
    }

    public static Geometry centroid(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.getCentroid();
    }

    public static Geometry interiorPoint(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.getInteriorPoint();
    }

    public static int dimension(Geometry arg0) {
        if (arg0 == null) {
            return -1;
        }
        Geometry _this = arg0;
        return _this.getDimension();
    }

    public static Geometry boundary(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.getBoundary();
    }

    public static int boundaryDimension(Geometry arg0) {
        if (arg0 == null) {
            return -1;
        }
        Geometry _this = arg0;
        return _this.getBoundaryDimension();
    }

    public static Geometry envelope(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.getEnvelope();
    }

    public static boolean disjoint(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.disjoint(arg1);
    }

    public static boolean touches(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.touches(arg1);
    }

    public static boolean crosses(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.crosses(arg1);
    }

    public static boolean within(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.within(arg1);
    }

    public static boolean overlaps(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.overlaps(arg1);
    }

    public static boolean relatePattern(Geometry arg0, Geometry arg1, String arg2) {
        if (arg0 == null || arg1 == null || arg2 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.relate(arg1, arg2);
    }

    public static String relate(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.relate(arg1).toString();
    }

    public static Geometry bufferWithSegments(Geometry arg0, Double arg1, Integer arg2) {
        if (arg0 == null || arg1 == null || arg2 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.buffer(arg1.doubleValue(), arg2.intValue());
    }

    public static Geometry buffer(Geometry arg0, Double arg1) {
        if (arg0 == null || arg1 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.buffer(arg1.doubleValue());
    }

    public static Geometry convexHull(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.convexHull();
    }

    public static Geometry intersection(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.intersection(arg1);
    }

    public static Geometry union(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.union(arg1);
    }

    public static Geometry difference(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.difference(arg1);
    }

    public static Geometry symDifference(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return null;
        }
        Geometry _this = arg0;
        return _this.symDifference(arg1);
    }

    public static boolean equalsExactTolerance(Geometry arg0, Geometry arg1, Double arg2) {
        if (arg0 == null || arg1 == null || arg2 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.equalsExact(arg1, arg2.doubleValue());
    }

    public static boolean equalsExact(Geometry arg0, Geometry arg1) {
        if (arg0 == null || arg1 == null) {
            return false;
        }
        Geometry _this = arg0;
        return _this.equalsExact(arg1);
    }

    public static int numGeometries(Geometry arg0) {
        if (!(arg0 instanceof GeometryCollection)) {
            return 0;
        }
        GeometryCollection _this = (GeometryCollection)arg0;
        return _this.getNumGeometries();
    }

    public static Geometry getGeometryN(Geometry arg0, Integer arg1) {
        if (!(arg0 instanceof GeometryCollection) || arg1 == null) {
            return null;
        }
        GeometryCollection _this = (GeometryCollection)arg0;
        if (arg1 < 0 || arg1 >= _this.getNumGeometries()) {
            return null;
        }
        return _this.getGeometryN(arg1.intValue());
    }

    public static double getX(Geometry arg0) {
        if (!(arg0 instanceof Point)) {
            return 0.0;
        }
        Point _this = (Point)arg0;
        return _this.getX();
    }

    public static double getY(Geometry arg0) {
        if (!(arg0 instanceof Point)) {
            return 0.0;
        }
        Point _this = (Point)arg0;
        return _this.getY();
    }

    public static boolean isClosed(Geometry arg0) {
        if (!(arg0 instanceof LineString)) {
            return false;
        }
        LineString _this = (LineString)arg0;
        return _this.isClosed();
    }

    public static Geometry pointN(Geometry arg0, Integer arg1) {
        if (!(arg0 instanceof LineString) || arg1 == null) {
            return null;
        }
        LineString _this = (LineString)arg0;
        if (arg1 < 0 || arg1 >= _this.getNumPoints()) {
            return null;
        }
        return _this.getPointN(arg1.intValue());
    }

    public static Geometry startPoint(Geometry arg0) {
        if (!(arg0 instanceof LineString)) {
            return null;
        }
        LineString _this = (LineString)arg0;
        return _this.getStartPoint();
    }

    public static Geometry endPoint(Geometry arg0) {
        if (!(arg0 instanceof LineString)) {
            return null;
        }
        LineString _this = (LineString)arg0;
        return _this.getEndPoint();
    }

    public static boolean isRing(Geometry arg0) {
        if (!(arg0 instanceof LineString)) {
            return false;
        }
        LineString _this = (LineString)arg0;
        return _this.isRing();
    }

    public static Geometry exteriorRing(Geometry arg0) {
        if (!(arg0 instanceof Polygon)) {
            return null;
        }
        Polygon _this = (Polygon)arg0;
        return _this.getExteriorRing();
    }

    public static int numInteriorRing(Geometry arg0) {
        if (!(arg0 instanceof Polygon)) {
            return 0;
        }
        Polygon _this = (Polygon)arg0;
        return _this.getNumInteriorRing();
    }

    public static Geometry interiorRingN(Geometry arg0, Integer arg1) {
        if (!(arg0 instanceof Polygon) || arg1 == null) {
            return null;
        }
        Polygon _this = (Polygon)arg0;
        if (arg1 < 0 || arg1 >= _this.getNumInteriorRing()) {
            return null;
        }
        return _this.getInteriorRingN(arg1.intValue());
    }

    public static Geometry minimumCircle(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        MinimumBoundingCircle circle = new MinimumBoundingCircle(arg0);
        return circle.getCircle();
    }

    public static Geometry minimumRectangle(Geometry arg0) {
        if (arg0 == null) {
            return null;
        }
        MinimumDiameter min = new MinimumDiameter(arg0);
        return min.getMinimumRectangle();
    }

    public static String strConcat(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return null;
        }
        return String.valueOf(s1) + s2;
    }

    public static boolean strEndsWith(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.endsWith(s2);
    }

    public static boolean strStartsWith(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.startsWith(s2);
    }

    public static boolean strEqualsIgnoreCase(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equalsIgnoreCase(s2);
    }

    public static int strIndexOf(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return -1;
        }
        return s1.indexOf(s2);
    }

    public static int strLastIndexOf(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return -1;
        }
        return s1.lastIndexOf(s2);
    }

    public static int strLength(String s1) {
        if (s1 == null) {
            return 0;
        }
        return s1.length();
    }

    public static String strToLowerCase(String s1) {
        if (s1 == null) {
            return null;
        }
        return s1.toLowerCase();
    }

    public static String strToUpperCase(String s1) {
        if (s1 == null) {
            return null;
        }
        return s1.toUpperCase();
    }

    public static String strCapitalize(String s) {
        if (s == null) {
            return null;
        }
        int strLength = s.length();
        StringBuilder sb = new StringBuilder(strLength);
        boolean titleCaseNext = true;
        int i = 0;
        while (i < strLength) {
            char ch = s.charAt(i);
            if (Character.isWhitespace(ch)) {
                sb.append(ch);
                titleCaseNext = true;
            } else if (titleCaseNext) {
                sb.append(Character.toTitleCase(ch));
                titleCaseNext = false;
            } else {
                sb.append(Character.toLowerCase(ch));
            }
            ++i;
        }
        return sb.toString();
    }

    public static boolean strMatches(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.matches(s2);
    }

    public static String strReplace(String s1, String s2, String s3, Boolean bAll) {
        if (s1 == null || s2 == null || s3 == null) {
            return null;
        }
        if (bAll != null && bAll.booleanValue()) {
            return s1.replaceAll(s2, s3);
        }
        return s1.replaceFirst(s2, s3);
    }

    public static String strSubstring(String s1, Integer beg, Integer end) {
        if (s1 == null || beg == null || end == null) {
            return null;
        }
        if (beg < 0 || end > s1.length() || beg > end) {
            return null;
        }
        return s1.substring(beg, end);
    }

    public static String strSubstringStart(String s1, Integer beg) {
        if (s1 == null || beg == null) {
            return null;
        }
        if (beg < 0 || beg > s1.length()) {
            return null;
        }
        return s1.substring(beg);
    }

    public static String strTrim(String s1) {
        if (s1 == null) {
            return null;
        }
        return s1.trim();
    }

    public static double parseDouble(String s) {
        if (s == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static int parseInt(String s) {
        if (s == null) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return (int)Math.round(FilterFunctionUtils.parseDouble(s));
        }
    }

    public static long parseLong(String s) {
        if (s == null) {
            return 0L;
        }
        try {
            return Long.parseLong(s);
        }
        catch (NumberFormatException e) {
            return Math.round(FilterFunctionUtils.parseDouble(s));
        }
    }

    public static boolean parseBoolean(String s) {
        return s != null && !s.equalsIgnoreCase("") && !s.equalsIgnoreCase("f") && !s.equalsIgnoreCase("false") && !s.equalsIgnoreCase("0") && !s.equalsIgnoreCase("0.0");
    }

    public static int roundDouble(Double d) {
        if (d == null) {
            return 0;
        }
        return (int)Math.round(d);
    }

    public static double int2ddouble(Integer i) {
        if (i == null) {
            return Double.NaN;
        }
        return i.intValue();
    }

    public static boolean int2bbool(Integer i) {
        if (i == null) {
            return false;
        }
        return i == 0;
    }

    public static boolean double2bool(Double d) {
        if (d == null) {
            return false;
        }
        return d == 0.0;
    }

    public static Object if_then_else(Boolean p, Object a, Object b) {
        if (p != null && p.booleanValue()) {
            return a;
        }
        return b;
    }

    public static boolean equalTo(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.getClass() == o2.getClass()) {
            return o1.equals(o2);
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number)o1).doubleValue() == ((Number)o2).doubleValue();
        }
        return o1.toString().equals(o2.toString());
    }

    public static boolean notEqualTo(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        return !FilterFunctionUtils.equalTo(o1, o2);
    }

    public static boolean lessThan(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer)o1 < (Integer)o2;
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number)o1).doubleValue() < ((Number)o2).doubleValue();
        }
        return o1.toString().compareTo(o2.toString()) == 0;
    }

    public static boolean greaterThan(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer)o1 > (Integer)o2;
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number)o1).doubleValue() > ((Number)o2).doubleValue();
        }
        return o1.toString().compareTo(o2.toString()) == 2;
    }

    public static boolean greaterEqualThan(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer)o1 >= (Integer)o2;
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number)o1).doubleValue() >= ((Number)o2).doubleValue();
        }
        return o1.toString().compareTo(o2.toString()) == 2 || o1.toString().compareTo(o2.toString()) == 1;
    }

    public static boolean lessEqualThan(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 instanceof Integer && o2 instanceof Integer) {
            return (Integer)o1 <= (Integer)o2;
        }
        if (o1 instanceof Number && o2 instanceof Number) {
            return ((Number)o1).doubleValue() <= ((Number)o2).doubleValue();
        }
        return o1.toString().compareTo(o2.toString()) == 0 || o1.toString().compareTo(o2.toString()) == 1;
    }

    public static boolean isLike(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.matches(s2);
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean between(Object o, Object o_low, Object o_high) {
        return FilterFunctionUtils.greaterEqualThan(o, o_low) && FilterFunctionUtils.lessEqualThan(o, o_high);
    }

    public static boolean not(Boolean b) {
        if (b == null) {
            return true;
        }
        return b == false;
    }

    public static boolean in2(Object s, Object s1, Object s2) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2);
    }

    public static boolean in3(Object s, Object s1, Object s2, Object s3) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3);
    }

    public static boolean in4(Object s, Object s1, Object s2, Object s3, Object s4) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4);
    }

    public static boolean in5(Object s, Object s1, Object s2, Object s3, Object s4, Object s5) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4) || FilterFunctionUtils.equalTo(s, s5);
    }

    public static boolean in6(Object s, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4) || FilterFunctionUtils.equalTo(s, s5) || FilterFunctionUtils.equalTo(s, s6);
    }

    public static boolean in7(Object s, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4) || FilterFunctionUtils.equalTo(s, s5) || FilterFunctionUtils.equalTo(s, s6) || FilterFunctionUtils.equalTo(s, s7);
    }

    public static boolean in8(Object s, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4) || FilterFunctionUtils.equalTo(s, s5) || FilterFunctionUtils.equalTo(s, s6) || FilterFunctionUtils.equalTo(s, s7) || FilterFunctionUtils.equalTo(s, s8);
    }

    public static boolean in9(Object s, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8, Object s9) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4) || FilterFunctionUtils.equalTo(s, s5) || FilterFunctionUtils.equalTo(s, s6) || FilterFunctionUtils.equalTo(s, s7) || FilterFunctionUtils.equalTo(s, s8) || FilterFunctionUtils.equalTo(s, s9);
    }

    public static boolean in10(Object s, Object s1, Object s2, Object s3, Object s4, Object s5, Object s6, Object s7, Object s8, Object s9, Object s10) {
        return FilterFunctionUtils.equalTo(s, s1) || FilterFunctionUtils.equalTo(s, s2) || FilterFunctionUtils.equalTo(s, s3) || FilterFunctionUtils.equalTo(s, s4) || FilterFunctionUtils.equalTo(s, s5) || FilterFunctionUtils.equalTo(s, s6) || FilterFunctionUtils.equalTo(s, s7) || FilterFunctionUtils.equalTo(s, s8) || FilterFunctionUtils.equalTo(s, s9) || FilterFunctionUtils.equalTo(s, s10);
    }

    public static int daysFrom(Date date1, Date date2) {
        return (int)TimeUnit.MILLISECONDS.toDays(date2.getTime() - date1.getTime());
    }
}

