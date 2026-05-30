/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

import java.util.Collection;
import javax.swing.ComboBoxModel;
import org.saig.jump.widgets.util.FilteringModel;

public class FilteringComboBoxModel<T>
extends FilteringModel<T>
implements ComboBoxModel {
    private static final long serialVersionUID = 1L;
    private Object selectedObject;

    public FilteringComboBoxModel() {
    }

    public FilteringComboBoxModel(Collection<T> elements) {
        super(elements);
        if (this.getSize() > 0) {
            this.selectedObject = this.getElementAt(0);
        }
    }

    @Override
    public Object getSelectedItem() {
        return this.selectedObject;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (this.selectedObject != null && !this.selectedObject.equals(anItem) || this.selectedObject == null && anItem != null) {
            this.selectedObject = anItem;
        }
    }

    @Override
    protected void fireContentsChanged(Object source, int index0, int index1) {
        super.fireContentsChanged(source, index0, index1);
        this.selectedObject = this.getSize() > 0 ? this.getElementAt(0) : null;
    }
}

