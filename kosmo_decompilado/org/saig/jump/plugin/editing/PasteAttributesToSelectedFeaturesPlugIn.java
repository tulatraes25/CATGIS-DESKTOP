/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.plugin.editing;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.plugin.clipboard.CollectionOfFeaturesTransferable;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.saig.jump.lang.I18N;

public class PasteAttributesToSelectedFeaturesPlugIn
extends AbstractPlugIn
implements ThreadedPlugIn {
    public static final Logger LOGGER = Logger.getLogger(PasteAttributesToSelectedFeaturesPlugIn.class);
    public static final String NAME = I18N.getString("org.saig.jump.plugin.editing.PasteAttributesToSelectedFeaturesPlugIn.paste-attributes-to-the-selected-features");
    public static final Icon ICON = IconLoader.icon("paste.gif");
    protected Feature originalFeature;
    private List<Layer> layers;
    protected PlugInContext context;
    private List<String> attributeNames;
    protected boolean undoable;
    protected boolean commitAtEnd;

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        block6: {
            block5: {
                this.context = context;
                this.reportNothingToUndoYet(context);
                try {
                    Transferable transferable = GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard());
                    if (transferable.isDataFlavorSupported(CollectionOfFeaturesTransferable.COLLECTION_OF_FEATURES_FLAVOR)) break block5;
                    return false;
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                    return false;
                }
            }
            ArrayList features = new ArrayList((Collection)GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard()).getTransferData(CollectionOfFeaturesTransferable.COLLECTION_OF_FEATURES_FLAVOR));
            if (features.size() == 1) {
                this.originalFeature = (Feature)features.get(0);
                this.originalFeature.getGeometry().getGeometryType();
                this.layers = this.getLayers();
                if (this.layers.isEmpty()) {
                    return false;
                }
                break block6;
            }
            return false;
        }
        return true;
    }

    protected List<Layer> getLayers() {
        ArrayList<Layer> capas = new ArrayList<Layer>();
        this.undoable = true;
        this.commitAtEnd = false;
        Layer editableLayer = this.context.getLayerManager().getEditableLayers().iterator().next();
        if (this.originalFeature.getSchema().equals(editableLayer.getFeatureSchema(), false)) {
            capas.add(editableLayer);
        }
        return capas;
    }

    @Override
    public void run(TaskMonitor monitor, PlugInContext context) throws Exception {
        SelectionManager sm = context.getLayerViewPanel().getSelectionManager();
        FeatureSchema schema = this.originalFeature.getSchema();
        this.attributeNames = this.getAttributeNamesToPaste(schema);
        if (this.undoable) {
            AbstractPlugIn.execute(this.createUndoableCommand(), JUMPWorkbench.getFrameInstance().getContext());
        } else {
            for (Layer capa : this.layers) {
                ArrayList<Feature> featuresToUpdate = new ArrayList<Feature>();
                Collection<Feature> features = sm.getFeaturesWithSelectedItems(capa);
                for (Feature feat : features) {
                    for (String name : this.attributeNames) {
                        feat.setAttribute(name, this.originalFeature.getAttribute(name));
                    }
                    featuresToUpdate.add(feat);
                }
                capa.getFeatureCollectionWrapper().updateAll(featuresToUpdate);
                capa.setFeatureCollectionModified(true);
                if (this.commitAtEnd) {
                    capa.getFeatureCollectionWrapper().commit();
                }
                capa.fireLayerChanged(LayerEventType.COMMITED);
            }
        }
    }

    protected List<String> getAttributeNamesToPaste(FeatureSchema schema) {
        List<String> names = schema.getAttributeNames();
        names.remove(schema.getPrimaryKeyName());
        names.remove(schema.getAttributeName(schema.getGeometryIndex()));
        if (schema.isVersionable()) {
            names.remove(schema.getFieldStartDate());
            names.remove(schema.getFieldEndDate());
            names.remove(schema.getHistoryField());
        }
        return names;
    }

    public static MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(new EnableCheck(){

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
                    try {
                        Collection features = (Collection)GUIUtil.getContents(Toolkit.getDefaultToolkit().getSystemClipboard()).getTransferData(CollectionOfFeaturesTransferable.COLLECTION_OF_FEATURES_FLAVOR);
                        if (features.size() == 1) {
                            return null;
                        }
                    }
                    catch (Exception e) {
                        LOGGER.warn((Object)"", (Throwable)e);
                    }
                }
                return I18N.getString("org.saig.jump.plugin.editing.PasteAttributesToSelectedFeaturesPlugIn.the-clipboard-must-contain-only-one-element");
            }
        }).add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1)).add(checkFactory.createSelectedLayersMustBeEditableCheck()).add(checkFactory.createAtLeastNItemsMustBeSelectedCheck(1));
    }

    @Override
    public EnableCheck getCheck() {
        return PasteAttributesToSelectedFeaturesPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    private UndoableCommand createUndoableCommand() {
        return new UndoableCommand(this.getName()){
            private Layer editableLayer;
            private Collection<Feature> selectedFeatures;
            private HashMap<Feature, HashMap<String, Object>> featuresIniciales;
            private HashMap<String, Object> mapAttributesToPaste;
            {
                this.editableLayer = (Layer)PasteAttributesToSelectedFeaturesPlugIn.this.layers.iterator().next();
                this.selectedFeatures = PasteAttributesToSelectedFeaturesPlugIn.this.context.getLayerViewPanel().getSelectionManager().getFeaturesWithSelectedItems(this.editableLayer);
                this.featuresIniciales = PasteAttributesToSelectedFeaturesPlugIn.this.getFeaturesInicialesMap(this.selectedFeatures);
                this.mapAttributesToPaste = PasteAttributesToSelectedFeaturesPlugIn.this.getMapAttributesOfFeature(PasteAttributesToSelectedFeaturesPlugIn.this.originalFeature);
            }

            @Override
            public void execute() {
                for (Feature feat : this.selectedFeatures) {
                    for (String name : PasteAttributesToSelectedFeaturesPlugIn.this.attributeNames) {
                        feat.setAttribute(name, this.mapAttributesToPaste.get(name));
                    }
                }
                try {
                    this.editableLayer.getFeatureCollectionWrapper().updateAll(this.selectedFeatures);
                    this.editableLayer.setFeatureCollectionModified(true);
                    this.editableLayer.fireLayerChanged(LayerEventType.COMMITED);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }

            @Override
            public void unexecute() {
                for (Feature feat : this.selectedFeatures) {
                    HashMap<String, Object> map = this.featuresIniciales.get(feat);
                    for (String name : PasteAttributesToSelectedFeaturesPlugIn.this.attributeNames) {
                        feat.setAttribute(name, map.get(name));
                    }
                }
                try {
                    this.editableLayer.getFeatureCollectionWrapper().updateAll(this.selectedFeatures);
                    this.editableLayer.setFeatureCollectionModified(true);
                    this.editableLayer.fireLayerChanged(LayerEventType.COMMITED);
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
            }
        };
    }

    private HashMap<Feature, HashMap<String, Object>> getFeaturesInicialesMap(Collection<Feature> selectedFeatures) {
        HashMap<Feature, HashMap<String, Object>> mapa = new HashMap<Feature, HashMap<String, Object>>();
        for (Feature feat : selectedFeatures) {
            mapa.put(feat, this.getMapAttributesOfFeature(feat));
        }
        return mapa;
    }

    private HashMap<String, Object> getMapAttributesOfFeature(Feature feature) {
        HashMap<String, Object> mapAttributes = new HashMap<String, Object>();
        for (String name : this.attributeNames) {
            mapAttributes.put(name, feature.getAttribute(name));
        }
        return mapAttributes;
    }
}

