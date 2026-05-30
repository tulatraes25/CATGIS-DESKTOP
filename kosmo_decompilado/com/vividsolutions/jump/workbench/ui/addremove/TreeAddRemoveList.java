/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.addremove.PostMoveElementBetweenListsListener;
import com.vividsolutions.jump.workbench.ui.addremove.PreMoveElementBetweenListsListener;
import com.vividsolutions.jump.workbench.ui.addremove.TreeAddRemoveListModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.saig.core.util.I18NUnsupportedOperationException;

public class TreeAddRemoveList<T>
extends JPanel
implements AddRemoveList<T> {
    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout1 = new BorderLayout();
    private TreeAddRemoveListModel<T> model = new TreeAddRemoveListModel(new JTree().getModel());
    protected InputChangedFirer inputChangedFirer = new InputChangedFirer();
    protected JTree tree = new JTree();
    protected List<PreMoveElementBetweenListsListener> preMoveElementsBetweenListsListenersList = new ArrayList<PreMoveElementBetweenListsListener>();
    protected List<PostMoveElementBetweenListsListener> postMoveElementsBetweenListsListenersList = new ArrayList<PostMoveElementBetweenListsListener>();

    public TreeAddRemoveList() {
        this.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener(){

            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreeAddRemoveList.this.inputChangedFirer.fire();
            }
        });
        try {
            this.jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void add(MouseListener listener) {
        this.tree.addMouseListener(listener);
    }

    @Override
    public void firePreMoveElementsBetweenListsAction() {
        for (PreMoveElementBetweenListsListener listener : this.preMoveElementsBetweenListsListenersList) {
            listener.moveElementBetweenListsFired();
        }
    }

    @Override
    public void firePreMoveElementsBetweenListsAction(boolean allElements) {
        for (PreMoveElementBetweenListsListener listener : this.preMoveElementsBetweenListsListenersList) {
            listener.moveElementBetweenListsFired(allElements);
        }
    }

    @Override
    public void firePostMoveElementsBetweenListsAction() {
        for (PostMoveElementBetweenListsListener listener : this.postMoveElementsBetweenListsListenersList) {
            listener.moveElementBetweenListsFired();
        }
    }

    @Override
    public void firePostMoveElementsBetweenListsAction(boolean allElements) {
        for (PostMoveElementBetweenListsListener listener : this.postMoveElementsBetweenListsListenersList) {
            listener.moveElementBetweenListsFired(allElements);
        }
    }

    @Override
    public void add(PreMoveElementBetweenListsListener listener) {
        this.preMoveElementsBetweenListsListenersList.add(listener);
    }

    @Override
    public void remove(PreMoveElementBetweenListsListener listener) {
        this.preMoveElementsBetweenListsListenersList.remove(listener);
    }

    @Override
    public void add(PostMoveElementBetweenListsListener listener) {
        this.postMoveElementsBetweenListsListenersList.add(listener);
    }

    @Override
    public void remove(PostMoveElementBetweenListsListener listener) {
        this.postMoveElementsBetweenListsListenersList.remove(listener);
    }

    @Override
    public void setSelectedItems(Collection<T> items) {
        throw new I18NUnsupportedOperationException();
    }

    public void setModel(TreeAddRemoveListModel<T> model) {
        this.model = model;
        this.tree.setModel(model.getTreeModel());
        this.inputChangedFirer.fire();
    }

    public JTree getTree() {
        return this.tree;
    }

    @Override
    public void add(InputChangedListener listener) {
        this.inputChangedFirer.add(listener);
    }

    void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.add((Component)this.tree, "Center");
    }

    @Override
    public AddRemoveListModel<T> getModel() {
        return this.model;
    }

    @Override
    public List<T> getSelectedItems() {
        ArrayList<Object> selectedNodes = new ArrayList<Object>();
        TreePath[] selectionPaths = this.tree.getSelectionPaths();
        if (selectionPaths == null) {
            return selectedNodes;
        }
        int i = 0;
        while (i < selectionPaths.length) {
            selectedNodes.add(selectionPaths[i].getLastPathComponent());
            ++i;
        }
        return selectedNodes;
    }
}

