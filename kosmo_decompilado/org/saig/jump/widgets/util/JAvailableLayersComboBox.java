/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 */
package org.saig.jump.widgets.util;

import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.apache.commons.collections.CollectionUtils;
import org.saig.jump.util.LayerUtil;

public class JAvailableLayersComboBox
extends JComboBox {
    private static final long serialVersionUID = 1L;
    private LayerManager layerManager;
    private boolean showOnlyEditableLayers;
    private boolean showRasterLayers;
    private boolean showVectorialLayers;
    private boolean showNullOption;
    private Set<String> validLayerNames;
    private Set<String> nonValidLayerNames;
    private Set<Integer> validGeometryTypes;
    private Map<String, Icon> symbologyIconMap;

    public JAvailableLayersComboBox(LayerManager layerManager, boolean showOnlyEditableLayers, boolean showRasterLayers, boolean showVectorialLayers) {
        this(layerManager, showOnlyEditableLayers, showRasterLayers, showVectorialLayers, null, new ArrayList<String>());
        this.refresh();
    }

    public JAvailableLayersComboBox(LayerManager layerManager, boolean showOnlyEditableLayers, boolean showRasterLayers, boolean showVectorialLayers, boolean showNullOption) {
        this(layerManager, showOnlyEditableLayers, showRasterLayers, showVectorialLayers, showNullOption, null, new ArrayList<String>());
        this.refresh();
    }

    public JAvailableLayersComboBox(LayerManager layerManager, boolean showOnlyEditableLayers, boolean showRasterLayers, boolean showVectorialLayers, Collection<String> validLayerNames, Collection<String> nonValidLayerNames) {
        this(layerManager, showOnlyEditableLayers, showRasterLayers, showVectorialLayers, false, validLayerNames, nonValidLayerNames);
    }

    public JAvailableLayersComboBox(LayerManager layerManager, boolean showOnlyEditableLayers, boolean showRasterLayers, boolean showVectorialLayers, boolean showNullOption, Collection<String> validLayerNames, Collection<String> nonValidLayerNames) {
        this(layerManager, showOnlyEditableLayers, showRasterLayers, showVectorialLayers, false, validLayerNames, nonValidLayerNames, new ArrayList<Integer>());
    }

    public JAvailableLayersComboBox(LayerManager layerManager, boolean showOnlyEditableLayers, boolean showRasterLayers, boolean showVectorialLayers, boolean showNullOption, Collection<String> validLayerNames, Collection<String> nonValidLayerNames, Collection<Integer> validGeometryTypes) {
        this.layerManager = layerManager;
        this.showOnlyEditableLayers = showOnlyEditableLayers;
        this.showRasterLayers = showRasterLayers;
        this.showVectorialLayers = showVectorialLayers;
        this.showNullOption = showNullOption;
        this.symbologyIconMap = new HashMap<String, Icon>();
        this.validLayerNames = new TreeSet<String>();
        if (CollectionUtils.isNotEmpty(validLayerNames)) {
            this.validLayerNames.addAll(validLayerNames);
        }
        this.nonValidLayerNames = new TreeSet<String>();
        if (CollectionUtils.isNotEmpty(nonValidLayerNames)) {
            this.nonValidLayerNames.addAll(nonValidLayerNames);
        }
        this.validGeometryTypes = new HashSet<Integer>();
        if (CollectionUtils.isNotEmpty(validGeometryTypes)) {
            this.validGeometryTypes.addAll(validGeometryTypes);
        }
        this.setRenderer(new JAvailableComboBoxCellRenderer());
        this.refresh();
    }

    public JAvailableLayersComboBox(LayerManager layerManager, Collection<String> validLayerNames) {
        this(layerManager, false, false, true, validLayerNames, new ArrayList<String>());
    }

    public void refresh() {
        List<Layerable> layers = this.getFilteredLayers();
        TreeSet<String> layerNames = new TreeSet<String>();
        for (Layerable currentLayerable : layers) {
            layerNames.add(currentLayerable.getName());
            this.symbologyIconMap.put(currentLayerable.getName(), LayerUtil.generateIconForLayerable(currentLayerable));
        }
        this.removeAllItems();
        if (this.showNullOption) {
            this.addItem(null);
        }
        for (String currentLayerableName : layerNames) {
            if (this.nonValidLayerNames.contains(currentLayerableName)) continue;
            this.addItem(currentLayerableName);
        }
    }

    private List<Layerable> getFilteredLayers() {
        ArrayList<Layerable> layersToAdd;
        block10: {
            block9: {
                layersToAdd = new ArrayList<Layerable>();
                Collection<Layerable> filteredLayerables = null;
                if (!CollectionUtils.isEmpty(this.validLayerNames)) break block9;
                if (this.showOnlyEditableLayers) {
                    filteredLayerables = new ArrayList<Layer>(this.layerManager.getEditableLayers());
                } else if (this.showRasterLayers && this.showVectorialLayers) {
                    filteredLayerables = this.layerManager.getAllLayers();
                } else if (this.showVectorialLayers) {
                    filteredLayerables = this.layerManager.getNoRasterLayers();
                } else if (this.showRasterLayers) {
                    filteredLayerables = this.layerManager.getAllLayers();
                    filteredLayerables.removeAll(this.layerManager.getNoRasterLayers());
                }
                if (filteredLayerables == null) break block10;
                for (Layerable currentLayerable : filteredLayerables) {
                    if (currentLayerable.isRaster() || !this.isValidType((Layer)currentLayerable)) continue;
                    layersToAdd.add(currentLayerable);
                }
                break block10;
            }
            for (String currentLayerName : this.validLayerNames) {
                Layer layer = this.layerManager.getLayer(currentLayerName);
                if (layer == null || !this.isValidType(layer)) continue;
                layersToAdd.add(layer);
            }
        }
        return layersToAdd;
    }

    private boolean isValidType(Layer currentLayer) {
        if (CollectionUtils.isEmpty(this.validGeometryTypes)) {
            return true;
        }
        return this.validGeometryTypes.contains(currentLayer.getGeometryType());
    }

    @Override
    public Object getSelectedItem() {
        Layerable selectedItem = null;
        Object layerName = super.getSelectedItem();
        if (layerName != null) {
            selectedItem = this.layerManager.getLayerable((String)layerName);
        }
        return selectedItem;
    }

    public Layer getSelectedLayer() {
        return (Layer)this.getSelectedItem();
    }

    public String getSelectedLayerName() {
        return (String)super.getSelectedItem();
    }

    @Override
    public void setSelectedItem(Object selectedItem) {
        if (selectedItem == null || !(selectedItem instanceof Layerable)) {
            super.setSelectedItem(selectedItem);
        } else {
            super.setSelectedItem(((Layerable)selectedItem).getName());
        }
    }

    public void setValidLayerNames(Collection<String> newValidLayerNames) {
        this.validLayerNames.clear();
        if (newValidLayerNames != null) {
            this.validLayerNames.addAll(newValidLayerNames);
        }
        this.refresh();
    }

    public void setNonValidLayerNames(Collection<String> nonValidNames) {
        this.nonValidLayerNames.clear();
        this.nonValidLayerNames.addAll(nonValidNames);
    }

    public void setLayerManager(LayerManager layerManager) {
        this.layerManager = layerManager;
    }

    private class JAvailableComboBoxCellRenderer
    extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private JAvailableComboBoxCellRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent((JList<?>)list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                String layerName = value.toString();
                this.setText(layerName);
                this.setIcon((Icon)JAvailableLayersComboBox.this.symbologyIconMap.get(layerName));
            } else {
                this.setText("----------");
            }
            return this;
        }
    }
}

