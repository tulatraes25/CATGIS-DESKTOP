/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.jump.widgets.util;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.apache.log4j.Logger;
import org.saig.jump.widgets.util.FilteringModel;

public class FilteringJList<T>
extends JList
implements ListDataListener {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FilteringJList.class);
    private JTextComponent input;

    public FilteringJList() {
        super(new FilteringModel());
    }

    public FilteringJList(FilteringModel<T> filteringModel) {
        super(filteringModel);
    }

    public void installJTextComponent(JTextComponent input) {
        if (input == null) {
            throw new IllegalArgumentException("No se puede instalar un TextComponent nulo");
        }
        if (this.input != null) {
            this.uninstallJTextComponent();
        }
        this.input = input;
        ListModel model = this.getModel();
        input.getDocument().addDocumentListener((DocumentListener)((Object)model));
    }

    public void uninstallJTextComponent() {
        if (this.input == null) {
            throw new IllegalStateException("No hay TextComponent instalado");
        }
        ListModel model = this.getModel();
        this.input.getDocument().removeDocumentListener((DocumentListener)((Object)model));
        this.input = null;
    }

    public void setModel(ListModel model) {
        if (!(model instanceof FilteringModel)) {
            throw new IllegalArgumentException("El nuevo ListModel debe ser FilteringModel");
        }
        JTextComponent preInput = this.input;
        if (preInput != null) {
            this.uninstallJTextComponent();
        }
        ((AbstractListModel)this.getModel()).removeListDataListener(this);
        super.setModel(model);
        model.addListDataListener(this);
        if (preInput != null) {
            this.installJTextComponent(preInput);
            try {
                ((FilteringModel)this.getModel()).filter(preInput.getDocument().getText(0, preInput.getDocument().getLength()));
            }
            catch (BadLocationException ble) {
                LOGGER.error((Object)"", (Throwable)ble);
            }
        }
    }

    public void addElement(T element) {
        ((FilteringModel)this.getModel()).addElement(element);
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        this.getSelectionModel().clearSelection();
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
    }

    public FilteringModel<T> getModel() {
        return (FilteringModel)super.getModel();
    }
}

