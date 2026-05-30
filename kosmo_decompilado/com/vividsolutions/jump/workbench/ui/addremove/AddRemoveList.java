/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.addremove.PostMoveElementBetweenListsListener;
import com.vividsolutions.jump.workbench.ui.addremove.PreMoveElementBetweenListsListener;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;

public interface AddRemoveList<T> {
    public AddRemoveListModel<T> getModel();

    public void add(InputChangedListener var1);

    public void add(MouseListener var1);

    public void add(PreMoveElementBetweenListsListener var1);

    public void add(PostMoveElementBetweenListsListener var1);

    public void firePreMoveElementsBetweenListsAction();

    public void firePreMoveElementsBetweenListsAction(boolean var1);

    public void firePostMoveElementsBetweenListsAction();

    public void firePostMoveElementsBetweenListsAction(boolean var1);

    public void remove(PreMoveElementBetweenListsListener var1);

    public void remove(PostMoveElementBetweenListsListener var1);

    public List<T> getSelectedItems();

    public void setSelectedItems(Collection<T> var1);
}

