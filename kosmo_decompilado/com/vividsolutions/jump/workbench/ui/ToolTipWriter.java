/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.commons.lang.StringUtils
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.geom.EnvelopeUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.cursortool.SpecifyFeaturesTool;
import com.vividsolutions.jump.workbench.ui.plugin.MapToolTipsPlugIn;
import com.vividsolutions.jump.workbench.ui.plugin.PersistentBlackboardPlugIn;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.saig.core.model.feature.Attribute;
import org.saig.core.util.DateFormatManager;
import org.saig.core.util.NumberFormatManager;
import org.saig.jump.lang.I18N;

public class ToolTipWriter {
    private boolean enabled = false;
    private LayerViewPanel panel;

    public ToolTipWriter(LayerViewPanel panel) {
        this.panel = panel;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String write(String template, Point2D mouseLocation) {
        Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap;
        int PIXEL_BUFFER = 2;
        if (!this.enabled) {
            return null;
        }
        Map<String, List<String>> layerNamesToAttributeMap = this.processTemplate(template);
        if (layerNamesToAttributeMap == null || layerNamesToAttributeMap.isEmpty()) {
            try {
                layerToSpecifiedFeaturesMap = SpecifyFeaturesTool.layerToSpecifiedFeaturesMap(this.panel.getLayerManager().getVisibleLayers(false).iterator(), EnvelopeUtil.expand(new Envelope(this.panel.getViewport().toModelCoordinate(mouseLocation)), (double)PIXEL_BUFFER / this.panel.getViewport().getScale()));
            }
            catch (Exception e) {
                return "";
            }
        }
        try {
            layerToSpecifiedFeaturesMap = SpecifyFeaturesTool.layerToSpecifiedFeaturesMap(this.panel.getLayerManager().getVisibleLayers(false).iterator(), layerNamesToAttributeMap.keySet(), EnvelopeUtil.expand(new Envelope(this.panel.getViewport().toModelCoordinate(mouseLocation)), (double)PIXEL_BUFFER / this.panel.getViewport().getScale()));
        }
        catch (Exception e) {
            return "";
        }
        if (layerToSpecifiedFeaturesMap.isEmpty()) {
            return null;
        }
        if (StringUtils.isEmpty((String)template)) {
            return this.writeDefaultToolTip(layerToSpecifiedFeaturesMap);
        }
        String toolTip = template;
        if (layerNamesToAttributeMap == null || layerNamesToAttributeMap.isEmpty()) {
            for (String attributeName : this.extractAttributeNames(template)) {
                toolTip = StringUtil.replaceAll(toolTip, "{" + attributeName + "}", this.findValue("", attributeName, layerToSpecifiedFeaturesMap));
            }
        } else {
            for (String layerName : layerNamesToAttributeMap.keySet()) {
                toolTip = StringUtil.replaceAll(toolTip, "[" + layerName, "");
                toolTip = StringUtil.replaceAll(toolTip, "]", "");
                List<String> layerAttributes = layerNamesToAttributeMap.get(layerName);
                for (String attributeName : layerAttributes) {
                    toolTip = StringUtil.replace(toolTip, "{" + attributeName + "}", this.findValue(layerName, attributeName, layerToSpecifiedFeaturesMap), false);
                }
            }
        }
        return toolTip;
    }

    private Map<String, List<String>> processTemplate(String template) {
        HashMap<String, List<String>> templateToLayerAttrNames = new HashMap<String, List<String>>();
        if (StringUtils.isNotEmpty((String)template)) {
            ArrayList<String> attrNames = new ArrayList<String>();
            String currentAttributeName = "";
            String currentLayerName = "";
            boolean layerName = false;
            int i = 0;
            while (i < template.length()) {
                switch (template.charAt(i)) {
                    case '[': {
                        layerName = true;
                        break;
                    }
                    case ']': {
                        if (currentLayerName == null || currentLayerName.equals("")) break;
                        templateToLayerAttrNames.put(currentLayerName.trim(), attrNames);
                        attrNames = new ArrayList();
                        currentLayerName = "";
                        break;
                    }
                    case '{': {
                        layerName = false;
                        currentAttributeName = "";
                        break;
                    }
                    case '}': {
                        attrNames.add(currentAttributeName.trim());
                        currentAttributeName = "";
                        break;
                    }
                    default: {
                        if (layerName) {
                            currentLayerName = String.valueOf(currentLayerName) + template.charAt(i);
                            break;
                        }
                        currentAttributeName = String.valueOf(currentAttributeName) + template.charAt(i);
                    }
                }
                ++i;
            }
        }
        return templateToLayerAttrNames;
    }

    private String writeDefaultToolTip(Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap) {
        boolean showAreaAndLength = PersistentBlackboardPlugIn.get(JUMPWorkbench.getBlackboard()).get(MapToolTipsPlugIn.SHOW_AREA_AND_LENGTH_KEY, true);
        Layerable layerable = layerToSpecifiedFeaturesMap.keySet().iterator().next();
        Feature feature = layerToSpecifiedFeaturesMap.get(layerable).iterator().next();
        int numFeatures = 0;
        for (Collection<Feature> currentCollection : layerToSpecifiedFeaturesMap.values()) {
            numFeatures += currentCollection.size();
        }
        String toolTip = "<HTML>";
        toolTip = String.valueOf(toolTip) + "<I>" + GUIUtil.escapeHTML(layerable.getName(), true) + "</I><BR>";
        FeatureSchema schema = feature.getSchema();
        toolTip = String.valueOf(toolTip) + this.format(schema.getPrimaryKeyName(), feature.getPrimaryKey());
        int i = 0;
        while (i < Math.min(10, feature.getSchema().getAttributeCount())) {
            AttributeType attrType;
            Attribute attr = schema.getAttribute(i);
            if (attr.isVisibility() && (attrType = attr.getType()) != AttributeType.GEOMETRY && schema.getPrimaryKeyIndex() != i) {
                Object value = feature.getAttribute(i);
                if (value != null) {
                    if (AttributeType.isDate(attrType)) {
                        value = DateFormatManager.getDateTimeFormat().format((Date)value);
                    } else if (AttributeType.isNumeric(attrType)) {
                        value = NumberFormatManager.getFormattedValue((Number)value);
                    }
                }
                toolTip = String.valueOf(toolTip) + "<BR>" + this.format(schema.getPublicName(i), value);
            }
            ++i;
        }
        if (feature.getSchema().getAttributeCount() > 10) {
            toolTip = String.valueOf(toolTip) + "<BR>...";
        }
        if (showAreaAndLength) {
            Geometry geom = feature.getGeometry();
            if (geom == null) {
                toolTip = String.valueOf(toolTip) + "<BR>" + I18N.getString("com.vividsolutions.jump.workbench.ui.ToolTipWriter.Null-geometry");
            } else if (geom instanceof Polygon || geom instanceof MultiPolygon) {
                toolTip = String.valueOf(toolTip) + "<BR>" + this.format(I18N.getString("com.vividsolutions.jump.workbench.ui.ToolTipWriter.Area"), geom.getArea());
                toolTip = String.valueOf(toolTip) + "<BR>" + this.format(I18N.getString("com.vividsolutions.jump.workbench.ui.ToolTipWriter.Perimeter"), geom.getLength());
            } else if (geom instanceof LineString || geom instanceof MultiLineString) {
                toolTip = String.valueOf(toolTip) + "<BR>" + this.format(I18N.getString("com.vividsolutions.jump.workbench.ui.ToolTipWriter.Length"), geom.getLength());
            }
        }
        if (numFeatures == 2) {
            toolTip = String.valueOf(toolTip) + "<BR>(" + (numFeatures - 1) + I18N.getString("com.vividsolutions.jump.workbench.ui.ToolTipWriter.additional-element-not-shown") + ")";
        } else if (numFeatures > 2) {
            toolTip = String.valueOf(toolTip) + "<BR>(" + (numFeatures - 1) + I18N.getString("com.vividsolutions.jump.workbench.ui.ToolTipWriter.additional-elements-not-shown") + ")";
        }
        toolTip = String.valueOf(toolTip) + "</HTML>";
        return toolTip;
    }

    private String format(String name, Object value) {
        return "<B>" + GUIUtil.escapeHTML(name, true) + ":</B> " + GUIUtil.escapeHTML("" + value, true);
    }

    private String findValue(String layerName, String attributeName, Map<Layer, Collection<Feature>> layerToSpecifiedFeaturesMap) {
        for (Layer layer : layerToSpecifiedFeaturesMap.keySet()) {
            if (!layerName.equals("") && !layerName.equals(layer.getName())) continue;
            Attribute attr = layer.getFeatureSchema().getPublicAttribute(attributeName);
            Collection<Feature> col = layerToSpecifiedFeaturesMap.get(layer);
            if (CollectionUtils.isEmpty(col)) {
                return "";
            }
            Feature feat = col.iterator().next();
            if (feat == null) {
                return "";
            }
            if ("fid".equalsIgnoreCase(attributeName)) {
                return "" + feat.getID();
            }
            return feat.getString(attr.getName());
        }
        return "";
    }

    private Set<String> extractAttributeNames(String template) {
        TreeSet<String> attributeNames = new TreeSet<String>();
        String currentAttributeName = "";
        int i = 0;
        while (i < template.length()) {
            switch (template.charAt(i)) {
                case '{': {
                    currentAttributeName = "";
                    break;
                }
                case '}': {
                    attributeNames.add(currentAttributeName.trim());
                    currentAttributeName = "";
                    break;
                }
                default: {
                    currentAttributeName = String.valueOf(currentAttributeName) + template.charAt(i);
                }
            }
            ++i;
        }
        return attributeNames;
    }

    public void dispose() {
        this.panel = null;
    }
}

