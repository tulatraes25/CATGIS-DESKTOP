/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;

public class DummyLayerNamePanel
extends JPanel
implements LayerNamePanel {
    private static final long serialVersionUID = 1L;
    private List<Category> selectedCategories = new ArrayList<Category>();
    private List selectedNodes = new ArrayList();
    private Layer[] selectedLayers = new Layer[0];

    @Override
    public Collection<Category> getSelectedCategories() {
        return this.selectedCategories;
    }

    public Collection selectedNodes(Class c) {
        return this.selectedNodes;
    }

    @Override
    public Layerable[] getSelectedLayers() {
        return this.selectedLayers;
    }

    @Override
    public Layer chooseEditableLayer() {
        return TreeLayerNamePanel.chooseEditableLayer(this);
    }

    @Override
    public void addListener(LayerNamePanelListener listener) {
    }

    @Override
    public void removeListener(LayerNamePanelListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public LayerManager getLayerManager() {
        return null;
    }

    @Override
    public void loadStatus() {
    }

    @Override
    public void saveStatus() {
    }
}

