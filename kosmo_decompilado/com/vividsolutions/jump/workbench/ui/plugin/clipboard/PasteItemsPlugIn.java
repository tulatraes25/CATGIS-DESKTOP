/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  com.vividsolutions.jts.geom.LineString
 *  com.vividsolutions.jts.geom.MultiLineString
 *  com.vividsolutions.jts.geom.MultiPoint
 *  com.vividsolutions.jts.geom.MultiPolygon
 *  com.vividsolutions.jts.geom.Point
 *  com.vividsolutions.jts.geom.Polygon
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin.clipboard;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.io.WKTReader;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CollectionOfFeaturesTransferable;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class PasteItemsPlugIn
extends AbstractPlugIn {
    private static final Logger LOGGER = Logger.getLogger(PasteItemsPlugIn.class);
    public static final String NAME = I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("paste.gif");
    private WKTReader reader = new WKTReader();

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
        Transferable transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
        List<Feature> features = transferable.isDataFlavorSupported(CollectionOfFeaturesTransferable.COLLECTION_OF_FEATURES_FLAVOR) ? (List<Feature>)GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard()).getTransferData(CollectionOfFeaturesTransferable.COLLECTION_OF_FEATURES_FLAVOR) : this.reader.read(new StringReader((String)transferable.getTransferData(DataFlavor.stringFlavor))).getFeatures();
        final Layer layer = (Layer)context.getSelectedLayer(0);
        final Collection<Feature> featureCopies = PasteItemsPlugIn.conform(features, layer.getUltimateFeatureCollectionWrapper().getFeatureSchema());
        int numFeatures = features.size();
        if (featureCopies.size() > 0) {
            this.execute(new UndoableCommand(this.getName()){

                @Override
                public void execute() throws Exception {
                    layer.getFeatureCollectionWrapper().addAll(featureCopies);
                }

                @Override
                public void unexecute() throws Exception {
                    layer.getFeatureCollectionWrapper().removeAll(featureCopies);
                }
            }, context);
            context.getWorkbenchFrame().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn.{0}-valid-features-have-been-pasted-from-the-{1}-elements-that-exist-in-the-clipboard", new Object[]{new Integer(featureCopies.size()), new Integer(numFeatures)}));
        } else {
            context.getWorkbenchFrame().warnUser(I18N.getMessage("com.vividsolutions.jump.workbench.ui.plugin.clipboard.PasteItemsPlugIn.No-valid-features-have-been-found-from-the-{0}-elements-that-exist-in-the-clipboard", new Object[]{new Integer(numFeatures)}));
        }
        return true;
    }

    public static Collection<Feature> conform(Collection<Feature> features, FeatureSchema targetFeatureSchema) {
        ArrayList<Feature> featureCopies = new ArrayList<Feature>();
        for (Feature feature : features) {
            if (!PasteItemsPlugIn.conformGeometryType(feature, targetFeatureSchema.getGeometryType())) continue;
            featureCopies.add(PasteItemsPlugIn.conform(feature, targetFeatureSchema));
        }
        return featureCopies;
    }

    private static boolean conformGeometryType(Feature feature, int geometryType) {
        Geometry geom = feature.getGeometry();
        boolean conform = false;
        if (geom != null) {
            switch (geometryType) {
                case 1: 
                case 8: {
                    conform = geom instanceof Point || geom instanceof MultiPoint;
                    break;
                }
                case 2: 
                case 3: {
                    conform = geom instanceof LineString || geom instanceof MultiLineString;
                    break;
                }
                case 4: 
                case 5: {
                    conform = geom instanceof Polygon || geom instanceof MultiPolygon;
                }
            }
        }
        return conform;
    }

    private static Feature conform(Feature original, FeatureSchema targetFeatureSchema) {
        BasicFeature copy = new BasicFeature(targetFeatureSchema);
        copy.setGeometry((Geometry)original.getGeometry().clone());
        int i = 0;
        while (i < original.getSchema().getAttributeCount()) {
            if (i != original.getSchema().getGeometryIndex()) {
                AttributeType copyAttrType;
                String attributeName = original.getSchema().getAttributeName(i);
                if (copy.getSchema().hasAttribute(attributeName) && AttributeType.areCompatibleTypes(copyAttrType = copy.getSchema().getAttributeType(attributeName), original.getSchema().getAttributeType(attributeName))) {
                    if (AttributeType.isNumeric(copyAttrType)) {
                        copy.setAttribute(attributeName, FeatureUtil.getGoodAttribute(copyAttrType, (Number)original.getAttribute(attributeName)));
                    } else {
                        copy.setAttribute(attributeName, FeatureUtil.getGoodAttribute(copyAttrType, original.getAttribute(attributeName)));
                    }
                }
            }
            ++i;
        }
        copy.setAttribute(targetFeatureSchema.getPrimaryKeyName(), null);
        return copy;
    }

    public static MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustBeEditableCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                Transferable transferable;
                try {
                    transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    return I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.wrong-clipboard-content");
                }
                if (transferable == null) {
                    return I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.clipboard-must-not-be-empty");
                }
                if (transferable.isDataFlavorSupported(CollectionOfFeaturesTransferable.COLLECTION_OF_FEATURES_FLAVOR)) {
                    return null;
                }
                try {
                    if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor) && this.isWKT((String)transferable.getTransferData(DataFlavor.stringFlavor))) {
                        return null;
                    }
                }
                catch (Exception e) {
                    workbenchContext.getErrorHandler().handleThrowable(e);
                }
                return I18N.getString("workbench.ui.plugin.clipboard.PasteItemsPlugIn.clipboard-must-contain-geometries-or-WKT");
            }

            private boolean isWKT(String s) {
                try {
                    new WKTReader().read(new StringReader(s));
                    return true;
                }
                catch (Exception e) {
                    return false;
                }
            }
        });
    }

    @Override
    public EnableCheck getCheck() {
        return PasteItemsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }
}

