/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.GeometryCollection
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.util.StringUtil;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import org.apache.log4j.Logger;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class FeatureStatisticsPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.name");
    public static final String nPtsAttr = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.npts");
    public static final String nHolesAttr = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.nholes");
    public static final String nCompsAttr = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.ncomponents");
    public static final String areaAttr = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.area");
    public static final String lengthAttr = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.length");
    public static final String typeAttr = I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.type");
    private static final String jtsGeometryClassPackagePrefix = "com.vividsolutions.jts.geom";
    private static final Logger LOGGER = Logger.getLogger(FeatureStatisticsPlugIn.class);

    @Override
    public String getName() {
        return NAME;
    }

    public static FeatureSchema getStatisticsSchema() {
        FeatureSchema featureSchema = new FeatureSchema();
        featureSchema.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        featureSchema.addAttribute(nPtsAttr, AttributeType.INTEGER);
        featureSchema.addAttribute(nHolesAttr, AttributeType.INTEGER);
        featureSchema.addAttribute(nCompsAttr, AttributeType.INTEGER);
        featureSchema.addAttribute(areaAttr, AttributeType.DOUBLE);
        featureSchema.addAttribute(lengthAttr, AttributeType.DOUBLE);
        featureSchema.addAttribute(typeAttr, AttributeType.STRING);
        return featureSchema;
    }

    public static String removeGeometryPackage(String fullClassName) {
        if (fullClassName.startsWith(jtsGeometryClassPackagePrefix)) {
            return StringUtil.classNameWithoutQualifiers(fullClassName);
        }
        return fullClassName;
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layerable[] selectedLayers = context.getSelectedLayers();
        int i = 0;
        while (i < selectedLayers.length) {
            this.featureStatistics((Layer)selectedLayers[i], context);
            ++i;
        }
        return true;
    }

    private void featureStatistics(Layer layer, PlugInContext context) throws Exception {
        FeatureDataset statsFC;
        block11: {
            FeatureSchema statsSchema = FeatureStatisticsPlugIn.getStatisticsSchema();
            statsFC = new FeatureDataset(statsSchema);
            FeatureIterator i = null;
            try {
                try {
                    i = layer.getFeatureCollectionWrapper().iterator();
                    while (i.hasNext()) {
                        Feature f = i.next();
                        Geometry g = f.getGeometry();
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
                        Feature statsf = FeatureUtil.toFeature(g, statsSchema);
                        statsf.setAttribute(nPtsAttr, (Object)new Integer(pts.length));
                        statsf.setAttribute(nHolesAttr, (Object)new Integer(holes));
                        statsf.setAttribute(nCompsAttr, (Object)new Integer(comps));
                        statsf.setAttribute(areaAttr, (Object)new Double(area));
                        statsf.setAttribute(lengthAttr, (Object)new Double(length));
                        statsf.setAttribute(typeAttr, (Object)FeatureStatisticsPlugIn.removeGeometryPackage(g.getClass().getName()));
                        statsFC.add(statsf);
                    }
                }
                catch (RuntimeException e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    if (i != null) {
                        i.close();
                    }
                    break block11;
                }
            }
            catch (Throwable throwable) {
                if (i != null) {
                    i.close();
                }
                throw throwable;
            }
            if (i != null) {
                i.close();
            }
        }
        Layer statsLayer = context.addLayer(StandardCategoryNames.QA, String.valueOf(I18N.getString("workbench.ui.plugin.FeatureStatisticsPlugIn.statistics")) + " - " + layer.getName(), statsFC);
        statsLayer.setStyles(layer.cloneStyles());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayersMustBeSelectedCheck(1));
    }
}

