/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.trees;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.saig.jump.widgets.util.trees.INavigableTree;
import org.saig.jump.widgets.util.trees.INavigableTreeVisitor;

public abstract class AbstractNavigableTreeVisitor
implements INavigableTreeVisitor {
    protected boolean found = false;

    @Override
    public void visit(INavigableTree tree, Object param) {
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        this.visit(model, root, param);
    }

    protected abstract void visit(DefaultTreeModel var1, DefaultMutableTreeNode var2, Object var3);

    protected abstract void processNode(DefaultTreeModel var1, DefaultMutableTreeNode var2, Object var3);
}

