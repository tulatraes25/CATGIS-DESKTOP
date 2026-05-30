/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 *  org.apache.log4j.Logger
 */
package com.vividsolutions.jump.workbench.ui;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.LayerTreeModel;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.ui.LayerNameRenderer;
import com.vividsolutions.jump.workbench.ui.renderer.RenderingManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import org.apache.log4j.Logger;
import org.saig.core.styling.Rule;
import org.saig.core.util.LocaleManager;
import org.saig.jump.widgets.util.RuleRenderer;

public class LayerTreeCellRenderer
implements TreeCellRenderer {
    private static final Logger LOGGER = Logger.getLogger(LayerTreeCellRenderer.class);
    private LayerNameRenderer layerNameRenderer = new LayerNameRenderer();
    private JLabel rootRendererComponent = new JLabel("Root");
    private DefaultTreeCellRenderer categoryRenderer = new DefaultTreeCellRenderer();
    private RuleRenderer ruleRenderer = new RuleRenderer();

    public LayerTreeCellRenderer(RenderingManager renderingManager) {
        this.layerNameRenderer.setCheckBoxVisible(true);
        this.layerNameRenderer.setIndicatingEditability(true);
        this.layerNameRenderer.setIndicatingProgress(true, renderingManager);
    }

    public LayerNameRenderer getLayerNameRenderer() {
        return this.layerNameRenderer;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Object node = value;
        if (node instanceof LayerTreeModel.Root) {
            return this.getTreeCellRendererComponent((LayerTreeModel.Root)node);
        }
        if (node instanceof Category) {
            this.categoryRenderer.setBackgroundNonSelectionColor(tree.getBackground());
            JLabel categoryRendererComponent = (JLabel)this.categoryRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            categoryRendererComponent.setFont(new JLabel().getFont().deriveFont(1));
            categoryRendererComponent.setText(((Category)node).getTitle(LocaleManager.getActiveLocale()));
            if (expanded) {
                categoryRendererComponent.setIcon(UIManager.getIcon("Tree.openIcon"));
            } else {
                categoryRendererComponent.setIcon(UIManager.getIcon("Tree.closedIcon"));
            }
            return categoryRendererComponent;
        }
        if (node instanceof Layerable) {
            return this.layerNameRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        if (node instanceof Rule) {
            return this.ruleRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
        Assert.shouldNeverReachHere((String)node.getClass().toString());
        return null;
    }

    private Component getTreeCellRendererComponent(LayerTreeModel.Root root) {
        return this.rootRendererComponent;
    }
}

