/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util.trees;

import javax.swing.tree.TreeModel;
import org.saig.jump.widgets.util.trees.INavigableTreeVisitor;

public interface INavigableTree {
    public INavigableTreeVisitor getTreeVisitor();

    public TreeModel getModel();
}

