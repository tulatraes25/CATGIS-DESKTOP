/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.scale;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.gui.components.MenuScroller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public class CategoryPopupFactory {
    public JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
        List<Category> allCategories = context.getLayerManager().getCategories();
        LayerNamePanel layerNamePanel = context.getLayerNamePanel();
        if (layerNamePanel != null) {
            layerNamePanel.saveStatus();
        }
        boolean allCollapsed = true;
        boolean allVisible = true;
        for (Category category : allCategories) {
            allCollapsed &= category.isCollapsed();
            List<Layerable> layerables = category.getLayerables();
            for (Layerable layerable : layerables) {
                allVisible &= layerable.isVisible();
            }
        }
        if (allCollapsed) {
            JMenuItem uncolapseAll = new JMenuItem(I18N.getString("org.saig.jump.widgets.scale.CategoryPopupFactory.Expand-all-the-categories"), IconLoader.icon("folder.png"));
            popup.add(uncolapseAll);
            uncolapseAll.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
                    List<Category> allCategories = context.getLayerManager().getCategories();
                    for (Category category : allCategories) {
                        category.setCollapsed(false);
                    }
                    LayerNamePanel layerNamePanel = context.getLayerNamePanel();
                    layerNamePanel.loadStatus();
                }
            });
        } else {
            JMenuItem collapseAll = new JMenuItem(I18N.getString("org.saig.jump.widgets.scale.CategoryPopupFactory.Collapse-all-the-categories"), IconLoader.icon("folder.png"));
            popup.add(collapseAll);
            collapseAll.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
                    List<Category> allCategories = context.getLayerManager().getCategories();
                    for (Category category : allCategories) {
                        category.setCollapsed(true);
                    }
                    LayerNamePanel layerNamePanel = context.getLayerNamePanel();
                    layerNamePanel.loadStatus();
                }
            });
        }
        if (allVisible) {
            JMenuItem hideAll = new JMenuItem(I18N.getString("org.saig.jump.widgets.scale.CategoryPopupFactory.Hide-all-the-categories"), IconLoader.icon("Eye.gif"));
            popup.add(hideAll);
            hideAll.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
                    List<Category> allCategories = context.getLayerManager().getCategories();
                    for (Category category : allCategories) {
                        List<Layerable> layerables = category.getLayerables();
                        for (Layerable layerable : layerables) {
                            layerable.setVisible(false);
                        }
                    }
                }
            });
        } else {
            JMenuItem showAll = new JMenuItem(I18N.getString("org.saig.jump.widgets.scale.CategoryPopupFactory.Show-all-the-categories"), IconLoader.icon("Eye.gif"));
            popup.add(showAll);
            showAll.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
                    List<Category> allCategories = context.getLayerManager().getCategories();
                    for (Category category : allCategories) {
                        List<Layerable> layerables = category.getLayerables();
                        for (Layerable layerable : layerables) {
                            layerable.setVisible(true);
                        }
                    }
                }
            });
        }
        popup.addSeparator();
        popup.add(new JLabel("<HTML><B>" + I18N.getString("org.saig.jump.widgets.scale.CategoryPopupFactory.Category-visibility") + "</B></HTML>"));
        for (Category category : allCategories) {
            boolean visible = true;
            List<Layerable> layerables = category.getLayerables();
            for (Layerable layerable : layerables) {
                visible &= layerable.isVisible();
            }
            JCheckBox catjch = new JCheckBox(category.getTitle(LocaleManager.getActiveLocale()));
            catjch.setName(category.getName());
            catjch.setSelected(visible);
            catjch.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    WorkbenchContext context = JUMPWorkbench.getFrameInstance().getContext();
                    JCheckBox source = (JCheckBox)e.getSource();
                    String name = source.getName();
                    Category category = context.getLayerManager().getCategory(name);
                    List<Layerable> layerables2 = category.getLayerables();
                    for (Layerable layerable : layerables2) {
                        layerable.setVisible(source.isSelected());
                    }
                }
            });
            popup.add(catjch);
        }
        MenuScroller.setScrollerFor(popup, 10, 125, 4, 0);
        return popup;
    }
}

