/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.io.WKTWriter
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.io.GMLGeometryWriter;
import com.vividsolutions.jump.util.Fmt;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.Color;
import java.util.Collection;
import java.util.Map;
import org.saig.jump.lang.I18N;

public class FeatureInfoWriter {
    public static FeatureWriter ATTRIBUTE_WRITER = new FeatureWriter(){

        @Override
        public String toString(Feature feature) {
            boolean firstAttributeWritten = false;
            StringBuffer s = new StringBuffer();
            int i = 0;
            while (i < feature.getSchema().getAttributeCount()) {
                if (!(feature.getAttribute(i) instanceof Geometry)) {
                    if (firstAttributeWritten) {
                        s.append("<BR>");
                    }
                    s.append("<b>" + GUIUtil.escapeHTML(feature.getSchema().getAttributeName(i), true) + ":</b> ");
                    if (feature.getAttribute(i) != null) {
                        s.append(GUIUtil.escapeHTML(feature.getAttribute(i).toString(), true));
                    }
                    firstAttributeWritten = true;
                }
                ++i;
            }
            return s.toString();
        }
    };
    public static FeatureWriter COORDINATE_WRITER = new FeatureWriter(){

        @Override
        public String toString(Feature feature) {
            StringBuffer s = new StringBuffer();
            String className = StringUtil.classNameWithoutQualifiers(feature.getGeometry().getClass().getName());
            s.append(String.valueOf(className) + "\n");
            Coordinate[] coordinates = feature.getGeometry().getCoordinates();
            int i = 0;
            while (i < coordinates.length) {
                s.append("[" + Fmt.fmt(i, 5) + "] ");
                s.append(String.valueOf(coordinates[i].x) + ", " + coordinates[i].y + "\n");
                ++i;
            }
            return GUIUtil.escapeHTML(s.toString().trim(), true);
        }
    };
    public static FeatureWriter EMPTY_WRITER = new FeatureWriter(){

        @Override
        public String toString(Feature feature) {
            return "";
        }
    };
    public static FeatureWriter GML_WRITER = new FeatureWriter(){

        @Override
        public String toString(Feature feature) {
            return GUIUtil.escapeHTML(new GMLGeometryWriter().write(feature.getGeometry()), true);
        }
    };
    public static FeatureWriter WKT_WRITER = new FeatureWriter(){

        @Override
        public String toString(Feature feature) {
            return GUIUtil.escapeHTML(wktWriter.write(feature.getGeometry()).trim(), true);
        }
    };
    private static final String BEIGE = "#E6E6E6";
    private static final String WHITE = "#FFFFFF";
    private static final String COLOR1 = "#E6E6E6";
    private static final String COLOR2 = "#FFFFFF";
    private static WKTWriter wktWriter = new WKTWriter();
    private boolean workingAroundJEditorPaneBug = true;

    public Color sidebarColor(Layer layer) {
        Color basicColor = layer.getBasicStyle().isRenderingFill() ? layer.getBasicStyle().getFillColor() : layer.getBasicStyle().getLineColor();
        int alpha = layer.getBasicStyle().getAlpha();
        return GUIUtil.toSimulatedTransparency(GUIUtil.alphaColor(basicColor, alpha));
    }

    public String writeGeom(Map layerToFeaturesMap, FeatureWriter featureWriter, FeatureWriter attributeWriter) {
        if (layerToFeaturesMap.isEmpty()) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (Layer layer : layerToFeaturesMap.keySet()) {
            Collection features = (Collection)layerToFeaturesMap.get(layer);
            stringBuffer.append("<table width=100%>");
            stringBuffer.append("  <tr>");
            stringBuffer.append("    <td width=5 bgcolor=" + this.toHTML(this.sidebarColor(layer)) + ">");
            stringBuffer.append("    </td>");
            stringBuffer.append("    <td width=100%>");
            stringBuffer.append("      <table width=100%>");
            stringBuffer.append("        <tr>");
            stringBuffer.append("          <td bgcolor=#FFFFCC>");
            stringBuffer.append("            <B>" + layer.getName() + "</B>");
            stringBuffer.append("          </td>");
            stringBuffer.append("        </tr>");
            String bgcolor = "#E6E6E6";
            for (Feature feature : features) {
                bgcolor = !bgcolor.equals("#E6E6E6") ? "#E6E6E6" : "#FFFFFF";
                stringBuffer.append("        <tr bgcolor='" + bgcolor + "'>");
                stringBuffer.append("          <td>");
                stringBuffer.append("            FID <font color='#3300cc'><b>" + feature.getID() + "</b></font>");
                if (featureWriter != EMPTY_WRITER) {
                    this.append(feature, stringBuffer, featureWriter);
                }
                if (attributeWriter != EMPTY_WRITER) {
                    stringBuffer.append("            <BR>" + attributeWriter.toString(feature));
                }
                stringBuffer.append("          </td>");
                stringBuffer.append("        </tr>");
            }
            stringBuffer.append("      </table>");
            stringBuffer.append("    </td>");
            stringBuffer.append("  </tr>");
            stringBuffer.append("</table>");
        }
        return stringBuffer.toString();
    }

    private String pad(String s) {
        return s.length() == 1 ? "0" + s : s;
    }

    private String toHTML(Color color) {
        String colorString = "#";
        colorString = String.valueOf(colorString) + this.pad(Integer.toHexString(color.getRed()));
        colorString = String.valueOf(colorString) + this.pad(Integer.toHexString(color.getGreen()));
        colorString = String.valueOf(colorString) + this.pad(Integer.toHexString(color.getBlue()));
        return colorString;
    }

    private void append(Feature feature, StringBuffer stringBuffer, FeatureWriter featureWriter) {
        String text = featureWriter.toString(feature);
        if (this.workingAroundJEditorPaneBug && stringBuffer.length() + featureWriter.toString(feature).length() > 30768) {
            text = I18N.getString("workbench.ui.FeatureInfoWriter.the-text-representation-of-this-geometry-is-too-large-for-this-view");
        }
        stringBuffer.append("            <BR><CODE>" + text + "</CODE>");
    }

    public static interface FeatureWriter {
        public String toString(Feature var1);
    }
}

