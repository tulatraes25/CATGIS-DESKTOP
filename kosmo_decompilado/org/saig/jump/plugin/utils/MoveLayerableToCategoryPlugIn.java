/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.plugin.utils;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.TreeLayerNamePanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.gui.components.MenuScroller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public class MoveLayerableToCategoryPlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("org.saig.jump.plugin.utils.MoveLayerableToCategoryPlugIn.Move-to-the-category")) + " ...";
    public static final Icon ICON = IconLoader.icon("blank.png");
    protected Map<String, String> publicNameToInternalNameMap = new TreeMap<String, String>();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    public JMenu getMenu() {
        JMenu moveLayerableToCategoryMenu = new JMenu(NAME);
        moveLayerableToCategoryMenu.setIcon(GUIUtil.toSmallIcon(ICON));
        moveLayerableToCategoryMenu.addMenuListener(new MenuListener(){

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuSelected(MenuEvent e) {
                JMenu source = (JMenu)e.getSource();
                source.removeAll();
                MoveLayerableToCategoryPlugIn.this.publicNameToInternalNameMap.clear();
                List<Category> categories = JUMPWorkbench.getFrameInstance().getContext().getLayerManager().getCategories();
                for (Category currentCategory : categories) {
                    JMenuItem item = new JMenuItem(currentCategory.getTitle(LocaleManager.getActiveLocale()));
                    source.add(item);
                    MoveLayerableToCategoryPlugIn.this.publicNameToInternalNameMap.put(item.getText(), currentCategory.getName());
                    item.addActionListener(new ActionListener(){

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MoveLayerableToCategoryPlugIn.this.moveSelectedLayerablesToCategory((this).MoveLayerableToCategoryPlugIn.this.publicNameToInternalNameMap.get(((JMenuItem)e.getSource()).getText()));
                        }
                    });
                }
            }
        });
        MenuScroller.setScrollerFor(moveLayerableToCategoryMenu, 10);
        return moveLayerableToCategoryMenu;
    }

    protected Collection selectedLayerables(LayerNamePanel layerNamePanel) {
        return layerNamePanel.selectedNodes(Layerable.class);
    }

    protected void moveSelectedLayerablesToCategory(String categoryName) {
        ArrayList layerables = new ArrayList(this.selectedLayerables(JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel()));
        Collections.reverse(layerables);
        for (Layerable currentLayerable : layerables) {
            this.moveLayerable(currentLayerable, categoryName);
        }
        LayerNamePanel layerNamePanel = JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel();
        if (layerNamePanel != null && layerNamePanel instanceof TreeLayerNamePanel) {
            for (Layerable currentLayerable : layerables) {
                if (!(currentLayerable instanceof Layer)) continue;
                ((TreeLayerNamePanel)layerNamePanel).addSelectedLayer((Layer)currentLayerable);
            }
        }
    }

    private void moveLayerable(Layerable layerable, String categoryName) {
        JUMPWorkbench.getFrameInstance().getContext().getLayerManager().remove(layerable);
        JUMPWorkbench.getFrameInstance().getContext().getLayerManager().addLayerable(categoryName, layerable, 0);
    }
}

