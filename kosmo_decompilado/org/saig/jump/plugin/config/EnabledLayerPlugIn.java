/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.plugin.config;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.HiperLinkCompound;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.wms.WMService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.jump.lang.I18N;

public class EnabledLayerPlugIn
extends AbstractPlugIn {
    public static final String NAME = I18N.getString("org.saig.jump.plugin.config.EnabledLayerPlugIn.Enable-disable");
    public static final Icon ICON = IconLoader.icon("blank.png");
    public static final Logger LOGGER = Logger.getLogger(EnabledLayerPlugIn.class);

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
        return EnabledLayerPlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.reportNothingToUndoYet(context);
        Layerable obj = context.getSelectedLayer(0);
        if (obj instanceof Layer) {
            Layer layer = (Layer)obj;
            boolean newState = !layer.isEnabled();
            layer.setEnabled(newState);
            if (newState) {
                try {
                    layer.setFeatureCollection(this.executeQuery(layer.getDataSourceQuery().getQuery(), layer.getDataSourceQuery().getDataSource(), layer.getProjection())[0]);
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
                FeatureSchema schema = layer.getFeatureSchema();
                Map<String, Map<Locale, String>> attributesTranslations = layer.getAttributeTranslationsMap();
                Map<String, Boolean> attributesVisibilities = layer.getAttributeVisibility();
                int i = 0;
                while (i < schema.getAttributeCount()) {
                    String name = schema.getAttributeName(i);
                    if (attributesTranslations.containsKey(name)) {
                        schema.changeTranslations(name, attributesTranslations.get(name));
                    }
                    if (attributesVisibilities.containsKey(name)) {
                        schema.changeVisibility(name, attributesVisibilities.get(name));
                    }
                    ++i;
                }
                layer.setFeatureCollectionModified(false);
                List<Layer> layers = context.getTask().getLayerManager().getLayers();
                for (Layer iLayer : layers) {
                    if (!iLayer.isEnabled()) continue;
                    ArrayList<LayerRelation> addRelations = new ArrayList<LayerRelation>();
                    Collection<Relation<?>> relations = iLayer.getAllRelations();
                    for (Relation<?> relation : relations) {
                        LayerRelation layerRelation;
                        if (!(relation instanceof LayerRelation) || !(layerRelation = (LayerRelation)relation).getTargetLayer().equals(layer)) continue;
                        addRelations.add(layerRelation);
                    }
                    for (Relation relation : addRelations) {
                        try {
                            relation.fillValues();
                        }
                        catch (Exception e1) {
                            LOGGER.error((Object)"", (Throwable)e1);
                        }
                        iLayer.addRelation(relation);
                    }
                }
                Map<String, Relation<?>> relations = layer.getRelations();
                layer.setRelations(relations);
                if (layer.hashiperLink() && layer.getHiperLink() instanceof HiperLinkCompound) {
                    HiperLinkCompound hiper = (HiperLinkCompound)layer.getHiperLink();
                    hiper.setTable(context.getWorkbenchContext().getDataManager().getTable(hiper.getTable().getName()));
                }
                LayerManager layerManager = layer.getLayerManager();
                layerManager.remove(layer, false);
                layerManager.addLayerable(layer.getOldCategoryName(), layer, layer.getOldCategoryIndex());
                layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
            } else {
                LayerManager layerManager = layer.getLayerManager();
                String categoryName = layerManager.getCategory(layer).getName();
                int index = layerManager.getCategory(layer).indexOf(layer);
                layerManager.remove(layer, false);
                layer.setOldCategoryIndex(index);
                layer.setOldCategoryName(categoryName);
                FeatureDataset emptyFC = new FeatureDataset(layer.getUltimateFeatureCollectionWrapper().getFeatureSchema());
                layer.setFeatureCollection(emptyFC);
                layerManager.addLayerable(StandardCategoryNames.DISABLED, layer);
                layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
            }
        } else {
            boolean newState;
            WMSLayer layer = (WMSLayer)obj;
            boolean bl = newState = !layer.isEnabled();
            if (layer.isEnabled() != newState) {
                layer.setEnabled(newState);
                if (newState) {
                    try {
                        WMService service = new WMService(layer.getServerURL());
                        service.initialize();
                        if (!service.isInitialized()) {
                            throw new Exception(I18N.getMessage("org.saig.jump.plugin.config.EnabledLayerPlugIn.Error-while-connecting-with-server-{0}", new Object[]{service.getServerUrl()}));
                        }
                        layer.setService(service);
                    }
                    catch (Exception e1) {
                        LOGGER.error((Object)"", (Throwable)e1);
                    }
                    LayerManager layerManager = layer.getLayerManager();
                    layerManager.remove(layer, false);
                    layerManager.addLayerable(layer.getOldCategoryName(), layer, layer.getOldCategoryIndex());
                    layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
                } else {
                    LayerManager layerManager = layer.getLayerManager();
                    String categoryName = layerManager.getCategory(layer).getName();
                    int index = layerManager.getCategory(layer).indexOf(layer);
                    layerManager.remove(layer, false);
                    layer.setOldCategoryIndex(index);
                    layer.setOldCategoryName(categoryName);
                    layerManager.addLayerable(StandardCategoryNames.DISABLED, layer);
                    layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
                }
            }
        }
        return true;
    }

    private FeatureCollection[] executeQuery(String query, DataSource dataSource, IProjection proj) throws Exception {
        Connection connection = dataSource.getConnection();
        try {
            FeatureCollection[] featureCollectionArray = connection.executeQuery(query, proj);
            return featureCollectionArray;
        }
        finally {
            connection.close();
        }
    }

    public static EnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayerablesMustBeSelectedCheck(1, Layerable.class)).add(checkFactory.createSelectedLayersMustBeNoInternals()).add(checkFactory.createSelectedLayersMustNotBeFromMemoryCheck()).add(new EnableCheck(){

            @Override
            public String check(JComponent component) {
                ((JCheckBoxMenuItem)component).setSelected(workbenchContext.getLayerNamePanel().selectedNodes(Layerable.class).iterator().next().isEnabled());
                return null;
            }
        });
    }
}

