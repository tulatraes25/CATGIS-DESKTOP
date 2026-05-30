/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;

public class WKTDisplayHelper {
    private static final int LINE_SPLIT_THRESHOLD = -1;
    private static final String ITALIC_START_TAG = "<i>";
    private static final String ITALIC_END_TAG = "</i>";
    private static final String BOLD_START_TAG = "<b>";
    private static final String BOLD_END_TAG = "</b>";
    private static final String Z_REMARK_START_TAG = "<i><b>";
    private static final String Z_REMARK_END_TAG = "</b></i>";
    private static final String PREFORMAT_START_TAG = "<pre>";
    private static final String PREFORMAT_END_TAG = "</pre>";
    private static final String HTML_START_TAG = "<html>";
    private static final String HTML_END_TAG = "</html>";
    private static final String HEAD_START_TAG = "<head>";
    private static final String HEAD_END_TAG = "</head>";
    private static final String BODY_START_TAG = "<body>";
    private static final String BODY_END_TAG = "</body>";
    private static final String FONT_WKT_START_TAG = "<font size=\"3\">";
    private static final String FONT_WKT_END_TAG = "</font>";
    private static final String FONT_ANNOTATION_START_TAG = "<font color='#FFFFFF' size=\"3\"><b>";
    private static final String FONT_ANNOTATION_END_TAG = "</b></font>";
    private static final String BREAK_ROW_TAG = "<br>";

    public String format(String wkt) {
        String formattedWKT = this.format(wkt, false);
        if (formattedWKT.length() > -1) {
            formattedWKT = this.format(wkt, true);
        }
        return "<pre><font size=\"3\">" + formattedWKT + FONT_WKT_END_TAG + PREFORMAT_END_TAG;
    }

    private String format(String wkt, boolean splitting) {
        int level = 0;
        String lastNonBlankToken = "";
        StringBuffer formattedWKT = new StringBuffer();
        StringTokenizer tokenizer = new StringTokenizer(wkt, " \t\n\r\f,()", true);
        int countCurrentLineCoordinate = 0;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (StringUtils.trim((String)token).length() == 0) continue;
            if (token.equals(",")) {
                formattedWKT.append(", ");
            } else if (token.equals("(")) {
                ++level;
                if (this.wordToken(lastNonBlankToken)) {
                    formattedWKT.append(" ");
                }
                formattedWKT.append("(");
            } else if (token.equals(")")) {
                int oldLevel = level;
                level = Math.max(0, level - 1);
                if (this.wordToken(lastNonBlankToken)) {
                    this.newLineAndIndentIfSplitting(formattedWKT, level, splitting);
                    countCurrentLineCoordinate = 0;
                }
                formattedWKT.append(")");
                if (oldLevel == 1) {
                    formattedWKT.append(StringUtil.newLine());
                }
            } else {
                if (this.wordToken(lastNonBlankToken)) {
                    formattedWKT.append(" ");
                } else {
                    this.newLineAndIndentIfSplitting(formattedWKT, level, splitting);
                    countCurrentLineCoordinate = 0;
                }
                if (countCurrentLineCoordinate == 2) {
                    formattedWKT.append(Z_REMARK_START_TAG);
                }
                formattedWKT.append(token);
                if (countCurrentLineCoordinate == 2) {
                    formattedWKT.append(Z_REMARK_END_TAG);
                }
            }
            lastNonBlankToken = token;
            ++countCurrentLineCoordinate;
        }
        return StringUtils.trim((String)formattedWKT.toString());
    }

    private void newLineAndIndentIfSplitting(StringBuffer formattedWKT, int level, boolean splitting) {
        if (splitting) {
            formattedWKT.append(String.valueOf(StringUtil.newLine()) + this.indent(level));
        }
    }

    private boolean wordToken(String token) {
        return !token.equals("(") && !token.equals(")") && !token.equals(",");
    }

    public String getCleanWKT(String wktTextWithHtmlTags) {
        String result = StringUtils.remove((String)wktTextWithHtmlTags, (String)Z_REMARK_START_TAG);
        result = StringUtils.remove((String)result, (String)Z_REMARK_END_TAG);
        result = StringUtils.remove((String)result, (String)ITALIC_START_TAG);
        result = StringUtils.remove((String)result, (String)ITALIC_END_TAG);
        result = StringUtils.remove((String)result, (String)BOLD_START_TAG);
        result = StringUtils.remove((String)result, (String)BOLD_END_TAG);
        result = StringUtils.remove((String)result, (String)HTML_START_TAG);
        result = StringUtils.remove((String)result, (String)HTML_END_TAG);
        result = StringUtils.remove((String)result, (String)FONT_WKT_START_TAG);
        result = StringUtils.remove((String)result, (String)FONT_WKT_END_TAG);
        result = StringUtils.remove((String)result, (String)PREFORMAT_START_TAG);
        result = StringUtils.remove((String)result, (String)PREFORMAT_END_TAG);
        result = StringUtils.remove((String)result, (String)HEAD_START_TAG);
        result = StringUtils.remove((String)result, (String)HEAD_END_TAG);
        result = StringUtils.remove((String)result, (String)BODY_START_TAG);
        result = StringUtils.remove((String)result, (String)BODY_END_TAG);
        return StringUtils.trim((String)result);
    }

    private Integer inc(Object i) {
        return (Integer)i + 1;
    }

    public String annotate(String wkt) {
        int lineIndex = 0;
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(0);
        ArrayList<Object> annotations = new ArrayList<Object>();
        StringTokenizer tokenizer = new StringTokenizer(wkt, " \t\n\r\f,()", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("\n")) {
                ++lineIndex;
                continue;
            }
            if (token.trim().length() == 0) continue;
            if (token.equals(",")) {
                stack.push(this.inc(stack.pop()));
                continue;
            }
            if (token.equals("(")) {
                stack.push(new Integer(0));
                continue;
            }
            if (token.equals(")")) {
                if (stack.size() == 1) continue;
                stack.pop();
                if (stack.size() != 1) continue;
                stack.push(this.inc(stack.pop()));
                continue;
            }
            if (!StringUtil.isNumber(token)) continue;
            CollectionUtil.setIfNull(lineIndex, annotations, this.annotation(stack));
        }
        CollectionUtil.resize(annotations, lineIndex + 1);
        return "<br><pre><font color='#FFFFFF' size=\"3\"><b>" + StringUtil.toDelimitedString(annotations, StringUtil.newLine()) + FONT_ANNOTATION_END_TAG + PREFORMAT_END_TAG + BREAK_ROW_TAG;
    }

    private String annotation(List indices) {
        String annotation = "";
        for (Integer index : indices.subList(1, indices.size())) {
            if (annotation.trim().length() != 0) {
                annotation = String.valueOf(annotation) + ".";
            }
            annotation = String.valueOf(annotation) + index;
        }
        return annotation;
    }

    private String indent(int level) {
        return StringUtil.repeat(' ', level * 4);
    }

    public static void main(String[] args) {
        String wkt = new WKTDisplayHelper().format("POINT(5 5)POINT(10 10)", false);
        System.out.println(wkt);
        System.out.println(new WKTDisplayHelper().annotate(wkt));
    }

    public String annotation(Geometry geometry, Coordinate c) {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(0);
        Assert.isTrue((boolean)this.annotation(geometry, c, stack));
        return this.annotation(stack);
    }

    private boolean annotation(Geometry geometry, Coordinate c, Stack stack) {
        stack.push(0);
        if (geometry instanceof GeometryCollection) {
            int i = 0;
            while (i < ((GeometryCollection)geometry).getNumGeometries()) {
                if (this.annotation(((GeometryCollection)geometry).getGeometryN(i), c, stack)) {
                    return true;
                }
                ++i;
            }
        } else if (geometry instanceof Polygon) {
            if (this.annotation((Geometry)((Polygon)geometry).getExteriorRing(), c, stack)) {
                return true;
            }
            int i = 0;
            while (i < ((Polygon)geometry).getNumInteriorRing()) {
                if (this.annotation((Geometry)((Polygon)geometry).getInteriorRingN(i), c, stack)) {
                    return true;
                }
                ++i;
            }
        } else if (geometry instanceof LineString || geometry instanceof Point) {
            Coordinate[] coordinates = geometry.getCoordinates();
            int i = 0;
            while (i < coordinates.length) {
                if (coordinates[i] == c) {
                    return true;
                }
                stack.push(this.inc(stack.pop()));
                ++i;
            }
        } else {
            Assert.shouldNeverReachHere();
        }
        stack.pop();
        stack.push(this.inc(stack.pop()));
        return false;
    }

    private class DebugStringBuffer {
        private StringBuffer stringBuffer = new StringBuffer();

        private DebugStringBuffer() {
        }

        public void append(String s) {
            System.out.print(s);
            this.stringBuffer.append(s);
        }

        public String toString() {
            return this.stringBuffer.toString();
        }
    }
}

