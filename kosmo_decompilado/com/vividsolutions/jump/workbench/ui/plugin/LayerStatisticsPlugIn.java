/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.HTMLFrame;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.utils.GUITranslationsUtils;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class LayerStatisticsPlugIn
extends AbstractPlugIn {
    public static final Logger LOGGER = Logger.getLogger(LayerStatisticsPlugIn.class);
    public static final String NAME = I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("chart_curve.png");

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable[] selectedLayers = context.getSelectedLayers();
        if (selectedLayers == null || selectedLayers.length < 1) {
            return false;
        }
        HTMLFrame out = context.getOutputFrame();
        out.createNewDocument();
        out.addHeader(1, NAME);
        LayerStatistics totalStats = new LayerStatistics();
        Envelope totalEnv = new Envelope();
        int i = 0;
        while (i < selectedLayers.length) {
            Layerable layerable = selectedLayers[i];
            if (layerable instanceof Layer) {
                Layer layer = (Layer)layerable;
                LayerStatistics ls = this.layerStatistics(layer, totalStats);
                out.addHeader(2, String.valueOf(I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.layer")) + ": " + layer.getName());
                Envelope layerEnv = layer.getFeatureCollectionWrapper().getEnvelope();
                out.addField(String.valueOf(I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.envelope")) + ": ", "");
                out.append("<ul><li>");
                out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.X-Max")) + ":", new Double(layerEnv.getMaxX()).toString());
                out.append("</li><li>");
                out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.X-Min")) + ":", new Double(layerEnv.getMinX()).toString());
                out.append("</li><li>");
                out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.Y-Max")) + ":", new Double(layerEnv.getMaxY()).toString());
                out.append("</li><li>");
                out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.Y-Min")) + ":", new Double(layerEnv.getMinY()).toString());
                out.append("</li></ul>");
                totalEnv.expandToInclude(layerEnv);
                out.addField("SRS:", GUITranslationsUtils.getCRSDescription(layer.getProjection()));
                this.output(ls, out, true);
            }
            ++i;
        }
        if (selectedLayers.length > 1) {
            out.addHeader(1, I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.summary-for-all-layers"));
            out.addField(String.valueOf(I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.envelope")) + ": ", "");
            out.append("<ul><li>");
            out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.X-Max")) + ":", new Double(totalEnv.getMaxX()).toString());
            out.append("</li><li>");
            out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.X-Min")) + ":", new Double(totalEnv.getMinX()).toString());
            out.append("</li><li>");
            out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.Y-Max")) + ":", new Double(totalEnv.getMaxY()).toString());
            out.append("</li><li>");
            out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.Y-Min")) + ":", new Double(totalEnv.getMinY()).toString());
            out.append("</li></ul>");
            this.output(totalStats, out, false);
        }
        out.surface();
        return true;
    }

    private LayerStatistics layerStatistics(Layer layer, LayerStatistics totalStats) {
        int codTipo;
        LayerStatistics ls = new LayerStatistics();
        FeatureIterator itFeatures = null;
        FeatureCollectionWrapper fc = layer.getFeatureCollectionWrapper();
        String nombreTipo = layer.isRaster() ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Raster-image") : ((codTipo = layer.getGeometryType()) == 3 ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.LineString") : (codTipo == 2 ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.MultiLineString") : (codTipo == 8 ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.MultiPoint") : (codTipo == 4 ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.MultiPolygon") : (codTipo == 1 ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Point") : (codTipo == 5 ? I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Polygon") : I18N.getString("org.saig.jump.widgets.info.LayerInfoDialog.Unknown")))))));
        ls.setType(nombreTipo);
        try {
            try {
                itFeatures = fc.iterator();
                while (itFeatures.hasNext()) {
                    Feature feat = itFeatures.next();
                    Geometry g = feat.getGeometry();
                    double area = g.getArea();
                    double length = g.getLength();
                    int comps = 1;
                    if (g instanceof GeometryCollection) {
                        comps = ((GeometryCollection)g).getNumGeometries();
                    }
                    Coordinate[] pts = g.getCoordinates();
                    int holes = 0;
                    if (g instanceof Polygon) {
                        holes = ((Polygon)g).getNumInteriorRing();
                    }
                    ls.addFeature(pts.length, holes, comps, area, length);
                    totalStats.addFeature(pts.length, holes, comps, area, length);
                }
            }
            catch (Exception e) {
                LOGGER.warn((Object)I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.An-error-has-been-produced-while-processing-the-layer-{0}", new Object[]{layer.getName()}), (Throwable)e);
                if (itFeatures != null) {
                    itFeatures.close();
                }
            }
        }
        finally {
            if (itFeatures != null) {
                itFeatures.close();
            }
        }
        return ls;
    }

    public void output(LayerStatistics ls, HTMLFrame out, boolean showLayerType) {
        out.addField(String.valueOf(I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.num-features")) + ":", String.valueOf(ls.featureCount));
        if (showLayerType) {
            out.addField(String.valueOf(I18N.getString("com.vividsolutions.jump.workbench.ui.plugin.LayerStatisticsPlugIn.Layer-type")) + ": ", ls.type);
        }
        out.append("<table border='1'>");
        out.append("<tr><td bgcolor=#CCCCCC>&nbsp;</td><td  bgcolor=#CCCCCC align='center'> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.min") + "</td><td  bgcolor=#CCCCCC align='center'> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.max") + " </td><td  bgcolor=#CCCCCC align='center'> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.avg") + " </td><td  bgcolor=#CCCCCC align='center'> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.total") + " </td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.pts") + " </td><td align='right'>" + ls.minCoord + "</td><td align='right'>" + ls.maxCoord + "</td><td align='right'>" + ls.avgCoord() + "</td><td align='right'>" + ls.totalCoord + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.holes") + " </td><td align='right'>" + ls.minHoles + "</td><td align='right'>" + ls.maxHoles + "</td><td align='right'>" + ls.avgHoles() + "</td><td align='right'>" + ls.totalHoles + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.components") + " </td><td align='right'>" + ls.minComp + "</td><td align='right'>" + ls.maxComp + "</td><td align='right'>" + ls.avgComp() + "</td><td align='right'>" + ls.totalComp + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.area") + " </td><td align='right'>" + ls.minArea + "</td><td align='right'>" + ls.maxArea + "</td><td align='right'>" + ls.avgArea() + "</td><td align='right'>" + ls.totalArea + "</td></tr>");
        out.append("<tr><td bgcolor=#CCCCCC> " + I18N.getString("workbench.ui.plugin.LayerStatisticsPlugIn.length") + " </td><td align='right'>" + ls.minLength + "</td><td align='right'>" + ls.maxLength + "</td><td align='right'>" + ls.avgLength() + "</td><td align='right'>" + ls.totalLength + "</td></tr>");
        out.append("</table>");
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
    }

    @Override
    public EnableCheck getCheck() {
        return LayerStatisticsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    private static class LayerStatistics {
        public String type;
        boolean isFirst = true;
        int minCoord = 0;
        int maxCoord = 0;
        int totalCoord = 0;
        int minComp = 0;
        int maxComp = 0;
        int totalComp = 0;
        int minHoles = 0;
        int maxHoles = 0;
        int totalHoles = 0;
        double minArea = 0.0;
        double maxArea = 0.0;
        double totalArea = 0.0;
        double minLength = 0.0;
        double maxLength = 0.0;
        double totalLength = 0.0;
        int featureCount = 0;

        private LayerStatistics() {
        }

        public void setType(String typeName) {
            this.type = typeName;
        }

        public void addFeature(int coordCount, int holeCount, int compCount, double area, double length) {
            ++this.featureCount;
            if (this.isFirst || coordCount < this.minCoord) {
                this.minCoord = coordCount;
            }
            if (this.isFirst || coordCount > this.maxCoord) {
                this.maxCoord = coordCount;
            }
            this.totalCoord += coordCount;
            if (this.isFirst || holeCount < this.minHoles) {
                this.minHoles = holeCount;
            }
            if (this.isFirst || holeCount > this.maxHoles) {
                this.maxHoles = holeCount;
            }
            this.totalHoles += holeCount;
            if (this.isFirst || compCount < this.minComp) {
                this.minComp = compCount;
            }
            if (this.isFirst || compCount > this.maxComp) {
                this.maxComp = compCount;
            }
            this.totalComp += compCount;
            if (this.isFirst || area < this.minArea) {
                this.minArea = area;
            }
            if (this.isFirst || area > this.maxArea) {
                this.maxArea = area;
            }
            this.totalArea += area;
            if (this.isFirst || length < this.minLength) {
                this.minLength = length;
            }
            if (this.isFirst || length > this.maxLength) {
                this.maxLength = length;
            }
            this.totalLength += length;
            this.isFirst = false;
        }

        public double avgCoord() {
            return this.featureCount == 0 ? 0.0 : 1.0 * (double)this.totalCoord / (double)this.featureCount;
        }

        public double avgHoles() {
            return this.featureCount == 0 ? 0.0 : 1.0 * (double)this.totalHoles / (double)this.featureCount;
        }

        public double avgComp() {
            return this.featureCount == 0 ? 0.0 : 1.0 * (double)this.totalComp / (double)this.featureCount;
        }

        public double avgArea() {
            return this.featureCount == 0 ? 0.0 : 1.0 * this.totalArea / (double)this.featureCount;
        }

        public double avgLength() {
            return this.featureCount == 0 ? 0.0 : 1.0 * this.totalLength / (double)this.featureCount;
        }
    }
}

