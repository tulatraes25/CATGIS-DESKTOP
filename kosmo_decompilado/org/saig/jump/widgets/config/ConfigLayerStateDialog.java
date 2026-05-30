/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.cresques.cts.IProjection
 */
package org.saig.jump.widgets.config;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.HiperLinkCompound;
import com.vividsolutions.jump.io.datasource.Connection;
import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.model.WMSLayer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import com.vividsolutions.wms.WMService;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.cresques.cts.IProjection;
import org.saig.core.model.relations.LayerRelation;
import org.saig.core.model.relations.Relation;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.config.ConfigLayerStatePanel;

public class ConfigLayerStateDialog
extends JDialog {
    private OKCancelPanel okCancelPanel;
    private ConfigLayerStatePanel layerStatePanel;
    private WorkbenchContext context;

    public ConfigLayerStateDialog(JFrame parent, boolean modal, WorkbenchContext context) {
        super((Frame)parent, modal);
        this.context = context;
        this.setTitle(I18N.getString("org.saig.jump.widgets.config.ConfigLayerStateDialog.Enable-disable-layers"));
        this.getContentPane().setLayout(new BorderLayout());
        this.layerStatePanel = new ConfigLayerStatePanel(context);
        this.getContentPane().add((Component)this.layerStatePanel, "Center");
        this.getContentPane().add((Component)this.getOkCancelPanel(), "South");
        this.pack();
        GUIUtil.centreOnWindow(this);
        this.setVisible(true);
    }

    public OKCancelPanel getOkCancelPanel() {
        if (this.okCancelPanel == null) {
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigLayerStateDialog.this.okCancelPanel_actionPerformed(e);
                }
            });
        }
        return this.okCancelPanel;
    }

    private void okCancelPanel_actionPerformed(ActionEvent e) {
        if (this.okCancelPanel.wasOKPressed()) {
            Hashtable layerState = this.layerStatePanel.getLayerState();
            for (Object obj : layerState.keySet()) {
                String categoryName;
                LayerManager layerManager;
                boolean newState;
                AbstractLayerable layer;
                if (obj instanceof Layer) {
                    layer = (Layer)obj;
                    newState = (Boolean)layerState.get(layer);
                    if (layer.isEnabled() == newState) continue;
                    layer.setEnabled(newState);
                    if (newState) {
                        try {
                            ((Layer)layer).setFeatureCollection(this.executeQuery(((Layer)layer).getDataSourceQuery().getQuery(), ((Layer)layer).getDataSourceQuery().getDataSource(), ((Layer)layer).getProjection())[0]);
                        }
                        catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        FeatureSchema schema = ((Layer)layer).getFeatureSchema();
                        Map<String, Map<Locale, String>> attributesTranslations = ((Layer)layer).getAttributeTranslationsMap();
                        Map<String, Boolean> attributesVisibilities = ((Layer)layer).getAttributeVisibility();
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
                        ((Layer)layer).setFeatureCollectionModified(false);
                        List<Layer> layers = this.context.getTask().getLayerManager().getLayers();
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
                                    e1.printStackTrace();
                                }
                                iLayer.addRelation(relation);
                            }
                        }
                        Map<String, Relation<?>> relations = ((Layer)layer).getRelations();
                        ((Layer)layer).setRelations(relations);
                        if (((Layer)layer).hashiperLink() && ((Layer)layer).getHiperLink() instanceof HiperLinkCompound) {
                            HiperLinkCompound hiper = (HiperLinkCompound)((Layer)layer).getHiperLink();
                            hiper.setTable(this.context.getDataManager().getTable(hiper.getTable().getName()));
                        }
                        LayerManager layerManager2 = layer.getLayerManager();
                        layerManager2.remove(layer, false);
                        layerManager2.addLayerable(layer.getOldCategoryName(), layer, layer.getOldCategoryIndex());
                        layerManager2.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
                        continue;
                    }
                    layerManager = layer.getLayerManager();
                    categoryName = layerManager.getCategory(layer).getName();
                    int index = layerManager.getCategory(layer).indexOf(layer);
                    layerManager.remove(layer, false);
                    layer.setOldCategoryIndex(index);
                    layer.setOldCategoryName(categoryName);
                    FeatureDataset emptyFC = new FeatureDataset(((Layer)layer).getUltimateFeatureCollectionWrapper().getFeatureSchema());
                    ((Layer)layer).setFeatureCollection(emptyFC);
                    layerManager.addLayerable(StandardCategoryNames.DISABLED, layer);
                    layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
                    continue;
                }
                layer = (WMSLayer)obj;
                newState = (Boolean)layerState.get(layer);
                if (layer.isEnabled() == newState) continue;
                layer.setEnabled(newState);
                if (newState) {
                    try {
                        WMService service = new WMService(((WMSLayer)layer).getServerURL());
                        service.initialize();
                        if (!service.isInitialized()) {
                            throw new Exception(I18N.getMessage("org.saig.jump.widgets.config.ConfigLayerStateDialog.An-error-was-produced-with-the-connection-to-server-{0}", new Object[]{service.getServerUrl()}));
                        }
                        ((WMSLayer)layer).setService(service);
                    }
                    catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    layerManager = layer.getLayerManager();
                    layerManager.remove(layer, false);
                    layerManager.addLayerable(layer.getOldCategoryName(), layer, layer.getOldCategoryIndex());
                    layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
                    continue;
                }
                layerManager = layer.getLayerManager();
                categoryName = layerManager.getCategory(layer).getName();
                int index = layerManager.getCategory(layer).indexOf(layer);
                layerManager.remove(layer, false);
                layer.setOldCategoryIndex(index);
                layer.setOldCategoryName(categoryName);
                layerManager.addLayerable(StandardCategoryNames.DISABLED, layer);
                layerManager.fireLayerChanged(layer, LayerEventType.METADATA_CHANGED);
            }
        }
        this.setVisible(false);
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
}

