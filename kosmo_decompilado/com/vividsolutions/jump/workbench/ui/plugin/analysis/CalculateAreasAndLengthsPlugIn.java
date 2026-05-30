/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Geometry
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui.plugin.analysis;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureUtil;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.controllers.analysis.CalculateAreasAndLengthsOptionsDialogController;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Icon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.dao.datasource.memory.CollectionIterator;
import org.saig.core.model.feature.FeatureIterator;
import org.saig.jump.lang.I18N;

public class CalculateAreasAndLengthsPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    private static final Logger LOGGER = Logger.getLogger(CalculateAreasAndLengthsPlugIn.class);
    public static final String NAME = I18N.getString("workbench.ui.plugin.analysis.CalculateAreasAndLengthsPlugIn.name");
    public static final Icon ICON = IconLoader.icon("calculateAreasAndLenghts.png");
    public CalculateAreasAndLengthsOptionsDialogController controller;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public EnableCheck getCheck() {
        return CalculateAreasAndLengthsPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        int numFeaturesSelected = context.getLayerViewPanel().getSelectionManager().getNumFeaturesWithSelectedItems(editableLayer);
        if (this.controller == null) {
            this.controller = new CalculateAreasAndLengthsOptionsDialogController();
        }
        this.controller.refresh(editableLayer, numFeaturesSelected);
        this.controller.show();
        return this.controller.wasOkPressed();
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        FeatureIterator featIterator;
        monitor.allowCancellationRequests();
        Layer editableLayer = context.getLayerManager().getEditableLayers().iterator().next();
        FeatureSchema editableFeatureSchema = editableLayer.getFeatureSchema();
        boolean updateAreas = this.controller.isCalculateAreasSelected();
        String areasAttrPublicName = this.controller.getAreasAttributeName();
        String areasAttrName = null;
        if (updateAreas) {
            areasAttrName = editableFeatureSchema.getAttributeName(areasAttrPublicName);
        }
        boolean updateLengths = this.controller.isCalculateLengthsSelected();
        String lengthsAttrPublicName = this.controller.getLengthsAttributeName();
        String lengthsAttrName = null;
        if (updateLengths) {
            lengthsAttrName = editableFeatureSchema.getAttributeName(lengthsAttrPublicName);
        }
        boolean useSelectedOnly = this.controller.useSelectedOnly();
        Collection<Feature> selectedFeatures = null;
        if (useSelectedOnly) {
            selectedFeatures = context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(editableLayer);
            context.getLayerViewPanel().getSelectionManager().unselectItems(editableLayer, selectedFeatures);
            featIterator = new CollectionIterator(selectedFeatures);
        } else {
            featIterator = editableLayer.getFeatureCollectionWrapper().iterator();
        }
        this.update(editableLayer, featIterator, updateAreas, areasAttrName, updateLengths, lengthsAttrName, useSelectedOnly, monitor);
        if (!monitor.isCancelRequested()) {
            editableLayer.setFeatureCollectionModified(true);
            context.getLayerManager().fireLayerChanged(editableLayer, LayerEventType.COMMITED);
            if (useSelectedOnly) {
                context.getLayerViewPanel().getSelectionManager().getFeatureSelection().selectItems(editableLayer, selectedFeatures);
            }
            this.warnOperationSuccessful(context);
        } else {
            this.warnOperationCancelled(context);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected void update(Layer editableLayer, FeatureIterator featIterator, boolean updateAreas, String areasAttrName, boolean updateLengths, String lengthsAttrName, boolean useSelectedOnly, TaskMonitor monitor) throws Exception {
        FeatureCollectionWrapper fc = editableLayer.getFeatureCollectionWrapper();
        FeatureSchema schema = fc.getFeatureSchema();
        int areaAttrIndex = updateAreas ? schema.getAttributeIndex(areasAttrName) : -1;
        int lengthAttrIndex = updateLengths ? schema.getAttributeIndex(lengthsAttrName) : -1;
        AttributeType areasAttrType = updateAreas ? schema.getAttributeType(areaAttrIndex) : null;
        AttributeType lengthsAttrType = updateLengths ? schema.getAttributeType(lengthAttrIndex) : null;
        Op areasOp = new Op(){

            @Override
            public Double compute(Geometry g) {
                if (g != null) {
                    return g.getArea();
                }
                return null;
            }
        };
        Op lengthsOp = new Op(){

            @Override
            public Double compute(Geometry g) {
                if (g != null) {
                    return g.getLength();
                }
                return null;
            }
        };
        ArrayList<Feature> featsToUpdate = new ArrayList<Feature>();
        try {
            try {
                while (featIterator.hasNext() && !monitor.isCancelRequested()) {
                    Feature feat = featIterator.next();
                    Feature cloneFeature = (Feature)feat.clone();
                    if (updateAreas) {
                        cloneFeature.setAttribute(areaAttrIndex, this.convert(areasOp.compute(feat.getGeometry()), areasAttrType));
                    }
                    if (updateLengths) {
                        cloneFeature.setAttribute(lengthAttrIndex, this.convert(lengthsOp.compute(feat.getGeometry()), lengthsAttrType));
                    }
                    featsToUpdate.add(cloneFeature);
                }
            }
            catch (Exception ex) {
                LOGGER.error((Object)"", (Throwable)ex);
                throw ex;
            }
        }
        finally {
            if (featIterator != null) {
                featIterator.close();
            }
        }
        if (!monitor.isCancelRequested() && CollectionUtils.isNotEmpty(featsToUpdate)) {
            fc.updateAll(featsToUpdate);
        }
    }

    private Object convert(Double d, AttributeType attributeType) {
        return FeatureUtil.getGoodAttribute(attributeType, d);
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        MultiEnableCheck check = new MultiEnableCheck();
        check.add(checkFactory.createWindowWithLayerManagerMustBeActiveCheck());
        check.add(checkFactory.createAtLeastNLayersMustExistCheck(1));
        check.add(checkFactory.createAtLeastNLayersMustBeEditableCheck(1));
        check.add(checkFactory.createEditableLayerTypeGeometryCheck(new int[]{5, 4, 3, 2}));
        check.add(checkFactory.createEditableLayerMustHaveAtLeastOneValidAttributeType(CalculateAreasAndLengthsOptionsDialogController.VALID_ATTRIBUTE_TYPES));
        return check;
    }

    private static interface Op {
        public Double compute(Geometry var1);
    }
}

