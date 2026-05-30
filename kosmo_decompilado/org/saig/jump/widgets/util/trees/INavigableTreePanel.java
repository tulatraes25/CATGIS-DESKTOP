/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.trees;

import com.vividsolutions.jump.workbench.model.LayerManager;
import java.awt.event.MouseListener;
import javax.swing.tree.TreeCellRenderer;
import org.saig.jump.widgets.util.trees.INavigableTree;

public interface INavigableTreePanel {
    public TreeCellRenderer getNavigableTreeCellRenderer();

    public MouseListener getNavigableTreeListener();

    public INavigableTree getNavigableTree();

    public void buildNavigableTree(LayerManager var1);
}

