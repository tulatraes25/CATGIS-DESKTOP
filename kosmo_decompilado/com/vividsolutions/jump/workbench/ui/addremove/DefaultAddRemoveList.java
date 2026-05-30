/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.addremove;

import com.vividsolutions.jump.workbench.ui.InputChangedFirer;
import com.vividsolutions.jump.workbench.ui.InputChangedListener;
import com.vividsolutions.jump.workbench.ui.JListTypeAheadKeyListener;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveList;
import com.vividsolutions.jump.workbench.ui.addremove.AddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.addremove.DefaultAddRemoveListModel;
import com.vividsolutions.jump.workbench.ui.addremove.PostMoveElementBetweenListsListener;
import com.vividsolutions.jump.workbench.ui.addremove.PreMoveElementBetweenListsListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class DefaultAddRemoveList<T>
extends JPanel
implements AddRemoveList<T> {
    private static final long serialVersionUID = 1L;
    private BorderLayout borderLayout1 = new BorderLayout();
    private JList list = new JList();
    private DefaultAddRemoveListModel<T> model;
    private InputChangedFirer inputChangedFirer = new InputChangedFirer();
    protected List<PreMoveElementBetweenListsListener> preMoveElementsBetweenListsListenersList = new ArrayList<PreMoveElementBetweenListsListener>();
    protected List<PostMoveElementBetweenListsListener> postMoveElementsBetweenListsListenersList = new ArrayList<PostMoveElementBetweenListsListener>();

    public DefaultAddRemoveList() {
        this(new DefaultListModel());
    }

    public DefaultAddRemoveList(DefaultListModel listModel) {
        this(listModel, false);
    }

    public DefaultAddRemoveList(DefaultListModel listModel, boolean sorted) {
        this.model = new DefaultAddRemoveListModel(listModel, sorted);
        this.list.setModel(listModel);
        this.list.addKeyListener(new JListTypeAheadKeyListener(this.list));
        this.list.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

            @Override
            public void valueChanged(ListSelectionEvent e) {
                DefaultAddRemoveList.this.inputChangedFirer.fire();
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
        this.list.addMouseListener(listener);
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
        ArrayList<Integer> indicesToSelect = new ArrayList<Integer>();
        for (T item : items) {
            int index = this.getModel().getItems().indexOf(item);
            if (index == -1) continue;
            indicesToSelect.add(new Integer(index));
        }
        int[] indexArray = new int[indicesToSelect.size()];
        int i = 0;
        while (i < indicesToSelect.size()) {
            Integer index = (Integer)indicesToSelect.get(i);
            indexArray[i] = index;
            ++i;
        }
        this.list.setSelectedIndices(indexArray);
    }

    @Override
    public AddRemoveListModel<T> getModel() {
        return this.model;
    }

    @Override
    public void add(InputChangedListener listener) {
        this.inputChangedFirer.add(listener);
    }

    public JList getList() {
        return this.list;
    }

    @Override
    public List<T> getSelectedItems() {
        return Arrays.asList(this.list.getSelectedValues());
    }

    private void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.add((Component)this.list, "Center");
    }

    public void setSelectionMode(int selectionMode) {
        this.list.setSelectionMode(selectionMode);
    }
}

