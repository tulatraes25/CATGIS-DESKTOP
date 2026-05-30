/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManagerProxy;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerNamePanelListener;
import java.util.Collection;

public interface LayerNamePanel
extends LayerManagerProxy {
    public Collection<Category> getSelectedCategories();

    public <E> Collection<E> selectedNodes(Class<E> var1);

    public Layerable[] getSelectedLayers();

    public Layer chooseEditableLayer();

    public void addListener(LayerNamePanelListener var1);

    public void removeListener(LayerNamePanelListener var1);

    public void dispose();

    public void loadStatus();

    public void saveStatus();
}

